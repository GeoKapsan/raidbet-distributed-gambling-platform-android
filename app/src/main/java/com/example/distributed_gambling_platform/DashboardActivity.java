package com.example.distributed_gambling_platform;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;

import shared.GameSearch;
import shared.Request;

public class DashboardActivity extends AppCompatActivity {

    float balance = 0f;

    TextView tvDashboardUsername, tvAvatar, tvDashboardBalance;
    ListView listGames;

    MaterialButton btnSearch, btnAddBalance;

    private final ActivityResultLauncher<Intent> gameLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                    float updatedBalance = result.getData().getFloatExtra("NEW_BALANCE", 0f);

                    updateBalance(updatedBalance);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bindViews(getIntent());
        setupButtons();
    }

    private void bindViews(Intent i) {
        tvDashboardUsername = findViewById(R.id.tvDashboardUsername);
        tvDashboardUsername.setText(i.getStringExtra("USERNAME"));

        tvAvatar = findViewById(R.id.tvAvatar);
        tvAvatar.setText(String.valueOf(tvDashboardUsername.getText().toString().charAt(0)));

        btnAddBalance = findViewById(R.id.btnAddBalance);
        tvDashboardBalance = findViewById(R.id.tvDashboardBalance);
        updateBalance(100f);

        btnSearch = findViewById(R.id.btnSearch);

        listGames = findViewById(R.id.listGames);
    }

    private void setupButtons() {
        btnSearch.setOnClickListener(v -> showFilterDialog());

        btnAddBalance.setOnClickListener(v -> showBalanceDialog());
    }

    private void showBalanceDialog() {
        AddBalanceDialog balanceDialog = new AddBalanceDialog(DashboardActivity.this, balance,
                newBalance -> {
                    updateBalance(newBalance);
                });
        balanceDialog.show();
    }

    private void showFilterDialog() {
        GameFilterDialog filterDialog = new GameFilterDialog(DashboardActivity.this,
                (risk, cat, stars) -> {
                    showSearchResults(risk, cat, stars);
                });
        filterDialog.show();
    }

    private void showSearchResults(String risk, String cat, int stars) {

        //Create Request
        Request request = new Request(Request.Type.SEARCH);

        if (!risk.equals("any")) request.put("riskLevel", risk);
        if (!cat.equals("any")) request.put("bettingCategory", cat);
        if (stars != 0) request.put("stars", stars);

        new Thread(() -> {

            // Send Request to Master
            Request response = sendToMaster(request);

            // HashMap<String, String> games = (HashMap<String, String>) response.get("games");
            ArrayList<GameSearch> games = (ArrayList<GameSearch>) response.get("result");

            if (!searchResultIsValid(games)) return;

            ArrayList<ListItem> listItems = createListFromSearchResult(games);

            runOnUiThread(() -> {
                ListAdapter adapter = new ListAdapter(listItems, DashboardActivity.this, new ListAdapter.ListItemActionListener() {
                    @Override
                    public void onPlayClick(ListItem item) {
                        // Save image to shared object
                        ImageVault.setImageBm(item.image);

                        Intent i = new Intent(getApplicationContext(), GameActivity.class);

                        i.putExtra("USERNAME", tvDashboardUsername.getText().toString());
                        i.putExtra("SELECTED_GAME", item.text);
                        i.putExtra("MINBET", item.minBet);
                        i.putExtra("MAXBET", item.maxBet);
                        i.putExtra("JACKPOT", item.jackpot);
                        i.putExtra("BALANCE", balance);

                        gameLauncher.launch(i);
                    }

                    @Override
                    public void onRateClick(ListItem item) {
                        showRateDialog(item);
                    }
                });

                listGames.setAdapter(adapter);
            });
        }).start();
    }

    private void showRateDialog(ListItem item) {
        RateGameDialog rateDialog = new RateGameDialog(DashboardActivity.this, item.text,
                (stars) -> {
                    // Create request
                    Request request = new Request(Request.Type.RATE_GAME);
                    request.put("stars", stars);
                    request.put("gameName", item.text);
                    request.put("playerId", tvDashboardUsername.getText().toString());

                    new Thread(() -> {
                        // Send & Receive response
                        Request response = sendToMaster(request);

                        runOnUiThread(() -> {
                            if (!"OK".equals(response.get("status"))) {
                                Toast.makeText(DashboardActivity.this, "[FAIL] " + response.get("message"), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(DashboardActivity.this, item.text + " rated successfully.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).start();
                });
        rateDialog.show();
    }

    private void updateBalance(float newBalance) {
        balance = newBalance;
        tvDashboardBalance.setText("$" + formatAmount(balance));
    }

    private boolean searchResultIsValid(ArrayList<GameSearch> games) {
        if (games == null || games.isEmpty()) {
            runOnUiThread(() ->
                    Toast.makeText(DashboardActivity.this, "[FAIL] Workers don't have any games", Toast.LENGTH_SHORT).show()
            );
            return false;
        }
        return true;
    }

    private ArrayList<ListItem> createListFromSearchResult(ArrayList<GameSearch> games) {
        ArrayList<ListItem> listItems = new ArrayList<ListItem>();

        String gameName;
        float minBet;
        float maxBet;
        String riskLevel;
        String bettingCategory;
        double stars;
        float jackpot;

        for (GameSearch game : games) {

            // TODO Decode image to bitmap

            byte[] imageInBytes = game.getImg();
            Bitmap imageBm = BitmapFactory.decodeByteArray(imageInBytes, 0, imageInBytes.length);

            gameName = game.getGameName();
            minBet = game.getMinBet();
            maxBet = game.getMaxBet();
            riskLevel = game.getRiskLevel();
            bettingCategory = game.getBettingCategory();
            stars = game.getStars();
            jackpot = game.getJackpot();

            ListItem li = new ListItem(gameName, minBet, maxBet, riskLevel, bettingCategory, stars, jackpot, imageBm);
            listItems.add(li);
        }

        return listItems;
    }

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
                    Toast.makeText(DashboardActivity.this, "[FAIL] Could not communicate with Master", Toast.LENGTH_SHORT).show()
            );
            return null;
        }
    }

    private String formatAmount(float amount) {
        if (amount == Math.floor(amount)) {
            return Integer.toString((int) amount);
        }
        return String.format("%.2f", amount);
    }
}