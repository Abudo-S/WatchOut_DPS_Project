/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threads;

import beans.Player;
import io.grpc.*;
import io.grpc.stub.StreamObserver;
import java.util.HashMap;
import services.PlayerServiceGrpc;
import services.PlayerServiceOuterClass;

public class InformPlayerChangedThread extends Thread
{
    /**
     * holds stream channels for all threads
     */
    private static HashMap<String, ManagedChannel> RemoteStreamChannels = new HashMap();
    
    private String remotePlayerEndpoint;
    private String changedPlayerEndPoint;
    private Player changedPlayer;
    private boolean isSentBySeeker;
    
    public InformPlayerChangedThread(String remotePlayerEndpoint, String changedPlayerEndPoint, Player changedPlayer, boolean isSentBySeeker)
    {
        this.remotePlayerEndpoint = remotePlayerEndpoint;
        this.changedPlayerEndPoint = changedPlayerEndPoint;
        this.changedPlayer = changedPlayer;
        this.isSentBySeeker = isSentBySeeker;
        
        //init grpc service client if not present
        if(!RemoteStreamChannels.containsKey(this.remotePlayerEndpoint))
        {
            ManagedChannel channel = ManagedChannelBuilder.forTarget(this.remotePlayerEndpoint).usePlaintext().build();
            RemoteStreamChannels.put(this.remotePlayerEndpoint, channel);
        }
    }
    
    @Override
    public void run()
    {
        try
        {   
            //get remote player's relative stream channal
            final ManagedChannel channel = RemoteStreamChannels.get(this.remotePlayerEndpoint);
            
            PlayerServiceGrpc.PlayerServiceStub stub = PlayerServiceGrpc.newStub(channel);

            PlayerServiceOuterClass.ChangePositionOrStatusRequest msg = PlayerServiceOuterClass.ChangePositionOrStatusRequest.newBuilder()
                                                                                                .setTargetEndpoint(this.changedPlayerEndPoint)
                                                                                                .setStatus(changedPlayer.getStatus().name())
                                                                                                .setPositionX(changedPlayer.getPosition()[0])
                                                                                                .setPositionY(changedPlayer.getPosition()[1])
                                                                                                .setIsSentBySeeker(isSentBySeeker)
                                                                                                .build();

            StreamObserver streamObserver = stub.changePositionOrStatusStream(getServerResponseObserver());

            //insert player's msg in the stream
            streamObserver.onNext(msg);

        }
        catch(Exception e)
        {
            System.err.println("In run with remotePlayerEndpoint: " + this.remotePlayerEndpoint + ", msg: " +  e.getMessage());
            e.printStackTrace();
        }
    }
    
    public StreamObserver<PlayerServiceOuterClass.GenericResultResponse> getServerResponseObserver(){
      StreamObserver<PlayerServiceOuterClass.GenericResultResponse> observer = new StreamObserver<PlayerServiceOuterClass.GenericResultResponse>()
      {
          @Override
          public void onNext(PlayerServiceOuterClass.GenericResultResponse v) 
          {
              //System.out.println("Invoked InformPlayerChangedThread.getServerResponseObserver.onNext with result: " + v.getResult());
          }

          @Override
          public void onError(Throwable thrwbl) 
          {
              //System.out.println("Invoked InformPlayerChangedThread.getServerResponseObserver.onError with: " + thrwbl.getMessage());
          }

          @Override
          public void onCompleted() 
          {
              //System.out.println("Invoked InformPlayerChangedThread.getServerResponseObserver.onCompleted!");
          }
      };
      return observer;
   }
}
