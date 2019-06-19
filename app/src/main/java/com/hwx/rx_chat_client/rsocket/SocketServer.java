package com.hwx.rx_chat_client.rsocket;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hwx.rx_chat_client.Configuration;
import com.hwx.rx_chat_client.util.NetworkUtil;

import java.net.InetSocketAddress;

import io.netty.handler.ssl.SslContext;
import io.rsocket.ConnectionSetupPayload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.server.CloseableChannel;
import io.rsocket.transport.netty.server.WebsocketServerTransport;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServer;
import reactor.netty.tcp.TcpServer;

public class SocketServer {

    private Mono<CloseableChannel> monoCloseable;

    public SocketServer(SslContext sslContext, ObjectMapper objectMapper) {


        TcpServer tcpServer = TcpServer.create()
                .addressSupplier(() ->
                        new InetSocketAddress(Configuration.getIpV4(), Configuration.getPort()))
                .secure(sslContext);

        HttpServer httpServer = HttpServer.from(tcpServer);
//        monoCloseable = RSocketFactory
//                .receive()
//
//                .acceptor((a, b)-> acceptorHandler(a, b))
//                .transport(
//                        // TcpServerTransport.create("localhost", RSOCKET_PORT)
//                        WebsocketServerTransport.create(httpServer)
//                )
//                .start();
    }

    private Mono<RSocket> acceptorHandler(ConnectionSetupPayload a, RSocket b) {
        return null;
    }
}
