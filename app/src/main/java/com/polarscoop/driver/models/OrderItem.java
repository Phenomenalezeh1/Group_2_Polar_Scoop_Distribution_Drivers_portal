package com.polarscoop.driver.models;

import java.io.Serializable;

/**
 * Represents a single line-item within a delivery order.
 * e.g. "5x Vanilla Bean Tubs"
 */
public class OrderItem implements Serializable {

    private int    id;
    private int    deliveryId;   // FK – which delivery this item belongs to
    private String flavorName;   // e.g. "Vanilla Bean"
    private int    quantity;     // e.g. 5

    // ── Constructors ──────────────────────────────────────────────────────────

    public OrderItem() {}

    public OrderItem(int id, int deliveryId, String flavorName, int quantity) {
        this.id         = id;
        this.deliveryId = deliveryId;
        this.flavorName = flavorName;
        this.quantity   = quantity;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int    getId()                        { return id; }
    public void   setId(int id)                  { this.id = id; }

    public int    getDeliveryId()                { return deliveryId; }
    public void   setDeliveryId(int deliveryId)  { this.deliveryId = deliveryId; }

    public String getFlavorName()                { return flavorName; }
    public void   setFlavorName(String name)     { this.flavorName = name; }

    public int    getQuantity()                  { return quantity; }
    public void   setQuantity(int quantity)      { this.quantity = quantity; }

    /** Human-readable label used in the UI, e.g. "Vanilla Bean Tubs" */
    public String getDisplayName() {
        return flavorName + " Tubs";
    }
}
