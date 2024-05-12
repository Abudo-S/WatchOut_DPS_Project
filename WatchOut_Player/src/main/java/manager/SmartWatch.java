/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import beans.Player;
import java.util.Set;
import threads.*;
import simulators.HRSimulator;

/**
 * responsible of hr monitoring, coordination with other players through GRPC, like
 * H.B. permission acquired, seeker election, H.B.-simultaneous reaching consensus
 * @author Admin
 */
public class SmartWatch
{
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
     * inform all other player of the new player and update their position & status
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
                InformForNewEntryThread InformForNewEntry_thread = new InformForNewEntryThread(endpoint, this, grpcServiceEndpoint);
                InformForNewEntry_thread.start();
            }
        }
        catch(Exception e)
        {
            System.out.println("informNewEntry: " + e.getMessage());
        }
    }
    
    public void startGameCoordination()
    {
        //start bully algorithm to select a seeker
    }
    
    public void stopSmartWatch()
    {
        monitorHrValues_thread.stopMeGently();
    }
    
    public void updatePlayer(Player player)
    {
        this.playerLock.Acquire();
        this.player = player;
        this.playerLock.Release();
    }
    
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
