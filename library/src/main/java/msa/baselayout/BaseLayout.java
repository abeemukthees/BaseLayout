package msa.baselayout;

import android.content.Context;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelWithHolder;

import java.lang.ref.WeakReference;
import java.util.List;

import msa.baselayout.receivers.NetworkStateReceiver;

/**
 * Created by Abhimuktheeswarar on 07-08-2017.
 */

public class BaseLayout<T extends EpoxyModelWithHolder<EpoxyHolder>> extends FrameLayout implements BaseEpoxyAdapter.BaseEpoxyAdapterInterface, SwipeRefreshLayout.OnRefreshListener, NetworkStateReceiver.NetworkStateListener {

    private static final String TAG = BaseLayout.class.getSimpleName();
    int currentPage, currentPosition;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private FrameLayout frameLayout_Loading;
    private ProgressBar progressBar;
    private TextView textView_NoNetwork, textView_NoLocationAccess, textView_NotAvailable;
    private BaseLayoutInterface baseLayoutInterface;
    private LinearLayoutManager linearLayoutManager;
    private GridLayoutManager gridLayoutManager;
    private BaseEpoxyAdapter baseEpoxyAdapter;
    private EndlessRecyclerViewScrollListener scrollListener;
    private OnLayoutChangeListener onLayoutChangeListener;
    private boolean isFirstTimeLoading, isNetworkAvailable, isLoading, isLoadMoreEnabled = true, isSwipeRefreshEnabled = true;
    private WeakReference<NetworkStateReceiver> networkStateReceiver;
    private Context context;


    public BaseLayout(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public BaseLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public BaseLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BaseLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_baselayout, this);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView = findViewById(R.id.recycler_common);
        frameLayout_Loading = findViewById(R.id.frame_loading);
        progressBar = findViewById(R.id.progressBar);
        textView_NoNetwork = findViewById(R.id.text_noNetwork);
        textView_NoLocationAccess = findViewById(R.id.text_noLocationAccess);
        textView_NotAvailable = findViewById(R.id.text_notAvailable);

        frameLayout_Loading.setVisibility(GONE);
        progressBar.setVisibility(GONE);
        textView_NoNetwork.setVisibility(GONE);
        textView_NoLocationAccess.setVisibility(GONE);
        recyclerView.setScrollContainer(true);

        setFirstTimeLoading(true);

        @ColorRes
        int accentColor = fetchAccentColor();

        swipeRefreshLayout.setColorSchemeResources(accentColor);

        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setItemPrefetchEnabled(true);
        linearLayoutManager.setInitialPrefetchItemCount(20);
        recyclerView.setLayoutManager(linearLayoutManager);
        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        baseEpoxyAdapter = new BaseEpoxyAdapter(this);

