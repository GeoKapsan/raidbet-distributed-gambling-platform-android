package com.example.distributed_gambling_platform;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.LayoutInflater;
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

    AlertDialog balanceDialog, betDialog, resultDialog;

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
    public void onBackPressed() {
        Intent returnToDashboardIntent = new Intent();

        returnToDashboardIntent.putExtra("NEW_BALANCE", balance);

        setResult(RESULT_OK, returnToDashboardIntent);

        super.onBackPressed();
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

        // Create appearance from previous screen

        Intent i = getIntent();

        textViewUsername = (TextView) findViewById(R.id.textUsernameGame);
        textViewUsername.setText(i.getStringExtra("USERNAME"));

        btnBalance = (Button) findViewById(R.id.btnBalanceGame);
        btnBet = (Button) findViewById(R.id.btnBet);

        // Update balance
        balance = i.getDoubleExtra("BALANCE", 0.0);
        btnBalance.setText(String.format("$%.2f", balance));

        imageViewGame = (ImageView) findViewById(R.id.imageViewGame);
        imageViewGame.setImageBitmap(ImageVault.getImageBm());

        gameName = (TextView) findViewById(R.id.textViewGameName);
        gameName.setText(i.getStringExtra("SELECTED_GAME"));

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

                    btnSpin.setText("SPINNING...");

                    amountWon = (Double) response.get("amountWon");

                    balance += amountWon - bettingAmount;

                    bettingAmount = 0.0;
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

                                btnBalance.setText(String.format("$%.2f", balance));
                            }
                        }
                    };

                    handler.post(ticker);

                    AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);

                    View customView = LayoutInflater.from(GameActivity.this).inflate(R.layout.dialog_play_result, null);
                    builder.setView(customView);

                    TextView textViewResultTitle = (TextView) customView.findViewById(R.id.textViewResultTitle);
                    TextView textViewResultBody = (TextView) customView.findViewById(R.id.textViewResultBody);

                    if (amountWon == 0) {
                        textViewResultTitle.setText("You lose...");
                        textViewResultTitle.setTextColor(android.graphics.Color.parseColor("#F44336"));
                    } else {
                        textViewResultTitle.setText("You Win!");
                        textViewResultTitle.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
                    }

                    textViewResultBody.setText("Amount: " + String.format("$%.2f", amountWon));

                    builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                    runOnUiThread(() -> {
                        resultDialog = builder.create();

                        if (resultDialog.getWindow() != null) {
                            //resultDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                            int screenWidth = getResources().getDisplayMetrics().widthPixels;
                            int dialogWidth = (int) (screenWidth * 0.25);
                            resultDialog.getWindow().setLayout(dialogWidth, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
                        }

                        resultDialog.show();
                    });
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