package ro.tuscale.udacity.bake.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import butterknife.ButterKnife;
import ro.tuscale.udacity.bake.R;
import ro.tuscale.udacity.bake.models.Recipe;
import ro.tuscale.udacity.bake.models.RecipeStep;
import ro.tuscale.udacity.bake.ui.fragments.RecipeDetailFragment;
import ro.tuscale.udacity.bake.ui.fragments.RecipeStepFragment;

public class RecipeStepActivity extends AppCompatActivity {

    private int mRecipeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_step);
        ButterKnife.bind(this);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity using a fragment transaction.
            Bundle arguments = new Bundle();
            RecipeStepFragment fragment = new RecipeStepFragment();

            mRecipeId = getIntent().getIntExtra(RecipeStepFragment.ARG_RECIPE_ID, -1);
            arguments.putInt(RecipeStepFragment.ARG_RECIPE_ID, mRecipeId);
            arguments.putInt(RecipeStepFragment.ARG_STEP_ID,
                    getIntent().getIntExtra(RecipeStepFragment.ARG_STEP_ID, -1));
            arguments.putInt(RecipeStepFragment.ARG_MAX_STEP_ID,
                    getIntent().getIntExtra(RecipeStepFragment.ARG_MAX_STEP_ID, -1));
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.recipe_step_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            Intent upIntent = new Intent(this, RecipeDetailActivity.class);

            upIntent.putExtra(RecipeDetailFragment.ARG_RECIPE_ID, mRecipeId);
            navigateUpTo(upIntent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static void start(@NonNull Context ctx, @NonNull Recipe recipe, int stepId) {
        if (stepId < 0 || stepId > recipe.getRequiredStepsCount()) {
            throw new IllegalArgumentException("Please provide a valid recipe-step identifier.");
        }

        start(ctx, recipe.getId(), stepId, recipe.getRequiredStepsCount() - 1);
    }

    private static void start(@NonNull Context ctx, int recipeId, int stepId, int maxStepId) {
        Intent intent = new Intent(ctx, RecipeStepActivity.class);
        intent.putExtra(RecipeStepFragment.ARG_RECIPE_ID, recipeId);
        intent.putExtra(RecipeStepFragment.ARG_STEP_ID, stepId);
        intent.putExtra(RecipeStepFragment.ARG_MAX_STEP_ID, maxStepId);

        ctx.startActivity(intent);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        // No-op
    }
}
