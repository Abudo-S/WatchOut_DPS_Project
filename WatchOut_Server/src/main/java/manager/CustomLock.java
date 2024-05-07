/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

public class CustomLock 
{
    private volatile boolean lockAcquired = false;
    
    CustomLock()
    {
        
    }

    /**
     * the first thread enters and doesn't go in wait(), other threads find that lockAcquired = true, so they just keep waiting
     */
    public synchronized void Acquire()
    {
        while (this.lockAcquired) 
        {
            try 
            {
                this.wait();
            }
            catch(InterruptedException e) 
            {
                System.out.println("In Acquire: " + e.getMessage());
            }
        }
        
        this.lockAcquired = true;
    }

    public synchronized void Release() 
    {
        this.lockAcquired = false;
        this.notify();
    }
}
