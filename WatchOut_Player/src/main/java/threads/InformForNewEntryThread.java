/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threads;

import beans.Player;
import beans.PlayerStatus;
import io.grpc.*;
import manager.SmartWatch;
import services.PlayerServiceGrpc;
import services.PlayerServiceGrpc.PlayerServiceBlockingStub;
import services.PlayerServiceOuterClass.InformNewPlayerRequest;
import services.PlayerServiceOuterClass.InformNewPlayerResponse;

public class InformForNewEntryThread extends Thread
{
    private String remotePlayerEndpoint;
    private String currentPlayerEndpoint;
    private SmartWatch smartWatch;
    
    public InformForNewEntryThread(String remotePlayerEndpoint, SmartWatch smartWatch, String currentPlayerEndpoint)
    {
        this.remotePlayerEndpoint = remotePlayerEndpoint;
        this.currentPlayerEndpoint = currentPlayerEndpoint;
        this.smartWatch = smartWatch;
    }
    
    @Override
    public void run()
    {
        try
        {
            Player player = this.smartWatch.getPlayer();
            
            //Plaintext channel on the address (ip/port) which offers the GreetingService service
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(this.remotePlayerEndpoint).usePlaintext().build();

            PlayerServiceBlockingStub stub = PlayerServiceGrpc.newBlockingStub(channel);

            InformNewPlayerRequest request = InformNewPlayerRequest.newBuilder()
                                                .setNewPlayerEndpoint(this.currentPlayerEndpoint)
                                                .setPositionX(player.getPosition()[0])
                                                .setPositionY(player.getPosition()[1])
                                                .build();

            InformNewPlayerResponse response = stub.informNewPlayer(request);

            //add new otherPlayer
            player.upsertOtherPlayer(request.getNewPlayerEndpoint(),
                    new Player(0, new int[] {response.getPositionX(), response.getPositionY()}, PlayerStatus.valueOf(response.getStatus()))
            );
            
            smartWatch.updatePlayer(player);
            
            //printing the answer
            //System.out.println(response.toString());

            //closc the channel
            channel.shutdown();
        }
        catch(Exception e)
        {
            System.err.println("In run: " + e.getMessage());
        }
    }
}
