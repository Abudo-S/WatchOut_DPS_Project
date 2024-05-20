/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threads;

import beans.GamePhase;
import beans.Player;
import io.grpc.*;
import manager.CustomLock;
import manager.SmartWatch;
import services.PlayerServiceGrpc;
import services.PlayerServiceGrpc.PlayerServiceBlockingStub;
import services.PlayerServiceOuterClass.CanIbeSeekerRequest;
import services.PlayerServiceOuterClass.GenericResultResponse;

public class AskToBeSeekerThread extends Thread
{
    private String remotePlayerEndpoint;
    private volatile boolean isCompletedSuccessfully = false;
    private volatile boolean agreementResult = false;
    private CustomLock seekerAgreementLock;
    
    public AskToBeSeekerThread(String remotePlayerEndpoint)
    {
        this.remotePlayerEndpoint = remotePlayerEndpoint;
        this.seekerAgreementLock = new CustomLock();
        this.seekerAgreementLock.Acquire(); //be sure to acquire the seekerAgreementPhaseLock first, so the smartWatch should wait for the value.
    }
    
    @Override
    public void run()
    {
        try
        {
            System.out.println("Invoked AskToBeSeekerThread with remotePlayerEndpoint: " + remotePlayerEndpoint);
            
            Player player = SmartWatch.getSubsequentInstance().getPlayer();
            
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
            this.seekerAgreementLock.Release();
            
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
        
        this.isCompletedSuccessfully = true;
    }
    
    public boolean checkIsCompleted() 
    {
        return this.isCompletedSuccessfully;
    }
    
    /**
     * use customLock to get notified when the value is ready.
     * @return 
     */
    public boolean getAgreementResult()
    {
        this.seekerAgreementLock.Acquire();
        //do nothing
        this.seekerAgreementLock.Release();
        
        return this.agreementResult;
    }
}
