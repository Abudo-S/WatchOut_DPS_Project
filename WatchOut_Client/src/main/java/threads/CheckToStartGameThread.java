/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threads;

import beans.RestServerSuffixes;
import beans.GenericRestResponse;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import manager.GameManager;


public class CheckToStartGameThread extends RestPeriodicThread
{

    public CheckToStartGameThread (Client client, String serverAddress, Gson jsonSerializer, int waitMilliseconds)
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
                String url = serverAddress + RestServerSuffixes.GET_TOTAL_PLAYERS_NUMBER;
                
                //get total players number
                ClientResponse clientResponse = this.performGetRequest(this.client, url);
                
                if (clientResponse == null)
                {
                    System.out.println("In run: null response on " + url);
                    continue;
                }
                
                System.out.println("CheckToStartGameThread: " + clientResponse.toString());

                String response = clientResponse.getEntity(String.class);
                int playersNum = Integer.parseInt(jsonSerializer.fromJson(response, GenericRestResponse.class).getResult());

                boolean result = GameManager.getInstance().checkToStart(playersNum);

                //if game is started then exit
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
