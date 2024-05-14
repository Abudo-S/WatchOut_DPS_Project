/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threads;

import beans.Player;
import manager.SmartWatch;


public class HiderPlayerRole extends PlayerRoleThread
{
    private static final double TIME_TO_WAIT_OUT_HB = 10000; //10s
    
    public HiderPlayerRole(SmartWatch smartWatch, String playerEndPoint, double playerSpeed)
    {
        super(smartWatch, playerEndPoint, playerSpeed);
    }

    /**
     * A hider tries to obtain a permission to move towards the H.B.
     * when it obtains the permission, it informs otherPlayers of obtained permission by changing its status.
     * this hider waits the time to reach the H.B. (simulated reaching), then it asks to enter the H.B.
     * time = distance / speed
     */
    @Override
    public synchronized void run()
    {
        try 
        {
            while(true)
            {
                //try to get a permission to move towards the H.B
                break;
            }
            //permission acquired
            //inform changed status to Moving
            
            Double distance = Player.getMinDistanceToHB(this.smartWatch.getPlayer().getPosition());
            
            //wait the time required to reach the home base
            wait((long) Math.ceil(distance / this.playerSpeed));
                
            //to be implemented
            
        } 
        catch(Exception e)
        {
            System.err.println("In run: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
}
