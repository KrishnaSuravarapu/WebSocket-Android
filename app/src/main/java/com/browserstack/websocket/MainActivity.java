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

import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class MainActivity extends AppCompatActivity {

    boolean isConnected = false;

    URI url;

    WebSocketClient webSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        url = URI.create(getIntent().getStringExtra("server"));
        Log.d("bsstack",String.format("Arg is %s", getIntent().getStringExtra("server")));
        webSocket = new WebSocketClient(url){

            @Override
            public void onOpen(ServerHandshake handshakedata) {
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
            public void onMessage(String message) {
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
                Log.d("bsstack",message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
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
                Log.d("bsstack",String.format("Closed due to %s", reason));
            }

            @Override
            public void onError(Exception ex) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            showAlertDialog("Error");
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
                Log.d("bsstack",String.format("errored due to %s", ex.toString()));
            }
        };
        try {
            webSocket.setSocketFactory(fakeSocketFactory());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }


    public void connectToggle(View view) {
        Button connectToggleBtn = (Button)findViewById(R.id.connectToggleBtn);
        String buttonText = connectToggleBtn.getText().toString();
        if(buttonText.equals("Connect")){
            Log.d("bsstack", String.valueOf(webSocket.isFlushAndClose()));
            Log.d("bsstack", String.valueOf(url));
            connectToggleBtn.setText("Disconnect");
            if(webSocket.isClosed()){
                webSocket.reconnect();
            }
            else{
                webSocket.connect();
            }
        }
        else {
            connectToggleBtn.setText("Connect");
            webSocket.close();
        }
    }

    public void sendMessage(View view){
        webSocket.send("Hey");
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

    public SocketFactory fakeSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
        SSLContext sslContext = SSLContext.getInstance("SSL");
        TrustManager[] list = new TrustManager[1];
        list[0] = trustManager;
        sslContext.init(null, list, new SecureRandom());
        return sslContext.getSocketFactory();
    }
}