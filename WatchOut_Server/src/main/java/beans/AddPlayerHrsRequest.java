package beans;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

public class AddPlayerHrsRequest
{
    private int playerId;
    private SimpleEntry<Long, ArrayList<Double>> hrs;

    public AddPlayerHrsRequest(int playerId, SimpleEntry<Long, ArrayList<Double>> hrs)
    {
        this.playerId = playerId;
        this.hrs = hrs;
    }

    public int getPlayerId()
    {
        return this.playerId;
    }
    
    public SimpleEntry<Long, ArrayList<Double>> getHrs()
    {
        return this.hrs;
    }
    
    @Override
    public String toString()
    {
        return "playerId: " + this.playerId + ", hrs: " + Arrays.toString(this.hrs.getValue().toArray());
    }
}
