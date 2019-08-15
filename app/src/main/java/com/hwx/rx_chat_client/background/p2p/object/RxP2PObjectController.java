package com.hwx.rx_chat_client.background.p2p.object;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hwx.rx_chat_client.background.p2p.object.type.ObjectType;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import io.reactivex.processors.PublishProcessor;
import io.reactivex.subjects.PublishSubject;
import io.rsocket.Payload;
import io.rsocket.util.DefaultPayload;
import reactor.core.publisher.Flux;

public class RxP2PObjectController {

    private String clientId = UUID.randomUUID().toString();

    private ObjectMapper objectMapper;
    private String profileId;

    private String remoteProfileId;
    private PublishProcessor<RxP2PObject> txObj;
    private PublishSubject<RxP2PObject> rxObj;
    private Map<String, PipeHolder> pipesMap;

    public RxP2PObjectController(
              ObjectMapper objectMapper
            , PublishProcessor<RxP2PObject> txObj
            , PublishSubject<RxP2PObject> rxObj
            , String profileId
            , Map<String, PipeHolder> pipesMap
    ) {
        this.objectMapper = objectMapper;
        this.rxObj = rxObj;
        this.txObj = txObj;
        this.profileId = profileId;
        this.pipesMap = pipesMap;
        Log.w("AVX", "created new rxP2PController for with clientId = "+clientId);
    }

    public void accept(Payload payload) {
        RxP2PObject rxP2PObject = null;
        try {
            rxP2PObject = objectMapper.readValue(payload.getDataUtf8(), RxP2PObject.class);

            //base request handling
            handleRxP2PObject(rxP2PObject);

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
                        return DefaultPayload.create(objectMapper.writeValueAsString(rxP2PObject).getBytes());
                    } catch (JsonProcessingException e) {
                        Log.e("AVX", "err of wrapping obj :", e);
                    }
                    return null;
                });
    }

    private void handleRxP2PObject(RxP2PObject rxP2PObject) {
        Log.w("AVX", "handleRxP2PObject: "+rxP2PObject.toString());
        if (rxP2PObject.getObjectType().equals(ObjectType.PROFILE_ID_REQUEST)) {

            remoteProfileId = rxP2PObject.getValue();
            Log.w("AVX", "saving PIPE for remoteProfileId = "+remoteProfileId);
            PipeHolder pipeHolder = new PipeHolder(txObj, rxObj);
            pipesMap.put(remoteProfileId, pipeHolder);

            RxP2PObject respObj = new RxP2PObject();
            respObj.setObjectType(ObjectType.PROFILE_ID_RESPONSE);
            respObj.setValue(profileId);

            new Handler(Looper.getMainLooper()).postDelayed(()-> {
                txObj.onNext(respObj);
            }, 500);


            return;
        }

        rxObj.onNext(rxP2PObject);
    }
}
