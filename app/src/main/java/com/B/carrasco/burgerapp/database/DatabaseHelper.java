package com.B.carrasco.burgerapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "BurgerApp.db";
    private static final int DATABASE_VERSION = 1;

    // Tablas
    private static final String TABLE_USERS = "users";
    private static final String TABLE_INGREDIENTS = "ingredients";
    private static final String TABLE_BURGERS = "burgers";
    private static final String TABLE_ORDERS = "orders";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tabla de usuarios
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "username TEXT UNIQUE,"
                + "password TEXT,"
                + "email TEXT,"
                + "role TEXT,"
                + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Tabla de ingredientes
        String CREATE_INGREDIENTS_TABLE = "CREATE TABLE " + TABLE_INGREDIENTS + "("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT,"
                + "price REAL,"
                + "category TEXT,"
                + "available INTEGER DEFAULT 1"
                + ")";
        db.execSQL(CREATE_INGREDIENTS_TABLE);

        // Tabla de hamburguesas
        String CREATE_BURGERS_TABLE = "CREATE TABLE " + TABLE_BURGERS + "("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT,"
                + "user_id INTEGER,"
                + "total_price REAL,"
                + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";
        db.execSQL(CREATE_BURGERS_TABLE);

        // Tabla de pedidos
        String CREATE_ORDERS_TABLE = "CREATE TABLE " + TABLE_ORDERS + "("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "user_id INTEGER,"
                + "burger_id INTEGER,"
                + "status TEXT DEFAULT 'pending',"
                + "deposit_path TEXT,"
                + "deposit_verified INTEGER DEFAULT 0,"
                + "total_amount REAL,"
                + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";
        db.execSQL(CREATE_ORDERS_TABLE);

        // Tabla de relación hamburguesa-ingredientes
        String CREATE_BURGER_INGREDIENTS_TABLE = "CREATE TABLE burger_ingredients ("
                + "burger_id INTEGER,"
                + "ingredient_id INTEGER,"
                + "PRIMARY KEY (burger_id, ingredient_id)"
                + ")";
        db.execSQL(CREATE_BURGER_INGREDIENTS_TABLE);

        // Insertar datos iniciales
        insertInitialData(db);
    }

    private void insertInitialData(SQLiteDatabase db) {
        // Insertar admin por defecto
        ContentValues admin = new ContentValues();
        admin.put("username", "admin");
        admin.put("password", "admin123");
        admin.put("email", "admin@burgerapp.com");
        admin.put("role", "admin");
        db.insert(TABLE_USERS, null, admin);

        // Insertar cliente de prueba
        ContentValues client = new ContentValues();
        client.put("username", "cliente");
        client.put("password", "cliente123");
        client.put("email", "cliente@burgerapp.com");
        client.put("role", "client");
        db.insert(TABLE_USERS, null, client);

        // Insertar ingredientes
        String[] ingredients = {
                "INSERT INTO ingredients (name, price, category) VALUES ('Pan clásico', 1.0, 'pan')",
                "INSERT INTO ingredients (name, price, category) VALUES ('Pan integral', 1.5, 'pan')",
                "INSERT INTO ingredients (name, price, category) VALUES ('Carne de res', 3.0, 'carne')",
                "INSERT INTO ingredients (name, price, category) VALUES ('Pollo', 2.5, 'carne')",
                "INSERT INTO ingredients (name, price, category) VALUES ('Queso cheddar', 1.0, 'queso')",
                "INSERT INTO ingredients (name, price, category) VALUES ('Queso mozzarella', 1.2, 'queso')",
                "INSERT INTO ingredients (name, price, category) VALUES ('Lechuga', 0.5, 'vegetales')",
                "INSERT INTO ingredients (name, price, category) VALUES ('Tomate', 0.5, 'vegetales')",
                "INSERT INTO ingredients (name, price, category) VALUES ('Cebolla', 0.5, 'vegetales')",
                "INSERT INTO ingredients (name, price, category) VALUES ('Tocino', 1.5, 'extras')",
                "INSERT INTO ingredients (name, price, category) VALUES ('Huevo', 1.0, 'extras')"
        };

        for (String ingredient : ingredients) {
            db.execSQL(ingredient);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BURGERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS burger_ingredients");
        onCreate(db);
    }

    // Método para verificar usuario
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{"id"},
                "username = ? AND password = ?",
                new String[]{username, password},
                null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Método para obtener rol de usuario
    public String getUserRole(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{"role"},
                "username = ?",
                new String[]{username},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String role = cursor.getString(0);
            cursor.close();
            return role;
        }
        return null;
    }
}