package com.hwx.rx_chat_client.background.p2p.object;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.UUID;

import io.reactivex.processors.PublishProcessor;
import io.reactivex.subjects.PublishSubject;
import io.rsocket.Payload;
import io.rsocket.util.DefaultPayload;
import reactor.core.publisher.Flux;

public class RxP2PObjectController {

    private String clientId = UUID.randomUUID().toString();

    private ObjectMapper objectMapper;
    private PublishProcessor<RxP2PObject> txObj;
    private PublishSubject<RxP2PObject> rxObj;

    public RxP2PObjectController(
              ObjectMapper objectMapper
            , PublishProcessor<RxP2PObject> txObj
            , PublishSubject<RxP2PObject> rxObj
    ) {
        this.objectMapper = objectMapper;
        this.rxObj = rxObj;
        this.txObj = txObj;

        Log.w("AVX", "created new rxP2PController for with clientId = "+clientId);
    }

    public void accept(Payload payload) {
        RxP2PObject rxP2PObject = null;
        try {
            rxP2PObject = objectMapper.readValue(payload.getDataUtf8(), RxP2PObject.class);
            rxObj.onNext(rxP2PObject);
            Log.w("AVX", "accepted rxP2PObj="+rxP2PObject.toString());
        } catch (IOException e) {
            Log.e("AVX", "err of readValue obj :", e);
        }

    }

    public Flux<Payload> getReactiveFlux() {
        return Flux
                .from(txObj)
                .map(rxP2PObject -> {
                    try {
                        return DefaultPayload.create(objectMapper.writeValueAsString(rxObj).getBytes());
                    } catch (JsonProcessingException e) {
                        Log.e("AVX", "err of wrapping obj :", e);
                    }
                    return null;
                });
    }
}
