/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threads;

import beans.Player;
import beans.PlayerStatus;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Map.Entry;
import manager.SmartWatch;

public class SeekerPlayerRole extends PlayerRoleThread
{   
    public SeekerPlayerRole(SmartWatch smartWatch, String playerEndPoint, double playerSpeed)
    {
        super(smartWatch, playerEndPoint, playerSpeed);
    }

    /**
     * A seeker tries to reach the closest hider (shortest distance) to its position.
     * Once the hider to seek is defined, this seeker waits the time to reach it (simulated reaching), 
     * then it checks if hider's status is Active to tag it and inform all players of the tagged hider (including the hider).
     * time = distance / speed
     */
    @Override
    public synchronized void run() 
    {
        try 
        {
            while(true)
            {
                //choose player to seek
                SimpleEntry<String, Double> playerDistanceToSeek = detectShortestOtherPlayerDistance();
            
                //wait the time required to reach the target
                wait((long) Math.ceil(playerDistanceToSeek.getValue() / this.playerSpeed));
                
                Player currentPlayer = this.smartWatch.getPlayer();
                
                //check if the hider is Active to change it to tagged
                currentPlayer.AcquireOtherPlayerLock(playerDistanceToSeek.getKey());
                if(currentPlayer.getOtherPlayer(playerDistanceToSeek.getKey()).getStatus().equals(PlayerStatus.Active))
                {
                    Player otherPlayer = currentPlayer.getOtherPlayer(playerDistanceToSeek.getKey());
                    
                    otherPlayer.setStatus(PlayerStatus.Tagged);
                    
                    this.smartWatch.informPlayerChangedPositionOrStatus(playerDistanceToSeek.getKey(), otherPlayer, true);
                    
                    currentPlayer.upsertOtherPlayer(playerDistanceToSeek.getKey(), otherPlayer);
                }
                
                currentPlayer.ReleaseOtherPlayerLock(playerDistanceToSeek.getKey());
            } 
        }
        catch(Exception e)
        {
            System.err.println("In run: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * gets a copy of other player's data who is the closest one to the current player
     * the detected player's state should be Active
     * if the seeker doesn't find any player to seek then it informs smartWatch that the game is terminated.
     * @return <otherPlayerEndpoint, distanceToRun>
     */
    private SimpleEntry<String, Double> detectShortestOtherPlayerDistance()
    {
        SimpleEntry<String, Double> shortestOtherPlayerDistance = null;
        HashMap<String, Double> endpointsDistances = new HashMap();
                
        Player currentPlayer = this.smartWatch.getPlayer();
        
        this.smartWatch.AcquirePlayerLock(); //to mantain otherPlayers data coherency on the same object 
        currentPlayer.getOtherPlayers()
                     .entrySet()
                     .stream()
                     .filter(f -> f.getValue().getStatus().equals(PlayerStatus.Active))
                     .forEach(m -> endpointsDistances.put(m.getKey(), currentPlayer.getDistanceToPosition(m.getValue().getPosition())));
                
        Optional<Entry<String, Double>> shortestEndpointD = endpointsDistances.entrySet()
                                                                              .stream()
                                                                              .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                                                                              .findFirst();

        this.smartWatch.ReleasePlayerLock();
        
        if(shortestEndpointD.isPresent())
        {
            shortestOtherPlayerDistance = new SimpleEntry(shortestEndpointD.get().getKey(), shortestEndpointD.get().getValue());
        }
        else //inform game termination
        {
            this.smartWatch.informGameTermination();
        }
        
        return shortestOtherPlayerDistance;
    }
    
}
