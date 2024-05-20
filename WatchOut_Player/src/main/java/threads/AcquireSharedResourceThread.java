/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threads;

import beans.SharedResource;
import io.grpc.*;
import manager.CustomLock;
import services.PlayerServiceGrpc;
import services.PlayerServiceGrpc.PlayerServiceBlockingStub;
import services.PlayerServiceOuterClass.AcquireSharedResourceRequest;
import services.PlayerServiceOuterClass.GenericResultResponse;

public class AcquireSharedResourceThread extends Thread
{
    private String remotePlayerEndpoint;
    private String currentPlayerEndpoint;
    private SharedResource sharedResource;
    private int playerId;
    private long timestamp;
    private boolean isAgreed;
    private CustomLock sharedResouceAgreementLock;
    
    public AcquireSharedResourceThread(String remotePlayerEndpoint, String currentPlayerEndpoint, SharedResource sharedResource, int playerId , long timestamp)
    {
        this.remotePlayerEndpoint = remotePlayerEndpoint;
        this.currentPlayerEndpoint = currentPlayerEndpoint;
        this.sharedResource = sharedResource;
        this.playerId = playerId;
        this.timestamp = timestamp;
        this.sharedResouceAgreementLock = new CustomLock();
        this.sharedResouceAgreementLock.Acquire(); //be sure to acquire the playerGamePhaseLock first, so the smartWatch should wait for the value.
    }
    
    @Override
    public void run()
    {
        try
        {
            //init grpc service client
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(this.remotePlayerEndpoint).usePlaintext().build();

            PlayerServiceBlockingStub stub = PlayerServiceGrpc.newBlockingStub(channel);

            AcquireSharedResourceRequest request = AcquireSharedResourceRequest.newBuilder()
                                                                .setSenderEndpoint(this.currentPlayerEndpoint)
                                                                .setSharedResourceName(this.sharedResource.name())
                                                                .setPlayerId(playerId)
                                                                .setTimestamp(timestamp)
                                                                .build();

            GenericResultResponse response = stub.acquireSharedResource(request);
            this.isAgreed = response.getResult();
            this.sharedResouceAgreementLock.Release();
            
            //printing the answer
            System.out.println("AcquireSharedResourceThread for endpoint: " + this.remotePlayerEndpoint + ", with result: " + response.getResult());

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
    public boolean getAgreementResult()
    {
        this.sharedResouceAgreementLock.Acquire();
        //do nothing
        this.sharedResouceAgreementLock.Release();
        
        return this.isAgreed;
    }
    
    public String getRemotePlayerEndpoint()
    {
        return this.remotePlayerEndpoint;
    }
}
