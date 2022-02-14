package com.browserstack.websocket;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import okhttp3.*;
import okio.ByteString;

public class MainActivity extends AppCompatActivity {

    boolean isConnected = false;

    String url;

    Request request;

    WebSocketClient webSocket;

    OkHttpClient webSocketClient;

    WebSocketListener OkHTTPWebSocketListener;

    WebSocket webSocketokhttp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        url = getIntent().getStringExtra("server");
        request = new Request.Builder().addHeader("Connection", "close").addHeader("content-type", "application/json").url(url).build();

        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            builder.retryOnConnectionFailure(true);
            webSocketClient = builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


            Log.d("bsstack",String.format("Arg is %s", getIntent().getStringExtra("server")));
        OkHTTPWebSocketListener = new WebSocketListener() {
            private static final int NORMAL_CLOSURE_STATUS = 1000;
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                webSocketokhttp = webSocket;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            showAlertDialog("Connected");
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
                Log.d("bsstack","Connected");
            }
            @Override
            public void onMessage(WebSocket webSocket, String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            showAlertDialog(message);
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
                Log.d("bsstack", "Receiving : " + message);
            }
            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                Log.d("bsstack", "Receiving bytes : " + bytes.hex());
            }
            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(NORMAL_CLOSURE_STATUS, null);
                webSocket.cancel();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            showAlertDialog("Disconnected");
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
                Log.d("bsstack", "Closing : " + code + " / " + reason);
            }
            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.d("bsstack", "Failure : " + t.getMessage());
            }
        };

    }


    public void connectToggle(View view) {
        Button connectToggleBtn = (Button)findViewById(R.id.connectToggleBtn);
        String buttonText = connectToggleBtn.getText().toString();
        if(buttonText.equals("Connect")){
            Log.d("bsstack", String.valueOf(url));
            connectToggleBtn.setText("Disconnect");
            webSocketClient.newWebSocket(request, OkHTTPWebSocketListener);
        }
        else {
            connectToggleBtn.setText("Connect");
            try {
                webSocketokhttp.close(1000, "Disconnect");
            }
            catch (Exception e){
                Log.d("bsstack", e.toString());
            }
        }
    }

    public void sendMessage(View view){
        webSocketokhttp.send("Hey....");
    }

    public void showAlertDialog(String message) {

        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("WebSocket Response");
        builder.setMessage(message);

        // add a button
        builder.setPositiveButton("OK", null);

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}