package com.polarscoop.driver.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.polarscoop.driver.R;
import com.polarscoop.driver.adapters.DeliveryAdapter;
import com.polarscoop.driver.database.DatabaseHelper;
import com.polarscoop.driver.models.Delivery;

import java.util.List;

/**
 * DeliveryListActivity – the driver's dashboard.
 *
 * Displays all deliveries for the day in a scrollable RecyclerView.
 * Each card shows the store name, address, item count, and current status.
 * Tapping a card opens OrderDetailsActivity.
 *
 * The summary bar and progress indicator update automatically whenever the
 * driver returns from updating a delivery.
 */
public class DeliveryListActivity extends AppCompatActivity
        implements DeliveryAdapter.OnDeliveryClickListener {

    public static final String EXTRA_DRIVER_ID = "extra_driver_id";

    private RecyclerView       recyclerDeliveries;
    private DeliveryAdapter    adapter;
    private List<Delivery>     deliveries;
    private DatabaseHelper     db;

    private TextView           tvDriverName;
    private TextView           tvSummary;
    private ProgressBar        progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_list);

        // Retrieve driver ID passed from LoginActivity
        String driverId = getIntent().getStringExtra(EXTRA_DRIVER_ID);

        // Bind views
        tvDriverName       = findViewById(R.id.tvDriverName);
        tvSummary          = findViewById(R.id.tvSummary);
        progressBar        = findViewById(R.id.progressBar);
        recyclerDeliveries = findViewById(R.id.recyclerDeliveries);

        Button btnLogout = findViewById(R.id.btnLogout);

        // Show which driver is logged in
        tvDriverName.setText("Driver: " + (driverId != null ? driverId : "Unknown"));

        // Logout: confirm then return to login screen
        btnLogout.setOnClickListener(v -> confirmLogout());

        // Set up RecyclerView
        recyclerDeliveries.setLayoutManager(new LinearLayoutManager(this));

        // Load deliveries from SQLite
        db = DatabaseHelper.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list every time we return (e.g. after updating a status)
        loadDeliveries();
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    private void loadDeliveries() {
        deliveries = db.getAllDeliveries();

        adapter = new DeliveryAdapter(this, deliveries, this);
        recyclerDeliveries.setAdapter(adapter);

        updateSummary();
    }

    /**
     * Calculates how many deliveries are complete (DELIVERED or FAILED) and
     * updates the progress bar and summary text accordingly.
     */
    private void updateSummary() {
        int total    = deliveries.size();
        int complete = 0;

        for (Delivery d : deliveries) {
            if (!d.isPending()) complete++;
        }

        tvSummary.setText(complete + " of " + total + " Complete");

        // Progress bar: percentage of completed deliveries
        int progress = (total > 0) ? (complete * 100 / total) : 0;
        progressBar.setProgress(progress);
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    /**
     * Called when the driver taps a delivery card.
     * Passes the delivery's ID to OrderDetailsActivity.
     */
    @Override
    public void onDeliveryClick(Delivery delivery) {
        Intent intent = new Intent(this, OrderDetailsActivity.class);
        intent.putExtra(OrderDetailsActivity.EXTRA_DELIVERY_ID, delivery.getId());
        startActivity(intent);
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Prevent the driver from accidentally exiting the app with the back button.
     * We intercept it and show the logout confirmation instead.
     */
    @Override
    public void onBackPressed() {
        confirmLogout();
    }
}
