package ro.tuscale.udacity.bake;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.ConnectException;
import java.util.List;

import ro.tuscale.udacity.bake.models.Recipe;
import ro.tuscale.udacity.bake.models.RecipeRepository;
import ro.tuscale.udacity.bake.support.RecyclerViewItemCountAssertion;
import ro.tuscale.udacity.bake.ui.activities.RecipeDetailActivity;
import ro.tuscale.udacity.bake.ui.activities.RecipeStepActivity;
import ro.tuscale.udacity.bake.ui.fragments.RecipeDetailFragment;
import ro.tuscale.udacity.bake.ui.fragments.RecipeStepFragment;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.BundleMatchers.hasEntry;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtras;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;

@RunWith(AndroidJUnit4.class)
public class RecipeDetailInstrumentedTest {
    @Rule
    public ActivityTestRule<RecipeDetailActivity> mActivityRule = new IntentsTestRule<RecipeDetailActivity>(RecipeDetailActivity.class, true, false);

    private static final int LOADED_RECIPE_ID = 1;
    private static Recipe mRecipe;

    @BeforeClass
    public static void beforeAll() throws ConnectException {
        RecipeRepository recipeRepository = new RecipeRepository();

        if (Utils.isInternetConnected()) {
            final MutableLiveData<List<Recipe>> recipeSource = recipeRepository.getAll();

            recipeSource.observeForever(new Observer<List<Recipe>>() {
                @Override
                public void onChanged(@Nullable List<Recipe> recipes) {
                    recipeSource.removeObserver(this);

                    for (Recipe recipe : recipes) {
                        if (recipe.getId() == LOADED_RECIPE_ID) {
                            mRecipe = recipe;
                            break;
                        }
                    }
                }
            });
        } else {
            throw new ConnectException("No internet available.");
        }
    }

    @Before
    public void beforeEach() {
        startActivity();
    }

    @Test
    public void checkRecipeIngredientsCount() throws Exception {
        onView(withId(R.id.recipe_detail_ingredients_list)).check(new RecyclerViewItemCountAssertion(mRecipe.getIngredientsCount()));
    }

    @Test
    public void checkRecipeStepsCount() throws Exception {
        onView(withId(R.id.recipe_detail_steps_list)).check(new RecyclerViewItemCountAssertion(mRecipe.getRequiredStepsCount()));
    }

    @Test
    public void checkTriggeringOfRecipeStep_onClick() throws Exception {
        // Scroll all the way down to have the steps list in view before clicking the items
        onView(withId(android.R.id.content)).perform(ViewActions.swipeUp());
        onView(withId(R.id.recipe_detail_steps_list)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        intended(
                allOf(
                        hasComponent(RecipeStepActivity.class.getName()),
                        hasExtras(
                                allOf(
                                    hasEntry(equalTo(RecipeStepFragment.ARG_RECIPE_ID), equalTo(LOADED_RECIPE_ID)),
                                    hasEntry(equalTo(RecipeStepFragment.ARG_STEP_ID), equalTo(0)),
                                    hasEntry(equalTo(RecipeStepFragment.ARG_MAX_STEP_ID), equalTo(mRecipe.getRequiredStepsCount() - 1))
                                )
                        )
                )
        );
    }

    private void startActivity() {
        Intent intent = new Intent();

        intent.putExtra(RecipeDetailFragment.ARG_RECIPE_ID, LOADED_RECIPE_ID);
        mActivityRule.launchActivity(intent);
    }
}
