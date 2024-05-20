/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import beans.*;
import java.util.*;
import threads.*;
import simulators.HRSimulator;

/**
 * responsible of H.R. monitoring, coordination with other players through GRPC, like
 * H.B. permission acquired, seeker election, H.B.-simultaneous reaching consensus
 * @author Admin
 */
public class SmartWatch
{
    private static final double PLAYER_SPEED_UNITS = 2.0;
            
    private String grpcServiceEndpoint; 
    private volatile Player player;
    private MonitorHrValuesThread monitorHrValues_thread;
    private PlayerRoleThread playerRole_thread; //assigned after coordination
    private CustomLock playerLock;
    private int seekerWaitMilliseconds;
    private int hiderWaitMilliseconds;
    
    /**
     * contains all shared resources that this process wants to use or already in use.
     * used for distributed mutual exclusion
     * <sharedResource type, timestamp of usage request>
     */
    private volatile HashMap<SharedResource, Long> sharedResourcesToUse;
    
    /**
     * contains all SharedResource's agreements for an acquireSharedResource request.
     * <sharedResource type, all processes which agreed the shared-resource acquirement for the current process>
     */
    private volatile HashMap<SharedResource, HashSet<String>> sharedResourcesAgreements;
    
    /**
     * if another process asks to acquire a shared resource that's present in sharedResourcesInUse, then append it in sharedResourceAwaiters.
     * generalized for any future SharedResource type.
     * used for distributed mutual exclusion
     * <sharedResource type, all processes in hold>
     */
    private volatile HashMap<SharedResource, HashSet<String>> sharedResourceAwaiters;
    
    /**
     * used to manage the access to sharedResourcesToUse.
     * for simplicity we consider one lock for all shared resources {H.B. , ...}.
     */
    private volatile CustomLock sharedResourceLock;
    
    /**
     * when a player starts, he is convinced that the current game phase is preparation.
     * then otherPlayers informs him of the actual gamePhase as a response in InformForNewEntryThread.
     * if at least one response of players indicate that the currentGamePhase = Main, then the player can't start its AskToBeSeekerThread.
     */
    private volatile GamePhase currentGamePhase = GamePhase.Preparation;
    
    /**
     * it will be false when this player send seekerAgreement to another player.
     * when false, it prevents sending further canIbeSeekerRequest and 
     * the current player doesn't have to wait for AskToBeSeekerThreads responses.
     */
    private volatile boolean isCanBeSeeker = true;
    
    private static SmartWatch instance;
    
    private SmartWatch(Player player, String grpcServiceEndpoint, CheckToSendHrAvgsThread checkToSendHrAvgs_thread, int seekerWaitMilliseconds, int hiderWaitMilliseconds)
    {
        this.seekerWaitMilliseconds = seekerWaitMilliseconds;
        this.hiderWaitMilliseconds = hiderWaitMilliseconds;
        
        this.player = player;
        this.grpcServiceEndpoint = grpcServiceEndpoint;
        this.playerLock = new CustomLock();
        this.sharedResourcesToUse = new HashMap();
        this.sharedResourcesAgreements = new HashMap();
        this.sharedResourceAwaiters = new HashMap();
        this.sharedResourceLock = new CustomLock();
        
        HRSimulator hrSimulator_thread = new HRSimulator("Player-" + this.player.getId(), new HRSimulatorBuffer());
        monitorHrValues_thread = new MonitorHrValuesThread(0, hrSimulator_thread, checkToSendHrAvgs_thread);
        
        monitorHrValues_thread.start();
        monitorHrValues_thread.startAuxiliaryThreads();
        
        System.err.println("Initialized smartWatch with playerId: " + this.player.toString());
        
        new Thread(() -> informNewEntry()).start();
    }
    
