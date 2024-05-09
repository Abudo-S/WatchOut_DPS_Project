/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Threads;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public abstract class RestPeriodicThread extends Thread
{
    protected String serverAddress;
    protected Gson jsonSerializer;
    protected int sleepMilliseconds;
    protected Client client;
    
    public RestPeriodicThread (Client client, String serverAddress, Gson jsonSerializer, int sleepMilliseconds)
    {
        this.client = client;
        this.serverAddress = serverAddress;
        this.jsonSerializer = jsonSerializer;
        this.sleepMilliseconds = sleepMilliseconds;
    }

    @Override
    public void run(){}
    
    protected ClientResponse performGetRequest(Client client, String url)
    {
        WebResource webResource = client.resource(url);
        
        try
        {
            return webResource.get(ClientResponse.class);
        } 
        catch (ClientHandlerException e) 
        {
            System.err.println("in performGetRequest: " + e);
        }
        
        return null;
    }
    
    //add further rest method here
}
