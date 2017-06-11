package ro.tuscale.udacity.bake.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ro.tuscale.udacity.bake.R;
import ro.tuscale.udacity.bake.Utils;
import ro.tuscale.udacity.bake.models.Recipe;

public class RecipesAdapter extends RecyclerView.Adapter<RecipesAdapter.ViewHolder> {

    private Context mContext;
    private RecipeClickListener mRecipeClickListener;
    private List<Recipe> mRecipes;

    public RecipesAdapter(@NonNull Context ctx, @NonNull RecipeClickListener recipeClickListener) {
        this.mContext = ctx;
        this.mRecipeClickListener = recipeClickListener;
        this.mRecipes = new ArrayList<>();
    }

    public void loadRecipes(List<Recipe> recipes) {
        mRecipes.clear();
        if (recipes != null) {
            mRecipes.addAll(recipes);
        }
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recipe_list_content, parent, false);
        return new ViewHolder(mContext, view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder recipeHolder, int itemPosition) {
        recipeHolder.bind(mRecipes.get(itemPosition));
    }

    @Override
    public int getItemCount() {
        return mRecipes.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private Context mContext;
        private View mRootView;
        @BindView(R.id.im_recipe_result) ImageView mRecipeImage;
        @BindView(R.id.txt_recipe_name) TextView mRecipeName;
        @BindView(R.id.txt_ingredients_count) TextView mRecipeIngredientsCount;
        @BindView(R.id.txt_steps_count) TextView mRecipeStepsCount;
        @BindView(R.id.txt_portions_count) TextView mRecipeServingsCount;

        private Recipe mItem;
        private Picasso mCustomPicasso;

        /* package */ ViewHolder(@NonNull Context ctx, @NonNull View v) {
            super(v);
            ButterKnife.bind(this, v);

            this.mContext = ctx;
            this.mRootView = v;
            this.mCustomPicasso = Utils.getPicasso(ctx);
        }

        /* package */ void bind(@NonNull Recipe recipe) {
            int recipeIngredients = recipe.getIngredientsCount();
            int recipeSteps = recipe.getRequiredStepsCount();
            int recipeServings = recipe.getServingsCount();

            // Do an image load with a custom OkHttp downloader so that http->https redirects are taken
            // and image are loaded properly
            mCustomPicasso.load(recipe.getImageUrl())
                    .placeholder(R.drawable.recipe_list_loading_placeholder).fit()
                    .into(mRecipeImage);
            mRecipeName.setText(recipe.getName());

            mRecipeIngredientsCount.setText(mContext.getResources().getQuantityString(R.plurals.list_recipe_ingredients_format, recipeIngredients, recipeIngredients));
            mRecipeStepsCount.setText(mContext.getResources().getQuantityString(R.plurals.list_recipe_steps_format, recipeSteps, recipeSteps));
            mRecipeServingsCount.setText(mContext.getResources().getQuantityString(R.plurals.list_recipe_servings_format, recipeServings, recipeServings));

            // Tie events
            mRootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mRecipeClickListener.onRecipeClicked(mItem);
                }
            });

            mItem = recipe;
        }
    }

    public interface RecipeClickListener {
        void onRecipeClicked(Recipe recipe);
    }
}
