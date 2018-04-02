package com.github.ayltai.newspaper.app.config;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import android.support.annotation.NonNull;

import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

public final class AppConfig {
    //region Subscriptions

    private static final FlowableProcessor<Boolean> VIDEO_PLAYBACK_STATE_CHANGES = PublishProcessor.create();
    private static final FlowableProcessor<Long>    VIDEO_SEEK_POSITION_CHANGES  = PublishProcessor.create();

    //endregion

    //region Global app states

    private static final AtomicBoolean VIDEO_IS_PLAYING    = new AtomicBoolean(false);
    private static final AtomicLong    VIDEO_SEEK_POSITION = new AtomicLong(0);

    //endregion

    AppConfig() {
    }

    public boolean isVideoPlaying() {
        return AppConfig.VIDEO_IS_PLAYING.get();
    }

    public void setVideoPlaying(final boolean isPlaying) {
        VIDEO_IS_PLAYING.set(isPlaying);

        AppConfig.VIDEO_PLAYBACK_STATE_CHANGES.onNext(isPlaying);
    }

    public long getVideoSeekPosition() {
        return AppConfig.VIDEO_SEEK_POSITION.get();
    }

    public void setVideoSeekPosition(final long seekPosition) {
        VIDEO_SEEK_POSITION.set(seekPosition);

        AppConfig.VIDEO_SEEK_POSITION_CHANGES.onNext(seekPosition);
    }

    @NonNull
    public Flowable<Boolean> videoPlaybackStateChanges() {
        return AppConfig.VIDEO_PLAYBACK_STATE_CHANGES;
    }

    @NonNull
    public Flowable<Long> videoSeekPositionChanges() {
        return AppConfig.VIDEO_SEEK_POSITION_CHANGES;
    }
}
