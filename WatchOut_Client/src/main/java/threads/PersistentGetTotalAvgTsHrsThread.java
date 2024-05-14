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

public class PersistentGetTotalAvgTsHrsThread extends RestPeriodicThread
{
    private long ts1;
    private long ts2;
    private volatile double result;

    public PersistentGetTotalAvgTsHrsThread (Client client, String serverAddress, Gson jsonSerializer, int waitMilliseconds, long ts1, long ts2)
    {
       super(client, serverAddress, jsonSerializer, waitMilliseconds);
       this.ts1 = ts1;
       this.ts2 = ts2;
    }
    
    @Override
    public synchronized void run()
    {
        try
        {
            while (true)
            {
                String url = serverAddress + RestServerSuffixes.GET_PLAYER_AVG_TIMESTAMPED_HRS;
                
                //set url parameters
                url = url.replaceAll("\\{ts1\\}", String.valueOf(this.ts1)).replaceAll("\\{ts2\\}", String.valueOf(this.ts2));
                
                //transmit player's endpoint
                ClientResponse clientResponse = this.performGetRequest(this.client, url);

                if (clientResponse == null)
                {
                    System.out.println("In run: null response on " + url);
                    continue;
                }

                System.out.println("PersistentGetPlayerAvgTimstampedHrsThread: " + clientResponse.toString());
                
                StatusType responseStatus = clientResponse.getStatusInfo();

                //check if sent successfully 
                if(responseStatus.getStatusCode() == Response.Status.OK.getStatusCode())
                {
                    String response = clientResponse.getEntity(String.class);
                    GenericRestResponse genericRestResponse = jsonSerializer.fromJson(response, GenericRestResponse.class);
                    this.result = Double.parseDouble(genericRestResponse.getResult());
                
                    break;
                }
                else
                    System.err.println("Couldn't process GET_PLAYER_AVG_TIMESTAMPED_HRS to the server with status: " + responseStatus);
                
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
    
    public double getResult()
    {
        return this.result;
    }
}
