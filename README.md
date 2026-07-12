# FreelanceJobHub

FreelanceJobHub is an offline-first Android application for freelancers who need a private client and project ledger that continues working without internet access.

## Technical overview

- Main language: Java
- Minimum SDK: 24 (Android 7.0)
- Local storage: SQLite through `SQLiteOpenHelper`
- UI: Material 3, RecyclerView, custom project cards, responsive grid
- Data transfer: complete `Project` objects implement `Parcelable`
- Session: local `SharedPreferences` with a Remember Me option
- Bonus: bundled Google ML Kit text recognition for offline contract scanning

The app does not request internet access. User accounts, profile data, images, and projects stay on the device.

## Included screens

1. Register
2. Login
3. Dashboard
4. Projects list
5. Project details
6. Add project
7. Edit project
8. User profile

## Main capabilities

- Local registration and login with regex email validation and strong-password checks
- SHA-256 password storage instead of plaintext
- Remembered user sessions
- Per-user project isolation and complete create/read/update/delete operations
- Dashboard cards for active projects, managed budget, and pending invoices
- Searchable RecyclerView with custom edit and delete controls
- Safe delete confirmation from list and details screens
- Explicit intents for internal navigation
- Dialer, maps, and share actions through implicit intents
- Add/edit form state restoration after device rotation
- Status Spinner and native DatePicker dialog
- Profile editing, password changes, gallery selection, and camera capture
- Offline OCR that fills the project description from a photographed contract
- Consistent light and dark themes

## Run the project

1. Open the project folder in Android Studio.
2. Allow Gradle to sync and download dependencies.
3. Select an emulator or Android device running Android 7.0 or newer.
4. Run the `app` configuration.
5. Create a local account from the Register screen; no preset credentials are required.

## Suggested demonstration flow

1. Register, log in, and enable Remember Me.
2. Add a project and rotate the device before saving to demonstrate form state retention.
3. Save the project and show the dashboard statistics update.
4. Open the project, launch the dialer or maps, edit it, and then return to confirm the update.
5. Add a second record, delete it with confirmation, and demonstrate search.
6. Update the profile image from gallery or camera and change the password.
7. Use Scan contract text on an add/edit form to demonstrate offline OCR.

## Documentation

- [Database schema](docs/DATABASE_SCHEMA.md)
- [Database schema diagram](docs/database_schema.svg)
- [Requirements checklist](docs/REQUIREMENTS_CHECKLIST.md)

The implementation follows the supplied FreelanceJobHub proposal and the Android Workshops final-project specification.
