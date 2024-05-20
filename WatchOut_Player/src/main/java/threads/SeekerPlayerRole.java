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
    private static final long TIMEOUT_TO_TERMINATE = 30000; //30s
    
    public SeekerPlayerRole(String playerEndPoint, double playerSpeed, int waitMilliseconds)
    {
        super(playerEndPoint, playerSpeed, waitMilliseconds);
        
        System.err.println("Started seeker role.");
    }

    /**
     * A seeker tries to reach the closest hider (shortest distance) to its position.
     * Once the hider to seek is defined, this seeker waits the time to reach it (simulated reaching), 
     * then it checks if hider's status is Active to tag it and inform all players of the tagged hider (including the hider).
     * time = distance / speed
     */
    @Override
    public void run() 
    {
        try 
        {
            while(true)
            {
                //choose player to seek
                SimpleEntry<String, Double> playerDistanceToSeek = detectShortestOtherPlayerDistance();
            
                if(playerDistanceToSeek == null)
                {
                    System.err.println("No player to seek is found! Asking smartWatch to terminate the game...");
                    
                    //inform game termination
                    SmartWatch.getSubsequentInstance().informGameTermination();
                    break;
                }
                
                long timeToReachTarget = (long) ((playerDistanceToSeek.getValue() / this.playerSpeed) * 1000);
                        
                System.out.println("Seeking player with endpoint: " + playerDistanceToSeek.getKey() + ", distance: " + playerDistanceToSeek.getValue() +
                                   ", time to reach him: " + timeToReachTarget);
                    
                //wait the time required to reach the target
                Thread.sleep(timeToReachTarget);
                
                //add player role's delayment
                Thread.sleep(this.waitMilliseconds);
                
                Player currentPlayer = SmartWatch.getSubsequentInstance().getPlayer();
                
                //check if the hider is Active to change it to tagged
                currentPlayer.AcquireOtherPlayerLock(playerDistanceToSeek.getKey());
                if(currentPlayer.getOtherPlayer(playerDistanceToSeek.getKey()).getStatus().equals(PlayerStatus.Active))
                {
                    //read other player
                    Player otherPlayer = currentPlayer.getOtherPlayer(playerDistanceToSeek.getKey());
                    
                    otherPlayer.setStatus(PlayerStatus.Tagged);
                    
                    SmartWatch.getSubsequentInstance().informPlayerChangedPositionOrStatus(playerDistanceToSeek.getKey(), otherPlayer, true);
                    
                    currentPlayer.upsertOtherPlayer(playerDistanceToSeek.getKey(), otherPlayer);
                    
                    System.out.println("Tagged player with endpoint: " + playerDistanceToSeek.getKey());
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
    private SimpleEntry<String, Double> detectShortestOtherPlayerDistance() throws InterruptedException
    {
        long timeoutCounter = 0;
        SimpleEntry<String, Double> shortestOtherPlayerDistance = null;
        HashMap<String, Double> endpointsDistances = new HashMap();
        
        while(timeoutCounter < TIMEOUT_TO_TERMINATE)
        {
            Player currentPlayer = SmartWatch.getSubsequentInstance().getPlayer();

            SmartWatch.getSubsequentInstance().AcquirePlayerLock(); //to mantain otherPlayers data coherency on the same object 
            currentPlayer.getOtherPlayers()
                         .entrySet()
                         .stream()
                         .filter(f -> f.getValue().getStatus().equals(PlayerStatus.Active))
                         .forEach(m -> endpointsDistances.put(m.getKey(), currentPlayer.getDistanceToPosition(m.getValue().getPosition())));

            Optional<Entry<String, Double>> shortestEndpointD = endpointsDistances.entrySet()
                                                                                  .stream()
                                                                                  .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                                                                                  .findFirst();

            SmartWatch.getSubsequentInstance().ReleasePlayerLock();

            if(shortestEndpointD.isPresent())
            {
                shortestOtherPlayerDistance = new SimpleEntry(shortestEndpointD.get().getKey(), shortestEndpointD.get().getValue());
                break;
            }
            else
            {
                timeoutCounter += 5000;
                Thread.sleep(5000); //5s
            }
        }         
        
        return shortestOtherPlayerDistance;
    }
    
}
