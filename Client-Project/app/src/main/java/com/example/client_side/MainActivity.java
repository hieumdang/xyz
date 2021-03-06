package com.example.client_side;


import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    public static final int SERVER_PORT = 5050;

    public static final String SERVER_IP = "192.0.0.4";
    private ClientThread clientThread;
    private Thread thread;
    private LinearLayout msgList;
    private Handler handler;
    private int clientTextColor;
    private EditText edMessage;
    String mes;
    BufferedReader br;
    private Button clear;
    FileInputStream fis = null;
    BufferedInputStream bis = null;
    byte[] miBytes;
    Button send_button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        clear = findViewById(R.id.clear);
        setTitle("Client");
        clientTextColor = ContextCompat.getColor(this, R.color.green);
        handler = new Handler();
        msgList = findViewById(R.id.msgList);
        edMessage = findViewById(R.id.edMessage);
        send_button = findViewById(R.id.send_data);
    }
    private final String htmlText = "<body><img src=\"likes.png\" width=\"20px\" height=\"20px\"></body>";
    public TextView textView(String message, int color, Boolean value) {
        if (null == message || message.trim().isEmpty()) {
            message = "<Empty Message>";
        }
        TextView tv = new TextView(MainActivity.this);
        tv.setTextColor(color);
        tv.setTextSize(20);
        tv.setPadding(0, 5, 0, 0);
        if (message.contains("like"))
        {
            System.out.println("Should be like icon");
            Log.e("like", "=like");
            tv.setText(Html.fromHtml(htmlText, new ImageGetter(), null));
        }
        if (message.contains("send"))
        {
            message = "Downloading";
            showMessage("Server: " + message, clientTextColor, true);
            //
            ClientRxThread clientRxThread =
                    new ClientRxThread("192.0.0.4", 5050);
            clientRxThread.start();

            //      clientRxThread.start();
            //    sendSend();

        }
        else {
            tv.setText(message + " [" + getTime() + "]");
        }
        tv.setLayoutParams(new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 0));
        if (value) {
       //     tv.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        }
        return tv;
    }

    public void showMessage(final String message, final int color, final Boolean value) {
        handler.post(new Runnable() {

            @Override
            public void run() {
                msgList.addView(textView(message, color, value));
            }
        });
    }

    public void onClick(View view) {

        if (view.getId() == R.id.connect_server) {
            msgList.removeAllViews();
            clientThread = new ClientThread();
            thread = new Thread(clientThread);
            thread.start();
            return;
        }
        if (view.getId() == R.id.clear)
        {
            msgList.removeAllViews();
        }
        if (view.getId() == R.id.send_data) {
            String clientMessage = edMessage.getText().toString().trim();
            showMessage(clientMessage, Color.BLUE, false);
            if (null != clientThread) {
                if (clientMessage.length() > 0){
                    clientThread.sendMessage(clientMessage);
                }
                edMessage.setText("");
            }
        }
    }

    /* clientThread class defined to run the client connection to the socket network using the server ip and port
     * and send message */
    class ClientThread implements Runnable {

        private Socket socket;
        private BufferedReader input;

        @Override
        public void run() {

            try {

                    InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                    showMessage("Connecting to Server...", clientTextColor, true);

                    socket = new Socket(serverAddr, SERVER_PORT);

                    if (socket.isBound()){
                  //      sendImage();
                        showMessage("Connected to Server...", clientTextColor, true);

                    }


        while (!Thread.currentThread().isInterrupted()) {


                    this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    br = this.input;
                    String message = input.readLine();

                    if (message.contains("like"))
                    {
              //          sendImage();

                        showMessage("Server: " + htmlText, clientTextColor, true);
                    }

                    else {
                        if (null == message || "Disconnect".contentEquals(message)) {
                            Thread.interrupted();
                            message = "Server Disconnected...";
                            showMessage(message, Color.RED, false);
                            break;
                        }
                        showMessage("Server: " + message, clientTextColor, true);
                        mes = message;
                    }
                }

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                showMessage("Problem Connecting to server... Check your server IP and Port and try again", Color.RED, false);
                Thread.interrupted();
                e1.printStackTrace();
            } catch (NullPointerException e3) {
                showMessage("error returned", Color.RED,true);
            }

        }

        void sendMessage(final String message) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (null != socket) {
                            PrintWriter out = new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(socket.getOutputStream())),
                                    true);
                            out.println(message);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }


          File plus_icon = new File(
                                    Environment.getExternalStorageDirectory(),
                                    "plus_iconEX.JPG");
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        OutputStream os = null;

        public void sendImage() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                            // send file
                            byte [] mybytearray  = new byte [FILE_SIZE];
                            fis = new FileInputStream(plus_icon);
                            bis = new BufferedInputStream(fis);
                            bis.read(mybytearray,0,mybytearray.length);
                            os = socket.getOutputStream();
                            System.out.println("Sending " + plus_icon + "(" + mybytearray.length + " bytes)");
                            os.write(mybytearray,0,mybytearray.length);
                            os.flush();
                            System.out.println("Done.");
                        } catch (FileNotFoundException ex) {
                        ex.printStackTrace();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                }
            }).start();
        }
    }
    public final static int FILE_SIZE = 5881; // file size temporary hard coded
    String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }


    public void sendSend()
    {
        edMessage.setText("send");
        send_button.performClick();
        edMessage.setText("");
    }
    public class ClientRxThread extends Thread {
        String dstAddress;
        int dstPort;

        ClientRxThread(String address, int port) {
            dstAddress = address;
            dstPort = port;
        }

        @Override
        public void run() {
            Socket socket = null;

            try {
                socket = new Socket(dstAddress, dstPort);

                File file = new File(
                        Environment.getExternalStorageDirectory(),
                        "plus_iconEX.jpg");

                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                byte[] bytes;
                FileOutputStream fos = null;
                try {
                    bytes = (byte[])ois.readObject();
                    fos = new FileOutputStream(file);
                    fos.write(bytes);
                    miBytes = bytes;
                } catch (ClassNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    if(fos!=null){
                        fos.close();
                    }

                }

           //     socket.close();

                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,
                                "Finished",
                                Toast.LENGTH_LONG).show();
                    }});

            } catch (IOException e) {

                e.printStackTrace();

                final String eMsg = "Something wrong: " + e.getMessage();
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,
                                eMsg,
                                Toast.LENGTH_LONG).show();
                    }});

            } finally {
                /*
                if(socket != null){
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                */
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != clientThread) {
            clientThread.sendMessage("Disconnect");
            clientThread = null;
        }
    }

    private class ImageGetter implements Html.ImageGetter {

        public Drawable getDrawable(String source) {
            int id;
            if (source.equals("likes.png")) {
                id = R.drawable.likes;
            }
            else {
                return null;
            }

            Drawable d = getResources().getDrawable(id);
            d.setBounds(0,0,40,40);
            return d;
        }
    };
}
