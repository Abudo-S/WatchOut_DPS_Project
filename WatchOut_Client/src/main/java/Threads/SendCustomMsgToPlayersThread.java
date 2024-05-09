/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Threads;

import manager.GameManager;

public class SendCustomMsgToPlayersThread extends Thread
{
    private String customMessage;
    private boolean keepPeriodic;
    private int sleepMilliseconds;
    
    public SendCustomMsgToPlayersThread (String customMessage, boolean keepPeriodic, int sleepMilliseconds)
    {
        this.customMessage = customMessage;
        this.keepPeriodic = keepPeriodic;
        this.sleepMilliseconds = sleepMilliseconds;
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
                
                if (sleepMilliseconds > 0)
                    Thread.sleep(sleepMilliseconds);
            }
        }
        catch (Exception e)
        {
            System.err.println("In run: " + e.getMessage());
        }
    }
}
