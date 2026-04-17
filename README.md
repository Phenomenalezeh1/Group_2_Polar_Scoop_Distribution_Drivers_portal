#  Polar Scoop Driver Portal

> **Mobile App Development Assignment – PoC Submission**
> Course Level: Beginner / Intermediate Android Development

---

## 1. Chosen Technical Stack

| Layer | Technology | Reason |
|---|---|---|
| Language | **Java** | Widely taught, strong Android tooling support |
| UI Framework | **Android XML + LinearLayout** | Explicit requirement; predictable layout behaviour |
| IDE | **Android Studio** | Official Android IDE; Gradle build system |
| Local Database | **SQLite via SQLiteOpenHelper** | Built into Android, no extra dependencies, perfect for PoC |
| Min SDK | **API 24 (Android 7.0)** | Covers >95 % of active Android devices |

---

## 2. App Screens (User Flow)

```
LoginActivity  ──(success)──►  DeliveryListActivity  ──(tap card)──►  OrderDetailsActivity
                                        ▲                                      │
                                        └──────────────(back / updated)────────┘
```

### Screen 1 – Login
- Large Driver ID + Password fields.
- Hard-coded credentials: `DRIVER01` or `DRIVER02` / `password123`.
- Inline error message on bad input; no navigation on failure.

### Screen 2 – Delivery List (Dashboard)
- RecyclerView of all 6 mock deliveries.
- Progress bar + counter show how many stops are complete.
- Colour-coded status badge per card (amber=PENDING, green=DELIVERED, red=FAILED).
- Logout button with confirmation dialog.

### Screen 3 – Order Details
- Prominent store name + address header in brand blue.
- Dynamically inflated item rows with coloured flavour dots and quantity badges.
- **Mark as Delivered** (green, full-width) and **Mark as Failed** (red outline) action buttons.
- "Mark as Failed" reveals a reason-input card before confirming.
- Once updated: buttons are replaced by a read-only status banner; changes can't be reversed.

---

## 3. Database Schema

### Table: `deliveries`

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | Auto-assigned |
| `store_name` | TEXT | NOT NULL | e.g. "Sandy's Cafe" |
| `address` | TEXT | NOT NULL | Full delivery address |
| `status` | TEXT | NOT NULL DEFAULT 'PENDING' | PENDING / DELIVERED / FAILED |
| `failed_reason` | TEXT | nullable | Optional driver note on failure |

### Table: `order_items`

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | |
| `delivery_id` | INTEGER | NOT NULL, FK → deliveries(id) | Links item to its delivery |
| `flavor_name` | TEXT | NOT NULL | e.g. "Vanilla Bean" |
| `quantity` | INTEGER | NOT NULL | Number of tubs |

### Entity Relationship

```
deliveries  1 ───< order_items
  (id)              (delivery_id)
```

---

## 4. Mock Data (pre-seeded on first launch)

| # | Store | Address | Items |
|---|---|---|---|
| 1 | Sandy's Cafe | 16 Rumuokoro Street, Port Harcourt | Vanilla Bean ×5, Chocolate Fudge ×3, Strawberry Swirl ×2 |
| 2 | The Immaculate Hotel Kitchen | 44 Hotel Avenue, GRA Phase 2 | Vanilla Bean ×10, Cookies & Cream ×6, Mango Sorbet ×4, Caramel Crunch ×4 |
| 3 | Sunny Groceries | 7 Futo Road, Rumuola | Chocolate Fudge ×8, Vanilla Bean ×6, Butter Pecan ×4 |
| 4 | Rhapsody's Restaurant | 12 Isaac John Street, Old GRA | Strawberry Swirl ×4, Mint Choc Chip ×4, Pineapple Coconut ×2 |
| 5 | FreshMart Superstore | 101 Aba Road, Rumuola Junction | Vanilla Bean ×12, Choc Fudge ×8, Cookies & Cream ×8, Mango Sorbet ×6, Caramel Crunch ×4 |
| 6 | Blue Ocean Bistro | 22 Waterfront Drive, Trans-Amadi | Pineapple Coconut ×3, Strawberry sundae ×3, Mint Chocolate Chip tubs ×2 |

---

## 5. Project Structure

```
PolarScoopDriver/
├── app/src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/polarscoop/driver/
│   │   ├── activities/
│   │   │   ├── LoginActivity.java          ← Screen 1
│   │   │   ├── DeliveryListActivity.java   ← Screen 2
│   │   │   └── OrderDetailsActivity.java  ← Screen 3
│   │   ├── adapters/
│   │   │   └── DeliveryAdapter.java        ← RecyclerView adapter
│   │   ├── database/
│   │   │   └── DatabaseHelper.java         ← SQLite helper + DAO + seeding
│   │   └── models/
│   │       ├── Delivery.java               ← Data model
│   │       └── OrderItem.java              ← Data model
│   └── res/
│       ├── layout/
│       │   ├── activity_login.xml
│       │   ├── activity_delivery_list.xml
│       │   ├── activity_order_details.xml
│       │   ├── item_delivery.xml
│       │   └── item_order_item.xml
│       ├── drawable/          ← Shapes, selectors, badges
│       └── values/
│           ├── strings.xml
│           ├── colors.xml
│           └── themes.xml
```
