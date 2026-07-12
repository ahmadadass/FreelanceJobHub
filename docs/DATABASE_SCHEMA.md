# Database schema

Database file: `freelance_job_hub.db`

Foreign-key enforcement is enabled in `DatabaseHelper.onConfigure()`. Deleting a user cascades to their projects. Every project query also includes the active `user_id`, so records remain isolated between local accounts.

## users

| Column | Type | Constraints |
| --- | --- | --- |
| id | INTEGER | PRIMARY KEY AUTOINCREMENT |
| name | TEXT | NOT NULL |
| email | TEXT | UNIQUE, COLLATE NOCASE, NOT NULL |
| username | TEXT | UNIQUE, COLLATE NOCASE, NOT NULL |
| password | TEXT | NOT NULL; SHA-256 hash |
| phone | TEXT | Optional |
| birthdate | TEXT | ISO-8601 date |
| profile_image | TEXT | Local URI/path |

## projects

| Column | Type | Constraints |
| --- | --- | --- |
| id | INTEGER | PRIMARY KEY AUTOINCREMENT |
| user_id | INTEGER | NOT NULL, FK to users(id), ON DELETE CASCADE |
| title | TEXT | NOT NULL |
| client_name | TEXT | NOT NULL |
| client_phone | TEXT | Optional |
| meeting_address | TEXT | Optional |
| budget | REAL | DEFAULT 0.0 |
| status | TEXT | NOT NULL |
| due_date | TEXT | ISO-8601 date |
| description | TEXT | Optional, manually entered or OCR-filled |

`due_date` is the domain-specific extension that supports the required native DatePicker and makes project deadlines visible in the list and details screens.
