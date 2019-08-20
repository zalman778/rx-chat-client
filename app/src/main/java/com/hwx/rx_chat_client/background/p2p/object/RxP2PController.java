package com.hwx.rx_chat_client.background.p2p.object;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hwx.rx_chat_client.background.p2p.object.type.ObjectType;
import com.hwx.rx_chat_client.util.SharedPreferencesProvider;

import org.reactivestreams.Publisher;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import io.netty.channel.socket.nio.NioSocketChannel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.rsocket.AbstractRSocket;
import io.rsocket.ConnectionSetupPayload;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.server.CloseableChannel;
import io.rsocket.transport.netty.server.TcpServerTransport;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.hwx.rx_chat_client.background.p2p.service.RxP2PService.DEFAULT_PORT;

public class RxP2PController {

    private ObjectMapper objectMapper;
//    private PublishSubject<RxP2PObject> rxObj;
    private SharedPreferencesProvider sharedPreferencesProvider;

    private Map<String, PipeHolder> pipesMap;

    public RxP2PController(
            ObjectMapper objectMapper
            , PublishSubject<RxP2PObject> rxObj
            , String profileId
            , Map<String, PipeHolder> pipesMap
            , SharedPreferencesProvider sharedPreferencesProvider) {
//        this.rxObj = rxObj;
        this.objectMapper = objectMapper;
        this.sharedPreferencesProvider = sharedPreferencesProvider;
        this.pipesMap = pipesMap;
    }

    private Mono<CloseableChannel> closeable = initRSocket();
    {
        closeable.subscribe(e->{
            Log.w("AVX", e.toString());
        },e->{
            Log.w("AVX", e.toString());
        });
    }

    private Mono<CloseableChannel> initRSocket() {

        Log.w("AVX", "creating P2P server...");

        return RSocketFactory
                .receive()

                .acceptor((a, b)-> handler(a, b))
                .transport(TcpServerTransport.create(DEFAULT_PORT))
                .start();
    }




    private Mono<RSocket> handler(ConnectionSetupPayload a, RSocket b) {
        InetSocketAddress remoteSocketAddr = null;
        try {
            Class objectClass = b.getClass();
            Field field = objectClass.getDeclaredField("connection");
            field.setAccessible(true);
            Object newObj = field.get(b);

            objectClass = newObj.getClass();
            field = objectClass.getDeclaredField("source");
            field.setAccessible(true);
            newObj = field.get(newObj);

            objectClass = newObj.getClass();
            field = objectClass.getDeclaredField("connection");
            field.setAccessible(true);
            newObj = field.get(newObj);

            objectClass = newObj.getClass();
            field = objectClass.getDeclaredField("connection");
            field.setAccessible(true);
            newObj = field.get(newObj);

            objectClass = newObj.getClass();
            field = objectClass.getDeclaredField("channel");
            field.setAccessible(true);
            newObj = field.get(newObj);
            NioSocketChannel nioSocketChannel = (NioSocketChannel) newObj;
            remoteSocketAddr = nioSocketChannel.remoteAddress();

            Log.w("AVX", "recieved rx connection from "+remoteSocketAddr.toString());

        } catch (IllegalAccessException e) {
            Log.e("AVX", "err rxP2Pcontroller", e);
        } catch (NoSuchFieldException e) {
            Log.e("AVX", "err2 rxP2Pcontroller", e);
        }

        InetSocketAddress finalRemoteSocketAddr = remoteSocketAddr;

        PublishProcessor<RxP2PObject> txObj = PublishProcessor.create();
        PublishSubject<RxP2PObject> rxObj = PublishSubject.create();

        RxP2PObjectController rSocketP2PObjectController = new RxP2PObjectController(objectMapper, txObj, rxObj, sharedPreferencesProvider, pipesMap);


        return Mono.just(new AbstractRSocket() {

            //2directional - sending in both ways:
            @Override
            public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
                Log.w("AVX", "P2P: got requestChannel from "+finalRemoteSocketAddr.getHostName()+":"+finalRemoteSocketAddr.getPort());
                Flux.from(payloads)
                        .subscribe(rSocketP2PObjectController::accept);
                return rSocketP2PObjectController.getReactiveFlux();
            }

        });
    }

}
