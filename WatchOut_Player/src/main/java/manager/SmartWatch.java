/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

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
    
    private static SmartWatch instance;
    
    private SmartWatch(Player player, CheckToSendHrAvgsThread checkToSendHrAvgs_thread)
    {
        this.player = player;
        this.playerLock = new CustomLock();
        HRSimulator hrSimulator_thread = new HRSimulator("Player-" + this.player.getId(), new HRSimulatorBuffer());
        monitorHrValues_thread = new MonitorHrValuesThread(0, hrSimulator_thread, checkToSendHrAvgs_thread);
    
        monitorHrValues_thread.start();
        
        informNewEntry();
    }
    
    /**
     * inform all other players of the new player and update their position & status
     * one thread for each other player
     */
    private void informNewEntry()
    {
        try
        {
            Set<String> otherPlayersEndsPoints;
            
            this.playerLock.Acquire();
            otherPlayersEndsPoints = this.player.getOtherPlayers().keySet();
            this.playerLock.Release();
            
            for(String endpoint : otherPlayersEndsPoints)
            {
                InformForNewEntryThread InformForNewEntry_thread = new InformForNewEntryThread(endpoint, this, this.grpcServiceEndpoint);
                InformForNewEntry_thread.start();
            }
        }
        catch(Exception e)
        {
            System.err.println("In informNewEntry: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * ask to be seeker from all other players; if all other players agree then this player is the seeker.
     * one thread for each other player
     * modified bully election
     */
    public void startGameCoordination()
    {
        try
        {    
            ArrayList<AskToBeSeekerThread> gatheredThreads = new ArrayList();
            Set<String> otherPlayersEndsPoints;

            this.playerLock.Acquire();
            otherPlayersEndsPoints = this.player.getOtherPlayers().keySet();
            this.playerLock.Release();

            for(String endpoint : otherPlayersEndsPoints)
            {
                AskToBeSeekerThread askToBeSeeker_thread = new AskToBeSeekerThread(endpoint, this);
                askToBeSeeker_thread.start();
                gatheredThreads.add(askToBeSeeker_thread);
            }
            
            //We need to wait for all threads' results to determine current player's role
            boolean resultsGathered = false;
            while(!resultsGathered)
            {
                resultsGathered = !gatheredThreads.stream()
                                                  .map(m -> m.checkIsCompleted())
                                                  .anyMatch(m -> m.equals(false));
            }
            
            boolean finalAgreedSeeker = !gatheredThreads.stream()
                                                        .map(m -> m.getAgreementResult())
                                                        .anyMatch(m -> m.equals(false));
            
            if(finalAgreedSeeker)
            {
                SeekerPlayerRole seekerRole_thread = new SeekerPlayerRole(this, this.grpcServiceEndpoint, PLAYER_SPEED_UNITS);
                seekerRole_thread.start();
            }
            else
            {
                //start hider thread
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
                InformForNewEntryThread informGameTerm_thread = new InformForNewEntryThread(endpoint, this, this.grpcServiceEndpoint);
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
                InformPlayerChangedThread informPlayerChanged_thread = new InformPlayerChangedThread(endpoint, this, changedPlayerEndPoint, changedPlayer, isSentBySeeker);
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
    
    public String getGrpcEndpoint()
    {
        return this.grpcServiceEndpoint;
    }
    
    public Player getPlayer()
    {
        return this.player;
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
    public static SmartWatch getInstance(Player player, CheckToSendHrAvgsThread checkToSendHrAvgs_thread)
    {
        if(instance == null)
            instance = new SmartWatch(player, checkToSendHrAvgs_thread);
        
        return instance;
    }
}
