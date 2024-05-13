package client;

import threads.*;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import java.util.Scanner;

public class AdminClient 
{
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 1337;
    private static Gson jsonSerializer;
    private static Client client;
    private static String serverAddress;
    
    public static void main(String[] argv)
    {
        jsonSerializer = new Gson();
        client = Client.create();
        serverAddress = "http://" + SERVER_HOST + ":" + SERVER_PORT + "/";
        
//        CheckToStartGameThread CheckToStart_thread = new CheckToStartGameThread(client, serverAddress, jsonSerializer, 0);
//        CheckToStopGameThread CheckToStop_thread = new CheckToStopGameThread(client, serverAddress, jsonSerializer, 0);
//        //SendCustomMsgToPlayersThread custom_thread = new SendCustomMsgToPlayersThread("Hello, this is custom!", false, 0);
//        
//        //start rest threads
//        CheckToStart_thread.start();
//        CheckToStop_thread.start();
        
        //start custom thread
        //custom_thread.start();
        
        startCLI_menu();
    }
    
    public static void startCLI_menu()
    {
        
        boolean stopCondition = false;
        int option;
        Scanner sc = new Scanner(System.in);

        System.out.println("1-Return all present player.");
        System.out.println("2-Calculate the average of last N HRS for a player.");
        System.out.println("3-Calculate the average of timestamp-bounded HRS for a player.");
        System.out.println("4-Send custom message to all players.");
        System.out.println("5-Exit");

        System.out.println("Insert option number: ");

        while(!stopCondition)
        {
            try
            {
                option = sc.nextInt();
                switch(option)
                {
                    case (1):
                        PersistentGetAllPlayersThread getAllPlayers = new PersistentGetAllPlayersThread(client, serverAddress, jsonSerializer, 0);
                        getAllPlayers.start();

                        while (!getAllPlayers.checkIsCompleted())
                        {
                            Thread.sleep(500);
                        }

                        System.out.println(getAllPlayers.getResponse().toString());
                        
                        break;

                    case (2):
                        System.out.println("Insert player id:");
                        int playerId = sc.nextInt();
                        System.out.println("Insert N:");
                        int n = sc.nextInt();
                        
                        PersistentGetAvgNHrsThread getAvgNHrsPlayers = new PersistentGetAvgNHrsThread(client, serverAddress, jsonSerializer, 0, playerId, n);
                        getAvgNHrsPlayers.start();

                        while (!getAvgNHrsPlayers.checkIsCompleted())
                        {
                            Thread.sleep(500);
                        }

                        System.out.println("Avg:" + getAvgNHrsPlayers.getResponse());
                        break;

                    case (3):
                        System.out.println("Insert lower-bound timestamp:");
                        long ts1 = sc.nextLong();
                        System.out.println("Insert higher-bound timestamp:");
                        long ts2 = sc.nextLong();
                        
                        PersistentGetTotalAvgTsHrsThread getPlayerAvgTsHrs = new PersistentGetTotalAvgTsHrsThread(client, serverAddress, jsonSerializer, 0, ts1, ts2);
                        getPlayerAvgTsHrs.start();

                        while (!getPlayerAvgTsHrs.checkIsCompleted())
                        {
                            Thread.sleep(500);
                        }

                        System.out.println("Avg:" + getPlayerAvgTsHrs.getResponse());
                        break;

                    case (4):
                        System.out.println("Insert custom message:");
                        String customMsg = sc.nextLine();
                        SendCustomMsgToPlayersThread custom_thread = new SendCustomMsgToPlayersThread(customMsg, false, 0);
                        custom_thread.start();

                        while (!custom_thread.checkIsCompleted())
                        {
                            Thread.sleep(500);
                        }

                        System.out.println("Done!");

                        break;

                    case (5):
                        stopCondition = true;
                        break;
                }
            }
            catch(Exception e)
            {
                System.err.println("In startCLI_menu: " + e.getMessage());
                System.err.println("Retry option!");
            }
        }
    }
}
