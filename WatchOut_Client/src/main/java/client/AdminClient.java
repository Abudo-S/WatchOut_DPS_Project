package client;

import threads.*;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;

public class AdminClient 
{
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 1337;
    
    public static void main(String[] argv)
    {
        Gson jsonSerializer = new Gson();
        Client client = Client.create();
        String serverAddress = "http://" + SERVER_HOST + ":" + SERVER_PORT + "/";
        
        CheckToStartGameThread CheckToStart_thread = new CheckToStartGameThread(client, serverAddress, jsonSerializer, 0);
        CheckToStopGameThread CheckToStop_thread = new CheckToStopGameThread(client, serverAddress, jsonSerializer, 0);
        SendCustomMsgToPlayersThread custom_thread = new SendCustomMsgToPlayersThread("Hello, this is custom!", false, 0);
        
        //start rest threads
        CheckToStart_thread.start();
        CheckToStop_thread.start();
        
        //start custom thread
        custom_thread.start();
    }
}
