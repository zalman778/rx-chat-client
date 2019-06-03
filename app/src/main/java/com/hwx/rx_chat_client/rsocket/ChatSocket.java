package com.hwx.rx_chat_client.rsocket;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hwx.rx_chat.common.entity.rx.RxMessage;
import com.hwx.rx_chat.common.object.rx.RxObject;
import com.hwx.rx_chat_client.Configuration;

import org.reactivestreams.Publisher;

import java.io.IOException;
import java.time.Duration;

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

    private PublishProcessor<Payload> processor;
    private PublishSubject<RxObject> psRxMessage = PublishSubject.create();
    private ObjectMapper objectMapper;


    public ChatSocket(SslContext sslContext, ObjectMapper objectMapper) {
        processor = PublishProcessor.create();
        this.objectMapper = objectMapper;

        TcpClient tcpClient = TcpClient.create()
                .host(Configuration.IP)
                .port(Configuration.PORT)
                .secure(sslContext);

        HttpClient httpClient = HttpClient.from(tcpClient);

        WebsocketClientTransport websocketClientTransport = WebsocketClientTransport.create(httpClient, "/");

        monoSocket = RSocketFactory
                .connect()
                .keepAlive(
                        Duration.ofSeconds(42)
                        , Duration.ofMinutes(1)
                        , 10
                )
                .transport(websocketClientTransport)
                .start();

        //subdcribing publisher
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(getEventFlowable()
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rxObject ->
                        psRxMessage.onNext(rxObject)
                        , err-> {
                    Log.e("AVX", "err", err);
                }));
    }

    //метод отправки в сокет на сервер
    //should be used...
    public void putRxObject(RxObject rxObject) {
        try {
            processor.onNext(DefaultPayload.create(objectMapper.writeValueAsString(rxObject).getBytes()));
        } catch (JsonProcessingException e) {
            Log.e("AVX", "err", e);
        }
    }


    public PublishProcessor<Payload> getProcessor() {
        return processor;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }



    public PublishSubject<RxObject> getPsRxMessage() {
        return psRxMessage;
    }

    public Flowable<RxObject> getEventFlowable() {
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
                        Log.e("AVX", "err on mapper", e1);
                    }
                    return null; //
                });
    }
}
