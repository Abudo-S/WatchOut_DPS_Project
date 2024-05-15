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
    private SmartWatch smartWatch;
    private String currentPlayerEndpoint;
    
    public InformForNewEntryThread(String remotePlayerEndpoint, SmartWatch smartWatch, String currentPlayerEndpoint)
    {
        this.remotePlayerEndpoint = remotePlayerEndpoint;
        this.smartWatch = smartWatch;
        this.currentPlayerEndpoint = currentPlayerEndpoint;
    }
    
    @Override
    public void run()
    {
        try
        {            
            Player player = this.smartWatch.getPlayer();
            
            //init grpc service client
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(this.remotePlayerEndpoint).usePlaintext().build();

            PlayerServiceBlockingStub stub = PlayerServiceGrpc.newBlockingStub(channel);

            InformNewPlayerRequest request = InformNewPlayerRequest.newBuilder()
                                                .setNewPlayerEndpoint(this.currentPlayerEndpoint)
                                                .setPositionX(player.getPosition()[0])
                                                .setPositionY(player.getPosition()[1])
                                                .build();

            InformNewPlayerResponse response = stub.informNewPlayer(request);

            player.AcquireOtherPlayerLock(this.remotePlayerEndpoint);
            //get otherPlayer
            Player otherPlayer = player.getOtherPlayer(this.remotePlayerEndpoint);
            otherPlayer.setPosition(new int[] {response.getPositionX(), response.getPositionY()});
            otherPlayer.setStatus(PlayerStatus.valueOf(response.getStatus()));
            
            //update otherPlayer data
            player.upsertOtherPlayer(this.remotePlayerEndpoint, otherPlayer);
            player.ReleaseOtherPlayerLock(this.remotePlayerEndpoint);
            
            //printing the answer
            //System.out.println(response.toString());

            //close the channel
            channel.shutdown();
        }
        catch(Exception e)
        {
            System.err.println("In run with remotePlayerEndpoint: " + this.remotePlayerEndpoint + ", msg: " +  e.getMessage());
            e.printStackTrace();
        }
    }
}
