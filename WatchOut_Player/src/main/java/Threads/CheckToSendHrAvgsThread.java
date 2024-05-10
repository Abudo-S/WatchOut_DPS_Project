/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Threads;

import beans.*;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import java.util.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;
import manager.*;


public class CheckToSendHrAvgsThread extends RestPeriodicThread
{
    private static final int HR_FREQ_TIME_TO_SERVER = 10000; //10s
    private int playerId;
    private volatile boolean stopCondition = false;
    
    private HashMap<Long, Double> reservedHrAvgs;
    private CustomLock reservedHrAvgs_Lock;

    public CheckToSendHrAvgsThread (Client client, String serverAddress, Gson jsonSerializer, int waitMilliseconds, int playerId)
    {
       super(client, serverAddress, jsonSerializer, waitMilliseconds);
       this.playerId = playerId;
       this.reservedHrAvgs = new HashMap();
    }
    
    @Override
    public synchronized void run()
    {
        try
        {
            while (!stopCondition)
            {
                String url = serverAddress + RestServerSuffixes.ADD_PLAYER_HRS;
                
                this.reservedHrAvgs_Lock.Acquire();
                Set reservedHrAvgsKeys = reservedHrAvgs.keySet(); //used later to clean already sent hrs
                AddPlayerHrsRequest addPlayerHrsRequest = new AddPlayerHrsRequest(this.playerId, reservedHrAvgs);
                this.reservedHrAvgs_Lock.Release();
                
                //transmit player's hrs
                ClientResponse clientResponse = this.performPostRequest(this.client, url, addPlayerHrsRequest);
                
                if (clientResponse == null)
                {
                    System.out.println("In run: null response on " + url);
                    continue;
                }
                
                System.out.println("MonitorHrValuesThread: " + clientResponse.toString());

                StatusType responseStatus = clientResponse.getStatusInfo();

                //check if sent successfully 
                if(responseStatus.getStatusCode() == Response.Status.OK.getStatusCode())
                {
                    //clean already sent hrs keys
                    this.reservedHrAvgs_Lock.Acquire();
                    this.reservedHrAvgs.keySet().removeAll(reservedHrAvgsKeys);
                    this.reservedHrAvgs_Lock.Release();
                    
                    //apply required waiting time
                    wait(HR_FREQ_TIME_TO_SERVER);
                }
                else
                    System.err.println("Couldn't send player's hr avgs to the server with status: " + responseStatus);
                
                
                if (waitMilliseconds > 0)
                    wait(waitMilliseconds);
            }
        }
        catch (Exception e)
        {
            System.err.println("In run: " + e.getMessage());
            this.reservedHrAvgs_Lock.Release(); //release the lock if it was acquired
        }
    }
    
    public void addToReservedHrAvg(Long timestamp, Double hrAvg)
    {
        this.reservedHrAvgs_Lock.Acquire();
        this.reservedHrAvgs.put(timestamp, hrAvg);
        this.reservedHrAvgs_Lock.Release();
    }
    
    public void stopMeGently() 
    {
        stopCondition = true;
    }
}
