/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import java.util.ArrayList;
import java.util.List;
import simulators.Buffer;
import simulators.Measurement;

public class HRSimulatorBuffer implements Buffer
{
    private static final int WINDOW_SIZE = 8;
    private volatile List<Measurement> loadedMeasurements;
    private CustomLock loadedMeasurements_lock;
    
    public HRSimulatorBuffer()
    {
        loadedMeasurements = new ArrayList(); //maintains insertion order
    }
    
    /**
     * adds a measurement to loadedMeasurements.
     * informs readAllAndClean of ready loadedMeasurements
     * @param m 
     */
    @Override
    public synchronized void addMeasurement(Measurement m) 
    {
        try
        {
            loadedMeasurements_lock.Acquire();
            loadedMeasurements.add(m);
            loadedMeasurements_lock.Release();
            
            if (loadedMeasurements.size() >= WINDOW_SIZE)
                notify();
        }
        catch(Exception e)
        {
            System.err.println("In addMeasurement: " + e.getMessage());
        }
    }

    /**
     * reads measurement with the limit of WINDOW_SIZE, and then cleans read data.
     * waits until it get informed by addMeasurement.
     * @return null if buffer hasn't reached WINDOW_SIZE
     */
    @Override
    public synchronized List<Measurement> readAllAndClean()
    {
        List<Measurement> bufferedMeasurements = null;
        
        try
        {
            if (this.loadedMeasurements.size() < 8)
            {
                wait();
            }
            
            loadedMeasurements_lock.Acquire();
            bufferedMeasurements = this.loadedMeasurements.subList(0, WINDOW_SIZE - 1);
            this.loadedMeasurements = this.loadedMeasurements.subList(WINDOW_SIZE, loadedMeasurements.size() - 1);
            loadedMeasurements_lock.Release();
        }
        catch(Exception e)
        {
            System.err.println("In readAllAndClean: " + e.getMessage());
        }
        
        return bufferedMeasurements;
    }
    
}
