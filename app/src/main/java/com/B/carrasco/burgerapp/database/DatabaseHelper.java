package com.B.carrasco.burgerapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.B.carrasco.burgerapp.models.Ingredient;
import com.B.carrasco.burgerapp.models.User;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "BurgerApp.db";
    private static final int DATABASE_VERSION = 2; // Aumentado porque cambiamos la estructura

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
        // Tabla de usuarios - ACTUALIZADA con phone y address
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "username TEXT UNIQUE,"
                + "password TEXT,"
                + "email TEXT,"
                + "phone TEXT,"           // NUEVO CAMPO
                + "address TEXT,"         // NUEVO CAMPO
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

        // Relación burger-ingredients
        String CREATE_BURGER_INGREDIENTS_TABLE = "CREATE TABLE burger_ingredients ("
                + "burger_id INTEGER,"
                + "ingredient_id INTEGER,"
                + "PRIMARY KEY (burger_id, ingredient_id)"
                + ")";
        db.execSQL(CREATE_BURGER_INGREDIENTS_TABLE);

        // Datos iniciales
        insertInitialData(db);
    }

    private void insertInitialData(SQLiteDatabase db) {
        // Insertar admin por defecto
        ContentValues admin = new ContentValues();
        admin.put("username", "admin");
        admin.put("password", "admin123");
        admin.put("email", "admin@burgerapp.com");
        admin.put("phone", "+56912345678");      // NUEVO
        admin.put("address", "Condominio Los Laureles, Casa 1"); // NUEVO
        admin.put("role", "admin");
        db.insert(TABLE_USERS, null, admin);

        // Insertar cliente de prueba
        ContentValues client = new ContentValues();
        client.put("username", "cliente");
        client.put("password", "cliente123");
        client.put("email", "cliente@burgerapp.com");
        client.put("phone", "+56987654321");     // NUEVO
        client.put("address", "Condominio Los Laureles, Casa 2"); // NUEVO
        client.put("role", "client");
        db.insert(TABLE_USERS, null, client);

        // Insertar ingredientes iniciales (ejemplo)
        String[] ingredients = {
                "INSERT INTO ingredients (name, price, category) VALUES ('Pan clásico', 800, 'pan')",
                "INSERT INTO ingredients (name, price, category) VALUES ('Carne de res', 600, 'carne')",
                "INSERT INTO ingredients (name, price, category) VALUES ('Queso cheddar', 100, 'queso')",
                "INSERT INTO ingredients (name, price, category) VALUES ('Lechuga', 50, 'vegetales')",
                "INSERT INTO ingredients (name, price, category) VALUES ('Tomate', 50, 'vegetales')",
                "INSERT INTO ingredients (name, price, category) VALUES ('Tocino', 150, 'extras')"
        };

        for (String ingredient : ingredients) {
            db.execSQL(ingredient);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Migración a versión 2: agregar phone y address a la tabla users
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN phone TEXT DEFAULT ''");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN address TEXT DEFAULT ''");
        }
        // Para versiones futuras, agregar más condiciones if (oldVersion < 3) ...
    }

    // ------------------------------------------------------------
    // MÉTODOS PARA USUARIOS - ACTUALIZADOS
    // ------------------------------------------------------------

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

    // NUEVO: Verificar si usuario ya existe
    public boolean userExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{"id"},
                "username = ?",
                new String[]{username},
                null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // NUEVO: Insertar usuario completo con todos los campos
    public long insertUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", user.getUsername());
        values.put("password", user.getPassword()); // En producción, usar hash
        values.put("email", user.getEmail());
        values.put("phone", user.getPhone());
        values.put("address", user.getAddress());
        values.put("role", user.getRole() != null ? user.getRole() : "client");

        return db.insert(TABLE_USERS, null, values);
    }

    // NUEVO: Obtener usuario por username
    public User getUserByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{"id", "username", "email", "phone", "address", "role"},
                "username = ?",
                new String[]{username},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            User user = new User();
            user.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow("username")));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
            user.setAddress(cursor.getString(cursor.getColumnIndexOrThrow("address")));
            user.setRole(cursor.getString(cursor.getColumnIndexOrThrow("role")));
            cursor.close();
            return user;
        }
        return null;
    }

    // NUEVO: Actualizar datos del usuario
    public int updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("email", user.getEmail());
        values.put("phone", user.getPhone());
        values.put("address", user.getAddress());

        return db.update(TABLE_USERS, values, "id = ?",
                new String[]{String.valueOf(user.getId())});
    }

    // =======================
    // MÉTODOS PARA INGREDIENTES (CRUD) - EXISTENTES
    // =======================

    // Obtener todos los ingredientes
    public List<Ingredient> getAllIngredients() {
        List<Ingredient> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_INGREDIENTS,
                new String[]{"id", "name", "price", "category", "available"},
                null, null, null, null, "name ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Ingredient ing = new Ingredient();
                ing.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                ing.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                ing.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("price")));
                ing.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
                ing.setAvailable(cursor.getInt(cursor.getColumnIndexOrThrow("available")) == 1);
                list.add(ing);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    // Insertar ingrediente (retorna id insertado o -1)
    public long insertIngredient(Ingredient ingredient) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", ingredient.getName());
        values.put("price", ingredient.getPrice());
        values.put("category", ingredient.getCategory());
        values.put("available", ingredient.isAvailable() ? 1 : 0);
        return db.insert(TABLE_INGREDIENTS, null, values);
    }

    // Actualizar ingrediente (retorna filas afectadas)
    public int updateIngredient(Ingredient ingredient) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", ingredient.getName());
        values.put("price", ingredient.getPrice());
        values.put("category", ingredient.getCategory());
        values.put("available", ingredient.isAvailable() ? 1 : 0);
        return db.update(TABLE_INGREDIENTS, values, "id = ?",
                new String[]{String.valueOf(ingredient.getId())});
    }

    // Borrar ingrediente por id (retorna filas afectadas)
    public int deleteIngredient(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_INGREDIENTS, "id = ?",
                new String[]{String.valueOf(id)});
    }

    // Obtener ingrediente por id
    public Ingredient getIngredientById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_INGREDIENTS,
                new String[]{"id", "name", "price", "category", "available"},
                "id = ?",
                new String[]{String.valueOf(id)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Ingredient ing = new Ingredient();
            ing.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            ing.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            ing.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("price")));
            ing.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
            ing.setAvailable(cursor.getInt(cursor.getColumnIndexOrThrow("available")) == 1);
            cursor.close();
            return ing;
        }
        return null;
    }
}