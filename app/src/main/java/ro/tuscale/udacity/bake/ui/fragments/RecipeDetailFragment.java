package ro.tuscale.udacity.bake.ui.fragments;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.functions.Consumer;
import ro.tuscale.udacity.bake.R;
import ro.tuscale.udacity.bake.Utils;
import ro.tuscale.udacity.bake.models.Recipe;
import ro.tuscale.udacity.bake.models.RecipeRepository;
import ro.tuscale.udacity.bake.models.RecipeStep;
import ro.tuscale.udacity.bake.ui.activities.RecipeStepActivity;
import ro.tuscale.udacity.bake.ui.adapters.IngredientsAdapter;
import ro.tuscale.udacity.bake.ui.adapters.StepsAdapter;

public class RecipeDetailFragment extends Fragment implements StepsAdapter.StepClickListener {
    public static final String ARG_RECIPE_ID = "recipe_id";

    @BindView(R.id.txt_ingredients_count) TextView mRecipeIngredientsCount;
    @BindView(R.id.txt_steps_count) TextView mRecipeStepsCount;
    @BindView(R.id.txt_portions_count) TextView mRecipeServingsCount;
    @BindView(R.id.recipe_detail_ingredients_list) RecyclerView mRecipeIngredientsList;
    @BindView(R.id.recipe_detail_steps_list) RecyclerView mRecipeStepsList;

    private boolean mIsTwoPane;
    private int mRecipeId;
    private Recipe mRecipe;
    private IngredientsAdapter mIngredientsAdapter;
    private StepsAdapter mStepsAdapter;

    public RecipeDetailFragment() {
        // Required by fragment manager
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle fragmentStartupArguments = getArguments();

        if (fragmentStartupArguments.containsKey(ARG_RECIPE_ID)) {
            mRecipeId = fragmentStartupArguments.getInt(ARG_RECIPE_ID);
        } else if (savedInstanceState != null) {
            mRecipeId = savedInstanceState.getInt(ARG_RECIPE_ID);
        } else {
            // TODO: Now what? Nothing to show since no recipe id was provided
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(ARG_RECIPE_ID, mRecipeId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.recipe_detail, container, false);
        RecipeRepository recipeRepository = ViewModelProviders.of(this).get(RecipeRepository.class);

        ButterKnife.bind(this, rootView);

        // Init the adapters and lists
        mIngredientsAdapter = new IngredientsAdapter();
        mStepsAdapter = new StepsAdapter(this);
        mRecipeIngredientsList.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecipeIngredientsList.setAdapter(mIngredientsAdapter);
        mRecipeStepsList.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecipeStepsList.setAdapter(mStepsAdapter);

        if (Utils.isInternetConnected()) {
            recipeRepository.getById(mRecipeId)
                    .subscribe(new Consumer<Recipe>() {
                        @Override
                        public void accept(Recipe recipe) throws Exception {
                            int recipeIngredients = recipe.getIngredientsCount();
                            int recipeSteps = recipe.getRequiredStepsCount();
                            int recipeServings = recipe.getServingsCount();

                            // Cache the result
                            mRecipe = recipe;

                            // Load the number region
                            mRecipeIngredientsCount.setText(getResources().getQuantityString(R.plurals.list_recipe_ingredients_format, recipeIngredients, recipeIngredients));
                            mRecipeStepsCount.setText(getResources().getQuantityString(R.plurals.list_recipe_steps_format, recipeSteps, recipeSteps));
                            mRecipeServingsCount.setText(getResources().getQuantityString(R.plurals.list_recipe_servings_format, recipeServings, recipeServings));

                            // Load the ingredients and the steps
                            mIngredientsAdapter.loadIngredientsFromRecipe(recipe);
                            mStepsAdapter.loadStepsFromRecipe(recipe);

                            Activity pActivity = getActivity();
                            if (pActivity instanceof RecipeLoadedNotifier) {
                                ((RecipeLoadedNotifier)pActivity).onRecipeLoaded(mRecipe);
                            }
                        }
                    });
        } else {
            Toast.makeText(getActivity(), R.string.no_internet_body, Toast.LENGTH_SHORT).show();
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // check and cache  activity is displayed on a wide screen container
        mIsTwoPane = (getActivity().findViewById(R.id.recipe_step_container) != null);
    }

    @Override
    public void onStepClicked(Recipe recipe, RecipeStep step) {
        if (mIsTwoPane) {
            Bundle arguments = new Bundle();
            RecipeStepFragment fragment = new RecipeStepFragment();

            arguments.putInt(RecipeStepFragment.ARG_RECIPE_ID, recipe.getId());
            arguments.putInt(RecipeStepFragment.ARG_STEP_ID, step.getId());
            arguments.putInt(RecipeStepFragment.ARG_MAX_STEP_ID, recipe.getRequiredStepsCount() - 1);
            fragment.setArguments(arguments);

            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.recipe_step_container, fragment)
                    .commit();
        } else {
            RecipeStepActivity.start(getContext(), recipe, step.getId());
        }
    }

    public interface RecipeLoadedNotifier {
        void onRecipeLoaded(Recipe recipe);
    }
}
