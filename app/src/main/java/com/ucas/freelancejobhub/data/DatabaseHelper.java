package com.ucas.freelancejobhub.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.ucas.freelancejobhub.models.Project;
import com.ucas.freelancejobhub.models.User;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "freelance_job_hub.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_USERS = "users";
    public static final String TABLE_PROJECTS = "projects";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "email TEXT UNIQUE COLLATE NOCASE NOT NULL, " +
                "username TEXT UNIQUE COLLATE NOCASE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "phone TEXT, " +
                "birthdate TEXT, " +
                "profile_image TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_PROJECTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "title TEXT NOT NULL, " +
                "client_name TEXT NOT NULL, " +
                "client_phone TEXT, " +
                "meeting_address TEXT, " +
                "budget REAL DEFAULT 0.0, " +
                "status TEXT NOT NULL, " +
                "due_date TEXT, " +
                "description TEXT, " +
                "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)");

        db.execSQL("CREATE INDEX idx_projects_user_id ON " + TABLE_PROJECTS + "(user_id)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Version 1 is the first release. Future migrations belong here.
    }

    public long registerUser(User user, String passwordHash) {
        ContentValues values = new ContentValues();
        values.put("name", user.getName());
        values.put("email", user.getEmail());
        values.put("username", user.getUsername());
        values.put("password", passwordHash);
        values.put("phone", user.getPhone());
        values.put("birthdate", user.getBirthdate());
        values.put("profile_image", user.getProfileImage());
        return getWritableDatabase().insertOrThrow(TABLE_USERS, null, values);
    }

    @Nullable
    public User getUserByIdentifier(String identifier) {
        String selection = "email = ? COLLATE NOCASE OR username = ? COLLATE NOCASE";
        String[] args = {identifier, identifier};
        try (Cursor cursor = getReadableDatabase().query(
                TABLE_USERS, userColumns(), selection, args, null, null, null, "1")) {
            return cursor.moveToFirst() ? readUser(cursor) : null;
        }
    }

    @Nullable
    public User getUserById(long userId) {
        try (Cursor cursor = getReadableDatabase().query(
                TABLE_USERS, userColumns(), "id = ?", new String[]{String.valueOf(userId)},
                null, null, null, "1")) {
            return cursor.moveToFirst() ? readUser(cursor) : null;
        }
    }

    public boolean isEmailTaken(String email, long excludedUserId) {
        return valueExists("email", email, excludedUserId);
    }

    public boolean isUsernameTaken(String username, long excludedUserId) {
        return valueExists("username", username, excludedUserId);
    }

    private boolean valueExists(String column, String value, long excludedUserId) {
        String selection = column + " = ? COLLATE NOCASE";
        List<String> args = new ArrayList<>();
        args.add(value);
        if (excludedUserId > 0) {
            selection += " AND id != ?";
            args.add(String.valueOf(excludedUserId));
        }
        try (Cursor cursor = getReadableDatabase().query(
                TABLE_USERS, new String[]{"id"}, selection, args.toArray(new String[0]),
                null, null, null, "1")) {
            return cursor.moveToFirst();
        }
    }

    public boolean verifyPassword(long userId, String passwordHash) {
        try (Cursor cursor = getReadableDatabase().query(
                TABLE_USERS, new String[]{"id"}, "id = ? AND password = ?",
                new String[]{String.valueOf(userId), passwordHash}, null, null, null, "1")) {
            return cursor.moveToFirst();
        }
    }

    public int updateUser(User user) {
        ContentValues values = new ContentValues();
        values.put("name", user.getName());
        values.put("email", user.getEmail());
        values.put("username", user.getUsername());
        values.put("phone", user.getPhone());
        values.put("birthdate", user.getBirthdate());
        values.put("profile_image", user.getProfileImage());
        return getWritableDatabase().update(
                TABLE_USERS, values, "id = ?", new String[]{String.valueOf(user.getId())});
    }

    public int updatePassword(long userId, String passwordHash) {
        ContentValues values = new ContentValues();
        values.put("password", passwordHash);
        return getWritableDatabase().update(
                TABLE_USERS, values, "id = ?", new String[]{String.valueOf(userId)});
    }

    public long addProject(Project project) {
        return getWritableDatabase().insertOrThrow(TABLE_PROJECTS, null, projectValues(project));
    }

    public int updateProject(Project project) {
        return getWritableDatabase().update(
                TABLE_PROJECTS,
                projectValues(project),
                "id = ? AND user_id = ?",
                new String[]{String.valueOf(project.getId()), String.valueOf(project.getUserId())}
        );
    }

    public int deleteProject(long projectId, long userId) {
        return getWritableDatabase().delete(
                TABLE_PROJECTS,
                "id = ? AND user_id = ?",
                new String[]{String.valueOf(projectId), String.valueOf(userId)}
        );
    }

    @Nullable
    public Project getProjectById(long projectId, long userId) {
        try (Cursor cursor = getReadableDatabase().query(
                TABLE_PROJECTS, null, "id = ? AND user_id = ?",
                new String[]{String.valueOf(projectId), String.valueOf(userId)},
                null, null, null, "1")) {
            return cursor.moveToFirst() ? readProject(cursor) : null;
        }
    }

    public List<Project> getProjects(long userId) {
        List<Project> projects = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query(
                TABLE_PROJECTS, null, "user_id = ?", new String[]{String.valueOf(userId)},
                null, null, "id DESC")) {
            while (cursor.moveToNext()) {
                projects.add(readProject(cursor));
            }
        }
        return projects;
    }

    public DashboardStats getDashboardStats(long userId) {
        String query = "SELECT " +
                "COUNT(*) AS total_count, " +
                "SUM(CASE WHEN status = 'Active' THEN 1 ELSE 0 END) AS active_count, " +
                "COALESCE(SUM(budget), 0) AS total_budget, " +
                "SUM(CASE WHEN status = 'Pending Invoice' THEN 1 ELSE 0 END) AS pending_count " +
                "FROM " + TABLE_PROJECTS + " WHERE user_id = ?";
        try (Cursor cursor = getReadableDatabase().rawQuery(
                query, new String[]{String.valueOf(userId)})) {
            if (cursor.moveToFirst()) {
                return new DashboardStats(
                        cursor.getInt(cursor.getColumnIndexOrThrow("total_count")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("active_count")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("total_budget")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("pending_count"))
                );
            }
        }
        return new DashboardStats(0, 0, 0, 0);
    }

    private ContentValues projectValues(Project project) {
        ContentValues values = new ContentValues();
        values.put("user_id", project.getUserId());
        values.put("title", project.getTitle());
        values.put("client_name", project.getClientName());
        values.put("client_phone", project.getClientPhone());
        values.put("meeting_address", project.getMeetingAddress());
        values.put("budget", project.getBudget());
        values.put("status", project.getStatus());
        values.put("due_date", project.getDueDate());
        values.put("description", project.getDescription());
        return values;
    }

    private String[] userColumns() {
        return new String[]{"id", "name", "email", "username", "phone", "birthdate", "profile_image"};
    }

    private User readUser(Cursor cursor) {
        return new User(
                cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("name")),
                cursor.getString(cursor.getColumnIndexOrThrow("email")),
                cursor.getString(cursor.getColumnIndexOrThrow("username")),
                cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                cursor.getString(cursor.getColumnIndexOrThrow("birthdate")),
                cursor.getString(cursor.getColumnIndexOrThrow("profile_image"))
        );
    }

    private Project readProject(Cursor cursor) {
        return new Project(
                cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                cursor.getLong(cursor.getColumnIndexOrThrow("user_id")),
                cursor.getString(cursor.getColumnIndexOrThrow("title")),
                cursor.getString(cursor.getColumnIndexOrThrow("client_name")),
                cursor.getString(cursor.getColumnIndexOrThrow("client_phone")),
                cursor.getString(cursor.getColumnIndexOrThrow("meeting_address")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("budget")),
                cursor.getString(cursor.getColumnIndexOrThrow("status")),
                cursor.getString(cursor.getColumnIndexOrThrow("due_date")),
                cursor.getString(cursor.getColumnIndexOrThrow("description"))
        );
    }

    public static class DashboardStats {
        private final int totalProjects;
        private final int activeProjects;
        private final double totalBudget;
        private final int pendingInvoices;

        public DashboardStats(int totalProjects, int activeProjects, double totalBudget,
                              int pendingInvoices) {
            this.totalProjects = totalProjects;
            this.activeProjects = activeProjects;
            this.totalBudget = totalBudget;
            this.pendingInvoices = pendingInvoices;
        }

        public int getTotalProjects() { return totalProjects; }
        public int getActiveProjects() { return activeProjects; }
        public double getTotalBudget() { return totalBudget; }
        public int getPendingInvoices() { return pendingInvoices; }
    }
}
