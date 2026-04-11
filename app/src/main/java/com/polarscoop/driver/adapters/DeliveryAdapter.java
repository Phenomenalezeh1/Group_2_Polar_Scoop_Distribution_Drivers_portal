package com.polarscoop.driver.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.polarscoop.driver.R;
import com.polarscoop.driver.models.Delivery;

import java.util.List;


public class DeliveryAdapter extends RecyclerView.Adapter<DeliveryAdapter.DeliveryViewHolder> {

    /** Callback interface – the Activity implements this to handle card taps. */
    public interface OnDeliveryClickListener {
        void onDeliveryClick(Delivery delivery);
    }

    private final Context                context;
    private final List<Delivery>         deliveries;
    private final OnDeliveryClickListener clickListener;

    public DeliveryAdapter(Context context,
                           List<Delivery> deliveries,
                           OnDeliveryClickListener clickListener) {
        this.context       = context;
        this.deliveries    = deliveries;
        this.clickListener = clickListener;
    }

    // ── RecyclerView.Adapter overrides ────────────────────────────────────────

    @NonNull
    @Override
    public DeliveryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_delivery, parent, false);
        return new DeliveryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeliveryViewHolder holder, int position) {
        Delivery delivery = deliveries.get(position);
        holder.bind(delivery);
    }

    @Override
    public int getItemCount() {
        return deliveries.size();
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────

    class DeliveryViewHolder extends RecyclerView.ViewHolder {

        private final View     viewStatusBar;
        private final TextView tvStoreInitial;
        private final TextView tvStoreName;
        private final TextView tvAddress;
        private final TextView tvItemCount;
        private final TextView tvStatus;

        DeliveryViewHolder(@NonNull View itemView) {
            super(itemView);
            viewStatusBar  = itemView.findViewById(R.id.viewStatusBar);
            tvStoreInitial = itemView.findViewById(R.id.tvStoreInitial);
            tvStoreName    = itemView.findViewById(R.id.tvStoreName);
            tvAddress      = itemView.findViewById(R.id.tvAddress);
            tvItemCount    = itemView.findViewById(R.id.tvItemCount);
            tvStatus       = itemView.findViewById(R.id.tvStatus);
        }

        void bind(Delivery delivery) {
            // Basic text fields
            tvStoreInitial.setText(delivery.getStoreInitial());
            tvStoreName.setText(delivery.getStoreName());
            tvAddress.setText(delivery.getAddress());

            int totalTubs = delivery.getTotalTubs();
            tvItemCount.setText(totalTubs + " tub" + (totalTubs != 1 ? "s" : ""));

            // Apply status-dependent colours and labels
            applyStatusStyle(delivery.getStatus());

            // Click – forward to the Activity
            itemView.setOnClickListener(v -> clickListener.onDeliveryClick(delivery));
        }

        /**
         * Changes the top colour bar, badge text, badge text colour, and badge
         * background to match the current delivery status.
         */
        private void applyStatusStyle(String status) {
            switch (status) {
                case Delivery.STATUS_DELIVERED:
                    viewStatusBar.setBackgroundColor(
                            context.getResources().getColor(R.color.status_delivered, null));
                    tvStatus.setText(context.getString(R.string.status_delivered));
                    tvStatus.setTextColor(
                            context.getResources().getColor(R.color.status_delivered, null));
                    tvStatus.setBackgroundResource(R.drawable.badge_delivered);
                    break;

                case Delivery.STATUS_FAILED:
                    viewStatusBar.setBackgroundColor(
                            context.getResources().getColor(R.color.status_failed, null));
                    tvStatus.setText(context.getString(R.string.status_failed));
                    tvStatus.setTextColor(
                            context.getResources().getColor(R.color.status_failed, null));
                    tvStatus.setBackgroundResource(R.drawable.badge_failed);
                    break;

                default: // PENDING
                    viewStatusBar.setBackgroundColor(
                            context.getResources().getColor(R.color.status_pending, null));
                    tvStatus.setText(context.getString(R.string.status_pending));
                    tvStatus.setTextColor(
                            context.getResources().getColor(R.color.status_pending, null));
                    tvStatus.setBackgroundResource(R.drawable.badge_pending);
                    break;
            }
        }
    }
}
