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
import java.util.ArrayList;
import java.util.HashMap;
import manager.GameManager;

public class CheckToStopGameThread extends RestPeriodicThread
{

    public CheckToStopGameThread (Client client, String serverAddress, Gson jsonSerializer, int waitMilliseconds)
    {
       super(client, serverAddress, jsonSerializer, waitMilliseconds);
    }
    
    /**
     * control player's hrs periodically and stop the game if necessary.
     */
    @Override
    public void run()
    {
        try
        {
            long fromTimestamp = 0;
            
            while (true)
            {
                String url = this.serverAddress + RestServerSuffixes.GET_PLAYERS_HRS;
                
                url = url.replaceAll("\\{ts1\\}", String.valueOf(fromTimestamp));
                        
                //get total players number
                ClientResponse clientResponse = this.performGetRequest(this.client, url);
                
                if (clientResponse == null)
                {
                    System.out.println("In run: null response on " + url);
                    continue;
                }
                
                //System.out.println("CheckToStopGameThread: " + clientResponse.toString());

                String response = clientResponse.getEntity(String.class);
                
                if(response != null && !response.isEmpty())
                {
                    HashMap<Integer, HashMap<Long, ArrayList<Double>>> allPlayersHrs = jsonSerializer.fromJson(response.replaceAll("\\\\\"",""), AllPlayerHRsResponse.class).getAllPlayerHrs();

                    boolean result = GameManager.getInstance().checkToStop(allPlayersHrs);

                    //if game is stopped then exit
                    if(result)
                        break;
                    
                    //update the start timestamp
                    fromTimestamp = System.currentTimeMillis();
                }
                
                
                if (waitMilliseconds > 0)
                    Thread.sleep(waitMilliseconds);
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
