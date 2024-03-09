package com.example.se2_einzelphase_wassertheurer;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private Socket client;
    private BufferedWriter out;
    private BufferedReader in;

    private static final String ip = "se2-submission.aau.at";
    private static final int port = 20080;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tvResponse = findViewById(R.id.tvResponse);
        EditText etnMatrikelnummer = findViewById(R.id.eTNMatrikelnummer);
        Button btnSend = findViewById(R.id.btnSend);

        TextView tvChangeId = findViewById(R.id.tvChange);
        Button btnChangeId = findViewById(R.id.btnChange);

        // network running on separate thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                startConnection(ip, port);
            }
        }).start();

        // button "Abschicken"
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sidString = etnMatrikelnummer.getText().toString();
                int sid = Integer.parseInt(sidString);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String respMsg = sendStudentId(sid);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvResponse.setText(respMsg);
                            }
                        });
                    }
                }).start();
            }
        });

        // button "Ändern"
        btnChangeId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sidString = etnMatrikelnummer.getText().toString();
                int sid = Integer.parseInt(sidString);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvChangeId.setText(changeStudentId(sid));
                            }
                        });
                    }
                }).start();
            }
        });
    }

    // handles closing the app
    @Override
    protected void onStop() {
        super.onStop();
        stopConnection();
    }

    // builds up connection
    public void startConnection(String ip, int port) {
        try {
            client = new Socket(ip, port);
            out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch (Exception e) {
            Log.e("Error", "Exception: " + e + " thrown, when trying to start the connection.");
        }
        Log.i("Info", "Client connected to server on ip " + ip + ":" + port + ".");
    }

    // sends the studentId to the server
    public String sendStudentId(int sid) {
        String response = "";
        try {
            // sending the student Id to Server
            out.write(Integer.toString(sid));
            out.newLine(); // in.readLine() on server side
            out.flush(); // "flushes" (sends) the data immediately
            // return the response from the Server
            response = in.readLine();
        } catch(Exception e) {
            Log.e("Error", "Exception: " + e + " thrown, when trying to send a message.");
        }
        return response;
    }

    // swaps every second digit of the studentId to a corresponding letter
    public String changeStudentId(int sid) {
        StringBuilder result = new StringBuilder();
        int numDigits = 8;
        int[] digits = new int[numDigits];

        // filling array
        for(int i = 0; i < numDigits; i++) {
            digits[numDigits-1-i] = sid % 10; // getting last digit
            sid /= 10; // "cutting off" last digit
        }

        // changing studentId
        for(int i = 0; i < numDigits; i++) {
            if(i % 2 == 1) {
                result.append("").append((char) (digits[i] + 97));
            } else {
                result.append("").append(digits[i]);
            }
        }

        return result.toString();
    }

    // closes the connection
    public void stopConnection() {
        try {
            in.close();
            out.close();
            client.close();
        } catch (Exception e) {
            Log.e("Error", "Exception: " + e + " thrown, when trying to stop the connection.");
        }
        Log.i("Info", "Connection closed successfully.");
    }
}
