package ro.tuscale.udacity.bake.ui.activities;

import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ro.tuscale.udacity.bake.R;
import ro.tuscale.udacity.bake.Utils;
import ro.tuscale.udacity.bake.ui.adapters.RecipesAdapter;
import ro.tuscale.udacity.bake.models.RecipeRepository;
import ro.tuscale.udacity.bake.models.Recipe;

public class RecipeListActivity extends AppCompatActivity
        implements RecipesAdapter.RecipeClickListener, LifecycleRegistryOwner {

    // Binded view region
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.recipe_list) RecyclerView mRecipesList;

    private LifecycleRegistry mLifecycleRegistry = new LifecycleRegistry(this);
    private RecipesAdapter mRecipesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);
        ButterKnife.bind(this);

        // A little toolbar magic
        setSupportActionBar(mToolbar);
        mToolbar.setTitle(getTitle());

        // Recipes list setup, loading and wiring
        RecipeRepository restManager = ViewModelProviders.of(this).get(RecipeRepository.class);

        mRecipesAdapter = new RecipesAdapter(getApplicationContext(), this);
        if (Utils.isInternetConnected()) {
            restManager.getAll().observe(this, new Observer<List<Recipe>>() {
                @Override
                public void onChanged(@Nullable List<Recipe> recipes) {
                    mRecipesAdapter.loadRecipes(recipes);
                }
            });
        } else {
            Toast.makeText(this, R.string.no_internet_body, Toast.LENGTH_SHORT).show();
        }
        mRecipesList.setLayoutManager(new GridLayoutManager(this, getResources().getInteger(R.integer.recipe_list_col_count)));
        mRecipesList.setAdapter(mRecipesAdapter);
    }

    @Override
    public void onRecipeClicked(Recipe recipe) {
        RecipeDetailActivity.startForRecipe(this, recipe);
    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return mLifecycleRegistry;
    }
}
