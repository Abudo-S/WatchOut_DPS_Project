/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threads;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public abstract class RestPeriodicThread extends Thread
{
    protected volatile boolean isCompletedSuccessfully = false;
    protected String serverAddress;
    protected Gson jsonSerializer;
    protected Client client;
    protected int waitMilliseconds;
    
    public RestPeriodicThread (Client client, String serverAddress, Gson jsonSerializer, int waitMilliseconds)
    {
        this.client = client;
        this.serverAddress = serverAddress;
        this.jsonSerializer = jsonSerializer;
        this.waitMilliseconds = waitMilliseconds;
    }

    @Override
    public abstract void run();
    
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
    
    public boolean checkIsCompleted() 
    {
        return this.isCompletedSuccessfully;
    }
}
