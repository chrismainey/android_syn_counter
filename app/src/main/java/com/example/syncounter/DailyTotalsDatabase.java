package com.example.syncounter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class DailyTotalsDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "syn_counter.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_TOTALS = "daily_totals";

    DailyTotalsDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE " + TABLE_TOTALS + " ("
                + "day TEXT PRIMARY KEY NOT NULL, "
                + "total REAL NOT NULL CHECK(total >= 0))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        throw new IllegalStateException("No database migration is defined.");
    }

    double getTotal(String day) {
        try (Cursor cursor = getReadableDatabase().query(
                TABLE_TOTALS,
                new String[]{"total"},
                "day = ?",
                new String[]{day},
                null,
                null,
                null)) {
            return cursor.moveToFirst() ? cursor.getDouble(0) : 0.0;
        }
    }

    void saveTotal(String day, double total) {
        ContentValues values = new ContentValues();
        values.put("day", day);
        values.put("total", total);
        long rowId = getWritableDatabase().insertWithOnConflict(
                TABLE_TOTALS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        if (rowId == -1) {
            throw new SQLiteException("Could not save the daily total.");
        }
    }

    List<Entry> getRecentEntries(int limit) {
        List<Entry> entries = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query(
                TABLE_TOTALS,
                new String[]{"day", "total"},
                null,
                null,
                null,
                null,
                "day DESC",
                Integer.toString(limit))) {
            while (cursor.moveToNext()) {
                entries.add(new Entry(cursor.getString(0), cursor.getDouble(1)));
            }
        }
        Collections.reverse(entries);
        return entries;
    }

    static final class Entry {
        final String day;
        final double total;

        Entry(String day, double total) {
            this.day = day;
            this.total = total;
        }
    }
}
