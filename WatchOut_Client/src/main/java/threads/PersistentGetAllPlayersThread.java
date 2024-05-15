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

public class PersistentGetAllPlayersThread extends RestPeriodicThread
{
    private volatile GetAllPlayersResponse response;

    public PersistentGetAllPlayersThread (Client client, String serverAddress, Gson jsonSerializer, int waitMilliseconds)
    {
       super(client, serverAddress, jsonSerializer, waitMilliseconds);
    }
    
    @Override
    public synchronized void run()
    {
        try
        {
            while (true)
            {
                String url = serverAddress + RestServerSuffixes.GET_ALL_PLAYERS;

                //transmit player's endpoint
                ClientResponse clientResponse = this.performGetRequest(this.client, url);

                if (clientResponse == null)
                {
                    System.out.println("In run: null response on " + url);
                    continue;
                }

                System.out.println("PersistentGetAllPlayersThread: " + clientResponse.toString());
                
                StatusType responseStatus = clientResponse.getStatusInfo();

                //check if sent successfully 
                if(responseStatus.getStatusCode() == Response.Status.OK.getStatusCode())
                {
                    String response = clientResponse.getEntity(String.class);
                    this.response = jsonSerializer.fromJson(response.replaceAll("\\\\\"",""), GetAllPlayersResponse.class);
                
                    break;
                }
                else
                    System.err.println("Couldn't process GET_ALL_PLAYERS to the server with status: " + responseStatus);
                
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
    
    public GetAllPlayersResponse getResponse()
    {
        return this.response;
    }
}
