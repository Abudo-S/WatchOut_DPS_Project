/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import beans.Player;
import threads.*;
import simulators.HRSimulator;

/**
 * responsible of hr monitoring, coordination with other players through GRPC, like
 * H.B. permission acquired, seeker election, H.B.-simultaneous reaching consensus
 * @author Admin
 */
public class SmartWatch
{
    private Player player;
    private MonitorHrValuesThread monitorHrValues_thread;
    
    public SmartWatch(Player player, CheckToSendHrAvgsThread checkToSendHrAvgs_thread)
    {
        this.player = player;
        HRSimulator hrSimulator_thread = new HRSimulator("Player-" + this.player.getId(), new HRSimulatorBuffer());
        monitorHrValues_thread = new MonitorHrValuesThread(0, hrSimulator_thread, checkToSendHrAvgs_thread);
    
        monitorHrValues_thread.start();
        
        //inform all other player of the new player and update their position & status
        //one thread for each other player
    }
    
    public void startGameCoordination()
    {
        //start bully algorithm to select a seeker
    }
    
    public void stopSmartWatch()
    {
        monitorHrValues_thread.stopMeGently();
    }
    
}
