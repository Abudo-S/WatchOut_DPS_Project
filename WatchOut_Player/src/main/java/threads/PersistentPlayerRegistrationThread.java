/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threads;

import beans.*;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

public class PersistentPlayerRegistrationThread extends RestPeriodicThread
{
    private volatile boolean isCompletedSuccessfully = false;
    private volatile Player builtPlayer;
    private String playerEndPoint;

    public PersistentPlayerRegistrationThread (Client client, String serverAddress, Gson jsonSerializer, int waitMilliseconds, String playerEndPoint)
    {
       super(client, serverAddress, jsonSerializer, waitMilliseconds);
       this.playerEndPoint = playerEndPoint;
    }
    
    @Override
    public synchronized void run()
    {
        try
        {
            while (true)
            {
                String url = serverAddress + RestServerSuffixes.POST_ADD_PLAYER;

                //transmit player's endpoint
                ClientResponse clientResponse = this.performPostRequest(this.client, url, this.playerEndPoint);

                if (clientResponse == null)
                {
                    System.out.println("In run: null response on " + url);
                    continue;
                }

                System.out.println("PersistentPlayerRegistrationThread: " + clientResponse.toString());
                
                StatusType responseStatus = clientResponse.getStatusInfo();

                //check if sent successfully 
                if(responseStatus.getStatusCode() == Response.Status.OK.getStatusCode())
                {
                    String response = clientResponse.getEntity(String.class);
                    AddPlayerResponse addPlayerResponse = jsonSerializer.fromJson(response.replaceAll("\\\\\"",""), AddPlayerResponse.class);
                
                    //intially after registration the player is active
                    this.builtPlayer = new Player(addPlayerResponse.getPlayerId(), addPlayerResponse.getPlayerCoordinates(), PlayerStatus.Active);
                    this.builtPlayer.addInitialOtherPlayers(addPlayerResponse.getplayersEndpoints());
                    
                    break;
                }
                else
                    System.err.println("Couldn't process POST_ADD_PLAYER to the server with status: " + responseStatus);
                
                if (waitMilliseconds > 0)
                    wait(waitMilliseconds);
            }
        }
        catch (Exception e)
        {
            System.err.println("In run: " + e.getMessage());
            e.printStackTrace();
        }
        
        this.isCompletedSuccessfully = true;
    }
    
    public Player getBuiltPlayer()
    {
        return this.builtPlayer;
    }
    
    public boolean checkIsCompleted() 
    {
        return this.isCompletedSuccessfully;
    }
}
