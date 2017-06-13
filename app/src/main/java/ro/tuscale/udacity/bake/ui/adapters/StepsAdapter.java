package ro.tuscale.udacity.bake.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ro.tuscale.udacity.bake.R;
import ro.tuscale.udacity.bake.models.Recipe;
import ro.tuscale.udacity.bake.models.RecipeStep;

public class StepsAdapter extends RecyclerView.Adapter<StepsAdapter.ViewHolder> {

    private Recipe mRecipe;
    private List<RecipeStep> mSteps;
    private StepClickListener mStepClickListener;

    public StepsAdapter(@NonNull StepClickListener itemClickListener) {
        this.mSteps = new ArrayList<>();
        this.mStepClickListener = itemClickListener;
    }

    public void loadStepsFromRecipe(Recipe recipe) {
        List<RecipeStep> steps = (recipe == null ? null : recipe.getSteps());

        mSteps.clear();
        if (steps != null) {
            mRecipe = recipe;

            mSteps.addAll(steps);
        }
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recipe_step_item, parent, false);

        return new ViewHolder(view, mRecipe, mStepClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(mSteps.get(position));
    }

    @Override
    public int getItemCount() {
        return mSteps.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener

    {
        @BindView(R.id.txt_recipe_step_name) TextView mName;
        @BindView(R.id.bt_recipe_step_play) ImageButton mPlayButton;

        private Recipe mRecipe;
        private RecipeStep mStep;
        private View mRootView;
        private StepClickListener mStepClickHandler;

        public ViewHolder(@NonNull View itemView, @NonNull Recipe recipe, @NonNull StepClickListener clickHandler) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            this.mRecipe = recipe;
            this.mRootView = itemView;
            this.mStepClickHandler = clickHandler;

            // Tie events
            mRootView.setOnClickListener(this);
            mPlayButton.setOnClickListener(this);
        }

        public void bind(@NonNull RecipeStep step) {
            int stepNr = getAdapterPosition() + 1;

            mStep = step;
            mName.setText(String.format("%d. %s", stepNr, step.getShortDescription()));
        }

        @Override
        public void onClick(View view) {
            mStepClickHandler.onStepClicked(mRecipe, mStep);
        }
    }

    public interface StepClickListener {
        void onStepClicked(Recipe recipe, RecipeStep step);
    }
}
