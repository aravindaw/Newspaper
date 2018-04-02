package com.github.ayltai.newspaper.analytics;

import javax.inject.Singleton;

import android.content.Context;
import android.support.annotation.NonNull;

import dagger.Module;
import dagger.Provides;

@Module
public final class AnalyticsModule {
    private final Context context;

    public AnalyticsModule(@NonNull final Context context) {
        this.context = context;
    }

    @NonNull
    @Singleton
    @Provides
    FabricEventLogger provideFabricEventLogger() {
        return new FabricEventLogger();
    }

    @NonNull
    @Singleton
    @Provides
    FirebaseEventLogger provideFirebaseEventLogger() {
        return new FirebaseEventLogger(this.context);
    }

    @NonNull
    @Singleton
    @Provides
    EventLogger provideEventLogger() {
        return new CompositeEventLogger.Builder()
            .add(this.provideFabricEventLogger())
            .add(this.provideFirebaseEventLogger())
            .build();
    }
}
