/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threads;

import manager.SmartWatch;


public class HiderPlayerRole extends PlayerRoleThread
{
    public HiderPlayerRole(SmartWatch smartWatch, String playerEndPoint)
    {
        super(smartWatch, playerEndPoint);
    }

    @Override
    public void run() 
    {
        try 
        {
            //to be implemented
        } 
        catch(Exception e)
        {
            System.err.println("In run: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
}
