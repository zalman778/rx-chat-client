package com.hwx.rx_chat_client.rsocket;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hwx.rx_chat.common.object.rx.RxObject;
import com.hwx.rx_chat_client.Configuration;

import org.reactivestreams.Publisher;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;

import io.netty.handler.ssl.SslContext;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.subjects.PublishSubject;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.WebsocketClientTransport;
import io.rsocket.util.DefaultPayload;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;


public class ChatSocket {
    private Mono<RSocket> monoSocket;

    private PublishProcessor<Payload> processor = PublishProcessor.create();
    private PublishSubject<RxObject> psRxMessage = PublishSubject.create();
    private ObjectMapper objectMapper;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    //метод инициализации сокета
    private ChatSocket(SslContext sslContext, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        TcpClient tcpClient = TcpClient.create()
                .host(Configuration.IP)
                .port(Configuration.RSOCKET_PORT)
                .secure(sslContext);

        HttpClient httpClient = HttpClient.from(tcpClient);

        WebsocketClientTransport websocketClientTransport = WebsocketClientTransport.create(httpClient, "/");

        monoSocket = RSocketFactory
                .connect()
                .keepAlive(
                          Duration.ofSeconds(Configuration.RSOCKET_TICK_PERIOD)
                        , Duration.ofSeconds(Configuration.RSOCKET_ACK_PERIOD)
                        , Configuration.RSOCKET_MISSED_ACKS
                )
                .transport(websocketClientTransport)
                .start();
    }

    //версия A: канал открывается один раз при создании даггером объекта сокета.
    //public API:
    //метод отправки в сокет на сервер
    public void putRxObjectA(RxObject rxObject) {
        try {
            processor.onNext(DefaultPayload.create(objectMapper.writeValueAsString(rxObject).getBytes()));
        } catch (JsonProcessingException e) {
            Log.e("AVX", "err", e);
        }
    }

    //метод подписки на события с сервера
    public PublishSubject<RxObject> getPsRxMessageA() {
        return psRxMessage;
    }


    public static ChatSocket openSocketA(SslContext sslContext, ObjectMapper objectMapper) {
        ChatSocket socketA = new ChatSocket(sslContext, objectMapper);
        socketA.startEventChannel();
        return socketA;
    }

    private void startEventChannel() {
        //subdcribing publisher

        compositeDisposable.add(
                getEventFlowable()
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rxObject ->
                                psRxMessage.onNext(rxObject)
                        , err-> {
                            Log.e("AVX", "err", err);
                        }));
    }

    public void closeSocketA() {
        compositeDisposable.dispose();
    }





    private Flowable<RxObject> getEventFlowable() {
        return RxJava2Adapter.monoToFlowable(monoSocket)
                .flatMap(e->requestEventChannel(e));
    }

    private Publisher<RxObject> requestEventChannel(RSocket rSocket) {
        return rSocket
                .requestChannel(Flux.from(processor))
                .map(e-> {
                    try {
                        return objectMapper.readValue(e.getDataUtf8(), RxObject.class);
                    } catch (IOException e1) {
                        Log.e("AVX", "err on mapper 2 "+e1.getLocalizedMessage()+"; "+e1.getMessage(), e1);
                    }
                    return null; //
                });
    }

    //версия B - канал будет открываться каждый раз в новой вью модели.
    public Flowable<RxObject> getEventChannel(PublishProcessor<RxObject> publishProcessor) {
        return RxJava2Adapter
                .monoToFlowable(monoSocket)
                .flatMap(rSocket-> rSocket
                        .requestChannel(
                                Flux.from(publishProcessor)
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
                        .map(e-> {
                            try {
                                return objectMapper.readValue(e.getDataUtf8(), RxObject.class);
                            } catch (IOException e1) {
                                Log.e("AVX", "err on mapper 3 "+e1.getLocalizedMessage()+"; "+e1.getMessage(), e1);
                            }
                            return null;
                        }));
    }

}
