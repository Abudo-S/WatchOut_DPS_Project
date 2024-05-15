/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threads;

import manager.SmartWatch;

public abstract class PlayerRoleThread extends Thread
{
    protected String playerEndPoint;
    protected double playerSpeed;
    
    public PlayerRoleThread(String playerEndPoint, double playerSpeed)
    {
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
