package com.example.distributed_gambling_platform;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import shared.Request;

public class GameActivity extends AppCompatActivity {

    double balance, bettingAmount;
    double amountWon;
    TextView textViewUsername, gameName, slot1, slot2, slot3;
    Button btnBalance, btnSpin, btnBet;
    ImageView imageViewGame;

    AlertDialog balanceDialog, betDialog;

    final String[] symbols = {"🍒", "🍋", "⭐", "🔔", "🍇"};

    final Handler handler = new Handler(Looper.getMainLooper());

    private Request sendToMaster(Request request) {
        try (
                Socket client = new Socket("10.0.2.2", 5001);
                ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
        ) {
            oos.flush();
            oos.writeObject(request);
            oos.flush();
            return (Request) ois.readObject();

        } catch (IOException | ClassNotFoundException e) {
            runOnUiThread(()->
                    Toast.makeText(GameActivity.this, "[FAIL] Could not communicate with Master", Toast.LENGTH_SHORT).show()
            );
            return null;
        }
    }

    String getRandomSymbol() {
        return symbols[(int) (Math.random() * symbols.length)];
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Create appearance from previous screen

        Intent i = getIntent();

        textViewUsername = (TextView) findViewById(R.id.textUsernameGame);
        textViewUsername.setText(i.getStringExtra("username"));

        btnBalance = (Button) findViewById(R.id.btnBalanceGame);
        btnBet = (Button) findViewById(R.id.btnBet);
        btnBalance.setText(String.format("$%.2f", i.getDoubleExtra("balance", 0.0)));
        balance = i.getDoubleExtra("balance", 0.0);

        imageViewGame = (ImageView) findViewById(R.id.imageViewGame);
        imageViewGame.setImageBitmap(ImageVault.getImageBm());

        gameName = (TextView) findViewById(R.id.textViewGameName);
        gameName.setText(i.getStringExtra("gameName"));

        slot1 = (TextView) findViewById(R.id.slot1);
        slot2 = (TextView) findViewById(R.id.slot2);
        slot3 = (TextView) findViewById(R.id.slot3);
        btnSpin = (Button) findViewById(R.id.btnSpin);

        btnBet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
                builder.setTitle("Add betting amount");

                EditText input = new EditText(GameActivity.this);
                input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                input.setHint("Enter amount");
                builder.setView(input);


                builder.setPositiveButton("Confirm", (dialog, which) -> {
                    String bettingAmountStr = input.getText().toString();
                    if (!bettingAmountStr.isBlank()) {
                        bettingAmount = Double.parseDouble(bettingAmountStr);
                        if (bettingAmount <= balance) {
                            btnBet.setText(String.format("$%.2f", bettingAmount));
                        } else {
                            bettingAmount = 0.0;
                            Toast.makeText(GameActivity.this, "Not enough balance to bet", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

                if (!isFinishing()) {
                    betDialog = builder.create();
                    betDialog.show();
                }
            }
        });

        btnSpin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSpin.setEnabled(false);
                btnSpin.setText("SPINNING...");

                //Create Request & send to Master
                Request request = new Request(Request.Type.PLAY);
                request.put("gameName", gameName.getText().toString());

                request.put("bettingAmount", bettingAmount);

                request.put("playerId", textViewUsername.getText().toString());

                new Thread(() -> {
                    //Receive response from Master
                    Request response = sendToMaster(request);

                    String status = (String) response.get("status");

                    if (!"OK".equals(status)) {
                        runOnUiThread(()-> {
                                Toast.makeText(GameActivity.this, "[FAIL] " + response.get("message"), Toast.LENGTH_SHORT).show();
                                btnSpin.setEnabled(true);
                                btnSpin.setText("SPIN");
                            }
                        );
                        return;
                    }

                    amountWon = (Double) response.get("amountWon");

                    balance += amountWon - bettingAmount;
                    btnBalance.setText(String.format("$%.2f", balance));
                    btnBet.setText("0.0");

                    final long totalDuration = 2000L;
                    final long tickInterval = 80L;
                    final long[] elapsed = {0L};

                    Runnable ticker = new Runnable() {
                        @Override
                        public void run() {

                            slot1.setText(getRandomSymbol());
                            slot2.setText(getRandomSymbol());
                            slot3.setText(getRandomSymbol());

                            elapsed[0] += tickInterval;

                            if (elapsed[0] < totalDuration) {
                                handler.postDelayed(this, tickInterval);
                            } else {
                                String finalSymbol1 = getRandomSymbol();
                                String finalSymbol2 = getRandomSymbol();
                                String finalSymbol3 = getRandomSymbol();

                                slot1.setText(finalSymbol1);
                                handler.postDelayed(() -> {
                                    slot2.setText(finalSymbol2);
                                    handler.postDelayed(() -> {
                                        slot3.setText(finalSymbol3);
                                        btnSpin.setEnabled(true);
                                        btnSpin.setText("SPIN");
                                    }, 200);
                                }, 200);
                            }
                        }
                    };

                    handler.post(ticker);
                }).start();
            }
        });

        btnBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
                builder.setTitle("Add balance");

                EditText input = new EditText(GameActivity.this);
                input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                input.setHint("Enter amount");
                builder.setView(input);


                builder.setPositiveButton("Confirm", (dialog, which) -> {
                    String balanceStr = input.getText().toString();
                    if (!balanceStr.isBlank()) {
                        balance += Double.parseDouble(balanceStr);
                        btnBalance.setText(String.format("$%.2f", balance));
                    }
                });

                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

                if (!isFinishing()) {
                    balanceDialog = builder.create();
                    balanceDialog.show();
                }
            }
        });
    }
}