package com.hwx.rx_chat_client.p2p.service;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hwx.rx_chat_client.Configuration;
import com.hwx.rx_chat_client.p2p.P2PRxObject;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.PublishProcessor;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.WebsocketClientTransport;
import io.rsocket.util.DefaultPayload;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

/*
    Клиент для п2п сокета. Создается при создании нового п2п чата.
    Новый чат = коннект с новым клиентом, значит нвоый айпи.
    Как-то надо хранить
 */
public class RSocketP2PClient {

    @Inject
    RSocketP2PClient(RxP2PService service) {
        /*constructor stuff*/
    }

    private Map<String, Mono<RSocket>> monoMap = new HashMap<>();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public Flowable<P2PRxObject> openChannel(
              String profileIp
            , Integer profilePort
            , PublishProcessor<P2PRxObject> processor
          ) {
        TcpClient tcpClient = TcpClient.create()
                .host(profileIp)
                .port(profilePort);

        HttpClient httpClient = HttpClient.from(tcpClient);

        WebsocketClientTransport websocketClientTransport = WebsocketClientTransport.create(httpClient, "/");

        Mono<RSocket> monoSocket = RSocketFactory
                .connect()
                .keepAlive(
                          Duration.ofSeconds(Configuration.RSOCKET_TICK_PERIOD)
                        , Duration.ofSeconds(Configuration.RSOCKET_ACK_PERIOD)
                        , Configuration.RSOCKET_MISSED_ACKS
                )
                .transport(websocketClientTransport)
                .start();

        //monoMap.put(profileId, monoSocket);

        return RxJava2Adapter
                .monoToFlowable(monoSocket)
                .flatMap(rSocket-> rSocket
                        .requestChannel(
                                Flux.from(processor)
                                        .map(rxObject->
                                                {
                                                    try {
                                                        return DefaultPayload.create(new ObjectMapper().writeValueAsString(rxObject).getBytes());
                                                    } catch (JsonProcessingException e1) {
                                                        Log.e("AVX", "err on mapper 2 "+e1.getLocalizedMessage()+"; "+e1.getMessage(), e1);
                                                        return null;
                                                    }
                                                }
                                        )
                                        .filter(Objects::nonNull)
                        )
                        .map(e-> {
                            try {
                                //TODO fix new ObjectMapper to dependency!
                                return new ObjectMapper().readValue(e.getDataUtf8(), P2PRxObject.class);
                            } catch (IOException e1) {
                                Log.e("AVX", "err on mapper 3 "+e1.getLocalizedMessage()+"; "+e1.getMessage(), e1);
                            }
                            return null;
                        }));

    }

}
