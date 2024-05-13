/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;


public class GetAllPlayersResponse 
{
    /**
     * <playerId, <endpoint, position>>
     */
    private HashMap<Integer, SimpleEntry<String, Integer[]>> players;
    
    public GetAllPlayersResponse(HashMap<Integer, SimpleEntry<String, Integer[]>> players)
    {
     this.players = players;
    }
    
    public HashMap<Integer, SimpleEntry<String, Integer[]>> getPlayers()
    {
        return this.players;
    }
}
