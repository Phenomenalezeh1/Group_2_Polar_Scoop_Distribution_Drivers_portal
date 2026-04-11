package com.polarscoop.driver.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.polarscoop.driver.models.Delivery;
import com.polarscoop.driver.models.OrderItem;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // ── DB meta ───────────────────────────────────────────────────────────────
    private static final String DB_NAME    = "polar_scoop.db";
    private static final int    DB_VERSION = 1;

    // ── Table: deliveries ─────────────────────────────────────────────────────
    private static final String TABLE_DELIVERIES   = "deliveries";
    private static final String COL_D_ID           = "id";
    private static final String COL_D_STORE_NAME   = "store_name";
    private static final String COL_D_ADDRESS      = "address";
    private static final String COL_D_STATUS       = "status";
    private static final String COL_D_FAILED_REASON= "failed_reason";

    // ── Table: order_items ────────────────────────────────────────────────────
    private static final String TABLE_ITEMS        = "order_items";
    private static final String COL_I_ID           = "id";
    private static final String COL_I_DELIVERY_ID  = "delivery_id";
    private static final String COL_I_FLAVOR_NAME  = "flavor_name";
    private static final String COL_I_QUANTITY     = "quantity";

    // ── Singleton ─────────────────────────────────────────────────────────────
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context ctx) {
        if (instance == null) {
            instance = new DatabaseHelper(ctx.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // ── onCreate / onUpgrade ──────────────────────────────────────────────────

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create deliveries table
        db.execSQL("CREATE TABLE " + TABLE_DELIVERIES + " (" +
                COL_D_ID            + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_D_STORE_NAME    + " TEXT NOT NULL, " +
                COL_D_ADDRESS       + " TEXT NOT NULL, " +
                COL_D_STATUS        + " TEXT NOT NULL DEFAULT 'PENDING', " +
                COL_D_FAILED_REASON + " TEXT" +
                ");");

        // Create order_items table
        db.execSQL("CREATE TABLE " + TABLE_ITEMS + " (" +
                COL_I_ID           + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_I_DELIVERY_ID  + " INTEGER NOT NULL, " +
                COL_I_FLAVOR_NAME  + " TEXT NOT NULL, " +
                COL_I_QUANTITY     + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + COL_I_DELIVERY_ID + ") REFERENCES " +
                TABLE_DELIVERIES + "(" + COL_D_ID + ")" +
                ");");

        // Seed mock data
        seedMockData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DELIVERIES);
        onCreate(db);
    }

    // ── Mock data seeding ─────────────────────────────────────────────────────

    /**
     * Inserts 6 mock stores with multiple order items each.
     * All deliveries start with status = PENDING.
     */
    private void seedMockData(SQLiteDatabase db) {

        // Store 1 – Sandy's Cafe
        long d1 = insertDelivery(db, "Sandy's Cafe",
                "12 Baker Street, Port Harcourt");
        insertItem(db, d1, "Vanilla Bean",    5);
        insertItem(db, d1, "Chocolate Fudge", 3);
        insertItem(db, d1, "Strawberry Swirl",2);

        // Store 2 – The Grand Hotel Kitchen
        long d2 = insertDelivery(db, "The Grand Hotel Kitchen",
                "45 Hotel Avenue, GRA Phase 2");
        insertItem(db, d2, "Vanilla Bean",     10);
        insertItem(db, d2, "Cookies & Cream",   6);
        insertItem(db, d2, "Mango Sorbet",       4);
        insertItem(db, d2, "Caramel Crunch",     4);

        // Store 3 – Sunrise Groceries
        long d3 = insertDelivery(db, "Sunrise Groceries",
                "7 Futo Road, Obinze");
        insertItem(db, d3, "Chocolate Fudge",    8);
        insertItem(db, d3, "Vanilla Bean",        6);
        insertItem(db, d3, "Butter Pecan",        4);

        // Store 4 – Mama's Kitchen Restaurant
        long d4 = insertDelivery(db, "Mama's Kitchen Restaurant",
                "33 Aggrey Road, Old GRA");
        insertItem(db, d4, "Strawberry Swirl",   4);
        insertItem(db, d4, "Mint Chocolate Chip", 4);
        insertItem(db, d4, "Pineapple Coconut",   2);

        // Store 5 – FreshMart Superstore
        long d5 = insertDelivery(db, "FreshMart Superstore",
                "101 Aba Road, Rumuola Junction");
        insertItem(db, d5, "Vanilla Bean",        12);
        insertItem(db, d5, "Chocolate Fudge",      8);
        insertItem(db, d5, "Cookies & Cream",      8);
        insertItem(db, d5, "Mango Sorbet",          6);
        insertItem(db, d5, "Caramel Crunch",        4);

        // Store 6 – Blue Ocean Bistro
        long d6 = insertDelivery(db, "Blue Ocean Bistro",
                "22 Waterfront Drive, Trans-Amadi");
        insertItem(db, d6, "Pineapple Coconut",    3);
        insertItem(db, d6, "Mango Sorbet",          3);
        insertItem(db, d6, "Mint Chocolate Chip",   2);
    }

    private long insertDelivery(SQLiteDatabase db, String storeName, String address) {
        ContentValues cv = new ContentValues();
        cv.put(COL_D_STORE_NAME, storeName);
        cv.put(COL_D_ADDRESS,    address);
        cv.put(COL_D_STATUS,     Delivery.STATUS_PENDING);
        return db.insert(TABLE_DELIVERIES, null, cv);
    }

    private void insertItem(SQLiteDatabase db, long deliveryId,
                            String flavorName, int quantity) {
        ContentValues cv = new ContentValues();
        cv.put(COL_I_DELIVERY_ID, deliveryId);
        cv.put(COL_I_FLAVOR_NAME, flavorName);
        cv.put(COL_I_QUANTITY,    quantity);
        db.insert(TABLE_ITEMS, null, cv);
    }

    // ── Public DAO methods ────────────────────────────────────────────────────

    /**
     * Fetches all deliveries for today's list, each with their items populated.
     */
    public List<Delivery> getAllDeliveries() {
        List<Delivery> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_DELIVERIES,
                null, null, null, null, null,
                COL_D_ID + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Delivery d = cursorToDelivery(cursor);
                d.setItems(getItemsForDelivery(db, d.getId()));
                list.add(d);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return list;
    }

    /**
     * Fetches a single delivery by its primary key (with items).
     */
    public Delivery getDeliveryById(int deliveryId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_DELIVERIES,
                null,
                COL_D_ID + " = ?",
                new String[]{String.valueOf(deliveryId)},
                null, null, null
        );

        Delivery d = null;
        if (cursor != null && cursor.moveToFirst()) {
            d = cursorToDelivery(cursor);
            d.setItems(getItemsForDelivery(db, d.getId()));
            cursor.close();
        }
        return d;
    }

    /**
     * Updates the status (and optional failed reason) of a delivery.
     *
     * @param deliveryId  primary key of the delivery to update
     * @param newStatus   one of Delivery.STATUS_* constants
     * @param failedReason nullable; only stored when status is FAILED
     * @return number of rows affected (should be 1 on success)
     */
    public int updateDeliveryStatus(int deliveryId, String newStatus,
                                    String failedReason) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_D_STATUS,        newStatus);
        cv.put(COL_D_FAILED_REASON, failedReason);   // null is fine for DELIVERED

        return db.update(
                TABLE_DELIVERIES,
                cv,
                COL_D_ID + " = ?",
                new String[]{String.valueOf(deliveryId)}
        );
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /** Builds a Delivery object from the current Cursor row (no items loaded). */
    private Delivery cursorToDelivery(Cursor c) {
        Delivery d = new Delivery();
        d.setId(           c.getInt(   c.getColumnIndexOrThrow(COL_D_ID)));
        d.setStoreName(    c.getString(c.getColumnIndexOrThrow(COL_D_STORE_NAME)));
        d.setAddress(      c.getString(c.getColumnIndexOrThrow(COL_D_ADDRESS)));
        d.setStatus(       c.getString(c.getColumnIndexOrThrow(COL_D_STATUS)));
        d.setFailedReason( c.getString(c.getColumnIndexOrThrow(COL_D_FAILED_REASON)));
        return d;
    }

    /** Queries all OrderItems that belong to a given delivery. */
    private List<OrderItem> getItemsForDelivery(SQLiteDatabase db, int deliveryId) {
        List<OrderItem> items = new ArrayList<>();
        Cursor c = db.query(
                TABLE_ITEMS,
                null,
                COL_I_DELIVERY_ID + " = ?",
                new String[]{String.valueOf(deliveryId)},
                null, null, COL_I_ID + " ASC"
        );

        if (c != null && c.moveToFirst()) {
            do {
                OrderItem item = new OrderItem();
                item.setId(         c.getInt(   c.getColumnIndexOrThrow(COL_I_ID)));
                item.setDeliveryId( c.getInt(   c.getColumnIndexOrThrow(COL_I_DELIVERY_ID)));
                item.setFlavorName( c.getString(c.getColumnIndexOrThrow(COL_I_FLAVOR_NAME)));
                item.setQuantity(   c.getInt(   c.getColumnIndexOrThrow(COL_I_QUANTITY)));
                items.add(item);
            } while (c.moveToNext());
            c.close();
        }
        return items;
    }
}
