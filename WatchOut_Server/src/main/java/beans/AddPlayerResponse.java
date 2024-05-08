package beans;


import java.util.ArrayList;
import javax.xml.bind.annotation.XmlRootElement;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

@XmlRootElement
public class AddPlayerResponse
{
    private int playerId;
    private int[] coordinates;
    private ArrayList<String> playersEndpoints;

    public AddPlayerResponse(int playerId, int[] coordinates, ArrayList<String> playersEndpoints) throws RuntimeException
    {
        if (coordinates.length != 2)
            throw new RuntimeException("Assigned coordinates with length not = 2, playerId: " + playerId);
        
        this.playerId = playerId;
        this.coordinates = coordinates;
        this.playersEndpoints = playersEndpoints;
    }

    public long getPlayerId()
    {
        return this.playerId;
    }
    
    public int[] getPlayerCoordinates()
    {
        return this.coordinates;
    }

    public ArrayList<String> getplayersEndpoints()
    {
        return this.playersEndpoints;
    }
    
    @Override
    public String toString()
    {
        return "playerId: " + this.playerId + ", playersEndpoints : " + String.join(", ", playersEndpoints);
    }
}
