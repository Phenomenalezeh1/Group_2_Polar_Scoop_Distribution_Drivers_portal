package com.polarscoop.driver.models;

import java.io.Serializable;
import java.util.List;

/**
 * Delivery model representing a single delivery assignment for the driver.
 * Implements Serializable so it can be passed between Activities via Intent.
 */
public class Delivery implements Serializable {

    // Delivery statuses
    public static final String STATUS_PENDING   = "PENDING";
    public static final String STATUS_DELIVERED = "DELIVERED";
    public static final String STATUS_FAILED    = "FAILED";

    private int id;
    private String storeName;
    private String address;
    private String status;
    private String failedReason;   // Optional – populated when status = FAILED
    private List<OrderItem> items;

    // ── Constructors ──────────────────────────────────────────────────────────

    public Delivery() {}

    public Delivery(int id, String storeName, String address, List<OrderItem> items) {
        this.id        = id;
        this.storeName = storeName;
        this.address   = address;
        this.status    = STATUS_PENDING;
        this.items     = items;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int getId()                        { return id; }
    public void setId(int id)                 { this.id = id; }

    public String getStoreName()              { return storeName; }
    public void setStoreName(String name)     { this.storeName = name; }

    public String getAddress()                { return address; }
    public void setAddress(String address)    { this.address = address; }

    public String getStatus()                 { return status; }
    public void setStatus(String status)      { this.status = status; }

    public String getFailedReason()           { return failedReason; }
    public void setFailedReason(String r)     { this.failedReason = r; }

    public List<OrderItem> getItems()         { return items; }
    public void setItems(List<OrderItem> i)   { this.items = i; }

    // ── Helper methods ────────────────────────────────────────────────────────

    /** Returns true only when this delivery is still awaiting action. */
    public boolean isPending() {
        return STATUS_PENDING.equals(status);
    }

    /**
     * Counts the total number of individual tubs (sum of all item quantities)
     * so the list row can show "12 tubs" without loading the full item list.
     */
    public int getTotalTubs() {
        if (items == null) return 0;
        int total = 0;
        for (OrderItem item : items) {
            total += item.getQuantity();
        }
        return total;
    }

    /**
     * Returns the first character of the store name (upper-case) for use
     * as an avatar letter inside the coloured circle on each card.
     */
    public String getStoreInitial() {
        if (storeName == null || storeName.isEmpty()) return "?";
        return String.valueOf(storeName.charAt(0)).toUpperCase();
    }
}
