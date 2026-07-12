# Final-project requirements checklist

| Requirement | Implementation |
| --- | --- |
| Minimum seven screens | Eight distinct visible Activities plus a launcher router |
| Register and login | Local users table, validation, clear field errors |
| Remember Me | SharedPreferences stores the active user ID and remembered-session flag |
| Dashboard | Dynamic active count, total managed budget, pending invoice count |
| Items list | RecyclerView, custom adapter/card, responsive grid, search, FAB |
| Edit/delete controls | Present on each card and the details screen |
| Item details | All project fields plus edit, delete, dialer, maps, and share |
| Add form | Validated inputs, Spinner, DatePicker, SQLite insert, success feedback |
| Edit form | Existing values, validation, update, cancel without mutation |
| User profile | Editable metadata, hidden password, password update, gallery/camera image |
| Complex intent data | `Project implements Parcelable` and is passed as a complete entity |
| Local database | `DatabaseHelper extends SQLiteOpenHelper`; all core data survives restarts |
| Full CRUD | Add, query, update, and delete project records |
| Lifecycle state | Explicit `onSaveInstanceState` and `onRestoreInstanceState` in add/edit forms |
| Implicit intents | Dialer, maps, text sharing, and profile phone action |
| Offline OCR bonus | Bundled ML Kit recognizer fills the description from a camera image |
| Theme consistency | Shared Material theme with dedicated light and dark color resources |
