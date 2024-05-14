/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threads;

import beans.RestServerSuffixes;
import beans.*;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import java.util.HashMap;
import manager.GameManager;

public class CheckToStopGameThread extends RestPeriodicThread
{

    public CheckToStopGameThread (Client client, String serverAddress, Gson jsonSerializer, int waitMilliseconds)
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
                String url = this.serverAddress + RestServerSuffixes.GET_PLAYERS_HRS;
                
                //get total players number
                ClientResponse clientResponse = this.performGetRequest(this.client, url);
                
                if (clientResponse == null)
                {
                    System.out.println("In run: null response on " + url);
                    continue;
                }
                
                System.out.println("CheckToStopGameThread: " + clientResponse.toString());

                String response = clientResponse.getEntity(String.class);
                HashMap allPlayersHrs = jsonSerializer.fromJson(response, AllPlayerHRsResponse.class).getAllPlayerHrs();

                boolean result = GameManager.getInstance().checkToStop(allPlayersHrs);

                //if game is stopped then exit
                if(result)
                    break;
                
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
}
