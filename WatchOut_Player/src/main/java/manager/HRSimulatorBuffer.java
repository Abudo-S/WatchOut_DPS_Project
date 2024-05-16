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
        this.loadedMeasurements = new ArrayList(); //maintains insertion order
        this.loadedMeasurements_lock = new CustomLock();
    }
    
    /**
     * adds a measurement to loadedMeasurements.
     * informs readAllAndClean of ready loadedMeasurements.
     * @param m 
     */
    @Override
    public synchronized void addMeasurement(Measurement m)
    {
        try
        {
            //lock is needed here to guarantee that the list isn't modified
            //during adding or size check that may influence subList op. in readAllAndClean
            loadedMeasurements_lock.Acquire();
            loadedMeasurements.add(m);
            
            if (loadedMeasurements.size() >= WINDOW_SIZE)
                notify();
            
            loadedMeasurements_lock.Release();
        }
        catch(Exception e)
        {
            System.err.println("In addMeasurement: " + e.getMessage());
            e.printStackTrace();
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
            while (this.loadedMeasurements.size() < 8)
            {
                wait();
            }
            
            bufferedMeasurements = this.loadedMeasurements.subList(0, WINDOW_SIZE);
            
            loadedMeasurements_lock.Acquire();
            this.loadedMeasurements = this.loadedMeasurements.subList(WINDOW_SIZE, loadedMeasurements.size());
            loadedMeasurements_lock.Release();
        }
        catch(Exception e)
        {
            System.err.println("In readAllAndClean: " + e.getMessage());
            e.printStackTrace();
        }
        
        return bufferedMeasurements;
    }
    
}
