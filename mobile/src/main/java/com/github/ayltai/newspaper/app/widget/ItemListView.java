package com.github.ayltai.newspaper.app.widget;

import java.util.List;
import java.util.Set;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Filterable;
import android.widget.TextView;

import com.github.ayltai.newspaper.R;
import com.github.ayltai.newspaper.app.data.model.Item;
import com.github.ayltai.newspaper.app.view.ItemListAdapter;
import com.github.ayltai.newspaper.app.view.ItemListPresenter;
import com.github.ayltai.newspaper.util.ViewUtils;
import com.github.ayltai.newspaper.widget.ListView;
import com.jakewharton.rxbinding2.support.v7.widget.RxRecyclerView;

import io.reactivex.disposables.Disposable;

public abstract class ItemListView extends ListView<Item> implements ItemListPresenter.View, Disposable, LifecycleObserver {
    //region Supports initial searching

    private List<String> categories;
    private Set<String>  sources;
    private CharSequence searchText;

    //endregion

    //region Constructors

    public ItemListView(@NonNull final Context context) {
        super(context);
        this.init();
    }

    public ItemListView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public ItemListView(@NonNull final Context context, @Nullable final AttributeSet attrs, @AttrRes final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init();
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public ItemListView(@NonNull final Context context, @Nullable final AttributeSet attrs, @AttrRes final int defStyleAttr, @StyleRes final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.init();
    }

    //endregion

    //region Search properties

    public void setCategories(@NonNull final List<String> categories) {
        this.categories = categories;
    }

    public void setSources(@NonNull final Set<String> sources) {
        this.sources = sources;
    }

    public void setSearchText(final CharSequence searchText) {
        this.searchText = searchText;
    }

    //endregion

    @Override
    public boolean isDisposed() {
        return false;
    }

    @LayoutRes
    @Override
    protected int getSwipeRefreshLayoutId() {
        return R.id.swipeRefreshLayout;
    }

    @IdRes
    @Override
    protected int getRecyclerViewId() {
        return R.id.recyclerView;
    }

    @IdRes
    @Override
    protected int getLoadingViewId() {
        return R.id.loading;
    }

    @IdRes
    @Override
    protected int getEmptyViewId() {
        return android.R.id.empty;
    }

    @Override
    protected int getInfiniteLoadingThreshold() {
        return ListView.NO_INFINITE_LOADING;
    }

    //region Methods

    @Override
    public void bind(@NonNull final List<Item> models) {
        super.bind(models);

        if (!TextUtils.isEmpty(this.searchText) && this.adapter instanceof Filterable) {
            final ItemListAdapter.ItemListFilter filter = (ItemListAdapter.ItemListFilter)((Filterable)this.adapter).getFilter();

            if (filter != null) {
                filter.setCategories(this.categories);
                filter.setSources(this.sources);
                filter.setFeatured(true);

                // FIXME: performFiltering() should be executed on a background thread, but Filter.FilterResults class is protected
                filter.publishResults(this.searchText, filter.performFiltering(this.searchText));

                if (this.adapter.getItemCount() == 0) this.showEmptyView();
            }
        }
    }

    @Override
    public void scrollTo(final int scrollPosition) {
        if (scrollPosition > 0) this.recyclerView.scrollToPosition(scrollPosition);
    }

    @Override
    public void showLoadingView() {
        super.showLoadingView();

        if (this.loadingView != null) ViewUtils.startShimmerAnimation(this.loadingView);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    @Override
    public void dispose() {
        if (this.adapter instanceof Disposable) {
            final Disposable disposable = (Disposable)this.adapter;
            if (!disposable.isDisposed()) disposable.dispose();
        }
    }

    //endregion

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        final View view = this.findViewById(R.id.scrolling_background);
        if (view != null) this.manageDisposable(RxRecyclerView.scrollEvents(this.recyclerView).subscribe(event -> view.setTranslationY(view.getTranslationY() - event.dy())));
    }

    private void init() {
        ((TextView)this.emptyView.findViewById(R.id.empty_title)).setText(R.string.empty_news_title);
        ((TextView)this.emptyView.findViewById(R.id.empty_description)).setText(R.string.empty_news_description);

        final LifecycleOwner owner = this.getLifecycleOwner();
        if (owner != null) owner.getLifecycle().addObserver(this);
    }
}