package ro.tuscale.udacity.bake.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ro.tuscale.udacity.bake.R;
import ro.tuscale.udacity.bake.ui.fragments.RecipeDetailFragment;
import ro.tuscale.udacity.bake.Utils;
import ro.tuscale.udacity.bake.models.Recipe;

public class RecipeDetailActivity extends AppCompatActivity implements RecipeDetailFragment.RecipeLoadedNotifier {

    private ImageView mRecipeImage;
    private CollapsingToolbarLayout mAppBarLayout;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        // Lock onto needed views
        mRecipeImage = (ImageView) findViewById(R.id.im_recipe_detail);
        mAppBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        mToolbar = (Toolbar) findViewById(R.id.detail_toolbar);

        // Do some toolbar magic
        setSupportActionBar(mToolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity using a fragment transaction.
            Bundle arguments = new Bundle();
            RecipeDetailFragment fragment = new RecipeDetailFragment();

            arguments.putInt(RecipeDetailFragment.ARG_RECIPE_ID,
                    getIntent().getIntExtra(RecipeDetailFragment.ARG_RECIPE_ID, -1));
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.recipe_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            Intent upIntent = new Intent(this, RecipeListActivity.class);

            navigateUpTo(upIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRecipeLoaded(Recipe recipe) {
        // Change the title of the activity
        if (mAppBarLayout != null) {
            mAppBarLayout.setTitle(recipe.getName());
        }

        // Load recipe image
        if (mRecipeImage != null) {
            Utils.getPicasso(this)
                    .load(recipe.getImageUrl())
                    .placeholder(R.drawable.recipe_list_loading_placeholder).fit()
                    .into(mRecipeImage);
        }
    }

    public static void startForRecipe(@NonNull Context ctx, @NonNull Recipe recipe) {
        Intent intent = new Intent(ctx, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailFragment.ARG_RECIPE_ID, recipe.getId());

        ctx.startActivity(intent);
    }
}
