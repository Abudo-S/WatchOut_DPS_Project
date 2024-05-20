/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import beans.Player;
import beans.PlayerStatus;
import beans.SharedResource;
import io.grpc.stub.StreamObserver;
import manager.SmartWatch;

public class PlayerGrpcService extends PlayerServiceGrpc.PlayerServiceImplBase
{

    @Override
    public void informNewPlayer(PlayerServiceOuterClass.InformNewPlayerRequest request, StreamObserver<PlayerServiceOuterClass.InformNewPlayerResponse> responseObserver)
    {
        try
        {
            System.out.println("Invoked informNewPlayer with request: " + request);
        
            SmartWatch smartWatch = SmartWatch.getSubsequentInstance();
            Player player = smartWatch.getPlayer();

            smartWatch.AcquirePlayerLock();
            //add new otherPlayer
            player.upsertOtherPlayer(request.getNewPlayerEndpoint(),
                    new Player(0, new int[] {request.getPositionX(), request.getPositionY()}, PlayerStatus.Active)
            );
            smartWatch.ReleasePlayerLock();

            //prepare response
            PlayerServiceOuterClass.InformNewPlayerResponse response = PlayerServiceOuterClass.InformNewPlayerResponse.newBuilder()
                                                                        .setPositionX(player.getPosition()[0])
                                                                        .setPositionY(player.getPosition()[1])
                                                                        .setStatus(player.getStatus().name())
                                                                        .setCurrentGamePhase(smartWatch.getCurrentGamePhase().name())
                                                                        .build();

            //send response
            responseObserver.onNext(response);
        }
        catch (Exception e)
        {
            System.err.println("In informNewPlayer: " + e.getMessage());
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
    @Override
    public void canIbeSeeker(PlayerServiceOuterClass.CanIbeSeekerRequest request, StreamObserver<PlayerServiceOuterClass.GenericResultResponse> responseObserver)
    {
        try
        {
            System.out.println("Invoked canIbeSeeker with request: " + request);
        
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
            
            if(isAgreed)
                SmartWatch.getSubsequentInstance().setIsCanBeSeeker(false);
            
            //prepare response
            PlayerServiceOuterClass.GenericResultResponse response = PlayerServiceOuterClass.GenericResultResponse.newBuilder()
                                                                        .setResult(isAgreed)
                                                                        .build();

            //send response
            responseObserver.onNext(response);
        }
        catch (Exception e)
        {
            System.err.println("In canIbeSeeker: " + e.getMessage());
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
    @Override
    public void informGameTermination(PlayerServiceOuterClass.InformGameTerminationRequest request, StreamObserver<PlayerServiceOuterClass.GenericResultResponse> responseObserver)
    {
        try
        {
            System.out.println("Invoked informGameTermination with request: " + request);
        
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
            System.err.println("In informGameTermination: " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            //indicate termination
            responseObserver.onCompleted();
        }
    }
    
    /**
     * invoked when otherPlayer wants to acquire a shared resource so he need the acceptance from all players. 
     * @param request
     * @param responseObserver 
     */
    @Override
    public void acquireSharedResource(PlayerServiceOuterClass.AcquireSharedResourceRequest request, StreamObserver<PlayerServiceOuterClass.GenericResultResponse> responseObserver)
    {
        try
        {
            System.out.println("Invoked acquireSharedResource with request: " + request);
        
            SmartWatch smartWatch = SmartWatch.getSubsequentInstance();
            
            smartWatch.AcquirePlayerLock();
            Player player = smartWatch.getPlayer();
            smartWatch.ReleasePlayerLock();
            
            //if otherPlayerEndpoint belongs to a non-active player, then return false anyway.
            player.AcquireOtherPlayerLock(request.getSenderEndpoint());
            boolean isActivePlayer = player.getOtherPlayer(request.getSenderEndpoint()).getStatus().equals(PlayerStatus.Active);
            player.ReleaseOtherPlayerLock(request.getSenderEndpoint());
            
            boolean isOk = false;
            if(isActivePlayer)
            {
                smartWatch.AcquireSharedResourcesLock();
                isOk = smartWatch.checkSharedResourceAvailability(SharedResource.valueOf(request.getSharedResourceName()),
                                                                                         request.getPlayerId(),
                                                                                         request.getTimestamp(),
                                                                                         request.getSenderEndpoint());
                smartWatch.ReleaseSharedResourcesLock();
            }
            
            
            //prepare response
            PlayerServiceOuterClass.GenericResultResponse response = PlayerServiceOuterClass.GenericResultResponse.newBuilder()
                                                                        .setResult(isActivePlayer && isOk)
                                                                        .build();

            //send response
            responseObserver.onNext(response);
        }
        catch (Exception e)
        {
            System.err.println("In acquireSharedResource: " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            //indicate termination
            responseObserver.onCompleted();
        }
    }
    
    /**
     * 
     * @param request
     * @param responseObserver 
     */
    @Override
    public void informReleasedSharedResource(PlayerServiceOuterClass.InformReleasedSharedResourceRequest request, StreamObserver<PlayerServiceOuterClass.GenericResultResponse> responseObserver)
    {
        try
        {
            System.out.println("Invoked informReleasedSharedResource with request: " + request);
        
            SmartWatch smartWatch = SmartWatch.getSubsequentInstance();
            smartWatch.AcquireSharedResourcesLock();
            smartWatch.addSharedResourceAgreement(SharedResource.valueOf(request.getSharedResourceName()),
                                                                         request.getSenderEndpoint());
            smartWatch.ReleaseSharedResourcesLock();
            
            //prepare response
            PlayerServiceOuterClass.GenericResultResponse response = PlayerServiceOuterClass.GenericResultResponse.newBuilder()
                                                                        .setResult(true)
                                                                        .build();

            //send response
            responseObserver.onNext(response);
        }
        catch (Exception e)
        {
            System.err.println("In acquireSharedResource: " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            //indicate termination
            responseObserver.onCompleted();
        }
    }
    
    @Override
    public StreamObserver<PlayerServiceOuterClass.ChangePositionOrStatusRequest> changePositionOrStatusStream(StreamObserver<PlayerServiceOuterClass.GenericResultResponse> responseObserver)
    {

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
                    System.out.println("Invoked changePositionOrStatusStream with request: " + clientMessage);
                    
                    SmartWatch smartWatch = SmartWatch.getSubsequentInstance();
                    Player player = smartWatch.getPlayer();
                    
                    if(clientMessage.getTargetEndpoint().equals(smartWatch.getGrpcEndpoint()))//local player
                    {
                        smartWatch.AcquirePlayerLock();
                        smartWatch.getPlayer().setStatus(PlayerStatus.valueOf(clientMessage.getStatus()));
                        smartWatch.ReleasePlayerLock();
                    }
                    else //otherPlayer
                    {   
                        player.AcquireOtherPlayerLock(clientMessage.getTargetEndpoint());
                        Player otherPlayer = player.getOtherPlayer(clientMessage.getTargetEndpoint());

                        //update otherPlayer data.
                        otherPlayer.setStatus(PlayerStatus.valueOf(clientMessage.getStatus()));
                        otherPlayer.setPosition(new int[] {clientMessage.getPositionX(), clientMessage.getPositionY()});

                        player.upsertOtherPlayer(clientMessage.getTargetEndpoint(), otherPlayer);
                        player.ReleaseOtherPlayerLock(clientMessage.getTargetEndpoint());
                    }
 
                    result = true;
                }
                catch (Exception e) 
                {
                     System.err.println("In changePositionOrStatusStream: " + e.getMessage());
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


