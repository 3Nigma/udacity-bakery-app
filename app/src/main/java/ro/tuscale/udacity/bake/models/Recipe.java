package ro.tuscale.udacity.bake.models;

import android.arch.lifecycle.ViewModel;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Recipe {
    @SerializedName("id")
    private int mId;
    @SerializedName("name")
    private String mName;
    @SerializedName("ingredients")
    private List<RecipeIngredient> mIngredients;
    @SerializedName("steps")
    private List<RecipeStep> mSteps;
    @SerializedName("servings")
    private int mServingsCount;
    @SerializedName("image")
    private String mImageUrl;

    public int getId() {
        return mId;
    }
    public String getName() {
        return mName;
    }

    public List<RecipeIngredient> getIngredients() {
        return mIngredients;
    }
    public int getIngredientsCount() {
        return mIngredients.size();
    }

    public List<RecipeStep> getSteps() {
        return mSteps;
    }
    public int getRequiredStepsCount() {
        return mSteps.size();
    }
    public RecipeStep getStepById(int stepId) {
        RecipeStep step = null;

        for (RecipeStep checkedStep : mSteps) {
            if (checkedStep.getId() == stepId) {
                step = checkedStep;
                break;
            }
        }

        return step;
    }

    public int getServingsCount() {
        return mServingsCount;
    }
    public String getImageUrl() {
        return mImageUrl;
    }
}
