/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import beans.Player;
import beans.PlayerStatus;
import io.grpc.stub.StreamObserver;
import manager.SmartWatch;

public class PlayerGrpcService extends PlayerServiceGrpc.PlayerServiceImplBase
{

    public void InformNewPlayer(PlayerServiceOuterClass.InformNewPlayerRequest request, StreamObserver<PlayerServiceOuterClass.InformNewPlayerResponse> responseObserver)
    {
        try
        {
            System.out.println("Invoked InformNewPlayer with request: " + request);
        
            SmartWatch smartWatch = SmartWatch.getSubsequentInstance();
            Player player = smartWatch.getPlayer();

            player.AcquireOtherPlayerLock(request.getNewPlayerEndpoint());
            //add new otherPlayer
            player.upsertOtherPlayer(request.getNewPlayerEndpoint(),
                    new Player(0, new int[] {request.getPositionX(), request.getPositionY()}, PlayerStatus.Active)
            );
            player.ReleaseOtherPlayerLock(request.getNewPlayerEndpoint());

            //prepare response
            PlayerServiceOuterClass.InformNewPlayerResponse response = PlayerServiceOuterClass.InformNewPlayerResponse.newBuilder()
                                                                        .setPositionX(player.getPosition()[0])
                                                                        .setPositionY(player.getPosition()[1])
                                                                        .setStatus(player.getStatus().name())
                                                                        .build();

            //send response
            responseObserver.onNext(response);
        }
        catch (Exception e)
        {
            System.err.println("In InformNewPlayer: " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            //indicate termination
            responseObserver.onCompleted();
        }
    }
    
    /**
     * The current player compare its position with the remote player's position, if current-player position is closer to the H.B. then return false;
     * if current-player position is equal and its id is higher then return false;
     * otherwise return true.
     * If this node is a seeker or its associated otherPlayers contains a seeker then return false anyway. (means that phase 0 is terminated)
     * @param request
     * @param responseObserver 
     */
    public void CanIbeSeekerRequest(PlayerServiceOuterClass.CanIbeSeekerRequest request, StreamObserver<PlayerServiceOuterClass.GenericResultResponse> responseObserver){
        try
        {
            System.out.println("Invoked CanIbeSeekerRequest with request: " + request);
        
            Player currentPlayer = SmartWatch.getSubsequentInstance().getPlayer();
            boolean isAgreed;
            
            if(currentPlayer.getStatus().equals(PlayerStatus.Seeker) ||
               currentPlayer.getOtherPlayers().values()
                       .stream()
                       .anyMatch(m -> m.getStatus().equals(PlayerStatus.Seeker)))
            {
                isAgreed = false;
            }
            else
            {
                isAgreed = currentPlayer.compareCloserDistanceToHB(new int[] {request.getPositionX(), request.getPositionY()}, request.getPlayerId());
            }
            
            //prepare response
            PlayerServiceOuterClass.GenericResultResponse response = PlayerServiceOuterClass.GenericResultResponse.newBuilder()
                                                                        .setResult(isAgreed)
                                                                        .build();

            //send response
            responseObserver.onNext(response);
        }
        catch (Exception e)
        {
            System.err.println("In CanIbeSeekerRequest: " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            //indicate termination
            responseObserver.onCompleted();
        }
    }

    /**
     * The current player compare its position with the remote player's position, if current-player position is closer to the H.B. then return false;
     * if current-player position is equal and its id is higher then return false;
     * otherwise return true.
     * If this node is a seeker or its associated otherPlayers contains a seeker then return false anyway. (means that phase 0 is terminated)
     * @param request
     * @param responseObserver 
     */
    public void InformGameTermination(PlayerServiceOuterClass.InformGameTerminationRequest request, StreamObserver<PlayerServiceOuterClass.GenericResultResponse> responseObserver){
        try
        {
            System.out.println("Invoked InformGameTermination with request: " + request);
        
            System.out.println("Game terminated by player's endpoint: " + request.getSenderEndpoint());
            
            //prepare response
            PlayerServiceOuterClass.GenericResultResponse response = PlayerServiceOuterClass.GenericResultResponse.newBuilder()
                                                                        .setResult(true)
                                                                        .build();

            //send response
            responseObserver.onNext(response);
        }
        catch (Exception e)
        {
            System.err.println("In InformGameTermination: " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            //indicate termination
            responseObserver.onCompleted();
        }
    }
    
    public StreamObserver<PlayerServiceOuterClass.ChangePositionOrStatusRequest> ChangePositionOrStatusStream(StreamObserver<PlayerServiceOuterClass.GenericResultResponse> responseObserver){

        //the client will write on this stream
        return new StreamObserver<PlayerServiceOuterClass.ChangePositionOrStatusRequest>() 
        {
            //receiving a message from the client
            @Override
            public void onNext(PlayerServiceOuterClass.ChangePositionOrStatusRequest clientMessage)
            {
                boolean result = false;
                
                try 
                {
                    System.out.println("Invoked CanIbeSeekerRequest with request: " + clientMessage);
                    
                    Player player = SmartWatch.getSubsequentInstance().getPlayer();
                    
                    player.AcquireOtherPlayerLock(clientMessage.getTargetEndpoint());
                    Player otherPlayer = player.getOtherPlayer(clientMessage.getTargetEndpoint());
                    
                    //update otherPlayer data.
                    otherPlayer.setStatus(PlayerStatus.valueOf(clientMessage.getStatus()));
                    otherPlayer.setPosition(new int[] {clientMessage.getPositionX(), clientMessage.getPositionY()});
                    
                    player.upsertOtherPlayer(clientMessage.getTargetEndpoint(), otherPlayer);
                    player.ReleaseOtherPlayerLock(clientMessage.getTargetEndpoint());
                    
                    result = true;
                }
                catch (Exception e) 
                {
                     System.err.println("In ChangePositionOrStatusStream: " + e.getMessage());
                     e.printStackTrace();
                }
                finally
                {
                    responseObserver.onNext(PlayerServiceOuterClass.GenericResultResponse.newBuilder()
                                    .setResult(result)
                                    .build());
                }
            }

            @Override
            public void onError(Throwable throwable) 
            {

            }

            @Override
            public void onCompleted() 
            {

            }
        };
    }

}


