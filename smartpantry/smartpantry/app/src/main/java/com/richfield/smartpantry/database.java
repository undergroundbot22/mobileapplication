package com.richfield.smartpantry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "smartpantry.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_PANTRY = "pantry";
    public static final String COL_PANTRY_ID = "_id";
    public static final String COL_PANTRY_NAME = "name";
    public static final String COL_PANTRY_QTY = "quantity";
    public static final String COL_PANTRY_UNIT = "unit";
    public static final String COL_PANTRY_EXPIRY = "expiry_date";

    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_RECIPE_ID = "_id";
    public static final String COL_RECIPE_NAME = "name";
    public static final String COL_RECIPE_STEPS = "steps";

    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";
    public static final String COL_RI_ID = "_id";
    public static final String COL_RI_RECIPE_ID = "recipe_id";
    public static final String COL_RI_NAME = "ingredient_name";
    public static final String COL_RI_QTY = "quantity";
    public static final String COL_RI_UNIT = "unit";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createPantry = "CREATE TABLE " + TABLE_PANTRY + " (" +
                COL_PANTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PANTRY_NAME + " TEXT NOT NULL, " +
                COL_PANTRY_QTY + " REAL NOT NULL, " +
                COL_PANTRY_UNIT + " TEXT, " +
                COL_PANTRY_EXPIRY + " TEXT);";
        db.execSQL(createPantry);

        String createRecipes = "CREATE TABLE " + TABLE_RECIPES + " (" +
                COL_RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RECIPE_NAME + " TEXT NOT NULL, " +
                COL_RECIPE_STEPS + " TEXT);";
        db.execSQL(createRecipes);

        String createIngredients = "CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " (" +
                COL_RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RI_RECIPE_ID + " INTEGER, " +
                COL_RI_NAME + " TEXT NOT NULL, " +
                COL_RI_QTY + " REAL NOT NULL, " +
                COL_RI_UNIT + " TEXT);";
        db.execSQL(createIngredients);

        seedInitialRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        onCreate(db);
    }

    // --- Pantry CRUD Operations ---

    public boolean addPantryItem(String name, double qty, String unit, String expiry) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PANTRY_NAME, name != null ? name.trim() : "");
        cv.put(COL_PANTRY_QTY, qty);
        cv.put(COL_PANTRY_UNIT, unit != null ? unit.trim() : "");
        cv.put(COL_PANTRY_EXPIRY, expiry != null ? expiry.trim() : "");
        long result = db.insert(TABLE_PANTRY, null, cv);
        return result != -1;
    }

    public boolean updatePantryItem(int id, String name, double qty, String unit, String expiry) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PANTRY_NAME, name != null ? name.trim() : "");
        cv.put(COL_PANTRY_QTY, qty);
        cv.put(COL_PANTRY_UNIT, unit != null ? unit.trim() : "");
        cv.put(COL_PANTRY_EXPIRY, expiry != null ? expiry.trim() : "");
        int rows = db.update(TABLE_PANTRY, cv, COL_PANTRY_ID + " = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public boolean deletePantryItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_PANTRY, COL_PANTRY_ID + " = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public Cursor getAllPantryItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_PANTRY + " ORDER BY " + COL_PANTRY_NAME + " ASC", null);
    }

    public Cursor getALLPantryItems() {
        return getAllPantryItems();
    }

    public List<PantryItem> getAllPantryItemsList() {
        List<PantryItem> list = new ArrayList<>();
        Cursor cursor = getAllPantryItems();
        if (cursor != null) {
            try {
                int idIdx = cursor.getColumnIndexOrThrow(COL_PANTRY_ID);
                int nameIdx = cursor.getColumnIndexOrThrow(COL_PANTRY_NAME);
                int qtyIdx = cursor.getColumnIndexOrThrow(COL_PANTRY_QTY);
                int unitIdx = cursor.getColumnIndexOrThrow(COL_PANTRY_UNIT);
                int expIdx = cursor.getColumnIndexOrThrow(COL_PANTRY_EXPIRY);

                while (cursor.moveToNext()) {
                    int id = cursor.getInt(idIdx);
                    String name = cursor.getString(nameIdx);
                    double qty = cursor.getDouble(qtyIdx);
                    String unit = cursor.getString(unitIdx);
                    String expiry = cursor.getString(expIdx);
                    list.add(new PantryItem(id, name, qty, unit, expiry));
                }
            } finally {
                cursor.close();
            }
        }
        return list;
    }

    // --- Recipes Operations ---

    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_RECIPES + " ORDER BY " + COL_RECIPE_NAME + " ASC", null);

        if (cursor != null) {
            try {
                int idIdx = cursor.getColumnIndexOrThrow(COL_RECIPE_ID);
                int nameIdx = cursor.getColumnIndexOrThrow(COL_RECIPE_NAME);
                int stepsIdx = cursor.getColumnIndexOrThrow(COL_RECIPE_STEPS);

                while (cursor.moveToNext()) {
                    int recipeId = cursor.getInt(idIdx);
                    String recipeName = cursor.getString(nameIdx);
                    String steps = cursor.getString(stepsIdx);

                    List<Ingredient> ingredients = getIngredientsForRecipe(db, recipeId);
                    recipes.add(new Recipe(recipeId, recipeName, ingredients, steps));
                }
            } finally {
                cursor.close();
            }
        }
        return recipes;
    }

    private List<Ingredient> getIngredientsForRecipe(SQLiteDatabase db, int recipeId) {
        List<Ingredient> ingredients = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_RECIPE_INGREDIENTS +
                " WHERE " + COL_RI_RECIPE_ID + " = ?", new String[]{String.valueOf(recipeId)});

        if (cursor != null) {
            try {
                int nameIdx = cursor.getColumnIndexOrThrow(COL_RI_NAME);
                int qtyIdx = cursor.getColumnIndexOrThrow(COL_RI_QTY);
                int unitIdx = cursor.getColumnIndexOrThrow(COL_RI_UNIT);

                while (cursor.moveToNext()) {
                    String name = cursor.getString(nameIdx);
                    double qty = cursor.getDouble(qtyIdx);
                    String unit = cursor.getString(unitIdx);
                    ingredients.add(new Ingredient(name, qty, unit));
                }
            } finally {
                cursor.close();
            }
        }
        return ingredients;
    }

    private void seedInitialRecipes(SQLiteDatabase db) {
        insertRecipeWithIngredients(db,
                "Tomato Basil Pasta",
                "1. Boil pasta in salted water until al dente.\n2. Sauté minced garlic and diced tomatoes in olive oil.\n3. Combine pasta with sauce and garnish with basil.",
                Arrays.asList(
                        new Ingredient("Pasta", 200, "g"),
                        new Ingredient("Tomato", 3, "pcs"),
                        new Ingredient("Garlic", 2, "cloves")
                )
        );

        insertRecipeWithIngredients(db,
                "Classic Pancakes",
                "1. Whisk flour, milk, and eggs in a bowl until smooth.\n2. Heat a lightly oiled griddle or pan.\n3. Pour batter and cook until bubbly, then flip.",
                Arrays.asList(
                        new Ingredient("Flour", 200, "g"),
                        new Ingredient("Milk", 250, "ml"),
                        new Ingredient("Egg", 2, "pcs")
                )
        );

        insertRecipeWithIngredients(db,
                "Garlic Scrambled Eggs",
                "1. Beat eggs with milk and a pinch of salt.\n2. Sauté garlic lightly in a pan.\n3. Pour egg mixture and gently stir over low heat until set.",
                Arrays.asList(
                        new Ingredient("Egg", 3, "pcs"),
                        new Ingredient("Milk", 50, "ml"),
                        new Ingredient("Garlic", 1, "cloves")
                )
        );

        insertRecipeWithIngredients(db,
                "Egg Fried Rice",
                "1. Heat oil in a wok or large pan.\n2. Scramble eggs lightly and set aside.\n3. Stir-fry cooked rice with vegetables and fold in eggs.",
                Arrays.asList(
                        new Ingredient("Rice", 250, "g"),
                        new Ingredient("Egg", 2, "pcs"),
                        new Ingredient("Garlic", 2, "cloves")
                )
        );
    }

    private void insertRecipeWithIngredients(SQLiteDatabase db, String name, String steps, List<Ingredient> ingredients) {
        ContentValues cv = new ContentValues();
        cv.put(COL_RECIPE_NAME, name);
        cv.put(COL_RECIPE_STEPS, steps);
        long recipeId = db.insert(TABLE_RECIPES, null, cv);

        if (recipeId != -1 && ingredients != null) {
            for (Ingredient ing : ingredients) {
                ContentValues ingCv = new ContentValues();
                ingCv.put(COL_RI_RECIPE_ID, recipeId);
                ingCv.put(COL_RI_NAME, ing.getName());
                ingCv.put(COL_RI_QTY, ing.getQuantity());
                ingCv.put(COL_RI_UNIT, ing.getUnit());
                db.insert(TABLE_RECIPE_INGREDIENTS, null, ingCv);
            }
        }
    }
}
