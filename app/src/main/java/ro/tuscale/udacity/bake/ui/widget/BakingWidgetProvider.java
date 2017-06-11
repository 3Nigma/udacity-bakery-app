package ro.tuscale.udacity.bake.ui.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import java.util.List;

import ro.tuscale.udacity.bake.R;
import ro.tuscale.udacity.bake.models.Recipe;
import ro.tuscale.udacity.bake.models.RecipeIngredient;
import ro.tuscale.udacity.bake.models.RecipeRepository;

public class BakingWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        final int count = appWidgetIds.length;
        final RecipeRepository recipeRepository = new RecipeRepository();
        final MutableLiveData<List<Recipe>> recipesSource = recipeRepository.getAll();

        recipesSource.observeForever(new Observer<List<Recipe>>() {
            @Override
            public void onChanged(@Nullable List<Recipe> recipes) {
                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);

                recipesSource.removeObserver(this);

                for (int i = 0; i < count; i++) {
                    int widgetId = appWidgetIds[i];
                    int selectedRecipeId = (int)(Math.random() * recipes.size());
                    Recipe recipe = recipes.get(selectedRecipeId);
                    String widgetTitle = String.format("'%s' - %s", recipe.getName(), context.getString(R.string.recipe_ingredients_label));
                    String widgetBody = "";

                    // Make the body
                    for (RecipeIngredient ingredient : recipe.getIngredients()) {
                        widgetBody += String.format("%.1f x %s %s, ", ingredient.getQuantity(), ingredient.getUnitOfMeasure(), ingredient.getName());
                    }
                    widgetBody = widgetBody.substring(0, widgetBody.length() - 2);

                    // Update content
                    remoteViews.setTextViewText(R.id.txt_widget_title, widgetTitle);
                    remoteViews.setTextViewText(R.id.txt_widget_ingredients, widgetBody);

                    // Prepare pending intent for refresh requests
                    Intent intent = new Intent(context, BakingWidgetProvider.class);
                    intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                            0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    remoteViews.setOnClickPendingIntent(R.id.bt_widget_next_recipe, pendingIntent);

                    // Update all the widgets
                    appWidgetManager.updateAppWidget(widgetId, remoteViews);
                }
            }
        });
    }
}
