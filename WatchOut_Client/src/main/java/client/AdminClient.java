package client;

import threads.*;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import java.util.Scanner;

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
        //SendCustomMsgToPlayersThread custom_thread = new SendCustomMsgToPlayersThread("Hello, this is custom!", false, 0);
        
        //start rest threads
        CheckToStart_thread.start();
        CheckToStop_thread.start();
        
        //start custom thread
        //custom_thread.start();
    }
    
    public static void startCLI_menu()
    {
        try
        {
            boolean stopCondition = false;
            int option;
            Scanner sc = new Scanner(System.in);
            
            System.out.println("1-Return all present player.");
            System.out.println("1-Calculate the average of last N HRS for a player.");
            System.out.println("1-Calculate the average of timestamp-bounded HRS for a player.");
            System.out.println("1-Send custom message to all players.");
            
            System.out.println("Insert option number:");
            
            while(stopCondition)
            {
                option = sc.nextInt();
                switch(option)
                {
                    //to be completed
                }
            }
        }
        catch(Exception e)
        {
            System.err.println("In startCLI_menu: " + e.getMessage());
        }
    }
}
