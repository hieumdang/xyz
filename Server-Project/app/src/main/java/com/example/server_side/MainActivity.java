package com.example.server_side;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity  {
    //initializes all the private properties
    //For any server the ServerSocket and the Socket corresponding to the temp client
    // to be activated must be initialized
    private ServerSocket serverSocket;
    private Socket tempClientSocket;

    //here it sets the Thread initially to null
    Thread serverThread = null;

    //the SERVER_PORT is initialized which must correspond to the port of the client
    public static final int SERVER_PORT = 5050;

    //the msgList is initialized corresponding to the Linearlayout
    private LinearLayout msgList;
    private Handler handler;
    private int greenColor;
    private EditText edMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //This sets the initial content view that would be displayed
        setContentView(R.layout.activity_main);
        setTitle("Server-Side Endpoint");

        //initializes the identifier greenColor to be used anywhere within this file
        greenColor = ContextCompat.getColor(this, R.color.green);

        //initializes a new handler for message queueing
        handler = new Handler();
        msgList = findViewById(R.id.msgList);
        edMessage = findViewById(R.id.edMessage);
    }



    //method to implement the different Textviews widget and display the message on
    //the Scrollview LinearLayout...
    public TextView textView(String message, int color, Boolean value) throws IOException {

        //it checks if the message is empty then displays empty message
        if (null == message || message.trim().isEmpty()) {
            message = "<Empty Message>";
        }

        if (message.contains("send"))
        {
            FileTxThread fileTxThread = new FileTxThread(tempClientSocket);
            fileTxThread.start();
            message = "sending images";
      //      receiveFile();
        }
        TextView tv = new TextView(this);
        tv.setTextColor(color);
        tv.setText(message + " [" + getTime() +"]");
        tv.setTextSize(20);
        tv.setPadding(0, 5, 0, 0);

        if (value) {
      //      tv.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        }
        return tv;
    }

    //showMessage method to handle posting of mesage to the textView
    public void showMessage(final String message, final int color, final Boolean value) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    msgList.addView(textView(message, color, value));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //onClick method to handle clicking events whether to start up the  server or
    //send a message to the client
    public void onClick(View view) {
        if (view.getId() == R.id.start_server) {
            msgList.removeAllViews();
            showMessage("Server Started.", Color.BLACK, false);

            //this initiates the serverthread defined later and starts the thread
            this.serverThread = new Thread(new ServerThread());
            this.serverThread.start();
            view.setVisibility(View.GONE);
            return;
        }
        if (view.getId() == R.id.send_data) {
            String msg = edMessage.getText().toString().trim();
            showMessage("Server : " + msg, Color.BLUE, false);
            if (msg.length() > 0) {

                sendMessage(msg);
            }
            edMessage.setText("");
            return;
        }
    }


    //method implemented to send message to the client
    private void sendMessage(final String message) {
        try {
            if (null != tempClientSocket) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        PrintWriter out = null;
                        try {
                            out = new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(tempClientSocket.getOutputStream())),
                                    true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        out.println(message);
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* serverthread method implemented here to activate the server network */
    class ServerThread implements Runnable {

        public void run() {
            Socket socket;
            try {
                serverSocket = new ServerSocket(SERVER_PORT);

                //deactivates the visibility of the button
//               Button button = (Button) findViewById(R.id.start_server);
//               button.setVisibility(View.GONE);
            } catch (IOException e) {
                e.printStackTrace();
                showMessage("Error Starting Server : " + e.getMessage(), Color.RED, false);
            }

            //communicates to client and displays error if communication fails
            if (null != serverSocket) {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        socket = serverSocket.accept();
                        CommunicationThread commThread = new CommunicationThread(socket);
                        new Thread(commThread).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                        showMessage("Error Communicating to Client :" + e.getMessage(), Color.RED, false);
                    }
                }
            }
        }
    }

    /* communicationThread class that implements the runnable class to communicate with the client */
    class CommunicationThread implements Runnable {

        private Socket clientSocket;

        private BufferedReader input;

        public CommunicationThread(Socket clientSocket) throws IOException {
            this.clientSocket = clientSocket;
            tempClientSocket = clientSocket;
            try {
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
                showMessage("Error Connecting to Client!!", Color.RED, false);
            }
     //       receiveFile();
            showMessage("Connected to Client!!", greenColor, true);

        }

        public void run() {

            while (!Thread.currentThread().isInterrupted()) {
                try {

                    //checks to see if the client is still connected and displays disconnected if disconnected
                    String read = input.readLine();
                    if (null == read || "Disconnect".contentEquals(read)) {
                        Thread.interrupted();
                        read = "has left the chat room.";
                        showMessage("Client : " + read, Color.RED, true);
                        break;
                    }
                    showMessage("Client : " + read, greenColor, true);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

    }


    public class FileTxThread extends Thread {
        Socket socket;

        FileTxThread(Socket socket){
            this.socket= socket;
        }

        @Override
        public void run() {
            File file = new File(
                    Environment.getExternalStorageDirectory(),
                    "plus_icon.jpg");

            byte[] bytes = new byte[(int) file.length()];
            BufferedInputStream bis;
            try {
                bis = new BufferedInputStream(new FileInputStream(file));
                bis.read(bytes, 0, bytes.length);

                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeObject(bytes);
                oos.flush();

            //    socket.close();

                final String sentMsg = "File sent to: " + socket.getInetAddress();
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,
                                sentMsg,
                                Toast.LENGTH_LONG).show();
                    }});

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                /*
                try {
           //         socket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                 */
            }

        }
    }
    File plus_iconEX = new File(
            Environment.getExternalStorageDirectory(),
            "plus_iconEX.JPG");
    public void receiveFile() throws IOException {

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    // receive file
                    int bytesRead;
                    int current = 0;
                    FileOutputStream fos = null;
                    BufferedOutputStream bos = null;
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                            .permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    try {

                        // receive file
                        byte [] mybytearray  = new byte [5881];
                        InputStream is = tempClientSocket.getInputStream();
                        OutputStream os = tempClientSocket.getOutputStream();
                        int byteRead;
                        while ((byteRead = is.read()) != -1) {
                            os.write(byteRead);
                        }
                    //    os.write(mybytearray);
                        /*
                        fos = new FileOutputStream(plus_iconEX);
                        bos = new BufferedOutputStream(fos);
                        bytesRead = is.read(mybytearray,0,mybytearray.length);
                        current = bytesRead;

                        do {
                            bytesRead =
                                    is.read(mybytearray, current, (mybytearray.length-current));
                            if(bytesRead >= 0) current += bytesRead;
                        } while(bytesRead > 0);

                        bos.write(mybytearray, 0 , current);
                        bos.flush();

                         */
                        System.out.println("File " + plus_iconEX
                                + " downloaded (" + current + " bytes read)");
                    }
                    finally {
                              if (fos != null) fos.close();
                              if (bos != null) bos.close();
                              //if (tempClientSocket != null) tempClientSocket.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();




}
    //getTime method implemented to format the date into H:m:s
    String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }

    //personally described onDestroy method to disconnect from the network on destroy of the activity
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != serverThread) {
            sendMessage("Disconnect");
            serverThread.interrupt();
            serverThread = null;
        }
    }
}
