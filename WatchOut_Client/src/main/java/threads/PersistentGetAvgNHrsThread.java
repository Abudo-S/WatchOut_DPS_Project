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

public class PersistentGetAvgNHrsThread extends RestPeriodicThread
{
    private int playerId;
    private int n;
    private volatile double response;

    public PersistentGetAvgNHrsThread (Client client, String serverAddress, Gson jsonSerializer, int waitMilliseconds, int playerId, int n)
    {
       super(client, serverAddress, jsonSerializer, waitMilliseconds);
       this.playerId = playerId;
       this.n = n;
    }
    
    @Override
    public synchronized void run()
    {
        try
        {
            while (true)
            {
                String url = serverAddress + RestServerSuffixes.GET_PLAYER_AVG_N_HRS;
                
                //set url parameters
                url = url.replaceAll("{playerId}", String.valueOf(this.playerId)).replaceAll("{n}", String.valueOf(this.n));
                
                //transmit player's endpoint
                ClientResponse clientResponse = this.performGetRequest(this.client, url);

                if (clientResponse == null)
                {
                    System.out.println("In run: null response on " + url);
                    continue;
                }

                System.out.println("PersistentGetAvgNHrsThread: " + clientResponse.toString());
                
                StatusType responseStatus = clientResponse.getStatusInfo();

                //check if sent successfully 
                if(responseStatus.getStatusCode() == Response.Status.OK.getStatusCode())
                {
                    this.response = clientResponse.getEntity(Double.class);
                
                    break;
                }
                else
                    System.err.println("Couldn't process GET_PLAYER_AVG_N_HRS to the server with status: " + responseStatus);
                
                if (waitMilliseconds > 0)
                    wait(waitMilliseconds);
            }
        }
        catch (Exception e)
        {
            System.err.println("In run: " + e.getMessage());
        }
        
        this.isCompletedSuccessfully = true;
    }
    
    public double getResponse()
    {
        return this.response;
    }
}
