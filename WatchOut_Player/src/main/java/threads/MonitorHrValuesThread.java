/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threads;

import java.util.*;
import simulators.HRSimulator;
import simulators.Measurement;


public class MonitorHrValuesThread extends Thread
{
    private volatile boolean stopCondition = false;
    private CheckToSendHrAvgsThread checkToSendHrAvgs_thread;
    private HRSimulator hrSimulator_thread;
    private int waitMilliseconds;

    public MonitorHrValuesThread (int waitMilliseconds, HRSimulator hrSimulator_thread, CheckToSendHrAvgsThread checkToSendHrAvgs_thread)
    {
       this.waitMilliseconds = waitMilliseconds;
       this.hrSimulator_thread = hrSimulator_thread;
       this.checkToSendHrAvgs_thread = checkToSendHrAvgs_thread;
    }
    
    @Override
    public synchronized void run()
    {
        try
        {
            while (!stopCondition)
            {
                List<Measurement> measurements = this.hrSimulator_thread.getBuffer().readAllAndClean();
                double measurementsAvg = (measurements.stream()
                                                      .mapToDouble(m -> m.getValue())
                                                      .sum() / (double)measurements.size()) * 0.5;
                
                this.checkToSendHrAvgs_thread.addToReservedHrAvg(measurementsAvg);
                
                if (waitMilliseconds > 0)
                    wait(waitMilliseconds);
            }
        }
        catch (Exception e)
        {
            System.err.println("In run: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void starAuxiliaryThread() 
    {
        this.hrSimulator_thread.start();
        this.checkToSendHrAvgs_thread.start();
    }
    
    public void stopMeGently() 
    {
        stopCondition = true;
        
        this.hrSimulator_thread.stopMeGently();
        this.checkToSendHrAvgs_thread.stopMeGently();
    }
}
