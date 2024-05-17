/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threads;

import beans.GamePhase;
import beans.Player;
import beans.PlayerStatus;
import io.grpc.*;
import manager.CustomLock;
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
    private volatile GamePhase playerGamePhase = null;
    private CustomLock playerGamePhaseLock;
    
    public InformForNewEntryThread(String remotePlayerEndpoint, SmartWatch smartWatch, String currentPlayerEndpoint)
    {
        this.remotePlayerEndpoint = remotePlayerEndpoint;
        this.smartWatch = smartWatch;
        this.currentPlayerEndpoint = currentPlayerEndpoint;
        this.playerGamePhaseLock = new CustomLock();
        this.playerGamePhaseLock.Acquire(); //be sure to acquire the playerGamePhaseLock first, so the smartWatch should wait for the value.
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

            this.playerGamePhase = GamePhase.valueOf(response.getCurrentGamePhase());
            this.playerGamePhaseLock.Release();
            
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
    
    /**
     * use customLock to get notified when the value is ready.
     * @return 
     */
    public GamePhase getplayerGamePhase()
    {
        this.playerGamePhaseLock.Acquire();
        //do nothing
        this.playerGamePhaseLock.Release();
        
        return this.playerGamePhase;
    }
}
