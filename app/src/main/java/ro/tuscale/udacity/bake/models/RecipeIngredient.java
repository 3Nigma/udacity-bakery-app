package ro.tuscale.udacity.bake.models;

import com.google.gson.annotations.SerializedName;

public class RecipeIngredient {
    @SerializedName("quantity")
    private double mQuantity;
    @SerializedName("measure")
    private String mMeasure;
    @SerializedName("ingredient")
    private String mIngredient;

    public String getName() {
        return mIngredient;
    }
    public double getQuantity() {
        return mQuantity;
    }
    public String getUnitOfMeasure() {
        return mMeasure;
    }
}
