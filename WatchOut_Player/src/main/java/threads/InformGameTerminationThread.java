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
import services.PlayerServiceOuterClass.InformGameTerminationRequest;
import services.PlayerServiceOuterClass.GenericResultResponse;

public class InformGameTerminationThread extends Thread
{
    private String remotePlayerEndpoint;
    private String currentPlayerEndpoint;
    private SmartWatch smartWatch;
    
    public InformGameTerminationThread(String remotePlayerEndpoint, SmartWatch smartWatch, String currentPlayerEndpoint)
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
            //init grpc service client
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(this.remotePlayerEndpoint).usePlaintext().build();

            PlayerServiceBlockingStub stub = PlayerServiceGrpc.newBlockingStub(channel);

            InformGameTerminationRequest request = InformGameTerminationRequest.newBuilder()
                                                .setSenderEndpoint(this.currentPlayerEndpoint)
                                                .build();

            GenericResultResponse response = stub.informGameTermination(request);

            //printing the answer
            System.out.println("InformGameTerminationThread for endpoint: " + this.remotePlayerEndpoint + ", with result: " + response.toString());

            //close the channel
            channel.shutdown();
        }
        catch(Exception e)
        {
            System.err.println("In run: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
