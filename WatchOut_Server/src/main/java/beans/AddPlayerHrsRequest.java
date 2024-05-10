package beans;

import java.util.Arrays;
import java.util.HashMap;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

public class AddPlayerHrsRequest
{
    private int playerId;
    private HashMap<Long, Double> hrs;

    public AddPlayerHrsRequest(int playerId, HashMap<Long, Double> hrs)
    {
        this.playerId = playerId;
        this.hrs = hrs;
    }

    public int getPlayerId()
    {
        return this.playerId;
    }
    
    public HashMap<Long, Double> getHrs()
    {
        return this.hrs;
    }
    
    public String toString()
    {
        return "playerId: " + this.playerId + ", hrs: " + Arrays.toString(this.hrs.values().toArray());
    }
}
