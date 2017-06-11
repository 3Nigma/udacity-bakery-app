package ro.tuscale.udacity.bake.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ro.tuscale.udacity.bake.R;
import ro.tuscale.udacity.bake.models.Recipe;
import ro.tuscale.udacity.bake.models.RecipeIngredient;

public class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.ViewHolder> {

    private List<RecipeIngredient> mIngredients;

    public IngredientsAdapter() {
        this.mIngredients = new ArrayList<>();
    }

    public void loadIngredientsFromRecipe(Recipe recipe) {
        List<RecipeIngredient> ingredients = (recipe == null ? null : recipe.getIngredients());

        mIngredients.clear();
        if (ingredients != null) {
            mIngredients.addAll(ingredients);
        }
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recipe_ingredient_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(mIngredients.get(position));
    }

    @Override
    public int getItemCount() {
        return mIngredients.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.txt_recipe_ingredient_name) TextView mName;
        @BindView(R.id.txt_recipe_ingredient_count) TextView mCount;
        @BindView(R.id.txt_recipe_ingredient_measure) TextView mUnitOfMeasure;

        public ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        public void bind(RecipeIngredient ingredient) {
            mName.setText(ingredient.getName());
            mCount.setText(String.format("%.1f", ingredient.getQuantity()));
            mUnitOfMeasure.setText(ingredient.getUnitOfMeasure());
        }
    }
}
