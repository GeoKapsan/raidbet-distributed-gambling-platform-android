package com.example.distributed_gambling_platform;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

import shared.Request;

public class DashboardActivity extends AppCompatActivity {

    double balance = 0;

    TextView textViewUsername;
    ListView listView;

    Button btnSearch, btnBalance;

    AlertDialog filterDialog, balanceDialog;

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


    @Override
    protected void onDestroy() {
        super.onDestroy();

        // This intercepts the Activity's death and safely closes the dialog first!
        if (filterDialog != null && filterDialog.isShowing()) {
            filterDialog.dismiss();
        }
    }

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

        Intent i = getIntent();

        textViewUsername = findViewById(R.id.textUsername);
        btnBalance = (Button) findViewById(R.id.btnBalance);
        listView = (ListView) findViewById(R.id.listView);
        btnSearch = (Button) findViewById(R.id.btnSearch);

        //set username from login
        textViewUsername.setText(i.getStringExtra("username"));

        //set balance
        btnBalance.setText(Double.toString(balance));

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);
                builder.setTitle("Filter Games");

                View customLayout = LayoutInflater.from(DashboardActivity.this).inflate(R.layout.dialog_filter, null);
                builder.setView(customLayout);

                Spinner spinnerRisk = customLayout.findViewById(R.id.spinnerRiskLevel);
                Spinner spinnerBetting = customLayout.findViewById(R.id.spinnerBettingCategory);
                RatingBar ratingBarStars = customLayout.findViewById(R.id.ratingBarStars);

                String[] riskOptions = {"Any", "Low", "Medium", "High"};
                spinnerRisk.setAdapter(new ArrayAdapter<>(DashboardActivity.this, android.R.layout.simple_spinner_dropdown_item, riskOptions));

                String[] betOptions = {"Any", "$", "$$", "$$$"};
                spinnerBetting.setAdapter(new ArrayAdapter<>(DashboardActivity.this, android.R.layout.simple_spinner_dropdown_item, betOptions));

                builder.setPositiveButton("Apply Filter", (dialog, which) -> {
                    String risk = spinnerRisk.getSelectedItem().toString();
                    String cat = spinnerBetting.getSelectedItem().toString();
                    int stars = (int) ratingBarStars.getRating();

                    //Create Request
                    Request request = new Request(Request.Type.SEARCH);

                    if (!risk.equals("Any")) request.put("riskLevel", risk);
                    if (!cat.equals("Any")) request.put("bettingCategory", cat);
                    if (stars != 0) request.put("stars", stars);

                    new Thread(()-> {

                        Request response = sendToMaster(request);

                        // HashMap<String, String> games = (HashMap<String, String>) response.get("games");
                        ArrayList<String> games = (ArrayList<String>) response.get("result");

                        if (games == null || games.isEmpty()) {
                            runOnUiThread(()->
                                    Toast.makeText(DashboardActivity.this, "[FAIL] Workers don't have any games", Toast.LENGTH_SHORT).show()
                            );
                            return;
                        }

                        ArrayList<ListItem> listItems = new ArrayList<ListItem>();
                        /*
                        for (String gameName : games.keySet()) {

                            //Decode image to bitmap

                            byte[] imageInBytes = Base64.getDecoder().decode("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");
                            Bitmap imageBm = BitmapFactory.decodeByteArray(imageInBytes, 0, imageInBytes.length);

                            ListItem li = new ListItem(gameName, imageBm);
                            listItems.add(li);
                        }
                         */

                        for (String gameName : games) {

                            //Decode image to bitmap

                            byte[] imageInBytes = Base64.getDecoder().decode("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");
                            Bitmap imageBm = BitmapFactory.decodeByteArray(imageInBytes, 0, imageInBytes.length);

                            ListItem li = new ListItem(gameName, imageBm);
                            listItems.add(li);
                        }

                        runOnUiThread(()-> {
                            listView.setAdapter(new ListAdapter(listItems, DashboardActivity.this));

                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Intent i = new Intent(getApplicationContext(), GameActivity.class);

                                    i.putExtra("username", textViewUsername.getText().toString());
                                    ImageVault.setImageBm(listItems.get(position).image);
                                    i.putExtra("gameName", listItems.get(position).text);
                                    i.putExtra("balance", balance);
                                    startActivity(i);
                                }
                            });
                        });
                    }).start();
                });

                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

                if (!isFinishing()) {
                    filterDialog = builder.create();
                    filterDialog.show();
                }
            }
        });

        btnBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);
                builder.setTitle("Add balance");

                EditText input = new EditText(DashboardActivity.this);
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