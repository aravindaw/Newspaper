package com.github.ayltai.newspaper.data;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.util.Log;

import com.akaita.java.rxjava2debug.RxJava2Debug;
import com.github.ayltai.newspaper.util.DevUtils;
import com.github.ayltai.newspaper.util.RxUtils;

import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;

public abstract class RxLoader<D> extends Loader<List<D>> {
    private final Bundle args;

    private CompositeDisposable disposables;

    //region Constructors

    protected RxLoader(@NonNull final Context context, @Nullable final Bundle args) {
        super(context);

        this.args = args;
    }

    //endregion

    @CallSuper
    @Override
    protected void onForceLoad() {
        this.prepareDisposables();

        this.disposables.add(this.load(this.getContext(), this.args)
            .compose(RxUtils.applyFlowableBackgroundToMainSchedulers())
            .subscribe(
                this::deliverResult,
                error -> {
                    if (DevUtils.isLoggable()) Log.e(this.getClass().getSimpleName(), error.getMessage(), RxJava2Debug.getEnhancedStackTrace(error));
                }));
    }

    @CallSuper
    @Override
    protected void onStartLoading() {
        this.onForceLoad();
    }

    @CallSuper
    @Override
    protected void onStopLoading() {
        this.onCancelLoad();
    }

    @CallSuper
    @Override
    protected boolean onCancelLoad() {
        if (this.disposables != null && !this.disposables.isDisposed()) {
            this.disposables.dispose();
            this.disposables = null;
        }

        return true;
    }

    @CallSuper
    @Override
    protected void onReset() {
        this.onCancelLoad();
    }

    @NonNull
    protected abstract Flowable<List<D>> load(@NonNull Context context, @Nullable Bundle args);

    private void prepareDisposables() {
        if (this.disposables == null) this.disposables = new CompositeDisposable();
    }
}
