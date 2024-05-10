/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import Threads.*;
import java.util.ArrayList;
import simulators.HRSimulator;

public class SmartWatch
{
    private HRSimulator hrSimulator_thread;
    private MonitorHrValuesThread monitorHrValues_thread;
    private CheckToSendHrAvgsThread checkToSendHrAvgs_thread;
    
    public SmartWatch(int playerId, CheckToSendHrAvgsThread checkToSendHrAvgs_thread)
    {
        this.checkToSendHrAvgs_thread = checkToSendHrAvgs_thread;
        hrSimulator_thread = new HRSimulator("Player-" + playerId, new HRSimulatorBuffer());
        monitorHrValues_thread = new MonitorHrValuesThread(0, hrSimulator_thread, this.checkToSendHrAvgs_thread);
    
        monitorHrValues_thread.start();
    }
    
    public void stopSmartWatch()
    {
        monitorHrValues_thread.stopMeGently();
    }
    
}
