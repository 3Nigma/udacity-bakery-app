package ro.tuscale.udacity.bake;

import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ro.tuscale.udacity.bake.support.RecyclerViewItemCountAssertion;
import ro.tuscale.udacity.bake.ui.activities.RecipeDetailActivity;
import ro.tuscale.udacity.bake.ui.activities.RecipeListActivity;
import ro.tuscale.udacity.bake.ui.fragments.RecipeDetailFragment;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.BundleMatchers.hasEntry;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtras;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;

@RunWith(AndroidJUnit4.class)
public class RecipeListInstrumentedTest {
    @Rule
    public IntentsTestRule<RecipeListActivity> mActivityRule = new IntentsTestRule<>(RecipeListActivity.class);

    @Test
    public void checkAllRecipesLoaded() throws Exception {
        onView(withId(R.id.recipe_list)).check(new RecyclerViewItemCountAssertion(4));
    }

    @Test
    public void checkTriggeringOfRecipeDetail_onClick() throws Exception {
        onView(withId(R.id.recipe_list))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        intended (
                allOf (
                    hasComponent(RecipeDetailActivity.class.getName()),
                    hasExtras(allOf(hasEntry(equalTo(RecipeDetailFragment.ARG_RECIPE_ID), equalTo(1))))
                )
        );
    }
}
