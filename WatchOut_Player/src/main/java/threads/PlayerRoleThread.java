/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threads;

import manager.SmartWatch;

public abstract class PlayerRoleThread extends Thread
{
    protected SmartWatch smartWatch;
    protected String playerEndPoint;
    protected double playerSpeed;
    
    public PlayerRoleThread(SmartWatch smartWatch, String playerEndPoint, double playerSpeed)
    {
        this.smartWatch = smartWatch;
        this.playerEndPoint = playerEndPoint;
        this.playerSpeed = playerSpeed;
    }
    
    public double getSpeed()
    {
        return this.playerSpeed;
    }
    
    @Override
    public abstract void run();
    
}
