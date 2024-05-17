/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import beans.GamePhase;
import beans.Player;
import java.util.ArrayList;
import java.util.Set;
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
    private CustomLock playerLock;
    
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
    
    private SmartWatch(Player player, String grpcServiceEndpoint, CheckToSendHrAvgsThread checkToSendHrAvgs_thread)
    {
        this.player = player;
        this.grpcServiceEndpoint = grpcServiceEndpoint;
        this.playerLock = new CustomLock();
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
            Set<String> otherPlayersEndsPoints;
            
            this.playerLock.Acquire();
            otherPlayersEndsPoints = this.player.getOtherPlayers().keySet();
            this.playerLock.Release();
            
            for(String endpoint : otherPlayersEndsPoints)
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
                
                HiderPlayerRole hiderRole_thread = new HiderPlayerRole(this.grpcServiceEndpoint, PLAYER_SPEED_UNITS);
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
            Set<String> otherPlayersEndsPoints;

            //delay coordination
            if (waitMilliseconds > 0)
                    Thread.sleep(waitMilliseconds);
            
            this.playerLock.Acquire();
            otherPlayersEndsPoints = this.player.getOtherPlayers().keySet();
            this.playerLock.Release();

            for(String endpoint : otherPlayersEndsPoints)
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
            boolean resultsGathered = false;
            while(!resultsGathered && this.isCanBeSeeker)
            {
                resultsGathered = !gatheredThreads.stream()
                                                  .map(m -> m.checkIsCompleted())
                                                  .anyMatch(m -> m.equals(false));
            }
            
            boolean finalAgreedSeeker = !gatheredThreads.stream()
                                                        .map(m -> m.getAgreementResult())
                                                        .anyMatch(m -> m.equals(false));
            
            //since the current player knows his role so its coordination gamePhase is terminated -> Main
            this.currentGamePhase = GamePhase.Main;
                
            if(finalAgreedSeeker && this.isCanBeSeeker)
            {
                SeekerPlayerRole seekerRole_thread = new SeekerPlayerRole(this.grpcServiceEndpoint, PLAYER_SPEED_UNITS);
                seekerRole_thread.start();
            }
            else //hider
            {
                HiderPlayerRole hiderRole_thread = new HiderPlayerRole(this.grpcServiceEndpoint, PLAYER_SPEED_UNITS);
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
     * inform all players [considered safe] that the game is terminated
     */
    public void informGameTermination()
    {
        try 
        {
            Set<String> otherPlayersEndsPoints;

            this.playerLock.Acquire();
            otherPlayersEndsPoints = this.player.getOtherPlayers().keySet();
            this.playerLock.Release();

            for(String endpoint : otherPlayersEndsPoints)
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
            Set<String> otherPlayersEndsPoints;

            this.playerLock.Acquire();
            otherPlayersEndsPoints = this.player.getOtherPlayers().keySet();
            this.playerLock.Release();

            for(String endpoint : otherPlayersEndsPoints)
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
    
    public void stopSmartWatch()
    {
        monitorHrValues_thread.stopMeGently();
    }
    
    public void AcquirePlayerLock()
    {
       this.playerLock.Acquire();
    }
    
    public void ReleasePlayerLock()
    {
        this.playerLock.Release();
    }
    
    /**
     * may cause write-write conflict
     * @return 
     */
//    public void updatePlayer(Player player)
//    {
//        this.playerLock.Acquire();
//        this.player = player;
//        this.playerLock.Release();
//    }
    
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
    public static SmartWatch getInstance(Player player, String grpcServiceEndpoint, CheckToSendHrAvgsThread checkToSendHrAvgs_thread)
    {
        if(instance == null)
            instance = new SmartWatch(player, grpcServiceEndpoint, checkToSendHrAvgs_thread);
        
        return instance;
    }
}
