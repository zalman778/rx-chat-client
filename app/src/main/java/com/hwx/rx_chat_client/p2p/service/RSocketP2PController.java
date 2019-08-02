package com.hwx.rx_chat_client.p2p.service;

import android.util.Log;

import com.hwx.rx_chat_client.Configuration;
import com.hwx.rx_chat_client.p2p.RSocketP2PObjectController;
import com.hwx.rx_chat_client.util.NetworkUtil;

import org.reactivestreams.Publisher;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;

import javax.inject.Inject;

import io.netty.channel.epoll.EpollSocketChannel;
import io.rsocket.AbstractRSocket;
import io.rsocket.ConnectionSetupPayload;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.server.CloseableChannel;
import io.rsocket.transport.netty.server.WebsocketServerTransport;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServer;
import reactor.netty.tcp.TcpServer;

public class RSocketP2PController {

    @Inject
    RSocketP2PController(RxP2PService service) {
        /*constructor stuff*/
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

        Log.w("AVX", NetworkUtil.getIPAddress(true) + "; "+ Configuration.RSOCKET_CLIENT_SERVER_PORT);

        TcpServer tcpServer = TcpServer.create()
                .addressSupplier(() -> new InetSocketAddress(NetworkUtil.getIPAddress(true)
                        , Configuration.RSOCKET_CLIENT_SERVER_PORT));

        HttpServer httpServer = HttpServer.from(tcpServer);
        return RSocketFactory
                .receive()

                .acceptor((a, b)-> handler(a, b))
                .transport(
                        // TcpServerTransport.create("localhost", PORT)
                        WebsocketServerTransport.create(httpServer)
                )
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

            objectClass = newObj.getClass().getSuperclass().getSuperclass().getSuperclass();
            field = objectClass.getDeclaredField("connection");
            field.setAccessible(true);
            newObj = field.get(newObj);

            objectClass = newObj.getClass();
            field = objectClass.getDeclaredField("channel");
            field.setAccessible(true);
            newObj = field.get(newObj);
            EpollSocketChannel epollSocketChannel = (EpollSocketChannel) newObj;
            remoteSocketAddr = epollSocketChannel.remoteAddress();

            Log.w("AVX", "recieved rx connection from "+remoteSocketAddr.toString());

            } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        InetSocketAddress finalRemoteSocketAddr = remoteSocketAddr;
        return Mono.just(new AbstractRSocket() {


//            RSocketP2PObjectController rSocketP2PObjectController = new RSocketP2PObjectController(
//                    mapper, rxObjectHandler, finalRemoteSocketAddr
//            );
            RSocketP2PObjectController rSocketP2PObjectController = new RSocketP2PObjectController();

            //2directional - sending in both ways:
            @Override
            public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
                Flux.from(payloads)
                        .subscribe(rSocketP2PObjectController::accept);
                return rSocketP2PObjectController.getReactiveFlux();
            }

        });
    }
}
