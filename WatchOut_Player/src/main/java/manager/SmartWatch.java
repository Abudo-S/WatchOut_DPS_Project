/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import simulators.HRSimulator;

public class SmartWatch
{
    private HRSimulatorBuffer hrBuffer;
    
    public SmartWatch(int playerId)
    {
        this.hrBuffer = new HRSimulatorBuffer();
        HRSimulator hrSimulatorThread = new HRSimulator("Player_" + playerId, hrBuffer);
        hrSimulatorThread.start();
    }
    
}
