package com.hwx.rx_chat_client.viewModel.dialer;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;

import com.hwx.rx_chat_client.Configuration;
import com.hwx.rx_chat_client.background.p2p.object.RxP2PObject;
import com.hwx.rx_chat_client.background.p2p.object.type.ObjectType;
import com.hwx.rx_chat_client.background.p2p.service.RxP2PService;
import com.hwx.rx_chat_client.util.ResourceProvider;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import io.reactivex.disposables.CompositeDisposable;

public class DialerBaseViewModel extends ViewModel {

    protected CompositeDisposable compositeDisposable = new CompositeDisposable();
    protected MutableLiveData<String> lvVoiceCallCaptionTitle = new MutableLiveData<>();
    protected MutableLiveData<String> lvVoiceCallStatus = new MutableLiveData<>();


    protected RxP2PService rxP2PService;
    protected ResourceProvider resourceProvider;

    protected String remoteProfileId;
    protected String dialogCaption;

    protected AudioRecord recorder;
    private AudioTrack track;

    private SecretKeySpec secretKeySpec;
    private Cipher aliceCipher;
    private Cipher bobCipher;



    private int sampleRate = 16000 ;

    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int minBufSize = AudioRecord.getMinBufferSize(sampleRate,  AudioFormat.CHANNEL_IN_MONO, audioFormat);


    protected boolean status = true;

    public DialerBaseViewModel() {
        try {
            IvParameterSpec iv = new IvParameterSpec(Configuration.AES_INIT_VECTOR.getBytes("UTF-8"));

            aliceCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            aliceCipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv);

            bobCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            bobCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);

        } catch (NoSuchAlgorithmException | UnsupportedEncodingException | NoSuchPaddingException
                | InvalidAlgorithmParameterException | InvalidKeyException e) {
            Log.e("AVX", "err on init crypt...", e);
        }

    }

    public MutableLiveData<String> getLvVoiceCallCaptionTitle() {
        return lvVoiceCallCaptionTitle;
    }

    public MutableLiveData<String> getLvVoiceCallStatus() {
        return lvVoiceCallStatus;
    }

    public void setRxP2PService(RxP2PService rxP2PService) {
        this.rxP2PService = rxP2PService;
    }

    public void setRemoteProfileId(String remoteProfileId) {
        this.remoteProfileId = remoteProfileId;

        secretKeySpec = rxP2PService.getPipeHolder(remoteProfileId).getSecretKey();

    }

    protected void startVoicePlayback() {
        initAudioPlayback();
        compositeDisposable.add(
                rxP2PService
                        .getPipeHolder(remoteProfileId)
                        .getRxPipe()
                        .filter(rxP2PObject -> rxP2PObject.getObjectType() == ObjectType.VOICE_CALL_PAYLOAD)
                        .subscribe(rxP2PObject -> {
                            playVoiceAsync(rxP2PObject.getBytesPayload());
                        }, err-> Log.e("AVX", "err", err))
        );
    }

    private void initAudioPlayback() {
        Log.w("AVX", "init audio playback");

        int bufferSizeInBytes = Math.round(sampleRate / 60);

        Log.w("AVX", "got minBuffSize = "+minBufSize +"; and buffSize = "+bufferSizeInBytes);
        if (bufferSizeInBytes < minBufSize)
            bufferSizeInBytes = minBufSize;

        track = new AudioTrack(
                AudioManager.STREAM_VOICE_CALL
                ,sampleRate
                ,AudioFormat.CHANNEL_OUT_MONO
                ,audioFormat
                ,bufferSizeInBytes
                ,AudioTrack.MODE_STREAM
        );

        track.play();
    }

    private void playVoiceAsync(byte[] bytesPayload) {

        AsyncTask.execute(()->{
            try {
                byte[] decryptedBytes = aliceCipher.doFinal(bytesPayload);
                track.flush();
                track.write(decryptedBytes, 0, decryptedBytes.length);
            } catch (BadPaddingException | IllegalBlockSizeException e) {
                Log.e("AVX", "err on decrypt...", e);
            }

        });
    }


    protected void startVoiceStreaming() {
        status = true;

        Thread t1 = new Thread(() -> {

            //buffer size = sampleRate * (bitDepth / 8) * channelCount * 60 (minutes)

            //sampleRate * ( 16 / 8 ) * 1 * (1 / 120); // 1 sec

            int bufferSizeInBytes = Math.round(sampleRate / 60);

            Log.w("AVX", "got minBuffSize = "+minBufSize +"; and buffSize = "+bufferSizeInBytes);
            if (bufferSizeInBytes < minBufSize)
                bufferSizeInBytes = minBufSize;


            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate, AudioFormat.CHANNEL_IN_MONO, audioFormat,bufferSizeInBytes);


            recorder.startRecording();

            byte[] recordChunk = new byte[bufferSizeInBytes];
            RxP2PObject rxP2PObject = new RxP2PObject();
            rxP2PObject.setObjectType(ObjectType.VOICE_CALL_PAYLOAD);

            while (status)
            {
                recorder.read(recordChunk, 0, recordChunk.length);

                try {
                    rxP2PObject.setBytesPayload(bobCipher.doFinal(recordChunk));
                } catch (BadPaddingException | IllegalBlockSizeException e) {
                    Log.e("AVX", "err on encrypt...", e);
                }

                rxP2PService.sendRxP2PObject(remoteProfileId, rxP2PObject);
            }

            Log.v(this.getClass().toString(), "Stopped recording... ");

        });
        t1.start();

    }
}
