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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

import shared.Request;

public class GameActivity extends AppCompatActivity {

    float balance, bettingAmount, minBet, maxBet;
    float amountWon;
    String winStatus;
    TextView tvGameUsername, tvGameBalance, tvGameName, tvJackpot, tvGameCurrentBet, tvSlot1, tvSlot2, tvSlot3;
    MaterialButton btnSpin, btnBet;
    ImageButton btnBackToDashboard;

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

    private void updateBalance(float newBalance) {
        balance = newBalance;
        tvGameBalance.setText("$" + formatAmount(balance));
    }

    private void bindViews(Intent i) {
        btnBackToDashboard = findViewById(R.id.btnBackToDashboard);

        tvGameUsername = findViewById(R.id.tvGameUsername);
        tvGameUsername.setText(i.getStringExtra("USERNAME"));

        tvGameName = findViewById(R.id.tvGameName);
        tvGameName.setText(i.getStringExtra("SELECTED_GAME"));

        tvGameBalance = findViewById(R.id.tvGameBalance);
        updateBalance(i.getFloatExtra("BALANCE", 0f));

        minBet = i.getFloatExtra("MINBET", 0f);
        maxBet = i.getFloatExtra("MAXBET", 0f);

        tvJackpot = findViewById(R.id.tvJackpot);
        tvJackpot.setText("  x" + formatAmount(i.getFloatExtra("JACKPOT", 0f)));

        tvGameCurrentBet = findViewById(R.id.tvGameCurrentBet);
        tvGameCurrentBet.setText("$0");

        tvSlot1 = findViewById(R.id.tvSlot1);
        tvSlot2 = findViewById(R.id.tvSlot2);
        tvSlot3 = findViewById(R.id.tvSlot3);

        btnSpin = findViewById(R.id.btnSpin);
        btnBet = findViewById(R.id.btnBet);
    }

    private void showBetDialog() {
        PlaceBetDialog betDialog = new PlaceBetDialog(GameActivity.this, balance, minBet, maxBet,
                bet -> {
            bettingAmount = bet;
            tvGameCurrentBet.setText("$" + formatAmount(bettingAmount));
        });
        betDialog.show();
    }

    private void showPlayResult() {

        GameResultDialog.ResultType resultType = GameResultDialog.ResultType.LOSE;
        GameResultDialog resultDialog;

        if (winStatus.equals("WIN")) {
            resultType = GameResultDialog.ResultType.WIN;
        } else if (winStatus.equals("JACKPOT")) {
            resultType = GameResultDialog.ResultType.JACKPOT;
        }

        resultDialog = new GameResultDialog(GameActivity.this, resultType, amountWon, bettingAmount, balance);
        resultDialog.show();
    }

    private void setupButtons() {
        btnBackToDashboard.setOnClickListener(v -> {
            onBackPressed();
        });

        btnBet.setOnClickListener(v -> {
            if (balance > 0f) {
                showBetDialog();
            } else {
                Toast.makeText(GameActivity.this, "Please add money to bet", Toast.LENGTH_SHORT).show();
            }
        });

        btnSpin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSpin.setEnabled(false);

                //Create Request & send to Master
                Request request = new Request(Request.Type.PLAY);
                request.put("gameName", tvGameName.getText().toString());
                request.put("bettingAmount", bettingAmount);
                request.put("playerId", tvGameUsername.getText().toString());

                new Thread(() -> {
                    //Receive response from Master
                    Request response = sendToMaster(request);

                    String status = (String) response.get("status");
                    winStatus = (String) response.get("winStatus");

                    if (!"OK".equals(status)) {
                        runOnUiThread(() -> {
                                    Toast.makeText(GameActivity.this, "[FAIL] " + response.get("message"), Toast.LENGTH_SHORT).show();
                                    btnSpin.setEnabled(true);
                                    btnSpin.setText("SPIN");
                                }
                        );
                        return;
                    }

                    btnSpin.setText("SPINNING...");

                    amountWon = (Float) response.get("amountWon");

                    final long totalDuration = 2000L;
                    final long tickInterval = 80L;
                    final long[] elapsed = {0L};

                    Runnable ticker = new Runnable() {
                        @Override
                        public void run() {

                            tvSlot1.setText(getRandomSymbol());
                            tvSlot2.setText(getRandomSymbol());
                            tvSlot3.setText(getRandomSymbol());

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

                                tvSlot1.setText(finalSymbol1);

                                handler.postDelayed(() -> {
                                    tvSlot2.setText(finalSymbol2);

                                    handler.postDelayed(() -> {
                                        tvSlot3.setText(finalSymbol3);

                                        btnSpin.setEnabled(true);

                                        btnSpin.setText("SPIN");
                                    }, 200);
                                }, 200);

                                updateBalance(balance + amountWon - bettingAmount);

                                showPlayResult();

                                // Clear bet
                                bettingAmount = balance > bettingAmount ? bettingAmount : 0f;
                                tvGameCurrentBet.setText("$" + formatAmount(bettingAmount));
                            }
                        }
                    };

                    handler.post(ticker);
                }).start();
            }
        });
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
        bindViews(getIntent());
        setupButtons();
    }

    private String formatAmount(float amount) {
        if (amount == Math.floor(amount)) {
            return Integer.toString((int) amount);
        }
        return String.format("%.2f", amount);
    }
}