/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threads;


public abstract class PlayerRoleThread extends Thread
{
    protected String playerEndPoint;
    protected double playerSpeed;
    protected int waitMilliseconds; //used to delay player role
    
    public PlayerRoleThread(String playerEndPoint, double playerSpeed, int waitMilliseconds)
    {
        this.playerEndPoint = playerEndPoint;
        this.playerSpeed = playerSpeed;
        this.waitMilliseconds = waitMilliseconds;
    }
    
    public double getSpeed()
    {
        return this.playerSpeed;
    }
    
    @Override
    public abstract void run();
    
}
