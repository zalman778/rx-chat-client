//package com.hwx.rx_chat_client.background.service;
//
//import android.util.Log;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.hwx.rx_chat.common.object.rx.RxObject;
//import com.hwx.rx_chat_client.Configuration;
//
//import org.reactivestreams.Publisher;
//
//import java.io.IOException;
//import java.time.Duration;
//import java.util.Objects;
//
//import javax.inject.Inject;
//
//import io.netty.channel.ChannelOption;
//import io.netty.handler.ssl.SslContext;
//import io.reactivex.Flowable;
//import io.reactivex.android.schedulers.AndroidSchedulers;
//import io.reactivex.disposables.CompositeDisposable;
//import io.reactivex.subjects.PublishSubject;
//import io.rsocket.RSocket;
//import io.rsocket.RSocketFactory;
//import io.rsocket.transport.netty.client.WebsocketClientTransport;
//import io.rsocket.util.DefaultPayload;
//import reactor.adapter.rxjava.RxJava2Adapter;
//import reactor.core.publisher.DirectProcessor;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import reactor.netty.http.client.HttpClient;
//import reactor.netty.tcp.TcpClient;
//
///*
//    Клиент для п2п сокета. Создается при создании нового п2п чата.
//    Новый чат = коннект с новым клиентом, значит нвоый айпи.
//    Как-то надо хранить
// */
//public class RSocketClient {
//
//    private SslContext sslContext;
//    private ObjectMapper objectMapper;
//    private Mono<RSocket> monoSocket;
//
//    private DirectProcessor<RxObject> processor = DirectProcessor.create();
//    private PublishSubject<RxObject> psRxMessage = PublishSubject.create();
//    private CompositeDisposable compositeDisposable = new CompositeDisposable();
//
//
//    @Inject
//    RSocketClient(RxService service) {
//    }
//
//    public SslContext getSslContext() {
//        return sslContext;
//    }
//
//    public DirectProcessor<RxObject> getTxProcessor() {
//        return processor;
//    }
//
//    public PublishSubject <RxObject> getPsRxMessage() {
//        return psRxMessage;
//    }
//
//    public void setSslContext(SslContext sslContext) {
//        this.sslContext = sslContext;
//    }
//
//    public void setObjectMapper(ObjectMapper objectMapper) {
//        this.objectMapper = objectMapper;
//    }
//
//    public void openChannel() {
//        Log.w("AVX", "openChannel:62"+Thread.currentThread().getName());
//        for (StackTraceElement el : Thread.currentThread().getStackTrace()) {
//            Log.w("AVX", "got calling openChannel at:"+el.toString());
//        }
//        TcpClient tcpClient = TcpClient.create()
//                .host(Configuration.IP)
//                .port(Configuration.RSOCKET_PORT)
//                .option(ChannelOption.SO_REUSEADDR, true)
//                .secure(sslContext);
//
//        HttpClient httpClient = HttpClient.from(tcpClient);
//
//        WebsocketClientTransport websocketClientTransport = WebsocketClientTransport.create(httpClient, "/");
//
//        monoSocket = RSocketFactory
//                .connect()
//                .keepAlive(
//                        Duration.ofSeconds(Configuration.RSOCKET_TICK_PERIOD)
//                        , Duration.ofSeconds(Configuration.RSOCKET_ACK_PERIOD)
//                        , Configuration.RSOCKET_MISSED_ACKS
//                )
//                .transport(websocketClientTransport)
//                .start();
//
//        compositeDisposable.add(
//            getEventFlowable()
//                .subscribeOn(AndroidSchedulers.mainThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(rxObject ->
//                                psRxMessage.onNext(rxObject)
//                        , err-> {
//                            Log.e("AVX", "err", err);
//                        }));
//    }
//
//    public void closeChannel(){
//        compositeDisposable.dispose();
//    }
//
//    private Flowable<RxObject> getEventFlowable() {
//        return RxJava2Adapter.monoToFlowable(monoSocket)
//                .flatMap(e -> requestEventChannel(e));
//    }
//
//    private Publisher<? extends RxObject> requestEventChannel(RSocket rSocket) {
//        return
//            rSocket
//            .requestChannel(
//                //Flux.from(processor)
//                Flux.defer(()->processor)
//                        .log()
//                      //  .onBackpressureDrop()  //hiding err
//                   .map(rxObject->
//                {
//                    try {
//                        return DefaultPayload.create(objectMapper.writeValueAsString(rxObject).getBytes());
//                    } catch (JsonProcessingException e1) {
//                        Log.e("AVX", "err on mapper 2 "+e1.getLocalizedMessage()+"; "+e1.getMessage(), e1);
//                        return null;
//                    }
//                }
//                )
//                .filter(Objects::nonNull)
//            )
//            .doOnError(e2 -> Log.e("AVX", "err 4 "+e2.getLocalizedMessage()+"; "+e2.getMessage(), e2))
//            .retryBackoff(Integer.MAX_VALUE, Duration.ofMillis(200), Duration.ofMillis(1000))
//            .map(e-> {
//                try {
//                    return objectMapper.readValue(e.getDataUtf8(), RxObject.class);
//                } catch (IOException e1) {
//                    Log.e("AVX", "err on mapper 3 "+e1.getLocalizedMessage()+"; "+e1.getMessage(), e1);
//                }
//                return null;
//            });
//
//    }
//
//}
