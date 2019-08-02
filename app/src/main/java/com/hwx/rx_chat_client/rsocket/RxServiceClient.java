package com.hwx.rx_chat_client.rsocket;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hwx.rx_chat.common.object.rx.RxObject;
import com.hwx.rx_chat_client.Configuration;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.subjects.PublishSubject;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.WebsocketClientTransport;
import io.rsocket.util.DefaultPayload;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

/*
    Клиент rxService
 */
public class RxServiceClient {
    private SslContext sslContext;
    private ObjectMapper objectMapper;

    private ReconnectingRSocket reconnectingRSocket;

    private PublishProcessor<RxObject> txProcessor = PublishProcessor.create();

    private PublishSubject<RxObject> psRxMessage = PublishSubject.create(); //принимает rxObj


    public RxServiceClient(SslContext sslContext, ObjectMapper objectMapper) {
        this.sslContext = sslContext;
        this.objectMapper = objectMapper;
    }

    public PublishProcessor<RxObject> getTxProcessor() {
        return txProcessor;
    }

    public PublishSubject<RxObject> getPsRxMessage() {
        return psRxMessage;
    }

    public void connect() {

        TcpClient tcpClient =
                TcpClient.create()
                .host(Configuration.IP)
                .port(Configuration.RSOCKET_PORT)
                .option(ChannelOption.SO_REUSEADDR, true)
                .secure(sslContext);

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


        reconnectingRSocket = new ReconnectingRSocket(
                  monoSocket
                , Duration.ofMillis(500)
                , Duration.ofSeconds(1)
        );

    }

    public void requestChannel() {

        reconnectingRSocket.requestChannel(
                txProcessor
                        .map(rxObject->
                                {
                                    try {
                                        return DefaultPayload.create(objectMapper.writeValueAsString(rxObject).getBytes());
                                    } catch (JsonProcessingException e1) {
                                        Log.e("AVX", "err on mapper 2 "+e1.getLocalizedMessage()+"; "+e1.getMessage(), e1);
                                        return null;
                                    }
                                }
                        )
                        .filter(Objects::nonNull)
            )
            .doOnError(e2 -> Log.e("AVX", "err 4 "+e2.getLocalizedMessage()+"; "+e2.getMessage(), e2))
            //.retryBackoff(Integer.MAX_VALUE, Duration.ofMillis(200), Duration.ofMillis(1000))
            .subscribe(payload -> {
                Log.w("AVX", "got data "+payload.getDataUtf8());
                try {
                    psRxMessage.onNext(objectMapper.readValue(payload.getDataUtf8(), RxObject.class));
                } catch (IOException e1) {
                    Log.e("AVX", "err on mapper 3 "+e1.getLocalizedMessage()+"; "+e1.getMessage(), e1);
                }
            }, e->Log.e("AVX", "err5", e));
    }
}
