package services;

import io.grpc.ServerBuilder;
import java.io.IOException;
import java.net.ServerSocket;
import manager.PlayerManager;

public class App
{
    public static void main( String[] args )
    {
        PlayerManager playerManager = null;
        
        try 
        {
            //get random available port
            ServerSocket s = new ServerSocket(0);
            int port = s.getLocalPort();
            s.close();
            
            System.out.println("Using grpc port: " + port);
            
            playerManager = PlayerManager.getInstance("localhost:" + port);
            io.grpc.Server server = ServerBuilder.forPort(port).addService(new PlayerGrpcService()).build();
            server.start();
            System.out.println("Server started!");
            server.awaitTermination();
        } 
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
        }
        finally
        {
            playerManager.StopAll();
        }
    }
}

