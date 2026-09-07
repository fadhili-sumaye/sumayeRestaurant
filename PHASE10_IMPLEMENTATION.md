# Phase 10 — QR Table Ordering

Phase 10 lets a customer scan a table-specific QR code, browse only that branch's available menu, and submit a dine-in order without a staff login.

## Delivery checklist

- Opaque, 256-bit URL-safe QR tokens are stored per table; raw table IDs are never accepted by public endpoints.
- Public menu: `GET /api/public/qr/{token}`.
- Public order: `POST /api/public/qr/{token}/orders`.
- QR orders use the regular KOT and inventory workflow, are marked with `orderSource: QR`, and have no waiter.
- Public requests lock the table while creating an order, preventing simultaneous scans from creating multiple first orders.
- Managers, admins, and owners can obtain a table link at `GET /api/tables/{id}/qr-link` and revoke/replace a compromised link with `POST /api/tables/{id}/qr-link/rotate`.
- The Android app receives `sumaye://order/{token}` deep links and provides a public customer menu and checkout screen.

## Database rollout

For a new Docker database, `database_schema_phase10.sql` is already mounted by `docker-compose.yml`.

For an existing database volume, run the migration once:

```sql
SOURCE database_schema_phase10.sql;
```

Existing tables receive a token lazily when an authorized manager requests the QR link. Rotating a link invalidates the previous QR code immediately.

## Customer order request

```json
{
  "clientRequestId": "a2fcb820-3aee-4b8e-8f57-aae8fb7b13bf",
  "notes": "Bila pilipili",
  "items": [
    { "menuItemId": 12, "quantity": 2, "specialInstructions": "" }
  ]
}
```

`clientRequestId` must be retained when retrying the same submission. The Android customer screen creates it once per checkout, so a network retry returns the original QR order rather than adding another one.
