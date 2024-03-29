package com.hwx.rx_chat_client.di;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hwx.rx_chat_client.Configuration;
import com.hwx.rx_chat_client.R;
import com.hwx.rx_chat_client.RxChatApplication;
import com.hwx.rx_chat_client.background.p2p.db.P2pDatabase;
import com.hwx.rx_chat_client.background.p2p.db.service.P2pDbService;
import com.hwx.rx_chat_client.repository.ChatRepository;
import com.hwx.rx_chat_client.repository.DialogRepository;
import com.hwx.rx_chat_client.repository.FriendRepository;
import com.hwx.rx_chat_client.service.ChatService;
import com.hwx.rx_chat_client.service.DialogService;
import com.hwx.rx_chat_client.service.FriendService;
import com.hwx.rx_chat_client.util.NetworkUtil;
import com.hwx.rx_chat_client.util.ResourceProvider;
import com.hwx.rx_chat_client.util.SharedPreferencesProvider;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;

import dagger.Module;
import dagger.Provides;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import okhttp3.Cache;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class UtilsModule {

    private Context context;

    public UtilsModule(Context context) {
        this.context = context;
    }

    @Provides
    @Singleton
    RxChatApplication provideRxChatApplication() {
        return new RxChatApplication();
    }

    @Provides
    @Singleton
    Context provideContext() {
        return context;
    }

    @Provides
    @Singleton
    ResourceProvider getResourceProvider(Context context) {
        return new ResourceProvider(context);
    }

    @Provides
    @Singleton
    SharedPreferencesProvider getSharedPreferencesProvides(Context context) { return new SharedPreferencesProvider(context); }

    @Provides
    @Singleton
    Gson provideGson() {
        GsonBuilder builder =
                new GsonBuilder()
                        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                        .setDateFormat("dd-MM-yyyy HH:mm:ss");
        return builder.setLenient().create();
    }

    @Provides
    @Singleton
    public ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE);
        mapper.setDateFormat(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"));
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Provides
    @Singleton
    Picasso getPicasso(Context context, OkHttpClient okHttpClient) {
        Picasso picasso = new Picasso.Builder(context)
                .downloader(new OkHttp3Downloader(okHttpClient))
                .build();
        Picasso.setSingletonInstance(picasso);
        return picasso;
    }

    @Provides
    @Singleton
    OkHttpClient getOkHttpClient(SharedPreferencesProvider sharedPreferencesProvider) {

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        //caching...
        Long cacheSize = 5*1024*1024L; //5mb cache size
        Cache cache = new Cache(context.getCacheDir(), cacheSize);

        Interceptor onlineInterceptor = chain -> {
            Response response = chain.proceed(chain.request());
            int maxAge = 60; // read from cache for 60 seconds even if there is internet connection
            return response.newBuilder()
                    .header("Cache-Control", "public, max-age=" + maxAge)
                    .removeHeader("Pragma")
                    .build();
        };

        Interceptor offileInterceptor = chain -> {
            Request request = chain.request();
            if (!NetworkUtil.hasNetwork(context)) {
                request.newBuilder()
                        .header("Cache-Control", "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7)
                        .removeHeader("Pragma")
                        .build();
            }
            return chain.proceed(request);
        };


        //temporary way, later should be fixed:
        httpClient.hostnameVerifier((hostname, session) -> true);

        //https support
        SSLContext sslContext = getSslContext();
        if (sslContext != null)
            httpClient.sslSocketFactory(sslContext.getSocketFactory());

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        httpClient.addInterceptor(loggingInterceptor);

        httpClient.addInterceptor(chain -> {
            Request original = chain.request();
            Request request = original.newBuilder()
                    .build();
            return chain.proceed(request);
        })
                .connectTimeout(100, TimeUnit.SECONDS)
                .writeTimeout(100, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS);

        //if header map exists here:
        String token = sharedPreferencesProvider.getSharedPreferences("localPref", 0).getString("token", "");
        if (token != null && !token.isEmpty())
            httpClient.addInterceptor(chain -> {
                Request newRequest = chain.request()
                        .newBuilder()
                        .addHeader("Authorization", token)
                        .build();
                return chain.proceed(newRequest);
            });

        httpClient
            .addInterceptor(offileInterceptor)
            .addNetworkInterceptor(onlineInterceptor)
            .cache(cache);

        return httpClient.build();
    }


    @Provides
    @Singleton
    Retrofit provideRetrofit(Gson gson, OkHttpClient okHttpClient) {

        return new Retrofit.Builder()
                .baseUrl(HttpUrl.parse(Configuration.HTTPS_SERVER_URL))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    @Provides
    @Singleton
    public SSLContext getSslContext() {
        try {
            KeyStore keyStore = readKeyStore();
            SSLContext sslContext = SSLContext.getInstance("SSL");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, Configuration.CLEINT_NETTY_KEYSTORE_PASS.toCharArray());
            sslContext.init(keyManagerFactory.getKeyManagers(),trustManagerFactory.getTrustManagers(), new SecureRandom());
            return sslContext;
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | KeyManagementException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Provides
    @Singleton
    public SslContext getNettySslContext() {
        SslContext sslContext = null;
        try {

            KeyStore ks = readKeyStore();

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(ks);


            sslContext = SslContextBuilder.forClient()
                    .startTls(true)
                    .trustManager(trustManagerFactory)
                    .build();

            return sslContext;

        } catch (SSLException | NoSuchAlgorithmException | KeyStoreException e) {
            e.printStackTrace();
            Log.e("AVX", "err", e);

            return null;
        }
    }

    private KeyStore readKeyStore() {
        KeyStore ks = null;
        InputStream fis = null;
        try {
            ks = KeyStore.getInstance("PKCS12");
            char[] password = Configuration.CLIENT_CERT_PASS.toCharArray();
            fis = context.getResources().openRawResource(R.raw.keystore);
            ks.load(fis, password);

        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            Log.e("AVX", "err", e);
        }
        return ks;
    }

    @Provides
    @Singleton
    ChatService getChatService(Retrofit retrofit) {
        return retrofit.create(ChatService.class);
    }

    @Provides
    @Singleton
    ChatRepository getRepository(ChatService chatService) {
        return new ChatRepository(chatService);
    }

    @Provides
    @Singleton
    FriendService getFriendService(Retrofit retrofit) { return retrofit.create(FriendService.class); }

    @Provides
    @Singleton
    FriendRepository getFriendRepository(FriendService friendService) {
        return new FriendRepository(friendService);
    }

    @Provides
    @Singleton
    DialogService getDialogsService(Retrofit retrofit) { return retrofit.create(DialogService.class); }

    @Provides
    @Singleton
    DialogRepository getDialogsRepository(DialogService dialogService) {
        return new DialogRepository(dialogService);
    }

    @Provides
    @Singleton
    P2pDatabase provideP2pDatabase() {
        return Room.databaseBuilder(context, P2pDatabase.class, "p2pdatabase").build();
    }

    @Provides
    @Singleton
    P2pDbService provideP2pService(P2pDatabase p2pDatabase) {
        return new P2pDbService(p2pDatabase.p2pDialogDao(), p2pDatabase.p2pMessageDao());
    }


}