        recyclerView.setAdapter(baseEpoxyAdapter);
        if (isLoadMoreEnabled) {
            scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager, 20) {

                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    currentPage = page;
                    if (baseLayoutInterface != null && isNetworkAvailable) {
                        if (isLoadMoreEnabled) {
                            showLoadingMore();
                            baseLayoutInterface.onLoadMore(page);
                        }

                    } else if (!isNetworkAvailable && !isFirstTimeLoading) {
                        //Log.d(TAG, "Network not available in LoadMore");
                        if (isLoadMoreEnabled) showNetworkError();
                    }
                }
            };
            recyclerView.addOnScrollListener(scrollListener);
        }
        if (isSwipeRefreshEnabled) {
            swipeRefreshLayout.setEnabled(isSwipeRefreshEnabled);
            swipeRefreshLayout.setOnRefreshListener(this);
        } else swipeRefreshLayout.setEnabled(isSwipeRefreshEnabled);
        networkStateReceiver = new WeakReference<NetworkStateReceiver>(new NetworkStateReceiver());
        networkStateReceiver.get().setNetworkStateListener(this);
        getContext().registerReceiver(networkStateReceiver.get(), new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        showLoading();

        onLayoutChangeListener = new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

                if (bottom < oldBottom) {
                    final int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                    //Log.d(BaseLayout.class.getSimpleName(), "onLayoutChange TRUE -> POSITION :" + lastVisibleItemPosition + " ==" + baseEpoxyAdapter.getLastAddedItemPosition());
                    if (lastVisibleItemPosition == baseEpoxyAdapter.getLastAddedItemPosition() || (lastVisibleItemPosition == (baseEpoxyAdapter.getLastAddedItemPosition() - 2))) {
                        recyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                recyclerView.scrollToPosition(baseEpoxyAdapter.getItemCount() - 1);
                            }
                        });
                    }
                }
            }
        };

        recyclerView.addOnLayoutChangeListener(onLayoutChangeListener);
    }

    public void setGridLayoutManager(int count) {
        baseEpoxyAdapter = new BaseEpoxyAdapter(this, true);
        recyclerView.swapAdapter(baseEpoxyAdapter, true);
        gridLayoutManager = new GridLayoutManager(getContext(), count);
        baseEpoxyAdapter.setSpanCount(count);
        gridLayoutManager.setSpanSizeLookup(baseEpoxyAdapter.getSpanSizeLookup());
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    public void setBaseLayoutForSearch() {
        baseEpoxyAdapter = new BaseEpoxyAdapter(this, false, true);
        recyclerView.swapAdapter(baseEpoxyAdapter, true);
    }

    public void setBaseLayoutInterface(BaseLayoutInterface baseLayoutInterface) {
        this.baseLayoutInterface = baseLayoutInterface;
    }

    public void setLoadMoreEnabled(boolean loadMoreEnabled) {
        Log.d(TAG, "Load more enabled = " + loadMoreEnabled);
        isLoadMoreEnabled = loadMoreEnabled;
        if (isLoadMoreEnabled) recyclerView.addOnScrollListener(scrollListener);
        else {
            hideLoadingMore();
            recyclerView.removeOnScrollListener(scrollListener);
        }
    }

    public void setWhiteBackground() {
        recyclerView.setBackgroundColor(Color.WHITE);

    }

    public void setBackgroundColor(@ColorInt int color) {
        recyclerView.setBackgroundColor(color);

    }

    public void setFrameLayoutLoadingTransperant() {
        frameLayout_Loading.setBackgroundColor(Color.TRANSPARENT);
    }

    public boolean isSwipeRefreshEnabled() {
        return isSwipeRefreshEnabled;
    }

    public void setSwipeRefreshEnabled(boolean swipeRefreshEnabled) {
        isSwipeRefreshEnabled = swipeRefreshEnabled;
        swipeRefreshLayout.setEnabled(swipeRefreshEnabled);
    }

    public boolean isFirstTimeLoading() {
        return isFirstTimeLoading;
    }

    public void setFirstTimeLoading(boolean firstTimeLoading) {
        isFirstTimeLoading = firstTimeLoading;
    }

    public void setRecyclerViewItemDecoration(RecyclerView.ItemDecoration itemDecoration) {
        recyclerView.addItemDecoration(itemDecoration);
    }

    public int getItemPosition(T item) {
        return baseEpoxyAdapter.getItemPosition(item);
    }

    public void addItem(List<T> items) {
        if (isFirstTimeLoading()) {
            baseEpoxyAdapter.removeAllItems();
            if (scrollListener != null) scrollListener.resetState();
        }
        setFirstTimeLoading(false);
        swipeRefreshLayout.setRefreshing(false);
        hideLoadingMore();
        hideLoading();
        hideNetworkError();
        hideNotAvailable();
        //Log.d(BaseLayout.class.getSimpleName(), "addItem = " + (items == null));
        baseEpoxyAdapter.addItem(items);
    }

    public void addItem(T item) {
        if (isFirstTimeLoading()) {
            baseEpoxyAdapter.removeAllItems();
            if (scrollListener != null) scrollListener.resetState();
        }
        setFirstTimeLoading(false);
        swipeRefreshLayout.setRefreshing(false);
        hideNetworkError();
        hideLoadingMore();
        hideLoading();
        hideNotAvailable();
        //Log.d(BaseLayout.class.getSimpleName(), "addItem = " + (item == null));
        baseEpoxyAdapter.addItem(item);
    }

    public void addItemAtTop(T item) {
        baseEpoxyAdapter.addItemAtTop(item);
    }

    public void updateItem(T item) {
        baseEpoxyAdapter.updateItem(item);
    }

    @Deprecated
    public void delayedUpdateItem(T item) {
        baseEpoxyAdapter.delayedUpdateItem(item);

    }

    public void removeItem(T item) {
        baseEpoxyAdapter.removeItem(item);
    }

    public void removeAllItems() {
        baseEpoxyAdapter.removeAllItems();
    }

    public void removeAllItemsCompletely() {
        baseEpoxyAdapter.removeAllItemsCompletely();
    }

    public void showLoadingMore() {
        if (!isFirstTimeLoading()) {
            if (isLoadMoreEnabled) baseEpoxyAdapter.showLoadingMore();
        }
    }

    public void showLoadingMoreForced() {
        if (!isFirstTimeLoading()) {
            baseEpoxyAdapter.showLoadingMore();
        }
    }

    public void hideLoadingMore() {
        baseEpoxyAdapter.hideLoadingMore();
    }

    public void showLoading() {
        isLoading = true;
        //Log.d(TAG, "showLoading");
        frameLayout_Loading.setVisibility(VISIBLE);
        //progressBar.setVisibility(VISIBLE);
        //progressBar.smoothToShow();
        if (isNetworkAvailable) {
            progressBar.setVisibility(VISIBLE);
        } //else if (!isFirstTimeLoading) textView_NoNetwork.setVisibility(VISIBLE);
        hideNotAvailable();
    }

    public void hideLoading() {
        isLoading = false;
        progressBar.setVisibility(GONE);
        textView_NoLocationAccess.setVisibility(GONE);
        frameLayout_Loading.setVisibility(GONE);
        if (isNetworkAvailable) textView_NoNetwork.setVisibility(GONE);
    }

    public void showError() {
        Log.e(TAG, "Showing error");
    }

    public void hideError() {
        Log.e(TAG, "hiding error");
    }

    public void scrollToPosition(final int position) {
        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                recyclerView.scrollToPosition(position);
            }
        }, 100);

    }

    public void smoothScrollToPosition(final int position) {
        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                recyclerView.smoothScrollToPosition(position);
            }
        }, 100);

    }

    public void scrollToLastItem() {
        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                recyclerView.smoothScrollToPosition(baseEpoxyAdapter.getItemCount() - 1);
            }
        }, 100);
    }

    public void showNotAvailable() {
        progressBar.setVisibility(GONE);
        frameLayout_Loading.setVisibility(VISIBLE);
        textView_NotAvailable.setVisibility(VISIBLE);
    }

    public void showNotAvailable(String message) {
        progressBar.setVisibility(GONE);
        frameLayout_Loading.setVisibility(VISIBLE);
        textView_NotAvailable.setText(message);
        textView_NotAvailable.setVisibility(VISIBLE);
    }

    public void hideNotAvailable() {
        //frameLayout_Loading.setVisibility(GONE);
        textView_NotAvailable.setVisibility(GONE);
    }

    public int getItemCount() {
        return linearLayoutManager.getItemCount();
    }

    public void onCompleted() {
        setLoadMoreEnabled(false);
    }

    public void reset() {
        setFirstTimeLoading(true);
        showLoading();
        // NOT TESTED WITH GRID LAYOUT MANAGER
        baseEpoxyAdapter.removeAllItems();
    }

    public void resetScroll() {
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager, 20) {

            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                currentPage = page;
                if (baseLayoutInterface != null && isNetworkAvailable) {
                    if (isLoadMoreEnabled) {
                        showLoadingMore();
                        baseLayoutInterface.onLoadMore(page);
                    }

                } else if (!isNetworkAvailable && !isFirstTimeLoading) {
                    //Log.d(TAG, "Network not available in LoadMore");
                    if (isLoadMoreEnabled) showNetworkError();
                }
            }
        };
        recyclerView.addOnScrollListener(scrollListener);
    }

    public void showLocationError(boolean isGranted) {
        if (!isGranted) {
            frameLayout_Loading.setVisibility(VISIBLE);
            progressBar.setVisibility(GONE);
            textView_NoLocationAccess.setVisibility(VISIBLE);
        } else {
            textView_NoLocationAccess.setVisibility(GONE);
            frameLayout_Loading.setVisibility(VISIBLE);
            progressBar.setVisibility(VISIBLE);
        }
        hideNotAvailable();
    }

    private void showNetworkError() {
        //Log.d(TAG, "showNetworkError");
        if (isFirstTimeLoading()) {
            frameLayout_Loading.setVisibility(VISIBLE);
            textView_NoNetwork.setVisibility(VISIBLE);
            baseLayoutInterface.onError();

            if (!isLoading) {
                //Log.d(TAG, "Going to hide AVLoding !!!");
                progressBar.setVisibility(GONE);
            } //else Log.d(TAG, "Not hiding AVLoading...");

            if (progressBar.getVisibility() == VISIBLE) {


            }
        } else {
            //Log.d(TAG, "Not the 1st time dude");
            baseEpoxyAdapter.showNetworkError();
        }

        hideNotAvailable();

        //Log.e(TAG, "No network available");
    }

    private void hideNetworkError() {
        //Log.d(TAG, "hideNetworkError");
        if (baseEpoxyAdapter.getNetworkErrorPosition() != -1) {
            baseEpoxyAdapter.hideNetworkError();
            if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == baseEpoxyAdapter.getLastAddedItemPosition())
                baseEpoxyAdapter.showLoadingMoreAfterNetworkError();
            else if (isLoadMoreEnabled) baseEpoxyAdapter.showLoadingMore();

        }
        textView_NoNetwork.setVisibility(GONE);
        if (!isFirstTimeLoading()) {
            frameLayout_Loading.setVisibility(GONE);
        }
        //Log.e(TAG, "Network available");
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public RecyclerView.Adapter getRecyclerViewAdapter() {
        return baseEpoxyAdapter;
    }

    @Override
    public void onRefresh() {
        if (baseLayoutInterface != null && isNetworkAvailable) {
            setFirstTimeLoading(true);
            if (isLoadMoreEnabled) setLoadMoreEnabled(true);
            baseLayoutInterface.onRefresh();
        } else if (!isNetworkAvailable) {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getContext(), "No network", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNetworkChange(boolean isNetworkAvailable) {
        this.isNetworkAvailable = isNetworkAvailable;
        if (isNetworkAvailable) {
            hideNetworkError();
            if (isFirstTimeLoading) showLoading();
            if (!isFirstTimeLoading && baseLayoutInterface != null) {
                if (isLoadMoreEnabled) {
                    showLoadingMore();
                    baseLayoutInterface.onLoadMore(currentPage);
                }
            } else if (baseLayoutInterface != null) {
                if (isLoadMoreEnabled) baseLayoutInterface.onLoadMore(currentPage);
            }
        } else {
            //Log.d(TAG, "isNetworkAvailable = " + isNetworkAvailable);
            showNetworkError();
        }
    }

    @Override
    public void notifyLoadingMoreAdded(int position) {
        //Log.d(TAG, "Scrolling to position = " + position);
        recyclerView.scrollToPosition(position);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putBoolean("isLoadMoreEnabled", isLoadMoreEnabled);
        bundle.putBoolean("isSwipeRefreshEnabled", isSwipeRefreshEnabled);
        bundle.putParcelable("recycler_view", recyclerView.getLayoutManager().onSaveInstanceState());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) // implicit null check
        {
            Bundle bundle = (Bundle) state;
            isLoadMoreEnabled = bundle.getBoolean("isLoadMoreEnabled");
            isSwipeRefreshEnabled = bundle.getBoolean("isSwipeRefreshEnabled");
            state = bundle.getParcelable("superState");
            //recyclerView.getLayoutManager().onRestoreInstanceState(((Bundle) state).getParcelable("recycler_view"));
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //Log.d(TAG, "onDetachedFromWindow");
        getContext().unregisterReceiver(networkStateReceiver.get());
        networkStateReceiver.get().clearAbortBroadcast();
        recyclerView.removeAllViews();
        recyclerView.removeOnScrollListener(scrollListener);
        recyclerView.removeOnLayoutChangeListener(onLayoutChangeListener);

    }

    @ColorInt
    private int fetchAccentColor() {
        TypedValue typedValue = new TypedValue();

        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorAccent});
        @ColorInt
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }
}
