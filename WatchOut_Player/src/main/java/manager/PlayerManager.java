/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;


public class PlayerManager 
{
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 1337;
    private static Gson jsonSerializer;
    private static Client client;
    
    public static void main(String[] argv)
    {
        jsonSerializer = new Gson();
        client = Client.create();
        String serverAddress = "http://" + SERVER_HOST + ":" + SERVER_PORT + "/";
        
    }
}
