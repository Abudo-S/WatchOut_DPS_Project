///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package services;
//
//import io.grpc.stub.StreamObserver;
//
//public class PlayerGrpcService extends PlayerGrpcService.PlayerGrpcServiceBase
//{
//
//    @Override
//    public void simpleSum(SumServiceOuterClass.SimpleSumRequest request, StreamObserver<SumServiceOuterClass.SumServiceResponse> responseObserver){
//        System.out.println(request);
//
//        SumServiceOuterClass.SumServiceResponse response = SumServiceOuterClass.SumServiceResponse.newBuilder()
//                                                                .setRes(request.getA() + request.getB())
//                                                                .build();
//
//        responseObserver.onNext(response);
//        responseObserver.onCompleted();
//    }
//
//    @Override
//    public void repeatedSum(SumServiceOuterClass.RepeatedSumRequest request, StreamObserver<SumServiceOuterClass.SumServiceResponse> responseObserver){
//        System.out.println(request);
//
//        for(int i = 1; i<request.getT()+1; i++) {
//            SumServiceOuterClass.SumServiceResponse response = SumServiceOuterClass.SumServiceResponse.newBuilder()
//                    .setRes(request.getN() * i)
//                    .build();
//
//            responseObserver.onNext(response);
//        }
//
//        responseObserver.onCompleted();
//    }
//
//    @Override
//    public StreamObserver<SumServiceOuterClass.SimpleSumRequest> streamSum(final StreamObserver<SumServiceOuterClass.SumServiceResponse> responseObserver){
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
//
//}
//
//
