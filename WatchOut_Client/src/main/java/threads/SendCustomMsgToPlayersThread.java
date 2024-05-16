/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threads;

import manager.GameManager;

public class SendCustomMsgToPlayersThread extends Thread
{
    protected volatile boolean isCompletedSuccessfully = false;
    private String customMessage;
    private boolean keepPeriodic;
    private int waitMilliseconds;
    
    public SendCustomMsgToPlayersThread (String customMessage, boolean keepPeriodic, int waitMilliseconds)
    {
        this.customMessage = customMessage;
        this.keepPeriodic = keepPeriodic;
        this.waitMilliseconds = waitMilliseconds;
    }
    
    @Override
    public void run() 
    {
        try
        {
            while (true)
            {
                boolean result = GameManager.getInstance().sendCustomMsg(customMessage);
                
                if (result)
                    System.out.println("custom message sent successfully");
                
                if(result && !keepPeriodic)
                    break;
                
                if (waitMilliseconds > 0)
                    Thread.sleep(waitMilliseconds);
            }
        }
        catch (Exception e)
        {
            System.err.println("In run: " + e.getMessage());
            e.printStackTrace();
        }
        
        this.isCompletedSuccessfully = true;
    }
    
    public boolean checkIsCompleted() 
    {
        return this.isCompletedSuccessfully;
    }
}
