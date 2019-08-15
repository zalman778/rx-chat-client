package com.hwx.rx_chat_client.background.p2p.service;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hwx.rx_chat_client.Configuration;
import com.hwx.rx_chat_client.background.p2p.object.PipeHolder;
import com.hwx.rx_chat_client.background.p2p.object.RxP2PObject;
import com.hwx.rx_chat_client.background.p2p.object.type.ObjectType;
import com.hwx.rx_chat_client.rsocket.ReconnectingRSocket;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.netty.handler.ssl.SslContext;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.subjects.PublishSubject;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import reactor.core.publisher.Mono;

/*
    Клиент для п2п сокета. Создается при создании нового п2п чата.
 */
public class RxP2PServiceClient {



    private SslContext sslContext;
    private ObjectMapper objectMapper;

    private Map<String, ReconnectingRSocket> rsocketMap = new HashMap<>();

    private Map<String, PipeHolder> pipesMap;

    public PublishProcessor<RxP2PObject> getTxProcessor(String profileId) {
        return pipesMap.get(profileId).getTxPipe();
    }

    public PublishSubject<RxP2PObject> getRxPublisher(String profileId) {
        return pipesMap.get(profileId).getRxPipe();
    }


    public RxP2PServiceClient(SslContext sslContext, ObjectMapper objectMapper, Map<String, PipeHolder> pipesMap) {
        this.sslContext = sslContext;
        this.objectMapper = objectMapper;
        this.pipesMap = pipesMap;
    }



    public void openConnection(String profileIp, String profileId) {



        Mono<RSocket> monoSocket = RSocketFactory
                .connect()
                .keepAlive(
                        Duration.ofSeconds(Configuration.RSOCKET_TICK_PERIOD)
                        , Duration.ofSeconds(Configuration.RSOCKET_ACK_PERIOD)
                        , Configuration.RSOCKET_MISSED_ACKS
                )
//                .transport(websocketClientTransport)
                .transport( TcpClientTransport.create(profileIp, RxP2PService.DEFAULT_PORT))
                .start();

        ReconnectingRSocket reconnectingRSocket = new ReconnectingRSocket(
                  monoSocket
                , Duration.ofMillis(500)
                , Duration.ofSeconds(1)
        );

        rsocketMap.put(profileId, reconnectingRSocket);

        PipeHolder pipeHolder = new PipeHolder(PublishProcessor.create(), PublishSubject.create());
        pipesMap.put(profileId, pipeHolder);
    }

    public void requestChannel(String profileId) {

        rsocketMap.get(profileId)
               .requestChannel(
                        pipesMap.get(profileId).getTxPipe()
                                .map(rxObject ->
                                        {
                                            try {
                                                return DefaultPayload.create(objectMapper.writeValueAsString(rxObject).getBytes());
                                            } catch (JsonProcessingException e1) {
                                                Log.e("AVX", "err p2p on mapper 2 " + e1.getLocalizedMessage() + "; " + e1.getMessage(), e1);
                                                return null;
                                            }
                                        }
                                )
                                .filter(Objects::nonNull)
                        )
                                .doOnError(e2 -> Log.e("AVX", "err p2p 4 " + e2.getLocalizedMessage() + "; " + e2.getMessage(), e2))
                                //.retryBackoff(Integer.MAX_VALUE, Duration.ofMillis(200), Duration.ofMillis(1000))
                                .subscribe(payload -> {
                                    Log.w("AVX", "got data " + payload.getDataUtf8());
                                    try {
                                        RxP2PObject recievedObject = objectMapper.readValue(payload.getDataUtf8(), RxP2PObject.class);
                                        pipesMap.get(profileId).getRxPipe().onNext(recievedObject);
                                    } catch (IOException e1) {
                                        Log.e("AVX", "err p2p on mapper 3 " + e1.getLocalizedMessage() + "; " + e1.getMessage(), e1);
                                    }
                                }, e -> Log.e("AVX", "err5 p2p", e));


    }
}
