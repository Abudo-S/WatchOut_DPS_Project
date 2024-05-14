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
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;
import manager.*;


public class CheckToSendHrAvgsThread extends RestPeriodicThread
{
    private static final int HR_FREQ_TIME_TO_SERVER = 10000; //10s
    private int playerId;
    private volatile boolean stopCondition = false;
    
    /**
     * volatile helps to keep the size check updated
     * <HR_Avgs>
     */
    private volatile List<Double> reservedHrAvgs;
    private CustomLock reservedHrAvgs_Lock;

    public CheckToSendHrAvgsThread (Client client, String serverAddress, Gson jsonSerializer, int waitMilliseconds, int playerId)
    {
       super(client, serverAddress, jsonSerializer, waitMilliseconds);
       this.playerId = playerId;
       this.reservedHrAvgs = new ArrayList();
    }
    
    @Override
    public synchronized void run()
    {
        try
        {
            while (!stopCondition)
            {
                String url = serverAddress + RestServerSuffixes.POST_ADD_PLAYER_HRS;
                
                if(reservedHrAvgs.size() > 0)
                {
                    this.reservedHrAvgs_Lock.Acquire();
                    int reservedHrAvgsSize = reservedHrAvgs.size(); //used later to clean already sent hrs
                    SimpleEntry timestampedHrAvgs = new SimpleEntry(System.currentTimeMillis(), this.reservedHrAvgs);
                    AddPlayerHrsRequest addPlayerHrsRequest = new AddPlayerHrsRequest(this.playerId, timestampedHrAvgs);
                    this.reservedHrAvgs_Lock.Release();

                    //transmit player's hrs
                    ClientResponse clientResponse = this.performPostRequest(this.client, url, addPlayerHrsRequest);

                    if (clientResponse == null)
                    {
                        System.out.println("In run: null response on " + url);
                        continue;
                    }

                    System.out.println("CheckToSendHrAvgsThread: " + clientResponse.toString());

                    StatusType responseStatus = clientResponse.getStatusInfo();

                    //check if sent successfully 
                    if(responseStatus.getStatusCode() == Response.Status.OK.getStatusCode())
                    {
                        //clean already sent hrs averages
                        this.reservedHrAvgs_Lock.Acquire();
                        this.reservedHrAvgs = this.reservedHrAvgs.subList(0, reservedHrAvgsSize);
                        this.reservedHrAvgs_Lock.Release();

                        //apply required waiting time
                        wait(HR_FREQ_TIME_TO_SERVER);
                    }
                    else
                        System.err.println("Couldn't send player's hr avgs to the server with status: " + responseStatus);
                }
                
                if (waitMilliseconds > 0)
                    wait(waitMilliseconds);
            }
        }
        catch (Exception e)
        {
            System.err.println("In run: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void addToReservedHrAvg(Double hrAvg)
    {
        this.reservedHrAvgs_Lock.Acquire();
        this.reservedHrAvgs.add(hrAvg);
        this.reservedHrAvgs_Lock.Release();
    }
    
    public void stopMeGently() 
    {
        stopCondition = true;
    }
}
