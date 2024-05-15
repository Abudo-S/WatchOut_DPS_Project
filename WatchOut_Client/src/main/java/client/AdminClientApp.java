package client;

import threads.*;
import java.io.IOException;
import java.util.Scanner;
import manager.GameManager;

public class AdminClientApp 
{
    public static void main(String[] argv)
    {
        GameManager.getInstance();
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
                        PersistentGetAllPlayersThread getAllPlayers = GameManager.getInstance().preparePersistentGetAllPlayersThread();
                        getAllPlayers.start();

                        while (!getAllPlayers.checkIsCompleted())
                        {
                            Thread.sleep(500);
                        }

                        System.out.println("Players: " + getAllPlayers.getResponse().toString());
                        
                        break;

                    case (2):
                        System.out.println("Insert player id:");
                        int playerId = sc.nextInt();
                        System.out.println("Insert N:");
                        int n = sc.nextInt();
                        
                        PersistentGetAvgNHrsThread getAvgNHrsPlayers = GameManager.getInstance().preparePersistentGetAvgNHrsThread(playerId, n);
                        getAvgNHrsPlayers.start();

                        while (!getAvgNHrsPlayers.checkIsCompleted())
                        {
                            Thread.sleep(500);
                        }

                        System.out.println("Avg:" + getAvgNHrsPlayers.getResult());
                        break;

                    case (3):
                        System.out.println("Insert lower-bound timestamp:");
                        long ts1 = sc.nextLong();
                        System.out.println("Insert higher-bound timestamp:");
                        long ts2 = sc.nextLong();
                        
                        PersistentGetTotalAvgTsHrsThread getPlayerAvgTsHrs = GameManager.getInstance().preparePersistentGetTotalAvgTsHrsThread(ts1, ts2);
                        getPlayerAvgTsHrs.start();

                        while (!getPlayerAvgTsHrs.checkIsCompleted())
                        {
                            Thread.sleep(500);
                        }

                        System.out.println("Avg:" + getPlayerAvgTsHrs.getResult());
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
                e.printStackTrace();
                
                System.err.println("Retry option!");
                try 
                {
                    System.in.read();
                }               
                catch(IOException ex)
                {
                    System.err.println("In startCLI_menu: " + ex.getMessage());
                }
            }
        }
    }
}
