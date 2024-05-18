/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threads;

import beans.SharedResource;
import io.grpc.*;
import services.PlayerServiceGrpc;
import services.PlayerServiceGrpc.PlayerServiceBlockingStub;
import services.PlayerServiceOuterClass.InformReleasedSharedResourceRequest;
import services.PlayerServiceOuterClass.GenericResultResponse;

public class InformReleasedSharedResourceThread extends Thread
{
    private String remotePlayerEndpoint;
    private String currentPlayerEndpoint;
    private SharedResource sharedResource;
    
    public InformReleasedSharedResourceThread(String remotePlayerEndpoint, String currentPlayerEndpoint, SharedResource sharedResource)
    {
        this.remotePlayerEndpoint = remotePlayerEndpoint;
        this.currentPlayerEndpoint = currentPlayerEndpoint;
        this.sharedResource = sharedResource;
    }
    
    @Override
    public void run()
    {
        try
        {
            //init grpc service client
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(this.remotePlayerEndpoint).usePlaintext().build();

            PlayerServiceBlockingStub stub = PlayerServiceGrpc.newBlockingStub(channel);

            InformReleasedSharedResourceRequest request = InformReleasedSharedResourceRequest.newBuilder()
                                                                .setSenderEndpoint(this.currentPlayerEndpoint)
                                                                .setSharedResourceName(this.sharedResource.name())
                                                                .build();

            GenericResultResponse response = stub.informReleasedSharedResource(request);

            //printing the answer
            System.out.println("InformReleasedSharedResourceThread for endpoint: " + this.remotePlayerEndpoint + ", with result: " + response.getResult());

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
