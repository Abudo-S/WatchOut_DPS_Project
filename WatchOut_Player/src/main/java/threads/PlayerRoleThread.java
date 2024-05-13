/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threads;

import beans.Player;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import manager.SmartWatch;
import services.PlayerServiceGrpc;
import services.PlayerServiceOuterClass;

public abstract class PlayerRoleThread extends Thread
{
    private String remotePlayerEndpoint;
    private SmartWatch smartWatch;
    private String playerEndPoint;
    
    public PlayerRoleThread(String remotePlayerEndpoint, SmartWatch smartWatch, String playerEndPoint)
    {
        this.remotePlayerEndpoint = remotePlayerEndpoint;
        this.smartWatch = smartWatch;
        this.playerEndPoint = playerEndPoint;
    }
    
    @Override
    public abstract void run();
    
    public void informPositionAndStatus()
    {
        try
        {
            System.out.println("Invoked informPositionAndStatus with remotePlayerEndpoint: " + remotePlayerEndpoint);
            
            Player player = this.smartWatch.getPlayer();
            
            //init grpc service client
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(this.remotePlayerEndpoint).usePlaintext().build();

            PlayerServiceGrpc.PlayerServiceStub stub = PlayerServiceGrpc.newStub(channel);

            PlayerServiceOuterClass.ChangePositionOrStatusRequest msg = PlayerServiceOuterClass.ChangePositionOrStatusRequest.newBuilder()
                                                                                                .setSenderEndpoint(this.playerEndPoint)
                                                                                                .setStatus(player.getStatus().name())
                                                                                                .setPositionX(player.getPosition()[0])
                                                                                                .setPositionY(player.getPosition()[1])
                                                                                                .build();

            StreamObserver streamObserver = stub.changePositionOrStatusStream(getServerResponseObserver());

            //insert player's msg in the stream
            streamObserver.onNext(msg);
        }
        catch(Exception e)
        {
            System.err.println("In informPositionAndStatus: " + e.getMessage());
        }
    }
    
    public StreamObserver<PlayerServiceOuterClass.GenericResultResponse> getServerResponseObserver(){
      StreamObserver<PlayerServiceOuterClass.GenericResultResponse> observer = new StreamObserver<PlayerServiceOuterClass.GenericResultResponse>()
      {
          @Override
          public void onNext(PlayerServiceOuterClass.GenericResultResponse v) 
          {
              System.out.println("Invoked getServerResponseObserver.onNext with result: " + v.getResult());
          }

          @Override
          public void onError(Throwable thrwbl) 
          {
              System.out.println("Invoked getServerResponseObserver.onError with: " + thrwbl.getMessage());
          }

          @Override
          public void onCompleted() 
          {
              System.out.println("Invoked getServerResponseObserver.onCompleted!");
          }
      };
      return observer;
   }
}
