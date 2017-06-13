package ro.tuscale.udacity.bake.ui.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import io.reactivex.functions.Consumer;
import ro.tuscale.udacity.bake.R;
import ro.tuscale.udacity.bake.Utils;
import ro.tuscale.udacity.bake.models.Recipe;
import ro.tuscale.udacity.bake.models.RecipeRepository;
import ro.tuscale.udacity.bake.models.RecipeStep;

public class RecipeStepFragment extends Fragment implements ExoPlayer.EventListener {
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    public static final String ARG_RECIPE_ID = "recipe_id";
    public static final String ARG_STEP_ID = "step_id";
    public static final String ARG_MAX_STEP_ID = "max_step_id";

    private SimpleExoPlayerView mPlayerView;
    private ImageView mStepThumbnailImage;
    private TextView mStepDescription;
    private ImageButton mBtNavigateBack;
    private ImageButton mBtNavigateForward;
    private long mPlayerPosition;

    private int mRecipeId;
    private int mStepId;
    private int mMaxStepId;
    private RecipeStep mStep;

    private SimpleExoPlayer mPlayer;
    private long mPlaybackPosition;
    private int mCurrentWindow;

    public RecipeStepFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle fragmentStartupArguments = getArguments();

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
        mStepThumbnailImage = rootView.findViewById(R.id.recipe_step_thumbnail);
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

        initializePlayer();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mStep != null) {
            Uri stepVideoUri = mStep.getVideoUri();

            if (stepVideoUri.getScheme() != null) {
                MediaSource mediaSource = buildMediaSource(stepVideoUri);

                if (mPlayer != null) {
                    mPlayer.prepare(mediaSource, true, false);
                    mPlayer.seekTo(mPlayerPosition);
                    mPlayer.setPlayWhenReady(true);
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        mPlayer.setPlayWhenReady(false);
        mPlayerPosition = mPlayer.getCurrentPosition();
    }

    @Override
    public void onStop() {
        super.onStop();

        releasePlayer();
    }

    private void loadStep(int recipeId, final int stepId) {
        RecipeRepository recipeRepository = ViewModelProviders.of(this).get(RecipeRepository.class);

        if (Utils.isInternetConnected()) {
            recipeRepository.getById(recipeId)
                    .subscribe(new Consumer<Recipe>() {
                        @Override
                        public void accept(Recipe recipe) throws Exception {
                            Uri stepVideoUri;
                            String stepThumbnailAddress;

                            mStepId = stepId;
                            mStep = recipe.getStepById(stepId);
                            stepVideoUri = mStep.getVideoUri();
                            stepThumbnailAddress = mStep.getThumbnailAddress();

                            // Prepare the holder and load the thumbnail (if available) while waiting for the movie to buffer
                            mPlayerView.setVisibility(View.GONE);
                            mStepThumbnailImage.setVisibility(View.VISIBLE);
                            if (stepThumbnailAddress.isEmpty() == false) {
                                Utils.getPicasso(getActivity())
                                        .load(stepThumbnailAddress)
                                        .placeholder(R.drawable.recipe_list_loading_placeholder).fit()
                                        .into(mStepThumbnailImage);
                            } else {
                                // default to loading until proven otherwise
                                mStepThumbnailImage.setImageResource(R.drawable.recipe_list_loading_placeholder);
                            }

                            // Buffer the video and go
                            if (stepVideoUri.getScheme() != null) {
                                MediaSource mediaSource = buildMediaSource(stepVideoUri);

                                if (mPlayer != null) {
                                    mPlayerPosition = 0;
                                    mPlayer.prepare(mediaSource, true, false);
                                    mPlayer.setPlayWhenReady(true);
                                }
                            } else {
                                // No video available. Default to broken image thumbnail
                                mStepThumbnailImage.setImageResource(R.drawable.ic_broken_image_white_24dp);
                            }

                            // Update title & other stuff
                            getActivity().setTitle(getString(R.string.recipe_step_activity_title_format, recipe.getName(), mStepId + 1, mMaxStepId + 1));
                            if (mStepDescription != null) {
                                mStepDescription.setText(mStep.getDescription());
                            }
                        }
                    });
        } else {
            Toast.makeText(getActivity(), R.string.no_internet_body, Toast.LENGTH_SHORT).show();
        }
    }

    private void initializePlayer() {
        if (mPlayer == null) {
            TrackSelection.Factory adaptiveTrackSelectionFactory = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);

            // using a DefaultTrackSelector with an adaptive video selection factory
            mPlayer = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(getContext()),
                    new DefaultTrackSelector(adaptiveTrackSelectionFactory), new DefaultLoadControl());
            mPlayer.addListener(this);
            mPlayer.seekTo(mCurrentWindow, mPlaybackPosition);

            // Tie media controller to session
            mPlayerView.setPlayer(mPlayer);
        }
    }

    private void releasePlayer() {
        if (mPlayer != null) {
            mPlaybackPosition = mPlayer.getCurrentPosition();
            mCurrentWindow = mPlayer.getCurrentWindowIndex();
            mPlayer.removeListener(this);
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

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
        // No-op
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        // No-op
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        // No-op
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case ExoPlayer.STATE_IDLE:
                // No-op
                break;
            case ExoPlayer.STATE_BUFFERING:
                // No-op
                break;
            case ExoPlayer.STATE_READY:
                // We are ready. Switching thumbnail with actual thing
                mPlayerView.setVisibility(View.VISIBLE);
                mStepThumbnailImage.setVisibility(View.GONE);
                break;
            case ExoPlayer.STATE_ENDED:
                // Reset the player
                mPlayerPosition = 0;
                mPlayer.seekTo(mPlayerPosition);
                mPlayer.setPlayWhenReady(false);
                break;
            default:
                // No-op
                break;
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        // No-op
    }

    @Override
    public void onPositionDiscontinuity() {
        // No-op
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
        // No-op
    }
}
