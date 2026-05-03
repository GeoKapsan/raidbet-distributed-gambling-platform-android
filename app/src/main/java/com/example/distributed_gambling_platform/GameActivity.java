package com.example.distributed_gambling_platform;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GameActivity extends AppCompatActivity {

    TextView textViewUsername, gameName, slot1, slot2, slot3;
    Button btnBalance, btnSpin;
    ImageView imageViewGame;

    final String[] symbols = {"🍒", "🍋", "⭐", "🔔", "🍇"};

    final Handler handler = new Handler(Looper.getMainLooper());

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

        //create appearance from previous screen

        Intent i = getIntent();

        textViewUsername = (TextView) findViewById(R.id.textUsernameGame);
        textViewUsername.setText(i.getStringExtra("username"));

        btnBalance = (Button) findViewById(R.id.btnBalanceGame);
        btnBalance.setText(String.format("$%.2f", i.getDoubleExtra("balance", 0.0)));

        imageViewGame = (ImageView) findViewById(R.id.imageViewGame);
        imageViewGame.setImageBitmap(ImageVault.getImageBm());
        
        gameName = (TextView) findViewById(R.id.textViewGameName);
        gameName.setText(i.getStringExtra("gameName"));

        slot1 = (TextView) findViewById(R.id.slot1);
        slot2 = (TextView) findViewById(R.id.slot2);
        slot3 = (TextView) findViewById(R.id.slot3);
        btnSpin = (Button) findViewById(R.id.btnSpin);

        btnSpin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSpin.setEnabled(false);
                btnSpin.setText("SPINNING...");

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
            }
        });
    }
}