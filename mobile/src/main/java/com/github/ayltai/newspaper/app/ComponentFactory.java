package com.github.ayltai.newspaper.app;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.support.annotation.NonNull;

import com.github.ayltai.newspaper.analytics.AnalyticsComponent;
import com.github.ayltai.newspaper.analytics.AnalyticsModule;
import com.github.ayltai.newspaper.analytics.DaggerAnalyticsComponent;
import com.github.ayltai.newspaper.app.config.ConfigComponent;
import com.github.ayltai.newspaper.app.config.ConfigModule;
import com.github.ayltai.newspaper.app.config.DaggerConfigComponent;
import com.github.ayltai.newspaper.data.DaggerDataComponent;
import com.github.ayltai.newspaper.data.DataComponent;
import com.github.ayltai.newspaper.data.DataModule;
import com.github.ayltai.newspaper.media.DaggerImageComponent;
import com.github.ayltai.newspaper.media.ImageComponent;
import com.github.ayltai.newspaper.media.ImageModule;

import io.reactivex.disposables.Disposable;

public final class ComponentFactory implements Disposable, LifecycleObserver {
    private static ComponentFactory instance;

    private ConfigComponent    configComponent;
    private ImageComponent     imageComponent;
    private AnalyticsComponent analyticsComponent;

    public static ComponentFactory getInstance() {
        if (ComponentFactory.instance == null) ComponentFactory.init();

        return ComponentFactory.instance;
    }

    public static void init() {
        ComponentFactory.instance = new ComponentFactory();
    }

    @Override
    public boolean isDisposed() {
        return false;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    @Override
    public void dispose() {
        this.configComponent    = null;
        this.imageComponent     = null;
        this.analyticsComponent = null;

        ComponentFactory.init();
    }

    public ConfigComponent getConfigComponent(@NonNull final Activity activity) {
        if (this.configComponent == null) this.configComponent = DaggerConfigComponent.builder()
            .configModule(new ConfigModule(activity))
            .build();

        return this.configComponent;
    }

    public DataComponent getDataComponent(@NonNull final Context context) {
        return DaggerDataComponent.builder()
            .dataModule(new DataModule(context.getApplicationContext()))
            .build();
    }

    public ImageComponent getImageComponent(@NonNull final Context context) {
        if (this.imageComponent == null) this.imageComponent = DaggerImageComponent.builder()
            .imageModule(new ImageModule(context.getApplicationContext()))
            .build();

        return this.imageComponent;
    }

    public AnalyticsComponent getAnalyticsComponent(@NonNull final Context context) {
        if (this.analyticsComponent == null) this.analyticsComponent = DaggerAnalyticsComponent.builder()
            .analyticsModule(new AnalyticsModule(context.getApplicationContext()))
            .build();

        return this.analyticsComponent;
    }
}
