/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threads;

import beans.Player;
import beans.PlayerStatus;
import beans.SharedResource;
import manager.SmartWatch;


public class HiderPlayerRole extends PlayerRoleThread
{
    private static final int TIME_TO_WAIT_OUT_HB = 10000; //10s
    
    public HiderPlayerRole(String playerEndPoint, double playerSpeed, int waitMilliseconds)
    {
        super(playerEndPoint, playerSpeed, waitMilliseconds);
        
        System.err.println("Started hider role.");
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
            SmartWatch smartWatch = SmartWatch.getSubsequentInstance();
            
            //request a permission to H.B.
            new Thread(() -> smartWatch.acquireSharedResource(SharedResource.HomeBase, System.currentTimeMillis())).start();
            
            //wait for the PermissionAcquired() to be invoked
            this.wait();
            
            //permission acquired
            System.err.println("Home base permission acquired!");
            
            smartWatch.AcquirePlayerLock();
            Player currentPlayer = smartWatch.getPlayer();
            smartWatch.ReleasePlayerLock();
            
            //check player's status then go to the H.B
            if(currentPlayer.getStatus().equals(PlayerStatus.Active))
            {
                //change player's status to Moving
                smartWatch.AcquirePlayerLock();
                smartWatch.getPlayer().setStatus(PlayerStatus.Moving);
                smartWatch.ReleasePlayerLock();
                
                //inform changed status
                smartWatch.informPlayerChangedPositionOrStatus(smartWatch.getGrpcEndpoint(), smartWatch.getPlayer(), false);
                
                Double distance = Player.getMinDistanceToHB(SmartWatch.getSubsequentInstance().getPlayer().getPosition());
            
                //wait the time required to reach the home base
                Thread.sleep((long) Math.ceil(distance / this.playerSpeed));

                //wait the time required in the H.B. to be considered safe
                Thread.sleep(TIME_TO_WAIT_OUT_HB);
                
                //change player's status to Safe
                smartWatch.AcquirePlayerLock();
                smartWatch.getPlayer().setStatus(PlayerStatus.Safe);
                smartWatch.ReleasePlayerLock();
                
                //inform changed status
                smartWatch.informPlayerChangedPositionOrStatus(smartWatch.getGrpcEndpoint(), smartWatch.getPlayer(), false);
                
                //add player role's delayment
                Thread.sleep(this.waitMilliseconds);
            }
            
            System.err.println("Releasing home base!");
            
            //release the home base
            smartWatch.informReleasedSharedResource(SharedResource.HomeBase);
        } 
        catch(Exception e)
        {
            System.err.println("In run: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public synchronized void PermissionAcquired()
    {
        this.notify();
    }
    
}
