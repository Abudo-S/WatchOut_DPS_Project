/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threads;

import beans.Player;
import io.grpc.*;
import manager.SmartWatch;
import services.PlayerServiceGrpc;
import services.PlayerServiceGrpc.PlayerServiceBlockingStub;
import services.PlayerServiceOuterClass.CanIbeSeekerRequest;
import services.PlayerServiceOuterClass.GenericResultResponse;

public class AskToBeSeekerThread extends Thread
{
    private String remotePlayerEndpoint;
    private SmartWatch smartWatch;
    private volatile boolean isCompletedSuccessfully = false;
    private volatile boolean agreementResult = false;
    
    public AskToBeSeekerThread(String remotePlayerEndpoint, SmartWatch smartWatch)
    {
        this.remotePlayerEndpoint = remotePlayerEndpoint;
        this.smartWatch = smartWatch;
    }
    
    @Override
    public void run()
    {
        try
        {
            System.out.println("Invoked informPositionAndStatus with remotePlayerEndpoint: " + remotePlayerEndpoint);
            
            Player player = this.smartWatch.getPlayer();
            
            //init grpc service client
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(this.remotePlayerEndpoint).usePlaintext().build();

            PlayerServiceBlockingStub stub = PlayerServiceGrpc.newBlockingStub(channel);

            CanIbeSeekerRequest request = CanIbeSeekerRequest.newBuilder()
                                            .setPlayerId(player.getId())
                                            .setPositionX(player.getPosition()[0])
                                            .setPositionY(player.getPosition()[1])
                                            .build();

            GenericResultResponse response = stub.canIbeSeeker(request);

            this.agreementResult = response.getResult();
            
            //printing the answer
            //System.out.println(response.toString());

            //close the channel
            channel.shutdown();
        }
        catch(Exception e)
        {
            System.err.println("In run: " + e.getMessage());
            e.printStackTrace();
        }
        
        this.isCompletedSuccessfully = true;
    }
    
    public boolean getAgreementResult()
    {
        return this.agreementResult;
    }
    
    public boolean checkIsCompleted() 
    {
        return this.isCompletedSuccessfully;
    }
}
