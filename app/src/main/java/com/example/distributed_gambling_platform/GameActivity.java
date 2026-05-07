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
import java.util.Random;

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

    private void updateBalance(double newBalance) {
        balance = newBalance;
        btnBalance.setText(String.format("$%.2f", balance));
    }

    private void setActivityElements(Intent i) {
        textViewUsername = (TextView) findViewById(R.id.textUsernameGame);
        textViewUsername.setText(i.getStringExtra("USERNAME"));

        btnBalance = (Button) findViewById(R.id.btnBalanceGame);
        btnBet = (Button) findViewById(R.id.btnBet);

        // Update balance
        updateBalance(i.getDoubleExtra("BALANCE", 0.0));

        imageViewGame = (ImageView) findViewById(R.id.imageViewGame);
        imageViewGame.setImageBitmap(ImageVault.getImageBm());

        gameName = (TextView) findViewById(R.id.textViewGameName);
        gameName.setText(i.getStringExtra("SELECTED_GAME"));

        slot1 = (TextView) findViewById(R.id.slot1);
        slot2 = (TextView) findViewById(R.id.slot2);
        slot3 = (TextView) findViewById(R.id.slot3);
        btnSpin = (Button) findViewById(R.id.btnSpin);
    }

    private void showBetDialog() {
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

    private void showPlayResult() {
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
    }

    private void showBalanceDialog() {
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
        setActivityElements(getIntent());

        btnBet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBetDialog();
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

                                String winStatus = (String) response.get("winStatus");

                                String symbol1;
                                String symbol2;
                                String symbol3;

                                if (winStatus.equals("JACKPOT")) {
                                    symbol1 = symbol2 = symbol3= getRandomSymbol();
                                } else if (winStatus.equals("WIN")) {
                                    Random r = new Random();
                                    int randomInt = r.nextInt(3);

                                    switch (randomInt) {
                                        case 0:
                                            symbol1 = symbol2 = getRandomSymbol();

                                            do {
                                                symbol3 = getRandomSymbol();
                                            } while (symbol3.equals(symbol1));

                                            break;

                                        case 1:
                                            symbol1 = symbol3 = getRandomSymbol();

                                            do {
                                                symbol2 = getRandomSymbol();
                                            } while (symbol2.equals(symbol1));

                                            break;

                                        default:
                                            symbol2 = symbol3 = getRandomSymbol();

                                            do {
                                                symbol1 = getRandomSymbol();
                                            } while (symbol1.equals(symbol2));

                                            break;
                                    }

                                } else {
                                    symbol1 = getRandomSymbol();
                                    symbol2 = getRandomSymbol();
                                    symbol3 = getRandomSymbol();

                                    while (symbol1.equals(symbol2) || symbol1.equals(symbol3) || symbol2.equals(symbol3)) {
                                        symbol1 = getRandomSymbol();
                                        symbol2 = getRandomSymbol();
                                        symbol3 = getRandomSymbol();
                                    }
                                }

                                final String finalSymbol1 = symbol1;
                                final String finalSymbol2 = symbol2;
                                final String finalSymbol3 = symbol3;

                                slot1.setText(finalSymbol1);

                                handler.postDelayed(() -> {
                                    slot2.setText(finalSymbol2);

                                    handler.postDelayed(() -> {
                                        slot3.setText(finalSymbol3);

                                        btnSpin.setEnabled(true);

                                        btnSpin.setText("SPIN");
                                    }, 200);
                                }, 200);

                                showPlayResult();

                                updateBalance(balance + amountWon - bettingAmount);

                                // Clear bet
                                bettingAmount = 0.0;
                                btnBet.setText("0.0");
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
                showBalanceDialog();
            }
        });
    }
}