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
    private CheckToStopGameThread checkToStop_thread;
    
    public CheckToStartGameThread (Client client, String serverAddress, Gson jsonSerializer, int waitMilliseconds, CheckToStopGameThread checkToStop_thread)
    {
       super(client, serverAddress, jsonSerializer, waitMilliseconds);
       this.checkToStop_thread = checkToStop_thread;
    }
    
    /**
     * continue checking if the minimum number of players to start the game is satisfied.
     * After starting the game, it starts a CheckToStopGameThread.
     */
    @Override
    public void run()
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
                
                //System.out.println("CheckToStartGameThread: " + clientResponse.toString());

                String response = clientResponse.getEntity(String.class);
                int playersNum = Integer.parseInt(jsonSerializer.fromJson(response.replaceAll("\\\\\"",""), GenericRestResponse.class).getResult());
                
                boolean result = GameManager.getInstance().checkToStart(playersNum);

                //if game is started then start checkToStop_thread and exit
                if(result)
                {
                    this.checkToStop_thread.start();
                    break;
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
