package com.hwx.rx_chat_client.background.p2p.object;

import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hwx.rx_chat_client.background.p2p.StringUtils;
import com.hwx.rx_chat_client.background.p2p.object.type.ObjectType;
import com.hwx.rx_chat_client.util.SharedPreferencesProvider;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;
import java.util.UUID;

import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import io.reactivex.processors.PublishProcessor;
import io.reactivex.subjects.PublishSubject;
import io.rsocket.Payload;
import io.rsocket.util.DefaultPayload;
import reactor.core.publisher.Flux;

public class RxP2PObjectController {

    private String clientId = UUID.randomUUID().toString();

    private ObjectMapper objectMapper;
    private SharedPreferencesProvider sharedPreferencesProvider;
    private PublishSubject<String> psWelcomeHandshakeCompletedAction;

    private String remoteProfileId;
    private PublishProcessor<RxP2PObject> txObj;
    private PublishSubject<RxP2PObject> rxObj;
    private Map<String, PipeHolder> pipesMap;

    public RxP2PObjectController(
            ObjectMapper objectMapper
            , PublishProcessor<RxP2PObject> txObj
            , PublishSubject<RxP2PObject> rxObj
            , SharedPreferencesProvider sharedPreferencesProvider
            , Map<String, PipeHolder> pipesMap
            , PublishSubject<String> psWelcomeHandshakeCompletedAction
    ) {
        this.objectMapper = objectMapper;
        this.rxObj = rxObj;
        this.txObj = txObj;
        this.sharedPreferencesProvider = sharedPreferencesProvider;
        this.pipesMap = pipesMap;
        this.psWelcomeHandshakeCompletedAction = psWelcomeHandshakeCompletedAction;
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

        /*
            Handling welcome handshake request:
         */
        if (rxP2PObject.getObjectType().equals(ObjectType.WELCOME_HANDSHAKE_REQEST)) {

            remoteProfileId = rxP2PObject.getValue();
            Log.w("AVX", "saving PIPE for remoteProfileId = "+remoteProfileId);
            PipeHolder pipeHolder = new PipeHolder(txObj, rxObj);
            pipesMap.put(remoteProfileId, pipeHolder);


            String profileAvatarUrl = sharedPreferencesProvider.getSharedPreferences("localPref", 0).getString("profileAvatarUrl", "");
            profileAvatarUrl = profileAvatarUrl != null ? profileAvatarUrl.replace("api/image/", "") : null;

            String caption = sharedPreferencesProvider.getSharedPreferences("username", 0).getString("profileAvatarUrl", "");

            RxP2PObject respObj = new RxP2PObject();
            respObj.setObjectType(ObjectType.WELCOME_HANDSHAKE_RESPONSE);
            respObj.setValue(caption);
            respObj.setValueId(profileAvatarUrl);


            //dh.pt2
            try {
                byte[] alicePubKeyEnc = Base64.decode(rxP2PObject.getValueAdditional(), Base64.DEFAULT);

                KeyFactory bobKeyFac = KeyFactory.getInstance("DH");
                X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(alicePubKeyEnc);

                PublicKey alicePubKey = bobKeyFac.generatePublic(x509KeySpec);

                /*
                 * Bob gets the DH parameters associated with Alice's public key.
                 * He must use the same parameters when he generates his own key
                 * pair.
                 */
                DHParameterSpec dhParamFromAlicePubKey = ((DHPublicKey) alicePubKey).getParams();

                // Bob creates his own DH key pair
                Log.w("AVX", "BOB: Generate DH keypair ...");
                KeyPairGenerator bobKpairGen = KeyPairGenerator.getInstance("DH");
                bobKpairGen.initialize(dhParamFromAlicePubKey);
                KeyPair bobKpair = bobKpairGen.generateKeyPair();

                // Bob creates and initializes his DH KeyAgreement object
                Log.w("AVX", "BOB: Initialization ...");
                KeyAgreement bobKeyAgree = KeyAgreement.getInstance("DH");
                bobKeyAgree.init(bobKpair.getPrivate());

                Log.w("AVX", "BOB: Execute PHASE1 ...");
                bobKeyAgree.doPhase(alicePubKey, true);


                // Bob encodes his public key, and sends it over to Alice.
                byte[] bobPubKeyEnc = bobKpair.getPublic().getEncoded();

                String base64BobPublicKey = Base64.encodeToString(bobPubKeyEnc, Base64.DEFAULT);

                respObj.setValueAdditional(base64BobPublicKey);

                byte[] bobSharedSecret = bobKeyAgree.generateSecret();
                SecretKeySpec bobAesKey = new SecretKeySpec(bobSharedSecret, 0, 16, "AES");

                pipesMap.get(remoteProfileId).setSecretKey(bobAesKey);

                Log.w("AVX", "shared key = "+ StringUtils.toHexString(bobSharedSecret));


            } catch (InvalidKeyException | NoSuchAlgorithmException | InvalidAlgorithmParameterException  | InvalidKeySpecException e) {
                Log.e("AVX", "err on DH", e);
            }

            psWelcomeHandshakeCompletedAction.onNext(remoteProfileId);

            new Handler(Looper.getMainLooper()).postDelayed(()-> {
                txObj.onNext(respObj);
            }, 500);


            return;
        }

        rxObj.onNext(rxP2PObject);
    }
}
