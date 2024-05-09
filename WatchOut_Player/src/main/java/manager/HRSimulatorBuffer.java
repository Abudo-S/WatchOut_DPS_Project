/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import java.util.ArrayList;
import java.util.List;
import javax.lang.model.SourceVersion;
import simulators.Buffer;
import simulators.Measurement;

public class HRSimulatorBuffer implements Buffer
{
    private static final int WINDOW_SIZE = 8;
    private List<Measurement> loadedMeasurements;
    
    public HRSimulatorBuffer()
    {
        loadedMeasurements = new ArrayList(); //maintains insertion order
    }
    
    @Override
    public synchronized void addMeasurement(Measurement m) 
    {
        try
        {
            loadedMeasurements.add(m);
        }
        catch(Exception e)
        {
            System.err.println("In addMeasurement: " + e.getMessage());
        }
    }

    /**
     * returns null if buffer hasn't reached WINDOW_SIZE
     * reads measurement with the limit of WINDOW_SIZE, and then cleans read data
     * @return 
     */
    @Override
    public synchronized List<Measurement> readAllAndClean() 
    {
        List<Measurement> bufferedMeasurements = null;
        
        try
        {
            if (this.loadedMeasurements.size() >= 8)
            {
                //
            }
        }
        catch(Exception e)
        {
            System.err.println("In readAllAndClean: " + e.getMessage());
        }
        
        return bufferedMeasurements;
    }
    
}
