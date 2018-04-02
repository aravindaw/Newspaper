package com.github.ayltai.newspaper.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import android.animation.Animator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.github.ayltai.newspaper.view.binding.Binder;
import com.github.ayltai.newspaper.view.binding.FullBinderFactory;
import com.github.ayltai.newspaper.view.binding.PartBinderFactory;
import com.github.ayltai.newspaper.view.binding.Binders;

import io.reactivex.disposables.Disposable;

public abstract class UniversalAdapter<M, V, T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> {
    public static final int          DEFAULT_ANIMATION_DURATION     = 600;
    public static final Interpolator DEFAULT_ANIMATION_INTERPOLATOR = new AccelerateDecelerateInterpolator();

    private final List<FullBinderFactory<M>>                     factories;
    private final List<Pair<PartBinderFactory<M, V>, Binder<V>>> binders = new ArrayList<>();

    private int lastItemPosition;

    protected UniversalAdapter(@NonNull final List<FullBinderFactory<M>> factories) {
        this.factories = factories;
    }

    @Override
    public int getItemCount() {
        return this.binders.size();
    }

    @Override
    public int getItemViewType(final int position) {
        return this.binders.get(position).first.getPartType();
    }

    @NonNull
    protected Binder<V> getBinder(final int position) {
        return this.binders.get(position).second;
    }

    @NonNull
    protected Iterable<Animator> getItemAnimators(@NonNull final View view) {
        return Collections.emptyList();
    }

    protected long getAnimationDuration() {
        return UniversalAdapter.DEFAULT_ANIMATION_DURATION;
    }

    @Nullable
    protected Interpolator getAnimationInterpolator() {
        return UniversalAdapter.DEFAULT_ANIMATION_INTERPOLATOR;
    }

    public void clear() {
        for (final Pair<PartBinderFactory<M, V>, Binder<V>> binder : this.binders) {
            if (binder.second instanceof Disposable) {
                final Disposable disposable = (Disposable)binder.second;
                if (!disposable.isDisposed()) disposable.dispose();
            }
        }

        this.binders.clear();

        this.notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(final T holder, final int position) {
        final int adapterPosition = holder.getAdapterPosition();

        if (adapterPosition > this.lastItemPosition) {
            final Interpolator interpolator = this.getAnimationInterpolator();
            final long         duration     = this.getAnimationDuration();

            for (final Animator animator : this.getItemAnimators(holder.itemView)) {
                animator.setInterpolator(interpolator);
                animator.setDuration(duration).start();
            }

            this.lastItemPosition = adapterPosition;
        } else {
            holder.itemView.setAlpha(1f);
            holder.itemView.setScaleY(1f);
            holder.itemView.setScaleX(1f);
            holder.itemView.setTranslationY(0f);
            holder.itemView.setTranslationX(0f);
            holder.itemView.setRotation(0f);
            holder.itemView.setRotationY(0f);
            holder.itemView.setRotationX(0f);
            holder.itemView.setPivotY(holder.itemView.getMeasuredHeight() / 2);
            holder.itemView.setPivotX(holder.itemView.getMeasuredWidth() / 2);
            holder.itemView.animate().setInterpolator(null).setStartDelay(0L);
        }
    }

    /**
     * Calls this method instead of calling {@link #notifyDataSetChanged()} to update its associated {@link Binder}s.
     * @param items The items changed.
     */
    public void onDataSetChanged(@NonNull final Iterable<M> items) {
        this.binders.addAll(Binders.createBinders(items, this.factories));

        this.notifyDataSetChanged();
    }

    /**
     * Calls this method instead of calling {@link #notifyItemRangeInserted(int, int)} to update its associated {@link Binder}s.
     * @param items The items inserted.
     * @param positionStart Position of the first item that was inserted.
     */
    public void onItemRangeInserted(@NonNull final Collection<M> items, final int positionStart) {
        this.binders.addAll(positionStart, Binders.createBinders(items, this.factories));

        this.notifyItemRangeInserted(positionStart, items.size());
    }
}
