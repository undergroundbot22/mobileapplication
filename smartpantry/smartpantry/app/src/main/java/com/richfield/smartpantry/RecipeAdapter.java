package com.richfield.smartpantry;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;

import java.util.List;

@SuppressWarnings("unused")
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    public static class MatchedRecipeItem {
        public final Recipe recipe;
        public final boolean isStrict;
        public final String missingIngredientInfo;

        public MatchedRecipeItem(Recipe recipe, boolean isStrict, String missingIngredientInfo) {
            this.recipe = recipe;
            this.isStrict = isStrict;
            this.missingIngredientInfo = missingIngredientInfo;
        }
    }

    private final List<MatchedRecipeItem> recipeList;

    public RecipeAdapter(List<MatchedRecipeItem> recipeList) {
        this.recipeList = recipeList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (recipeList == null) return;
        MatchedRecipeItem item = recipeList.get(position);
        Recipe recipe = item.recipe;

        holder.tvName.setText(recipe.getName());

        Context context = holder.itemView.getContext();

        // Badge & Missing indicator
        if (item.isStrict) {
            holder.tvBadge.setText(context.getString(R.string.badge_ready_to_cook));
            holder.tvBadge.setBackgroundColor(ContextCompat.getColor(context, R.color.pantry_green));
            holder.tvMissing.setVisibility(View.GONE);
        } else {
            holder.tvBadge.setText(context.getString(R.string.badge_missing_one));
            holder.tvBadge.setBackgroundColor(ContextCompat.getColor(context, R.color.expiry_warning));
            if (item.missingIngredientInfo != null && !item.missingIngredientInfo.isEmpty()) {
                holder.tvMissing.setText(item.missingIngredientInfo);
                holder.tvMissing.setVisibility(View.VISIBLE);
            } else {
                holder.tvMissing.setVisibility(View.GONE);
            }
        }

        // Format ingredients summary
        StringBuilder ingBuilder = new StringBuilder("Ingredients: ");
        List<Ingredient> ingredients = recipe.getRequiredIngredients();
        if (ingredients != null && !ingredients.isEmpty()) {
            for (int i = 0; i < ingredients.size(); i++) {
                Ingredient ing = ingredients.get(i);
                double qty = ing.getQuantity();
                String qtyStr = (qty == (long) qty) ? String.format("%d", (long) qty) : String.valueOf(qty);
                ingBuilder.append(ing.getName()).append(" (").append(qtyStr).append(" ").append(ing.getUnit()).append(")");
                if (i < ingredients.size() - 1) {
                    ingBuilder.append(", ");
                }
            }
        }
        holder.tvIngredients.setText(ingBuilder.toString());

        // Steps
        if (recipe.getSteps() != null && !recipe.getSteps().trim().isEmpty()) {
            holder.tvSteps.setText(recipe.getSteps());
            holder.tvSteps.setVisibility(View.VISIBLE);
        } else {
            holder.tvSteps.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return recipeList != null ? recipeList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvBadge, tvMissing, tvIngredients, tvSteps;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_recipe_name);
            tvBadge = itemView.findViewById(R.id.tv_recipe_badge);
            tvMissing = itemView.findViewById(R.id.tv_recipe_missing);
            tvIngredients = itemView.findViewById(R.id.tv_recipe_ingredients);
            tvSteps = itemView.findViewById(R.id.tv_recipe_steps);
        }
    }
}
