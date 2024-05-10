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
    
    public ClientResponse performPostRequest(Client client, String url, Object req){
        WebResource webResource = client.resource(url);
        String input = new Gson().toJson(req);
        try 
        {
            return webResource.type("application/json").post(ClientResponse.class, input);
        } 
        catch (ClientHandlerException e)
        {
            System.err.println("in performGetRequest: " + e);
        }
        
        return null;
    }
    
    //add further rest method here
}
