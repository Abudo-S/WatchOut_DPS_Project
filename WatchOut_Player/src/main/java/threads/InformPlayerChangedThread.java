/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threads;

import beans.Player;
import io.grpc.*;
import io.grpc.stub.StreamObserver;
import manager.SmartWatch;
import services.PlayerServiceGrpc;
import services.PlayerServiceOuterClass;

public class InformPlayerChangedThread extends Thread
{
    private String remotePlayerEndpoint;
    private String changedPlayerEndPoint;
    private SmartWatch smartWatch;
    private boolean isSeeker;
    
    public InformPlayerChangedThread(String remotePlayerEndpoint, SmartWatch smartWatch, String changedPlayerEndPoint, boolean isSeeker)
    {
        this.remotePlayerEndpoint = remotePlayerEndpoint;
        this.smartWatch = smartWatch;
        this.changedPlayerEndPoint = changedPlayerEndPoint;
        this.isSeeker = isSeeker;
    }
    
    @Override
    public void run()
    {
        try
        {
            Player player = this.smartWatch.getPlayer();
            
            //init grpc service client
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(remotePlayerEndpoint).usePlaintext().build();

            PlayerServiceGrpc.PlayerServiceStub stub = PlayerServiceGrpc.newStub(channel);

            PlayerServiceOuterClass.ChangePositionOrStatusRequest msg = PlayerServiceOuterClass.ChangePositionOrStatusRequest.newBuilder()
                                                                                                .setTargetEndpoint(this.changedPlayerEndPoint)
                                                                                                .setStatus(player.getStatus().name())
                                                                                                .setPositionX(player.getPosition()[0])
                                                                                                .setPositionY(player.getPosition()[1])
                                                                                                .setIsSentBySeeker(isSeeker)
                                                                                                .build();

            StreamObserver streamObserver = stub.changePositionOrStatusStream(getServerResponseObserver());

            //insert player's msg in the stream
            streamObserver.onNext(msg);
        }
        catch(Exception e)
        {
            System.err.println("In run: " + e.getMessage());
            e.printStackTrace();
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
