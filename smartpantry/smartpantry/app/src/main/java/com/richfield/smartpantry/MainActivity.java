package com.richfield.smartpantry;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

@SuppressWarnings({"unused", "Convert2Lambda"})
public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private final List<PantryItem> pantryList = new ArrayList<>();
    private PantryAdapter pantryAdapter;

    private RecyclerView rvPantry;
    private View layoutEmptyState;
    private TextView tvRecipesSubtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        initViews();
        loadPantryItems();
    }

    private void initViews() {
        rvPantry = findViewById(R.id.rv_pantry_items);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        tvRecipesSubtitle = findViewById(R.id.tv_matching_recipes_subtitle);

        rvPantry.setLayoutManager(new LinearLayoutManager(this));
        pantryAdapter = new PantryAdapter(pantryList, new PantryAdapter.OnItemClickListener() {
            @Override
            public void onEdit(PantryItem item) {
                showAddEditDialog(item);
            }

            @Override
            public void onDelete(int id) {
                confirmDeleteItem(id);
            }
        });
        rvPantry.setAdapter(pantryAdapter);

        FloatingActionButton fabAdd = findViewById(R.id.fab_add_item);
        fabAdd.setOnClickListener(v -> showAddEditDialog(null));

        View cardRecipesBanner = findViewById(R.id.card_recipes_banner);
        cardRecipesBanner.setOnClickListener(v -> showRecipeMatchesDialog());
    }

    private void loadPantryItems() {
        pantryList.clear();
        pantryList.addAll(dbHelper.getAllPantryItemsList());
        pantryAdapter.notifyDataSetChanged();

        if (pantryList.isEmpty()) {
            rvPantry.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvPantry.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }

        updateRecipeMatchSummary();
    }

    private void updateRecipeMatchSummary() {
        List<Recipe> allRecipes = dbHelper.getAllRecipes();
        List<Recipe> strictMatches = RecipeMatcher.getStrictMatches(allRecipes, pantryList);
        List<Recipe> almostMatches = RecipeMatcher.getAlmostThereMatches(allRecipes, pantryList);

        if (strictMatches.isEmpty() && almostMatches.isEmpty()) {
            tvRecipesSubtitle.setText(R.string.hint_empty_recipes_subtitle);
        } else if (!strictMatches.isEmpty() && !almostMatches.isEmpty()) {
            tvRecipesSubtitle.setText(String.format(Locale.getDefault(),
                    "%d ready to cook, %d almost ready!", strictMatches.size(), almostMatches.size()));
        } else if (!strictMatches.isEmpty()) {
            tvRecipesSubtitle.setText(String.format(Locale.getDefault(),
                    "%d recipe%s ready to cook now!", strictMatches.size(), strictMatches.size() > 1 ? "s" : ""));
        } else {
            tvRecipesSubtitle.setText(String.format(Locale.getDefault(),
                    "%d recipe%s missing only 1 ingredient", almostMatches.size(), almostMatches.size() > 1 ? "s" : ""));
        }
    }

    private void showAddEditDialog(final PantryItem itemToEdit) {
        final boolean isEdit = (itemToEdit != null);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_pantry_item, null);

        TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_title);
        final TextInputEditText etName = dialogView.findViewById(R.id.et_item_name);
        final TextInputEditText etQty = dialogView.findViewById(R.id.et_item_quantity);
        final TextInputEditText etUnit = dialogView.findViewById(R.id.et_item_unit);
        final TextInputEditText etExpiry = dialogView.findViewById(R.id.et_item_expiry);

        tvTitle.setText(isEdit ? R.string.dialog_title_edit : R.string.dialog_title_add);

        if (isEdit) {
            etName.setText(itemToEdit.getName());
            double q = itemToEdit.getQuantity();
            etQty.setText(q == (long) q ? String.format(Locale.getDefault(), "%d", (long) q) : String.valueOf(q));
            etUnit.setText(itemToEdit.getUnit());
            etExpiry.setText(itemToEdit.getExpiryDate());
        }

        // Date picker trigger for expiry date
        etExpiry.setFocusable(false);
        etExpiry.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(MainActivity.this, (view, year, month, dayOfMonth) -> {
                String formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                etExpiry.setText(formattedDate);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_save, (dialog, which) -> {
                    String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                    String qtyStr = etQty.getText() != null ? etQty.getText().toString().trim() : "";
                    String unit = etUnit.getText() != null ? etUnit.getText().toString().trim() : "";
                    String expiry = etExpiry.getText() != null ? etExpiry.getText().toString().trim() : "";

                    if (name.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Please enter an ingredient name", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double qty = 1.0;
                    if (!qtyStr.isEmpty()) {
                        try {
                            qty = Double.parseDouble(qtyStr);
                        } catch (NumberFormatException ignored) {
                        }
                    }

                    boolean success;
                    if (isEdit) {
                        success = dbHelper.updatePantryItem(itemToEdit.getId(), name, qty, unit, expiry);
                    } else {
                        success = dbHelper.addPantryItem(name, qty, unit, expiry);
                    }

                    if (success) {
                        loadPantryItems();
                        Toast.makeText(MainActivity.this, isEdit ? "Item updated" : "Item added to pantry", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Database operation failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void confirmDeleteItem(final int id) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to remove this item from your pantry?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (dbHelper.deletePantryItem(id)) {
                        loadPantryItems();
                        Toast.makeText(MainActivity.this, "Item removed", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Failed to delete item", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void showRecipeMatchesDialog() {
        List<Recipe> allRecipes = dbHelper.getAllRecipes();
        List<Recipe> strictMatches = RecipeMatcher.getStrictMatches(allRecipes, pantryList);
        List<Recipe> almostMatches = RecipeMatcher.getAlmostThereMatches(allRecipes, pantryList);

        List<RecipeAdapter.MatchedRecipeItem> displayItems = new ArrayList<>();
        for (Recipe r : strictMatches) {
            displayItems.add(new RecipeAdapter.MatchedRecipeItem(r, true, null));
        }

        for (Recipe r : almostMatches) {
            Ingredient missing = RecipeMatcher.getSingleMissingIngredient(r, pantryList);
            String missingText = (missing != null)
                    ? "Missing: " + missing.getName() + " (" + missing.getQuantity() + " " + missing.getUnit() + ")"
                    : "Missing: 1 ingredient";
            displayItems.add(new RecipeAdapter.MatchedRecipeItem(r, false, missingText));
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_recipe_matches, null);
        RecyclerView rvRecipes = dialogView.findViewById(R.id.rv_recipe_matches);
        View layoutEmpty = dialogView.findViewById(R.id.layout_empty_recipes);

        rvRecipes.setLayoutManager(new LinearLayoutManager(this));
        RecipeAdapter recipeAdapter = new RecipeAdapter(displayItems);
        rvRecipes.setAdapter(recipeAdapter);

        if (displayItems.isEmpty()) {
            rvRecipes.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            rvRecipes.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }

        new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }
}
