package ro.tuscale.udacity.bake.ui.fragments;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.functions.Consumer;
import ro.tuscale.udacity.bake.R;
import ro.tuscale.udacity.bake.models.Recipe;
import ro.tuscale.udacity.bake.models.RecipeRepository;
import ro.tuscale.udacity.bake.models.RecipeStep;
import ro.tuscale.udacity.bake.player.ComponentListener;

public class RecipeStepFragment extends Fragment {
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    public static final String ARG_RECIPE_ID = "recipe_id";
    public static final String ARG_STEP_ID = "step_id";
    public static final String ARG_MAX_STEP_ID = "max_step_id";

    private SimpleExoPlayerView mPlayerView;
    private TextView mStepDescription;
    private ImageButton mBtNavigateBack;
    private ImageButton mBtNavigateForward;

    private int mRecipeId;
    private int mStepId;
    private int mMaxStepId;
    private RecipeStep mStep;

    private SimpleExoPlayer mPlayer;
    private ComponentListener mComponentListener;
    private long mPlaybackPosition;
    private int mCurrentWindow;

    public RecipeStepFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle fragmentStartupArguments = getArguments();

        this.mComponentListener = new ComponentListener();
        if (fragmentStartupArguments.containsKey(ARG_RECIPE_ID) &&
                fragmentStartupArguments.containsKey(ARG_STEP_ID) &&
                fragmentStartupArguments.containsKey(ARG_MAX_STEP_ID)) {
            mRecipeId = fragmentStartupArguments.getInt(ARG_RECIPE_ID);
            mStepId = fragmentStartupArguments.getInt(ARG_STEP_ID);
            mMaxStepId = fragmentStartupArguments.getInt(ARG_MAX_STEP_ID);
        } else {
            // TODO: Now what? Nothing to show since no recipe id, step id and/or max step id was provided
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recipe_step, container, false);

        // View binding. Can't use ButterKnife because of different layouts used for this fragment
        mPlayerView = rootView.findViewById(R.id.recipe_step_video);
        mStepDescription = rootView.findViewById(R.id.txt_recipe_step_description);
        mBtNavigateBack = rootView.findViewById(R.id.ibt_recipe_step_nav_back);
        mBtNavigateForward = rootView.findViewById(R.id.ibt_recipe_step_nav_forward);

        // Init button navigation and tie them to event handlers
        if (mBtNavigateBack != null && mBtNavigateForward != null) {
            mBtNavigateBack.setEnabled(mStepId != 0);
            mBtNavigateForward.setEnabled(mStepId != mMaxStepId);

            mBtNavigateBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean willBackwardStillBePossible = (mStepId > 1);

                    mPlayer.stop();
                    if (mStepId > 0) {
                        mBtNavigateBack.setEnabled(mStepId - 1 != 0);
                        loadStep(mRecipeId, mStepId - 1);
                    }
                    mBtNavigateForward.setEnabled(mStepId != mMaxStepId);
                }
            });
            mBtNavigateForward.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mPlayer.stop();
                    if (mStepId < mMaxStepId) {
                        mBtNavigateForward.setEnabled(mStepId + 1 != mMaxStepId);
                        loadStep(mRecipeId, mStepId + 1);
                    }
                    mBtNavigateBack.setEnabled(mStepId > 0);
                }
            });
        } else {
            // We are most likely in a landscape environment. Give a fullscreen experience only if
            // we are not running on tablets
            if (getActivity().findViewById(R.id.recipe_step_container) == null) {
                mPlayerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
        }

        // Acquire the recipe & step info
        loadStep(mRecipeId, mStepId);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if ((Util.SDK_INT <= 23 || mPlayer == null)) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    private void loadStep(int recipeId, final int stepId) {
        RecipeRepository recipeRepository = ViewModelProviders.of(this).get(RecipeRepository.class);

        recipeRepository.getById(recipeId)
                .subscribe(new Consumer<Recipe>() {
                    @Override
                    public void accept(Recipe recipe) throws Exception {
                        Uri stepVideoUri;

                        mStepId = stepId;
                        mStep = recipe.getStepById(stepId);
                        stepVideoUri = mStep.getVideoUri();

                        if (stepVideoUri.getScheme() != null) {
                            MediaSource mediaSource = buildMediaSource(stepVideoUri);

                            if (mPlayer != null) {
                                mPlayer.prepare(mediaSource, true, false);
                            }
                        }

                        // Update title & other stuff
                        getActivity().setTitle(getString(R.string.recipe_step_activity_title_format, recipe.getName(), mStepId + 1, mMaxStepId + 1));
                        if (mStepDescription != null) {
                            mStepDescription.setText(mStep.getDescription());
                        }
                    }
                });
    }

    private void initializePlayer() {
        if (mPlayer == null) {
            TrackSelection.Factory adaptiveTrackSelectionFactory = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);

            // using a DefaultTrackSelector with an adaptive video selection factory
            mPlayer = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(getContext()),
                    new DefaultTrackSelector(adaptiveTrackSelectionFactory), new DefaultLoadControl());
            mPlayer.addListener(mComponentListener);
            mPlayer.setVideoDebugListener(mComponentListener);
            mPlayer.setAudioDebugListener(mComponentListener);
            mPlayer.setPlayWhenReady(true);
            mPlayer.seekTo(mCurrentWindow, mPlaybackPosition);

            // Tie media controller to session
            mPlayerView.setPlayer(mPlayer);
        }
    }

    private void releasePlayer() {
        if (mPlayer != null) {
            mPlaybackPosition = mPlayer.getCurrentPosition();
            mCurrentWindow = mPlayer.getCurrentWindowIndex();
            mPlayer.removeListener(mComponentListener);
            mPlayer.setVideoListener(null);
            mPlayer.setVideoDebugListener(null);
            mPlayer.setAudioDebugListener(null);
            mPlayer.release();
            mPlayer = null;
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory = new DefaultHttpDataSourceFactory("ua", BANDWIDTH_METER);
        DefaultExtractorsFactory mediaDatasourceFactory = new DefaultExtractorsFactory();

        return new ExtractorMediaSource(uri, dataSourceFactory, mediaDatasourceFactory, null, null);
    }
}
