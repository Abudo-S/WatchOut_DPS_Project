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
            
            //add new otherPlayer
            player.upsertOtherPlayer(request.getNewPlayerEndpoint(),
                    new Player(0, new int[] {request.getPositionX(), request.getPositionY()}, PlayerStatus.Active)
            );
            
            smartWatch.updatePlayer(player);

            //prepare response
            PlayerServiceOuterClass.InformNewPlayerResponse response = PlayerServiceOuterClass.InformNewPlayerResponse.newBuilder()
                                                                        .setPositionX(player.getPosition()[0])
                                                                        .setPositionY(player.getPosition()[1])
                                                                        .setStatus(player.getStatus().name())
                                                                        .build();

            //send messages
            responseObserver.onNext(response);
        }
        catch (Exception e)
        {
            System.err.println("In InformNewPlayer: " + e.getMessage());
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

            //send messages
            responseObserver.onNext(response);
        }
        catch (Exception e)
        {
            System.err.println("In InformNewPlayer: " + e.getMessage());
        }
        finally
        {
            //indicate termination
            responseObserver.onCompleted();
        }
    }

//    public StreamObserver<PlayerServiceOuterClass.ChangePositionOrStatusRequest> ChangePositionOrStatusStream(StreamObserver<PlayerServiceOuterClass.GenericResultResponse> responseObserver){
//        //it returns the stream that will be used by the clients to send messages.
//        //the client will write on this stream
//        return new StreamObserver<SumServiceOuterClass.SimpleSumRequest>() {
//            //receiving a message from the client
//            public void onNext(SumServiceOuterClass.SimpleSumRequest clientMessage) {
//                System.out.println(clientMessage);
//
//                responseObserver.onNext(SumServiceOuterClass.SumServiceResponse.newBuilder()
//                        .setRes(clientMessage.getA() + clientMessage.getB())
//                        .build());
//            }
//
//            //if there is an error (client abruptly disconnect) we remove the client.
//            public void onError(Throwable throwable) {
//
//            }
//
//            //if the client explicitly terminated, we remove it from the hashset.
//            public void onCompleted() {
//
//            }
//        };
//    }

}


