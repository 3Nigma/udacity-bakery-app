package ro.tuscale.udacity.bake.player;

import android.view.Surface;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import timber.log.Timber;

public class ComponentListener implements ExoPlayer.EventListener, VideoRendererEventListener,
        AudioRendererEventListener {
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
        String stateString;

        switch (playbackState) {
            case ExoPlayer.STATE_IDLE:
                stateString = "ExoPlayer.STATE_IDLE      -";
                break;
            case ExoPlayer.STATE_BUFFERING:
                stateString = "ExoPlayer.STATE_BUFFERING -";
                break;
            case ExoPlayer.STATE_READY:
                stateString = "ExoPlayer.STATE_READY     -";
                break;
            case ExoPlayer.STATE_ENDED:
                stateString = "ExoPlayer.STATE_ENDED     -";
                break;
            default:
                stateString = "UNKNOWN_STATE             -";
                break;
        }

        Timber.d("changed state to " + stateString + " playWhenReady: " + playWhenReady);
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

    @Override
    public void onAudioEnabled(DecoderCounters counters) {
        // No-op
    }

    @Override
    public void onAudioSessionId(int audioSessionId) {
        // No-op
    }

    @Override
    public void onAudioDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {
        // No-op
    }

    @Override
    public void onAudioInputFormatChanged(Format format) {
        // No-op
    }

    @Override
    public void onAudioTrackUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {
        // No-op
    }

    @Override
    public void onAudioDisabled(DecoderCounters counters) {
        // No-op
    }

    @Override
    public void onVideoEnabled(DecoderCounters counters) {
        // No-op
    }

    @Override
    public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {
        // No-op
    }

    @Override
    public void onVideoInputFormatChanged(Format format) {
        // No-op
    }

    @Override
    public void onDroppedFrames(int count, long elapsedMs) {
        // No-op
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        // No-op
    }

    @Override
    public void onRenderedFirstFrame(Surface surface) {
        // No-op
    }

    @Override
    public void onVideoDisabled(DecoderCounters counters) {
        // No-op
    }
}