    /**
     * inform all other players of the new player and update their position & status
     * one thread for each other player.
     * if it's noticed that the game is in coordination phase, so participate in the coordination phase.
     * Otherwise the current player is automatically a hider (if and only if the game phase isn't preparation).
     */
    private void informNewEntry()
    {
        try
        {
            ArrayList<InformForNewEntryThread> gatheredThreads = new ArrayList();
            Set<String> otherPlayersEndPoints;
            
            this.playerLock.Acquire();
            //read current otherPlayers endpoints
            otherPlayersEndPoints = new HashSet(this.player.getOtherPlayers().keySet());
            this.playerLock.Release();
            
            for(String endpoint : otherPlayersEndPoints)
            {
                InformForNewEntryThread informForNewEntry_thread = new InformForNewEntryThread(endpoint, this, this.grpcServiceEndpoint);
                informForNewEntry_thread.start();
                gatheredThreads.add(informForNewEntry_thread);
            }
            
            boolean inMainPhase = gatheredThreads.stream()
                                                 .anyMatch(thread -> thread.getplayerGamePhase().equals(GamePhase.Main));
            
            boolean inPreparationPhase = gatheredThreads.stream()
                                                        .allMatch(thread -> thread.getplayerGamePhase().equals(GamePhase.Preparation));
            
            if(!inMainPhase && !inPreparationPhase)
                this.startGameCoordination(0); //partecipate in seeker coordination
            else if(!inPreparationPhase)
            {
                this.currentGamePhase = GamePhase.Main;
                
                HiderPlayerRole hiderRole_thread = new HiderPlayerRole(this.grpcServiceEndpoint, PLAYER_SPEED_UNITS, this.hiderWaitMilliseconds);
                hiderRole_thread.start();
            }
        }
        catch(Exception e)
        {
            System.err.println("In informNewEntry: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * ask to be seeker from all other players; if all other players agree then this player is the seeker.one thread for each other player
     * modified bully election
     * @param waitMilliseconds
     */
    public void startGameCoordination(int waitMilliseconds)
    {
        try
        {    
            System.out.println("invoked startGameCoordination with waitMilliseconds: " + waitMilliseconds);
            
            //change player's game phase
            this.currentGamePhase = GamePhase.Coordination;
            
            ArrayList<AskToBeSeekerThread> gatheredThreads = new ArrayList();
            Set<String> otherPlayersEndPoints;

            //delay coordination
            if (waitMilliseconds > 0)
                    Thread.sleep(waitMilliseconds);
            
            this.playerLock.Acquire();
            //read current otherPlayers endpoints
            otherPlayersEndPoints = new HashSet(this.player.getOtherPlayers().keySet());
            this.playerLock.Release();

            for(String endpoint : otherPlayersEndPoints)
            {
                if(this.isCanBeSeeker)
                {
                    AskToBeSeekerThread askToBeSeeker_thread = new AskToBeSeekerThread(endpoint);
                    askToBeSeeker_thread.start();
                    gatheredThreads.add(askToBeSeeker_thread);
                } 
            }
            
            //We need to wait for all threads' results to determine current player's role
            //if the player has already agreed the seeker role for another player, so the current player knows that he'll be a hider.
//            boolean resultsGathered = false;
//            while(!resultsGathered && this.isCanBeSeeker) //should be replaced by wait and notify like InformForNewEntryThread for this.informNewEntry
//            {
//                resultsGathered = !gatheredThreads.stream()
//                                                  .map(m -> m.checkIsCompleted())
//                                                  .anyMatch(m -> m.equals(false));
//            } //substituted with wait and notify result-waiting logic like InformForNewEntryThread
            
            boolean finalAgreedSeeker = !gatheredThreads.stream()
                                                        .map(m -> m.getAgreementResult())
                                                        .anyMatch(m -> m.equals(false));
            
            //since the current player knows his role so its coordination gamePhase is terminated -> Main
            this.currentGamePhase = GamePhase.Main;
                
            if(finalAgreedSeeker && this.isCanBeSeeker)
            {
                this.playerRole_thread = new SeekerPlayerRole(this.grpcServiceEndpoint, PLAYER_SPEED_UNITS, this.seekerWaitMilliseconds);
                this.playerRole_thread.start(); //start seeker role
                
                //change player's status
                this.AcquirePlayerLock();
                this.player.setStatus(PlayerStatus.Seeker);
                this.ReleasePlayerLock();
                
                //inform otherPlayer for the new seeker
                this.informPlayerChangedPositionOrStatus(this.grpcServiceEndpoint, this.player, true);
            }
            else //hider
            {
                this.playerRole_thread = new HiderPlayerRole(this.grpcServiceEndpoint, PLAYER_SPEED_UNITS, this.hiderWaitMilliseconds);
                this.playerRole_thread.start(); //start hider role
            }
        }
        catch(Exception e)
        {
            System.err.println("In informNewEntry: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * inform all other players [considered safe] that the game is terminated
     */
    public void informGameTermination()
    {
        try 
        {
            Set<String> otherPlayersEndPoints;

            this.playerLock.Acquire();
            //read current otherPlayers endpoints
            otherPlayersEndPoints = new HashSet(this.player.getOtherPlayers().keySet());
            this.playerLock.Release();

            for(String endpoint : otherPlayersEndPoints)
            {
                InformGameTerminationThread informGameTerm_thread = new InformGameTerminationThread(endpoint, this.grpcServiceEndpoint);
                informGameTerm_thread.start();
            }
        }
        catch(Exception e)
        {
            System.err.println("In informGameTermination: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * invoked by HiderPlayerThread or SeekerPlayerThread
     * @param changedPlayerEndPoint
     * @param changedPlayer
     * @param isSentBySeeker 
     */
    public void informPlayerChangedPositionOrStatus(String changedPlayerEndPoint, Player changedPlayer , boolean isSentBySeeker)
    {
        try 
        {
            Set<String> otherPlayersEndPoints;

            this.playerLock.Acquire();
            //read current otherPlayers endpoints
            otherPlayersEndPoints = new HashSet(this.player.getOtherPlayers().keySet());
            this.playerLock.Release();

            for(String endpoint : otherPlayersEndPoints)
            {
                InformPlayerChangedThread informPlayerChanged_thread = new InformPlayerChangedThread(endpoint, changedPlayerEndPoint, changedPlayer, isSentBySeeker);
                informPlayerChanged_thread.start();
            }
        }
        catch(Exception e)
        {
            System.err.println("In informPlayerChangedPositionOrStatus: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * remove the used shared resource from sharedResourcesToUse.
     * creates and starts a InformReleasedSharedResourceThread for all other players present in sharedResourceAwaiters for the same SharedResource type
     * resets sharedResource awaiters' queue.
     * @param sharedResource 
     */
    public void informReleasedSharedResource(SharedResource sharedResource)
    {
        try 
        {
            Set<String> otherAwaitersEndPoints;

            //remove the shared resource that we used
            this.sharedResourcesToUse.remove(sharedResource);
            
            this.playerLock.Acquire();
            //read current shared-resource awaiters endpoints
            otherAwaitersEndPoints = new HashSet(this.sharedResourceAwaiters.getOrDefault(sharedResource, new HashSet()));
            this.playerLock.Release();

            for(String endpoint : otherAwaitersEndPoints)
            {
                this.player.AcquireOtherPlayerLock(endpoint);
                if(this.player.getOtherPlayer(endpoint).getStatus().equals(PlayerStatus.Active))
                {
                    InformReleasedSharedResourceThread informReleasedSR_thread = new InformReleasedSharedResourceThread(endpoint, this.grpcServiceEndpoint, sharedResource);
                    informReleasedSR_thread.start();
                }
                this.player.ReleaseOtherPlayerLock(endpoint);
            }
            
            //reset sharedResource awaiters' queue.
            this.sharedResourceAwaiters.put(sharedResource, new HashSet());
        }
        catch(Exception e)
        {
            System.err.println("In informReleasedSharedResource: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * adds the desired shared resource to sharedResourcesToUse.
     * creates and starts a AcquireSharedResourceThread for all other players
     * @param sharedResource
     * @param timestamp 
     */
    public void acquireSharedResource(SharedResource sharedResource, long timestamp)
    {
        try 
        {
            ArrayList<AcquireSharedResourceThread> gatheredThreads = new ArrayList();
            Set<String> otherPlayersEndPoints;

            //sign the shared resource that we want to use
            this.sharedResourcesToUse.put(sharedResource, timestamp);
            
            this.playerLock.Acquire();
            //read current otherPlayers endpoints
            otherPlayersEndPoints = new HashSet(this.player.getOtherPlayers().keySet());
            this.playerLock.Release();

            for(String endpoint : otherPlayersEndPoints)
            {
                AcquireSharedResourceThread acquireSR_thread = new AcquireSharedResourceThread(endpoint, this.grpcServiceEndpoint, sharedResource, this.player.getId(), timestamp);
                acquireSR_thread.start();
                gatheredThreads.add(acquireSR_thread);
            }
            
            //wait for shared-resource result from all other players.
//            boolean resultsGathered = false;
//            while(!resultsGathered) //should be replaced by wait and notify like InformForNewEntryThread for this.informNewEntry
//            {
//                resultsGathered = !gatheredThreads.stream()
//                                                  .map(m -> m.checkIsCompleted())
//                                                  .anyMatch(m -> m.equals(false));
//            }//substituted with wait and notify result-waiting logic like InformForNewEntryThread
            
            for (AcquireSharedResourceThread gatheredThread : gatheredThreads)
            {
                if(gatheredThread.getAgreementResult()) //consider only true-valorized agreements
                {
                    this.addSharedResourceAgreement(sharedResource, gatheredThread.getRemotePlayerEndpoint());
                }
            }
        }
        catch(Exception e)
        {
            System.err.println("In AcquireSharedResource: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * stops hr buffer monitoring and sending.
     * stop player role.
     */
    public void stopSmartWatch()
    {
        this.monitorHrValues_thread.stopMeGently();
        this.playerRole_thread.interrupt();
    }
    
    public void AcquirePlayerLock()
    {
       this.playerLock.Acquire();
    }
    
    public void ReleasePlayerLock()
    {
        this.playerLock.Release();
    }
    
    public void AcquireSharedResourcesLock()
    {
       this.sharedResourceLock.Acquire();
    }
    
    public void ReleaseSharedResourcesLock()
    {
        this.sharedResourceLock.Release();
    }
    
//    /**
//     * may cause write-write conflict
//     * @return 
//     */
//    public void updatePlayer(Player player)
//    {
//        this.playerLock.Acquire();
//        this.player = player;
//        this.playerLock.Release();
//    }
    
    /**
     * checks if a shared process is available (not present in sharedResourcesToUse or earlier otherPlayer's timestamp),
     * we will use the highest playerId check if properly the two timeStamps are equal (almost can't happen);
     * otherwise, it adds otherPlayer to awaiters' queue.
     * @param sharedResource
     * @param otherPlayerId
     * @param timestamp
     * @param otherPlayerEndpoint
     * @return 
     */
    public boolean checkSharedResourceAvailability(SharedResource sharedResource, int otherPlayerId, long timestamp, String otherPlayerEndpoint)
    {
        if(!this.sharedResourcesToUse.containsKey(sharedResource) || this.sharedResourcesToUse.get(sharedResource) > timestamp ||
           (this.sharedResourcesToUse.get(sharedResource) == timestamp && this.player.getId() < otherPlayerId))
            return true;
        
        //consider the remote player as a shared-resource awaiter
        HashSet<String> awaiters = this.sharedResourceAwaiters.getOrDefault(sharedResource, new HashSet());
        awaiters.add(otherPlayerEndpoint);
        this.sharedResourceAwaiters.put(sharedResource, awaiters);
        
        return false;
    }
    
    
    /**
     * used to add another player's response who has accepted a shared-resource concession to the current player.
     * @param sharedResource
     * @param otherPlayerEndPoint 
     */
    public void addSharedResourceAgreement(SharedResource sharedResource, String otherPlayerEndPoint)
    {
        HashSet<String> otherPlayersAgreements = this.sharedResourcesAgreements.getOrDefault(sharedResource, new HashSet());
        otherPlayersAgreements.add(otherPlayerEndPoint);
        this.sharedResourcesAgreements.put(sharedResource, otherPlayersAgreements);
        
        Set<String> otherPlayersEndPoints;

        this.playerLock.Acquire();
        //read current otherPlayers endpoints
        otherPlayersEndPoints = new HashSet(this.player.getOtherPlayers().keySet());
        this.playerLock.Release();
        
        System.out.println("Granted agreement for sharedResource: " + sharedResource.toString() + ", by: " + otherPlayerEndPoint);
        
        if(this.sharedResourcesAgreements.get(sharedResource).size() == otherPlayersEndPoints.size())
            ((HiderPlayerRole)this.playerRole_thread).PermissionAcquired();
    }
    
    public void setIsCanBeSeeker(boolean isCanBeSeeker)
    {
        this.isCanBeSeeker = isCanBeSeeker;
    }
    
    public String getGrpcEndpoint()
    {
        return this.grpcServiceEndpoint;
    }
    
    public Player getPlayer()
    {
        return this.player;
    }
    
    public GamePhase getCurrentGamePhase()
    {
        return this.currentGamePhase;
    }
    
    /**
     * singleton pattern
     * @return instance
     */
    public static SmartWatch getSubsequentInstance()
    {   
        if(instance == null)
            throw new NullPointerException("Can't invoke getSubsequentInstance when initial instance is null!");
            
        return instance;
    }
    
    /**
     * singleton pattern
     * @return instance
     */
    public static SmartWatch getInstance(Player player, String grpcServiceEndpoint, CheckToSendHrAvgsThread checkToSendHrAvgs_thread, int seekerWaitMilliseconds, int hiderWaitMilliseconds)
    {
        if(instance == null)
            instance = new SmartWatch(player, grpcServiceEndpoint, checkToSendHrAvgs_thread, seekerWaitMilliseconds, hiderWaitMilliseconds);
        
        return instance;
    }
}
