package com.polarscoop.driver.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.polarscoop.driver.R;
import com.polarscoop.driver.database.DatabaseHelper;
import com.polarscoop.driver.models.Delivery;
import com.polarscoop.driver.models.OrderItem;

/**
 * OrderDetailsActivity – shows the full detail of a single delivery.
 *
 * Features:
 *  • Store name, order number and address prominently displayed.
 *  • All order items rendered as individual rows (inflated dynamically).
 *  • "Mark as Delivered" and "Mark as Failed" buttons at the bottom.
 *  • When "Mark as Failed" is tapped the reason input card slides in so the
 *    driver can optionally type a reason before confirming.
 *  • Once a status is set the action buttons are hidden and a confirmation
 *    banner is shown in their place so the driver can't double-update.
 */
public class OrderDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_DELIVERY_ID = "extra_delivery_id";

    // Flavor dot colours – cycled through for each item row
    private static final int[] FLAVOR_COLORS = {
            0xFF1565C0,   // Blue
            0xFF6A1B9A,   // Purple
            0xFFAD1457,   // Pink
            0xFF00695C,   // Teal
            0xFFE65100,   // Deep Orange
            0xFF37474F,   // Blue Grey
    };

    private DatabaseHelper db;
    private Delivery       delivery;

    // Views
    private TextView      tvStoreName;
    private TextView      tvStoreId;
    private TextView      tvAddress;
    private TextView      tvToolbarStatus;
    private TextView      tvTotalItems;
    private LinearLayout  layoutItemsContainer;
    private LinearLayout  layoutNotes;
    private TextView      tvNotes;

    // Reason card (shown when driver taps "Mark as Failed")
    private View          cardFailedReason;
    private EditText      etFailedReason;
    private Button        btnConfirmFailed;

    // Bottom action area
    private LinearLayout  layoutActionButtons;
    private Button        btnMarkDelivered;
    private Button        btnMarkFailed;

    // Post-update banner
    private LinearLayout  layoutAlreadyUpdated;
    private TextView      tvStatusMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        // Bind all views
        bindViews();

        // Load the delivery from the database
        db = DatabaseHelper.getInstance(this);
        int deliveryId = getIntent().getIntExtra(EXTRA_DELIVERY_ID, -1);
        delivery = db.getDeliveryById(deliveryId);

        if (delivery == null) {
            Toast.makeText(this, "Delivery not found.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Populate the UI
        populateStoreInfo();
        populateOrderItems();
        applyStatusUI();

        // Button listeners
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnMarkDelivered.setOnClickListener(v -> confirmMarkDelivered());

        btnMarkFailed.setOnClickListener(v -> showFailedReasonInput());

        btnConfirmFailed.setOnClickListener(v -> confirmMarkFailed());
    }

    // ── View binding ──────────────────────────────────────────────────────────

    private void bindViews() {
        tvStoreName          = findViewById(R.id.tvStoreName);
        tvStoreId            = findViewById(R.id.tvStoreId);
        tvAddress            = findViewById(R.id.tvAddress);
        tvToolbarStatus      = findViewById(R.id.tvToolbarStatus);
        tvTotalItems         = findViewById(R.id.tvTotalItems);
        layoutItemsContainer = findViewById(R.id.layoutItemsContainer);
        layoutNotes          = findViewById(R.id.layoutNotes);
        tvNotes              = findViewById(R.id.tvNotes);

        cardFailedReason     = findViewById(R.id.cardFailedReason);
        etFailedReason       = findViewById(R.id.etFailedReason);
        btnConfirmFailed      = findViewById(R.id.btnConfirmFailed);

        layoutActionButtons  = findViewById(R.id.layoutActionButtons);
        btnMarkDelivered     = findViewById(R.id.btnMarkDelivered);
        btnMarkFailed        = findViewById(R.id.btnMarkFailed);

        layoutAlreadyUpdated = findViewById(R.id.layoutAlreadyUpdated);
        tvStatusMessage      = findViewById(R.id.tvStatusMessage);
    }

    // ── UI population ─────────────────────────────────────────────────────────

    private void populateStoreInfo() {
        tvStoreName.setText(delivery.getStoreName());
        tvStoreId.setText("Order #" + String.format("%03d", delivery.getId()));
        tvAddress.setText(delivery.getAddress());

        // Show failed reason as a note if already failed
        if (Delivery.STATUS_FAILED.equals(delivery.getStatus())
                && delivery.getFailedReason() != null
                && !delivery.getFailedReason().isEmpty()) {
            tvNotes.setText("Reason: " + delivery.getFailedReason());
            layoutNotes.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Dynamically inflates one item_order_item row per OrderItem and adds it
     * to the vertical container inside the "Order Items" card.
     */
    private void populateOrderItems() {
        layoutItemsContainer.removeAllViews();

        java.util.List<OrderItem> items = delivery.getItems();
        int colorIndex = 0;

        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get(i);

            // Inflate the row layout
            View row = LayoutInflater.from(this)
                    .inflate(R.layout.item_order_item, layoutItemsContainer, false);

            // Colour dot – cycles through FLAVOR_COLORS
            View dot = row.findViewById(R.id.viewFlavorDot);
            dot.getBackground().setTint(FLAVOR_COLORS[colorIndex % FLAVOR_COLORS.length]);
            colorIndex++;

            // Item name and quantity
            TextView tvItemName = row.findViewById(R.id.tvItemName);
            TextView tvQuantity = row.findViewById(R.id.tvQuantity);
            tvItemName.setText(item.getDisplayName());
            tvQuantity.setText(item.getQuantity() + "x");

            layoutItemsContainer.addView(row);

            // Add a thin divider between rows (not after the last one)
            if (i < items.size() - 1) {
                View divider = new View(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
                lp.setMargins(16, 0, 16, 0);
                divider.setLayoutParams(lp);
                divider.setBackgroundColor(
                        getResources().getColor(R.color.divider, null));
                layoutItemsContainer.addView(divider);
            }
        }

        // Update total tubs count in the header
        int total = delivery.getTotalTubs();
        tvTotalItems.setText(total + " tub" + (total != 1 ? "s" : ""));
    }

    /**
     * Shows or hides the action buttons vs the completion banner based on the
     * delivery's current status, and applies the correct badge style to the toolbar.
     */
    private void applyStatusUI() {
        String status = delivery.getStatus();
        tvToolbarStatus.setText(status);

        if (Delivery.STATUS_PENDING.equals(status)) {
            // Still pending – show the action buttons
            layoutActionButtons.setVisibility(View.VISIBLE);
            layoutAlreadyUpdated.setVisibility(View.GONE);
            tvToolbarStatus.setBackgroundResource(R.drawable.badge_pending_white);
            tvToolbarStatus.setTextColor(Color.WHITE);
        } else {
            // Already actioned – hide buttons, show banner
            layoutActionButtons.setVisibility(View.GONE);
            cardFailedReason.setVisibility(View.GONE);
            layoutAlreadyUpdated.setVisibility(View.VISIBLE);

            if (Delivery.STATUS_DELIVERED.equals(status)) {
                tvStatusMessage.setText("✓ This delivery has been marked as DELIVERED");
                tvStatusMessage.setTextColor(
                        getResources().getColor(R.color.status_delivered, null));
                tvToolbarStatus.setTextColor(
                        getResources().getColor(R.color.status_delivered, null));
                tvToolbarStatus.setBackgroundResource(R.drawable.badge_delivered);
            } else {
                tvStatusMessage.setText("✕ This delivery has been marked as FAILED");
                tvStatusMessage.setTextColor(
                        getResources().getColor(R.color.status_failed, null));
                tvToolbarStatus.setTextColor(
                        getResources().getColor(R.color.status_failed, null));
                tvToolbarStatus.setBackgroundResource(R.drawable.badge_failed);
            }
        }
    }

    // ── Status update logic ───────────────────────────────────────────────────

    /** Shows a confirmation dialog before marking as delivered. */
    private void confirmMarkDelivered() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Delivery")
                .setMessage("Mark delivery to " + delivery.getStoreName()
                        + " as DELIVERED?")
                .setPositiveButton("Yes, Delivered", (dialog, which) -> {
                    saveStatus(Delivery.STATUS_DELIVERED, null);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Reveals the failed-reason input card so the driver can (optionally)
     * type a short reason before confirming.
     */
    private void showFailedReasonInput() {
        cardFailedReason.setVisibility(View.VISIBLE);
        // Scroll down so the reason card is visible
        cardFailedReason.requestFocus();
    }

    /** Reads the reason (if any) and saves the FAILED status. */
    private void confirmMarkFailed() {
        String reason = etFailedReason.getText().toString().trim();

        // If the driver left reason blank use a default message
        if (reason.isEmpty()) reason = "No reason provided";

        saveStatus(Delivery.STATUS_FAILED, reason);
    }

    /**
     * Persists the new status to SQLite then refreshes the UI to show the
     * completion banner.  The DeliveryListActivity will pick up the change
     * via onResume when the driver presses Back.
     */
    private void saveStatus(String newStatus, String failedReason) {
        int rowsUpdated = db.updateDeliveryStatus(
                delivery.getId(), newStatus, failedReason);

        if (rowsUpdated > 0) {
            // Update the in-memory object so the UI reflects the change
            delivery.setStatus(newStatus);
            delivery.setFailedReason(failedReason);

            // If there is a reason show it in the notes section
            if (failedReason != null && !failedReason.isEmpty()) {
                tvNotes.setText("Reason: " + failedReason);
                layoutNotes.setVisibility(View.VISIBLE);
            }

            applyStatusUI();

            String msg = Delivery.STATUS_DELIVERED.equals(newStatus)
                    ? "✓ Marked as Delivered!"
                    : "✕ Marked as Failed.";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this,
                    "Failed to update status. Please try again.",
                    Toast.LENGTH_LONG).show();
        }
    }
}
