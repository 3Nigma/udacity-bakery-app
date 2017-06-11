package ro.tuscale.udacity.bake.models;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import ro.tuscale.udacity.bake.BuildConfig;

/**
 * Handles querying of recipes (with lifecycle aware caching)
 */
public class RecipeRepository extends ViewModel {
    private Retrofit mRetrofit;

    private MutableLiveData<List<Recipe>> mRecipes;

    public RecipeRepository() {
        MyService myService;

        this.mRetrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BACKEND_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        // Since the web service has only 1 access point with little data, retrieve it all at once
        myService = mRetrofit.create(MyService.class);
        this.mRecipes = new MutableLiveData<>();

        myService.getRecipes()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Recipe>>() {
                    @Override
                    public void accept(List<Recipe> recipes) throws Exception {
                        mRecipes.setValue(recipes);
                    }
                });
    }

    public MutableLiveData<List<Recipe>> getAll() {
        return mRecipes;
    }

    public Single<Recipe> getById(final int id) {
        return Single.create(new SingleOnSubscribe<Recipe>() {
            @Override
            public void subscribe(final SingleEmitter<Recipe> e) throws Exception {
                List<Recipe> recipes = mRecipes.getValue();
                Observer<List<Recipe>> mRecipesObserver = new Observer<List<Recipe>>() {
                    @Override
                    public void onChanged(@Nullable List<Recipe> recipes) {
                        Recipe returnedRecipe = null;

                        for (Recipe checkedRecipe : recipes) {
                            if (checkedRecipe.getId() == id) {
                                returnedRecipe = checkedRecipe;
                                break;
                            }
                        }

                        if (returnedRecipe != null) {
                            e.onSuccess(returnedRecipe);
                        } else {
                            e.onError(new IndexOutOfBoundsException("The requested recipe does not exist."));
                        }

                        mRecipes.removeObserver(this);
                    }
                };

                if (recipes == null || recipes.size() == 0) {
                    mRecipes.observeForever(mRecipesObserver);
                } else {
                    mRecipesObserver.onChanged(recipes);
                }
            }
        });
    }

    /* package */ interface MyService {
        @GET("recipes")
        Single<List<Recipe>> getRecipes();
    }
}
