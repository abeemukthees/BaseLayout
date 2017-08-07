package msa.baselayout;

import android.os.Handler;

import com.airbnb.epoxy.EpoxyAdapter;
import com.airbnb.epoxy.EpoxyHolder;
import com.airbnb.epoxy.EpoxyModelWithHolder;

import java.util.List;

/**
 * Created by Abhimuktheeswarar on 07-08-2017.
 */

public class BaseEpoxyAdapter<T extends EpoxyModelWithHolder<EpoxyHolder>> extends EpoxyAdapter {

    private static final String TAG = BaseEpoxyAdapter.class.getSimpleName();
    private final BaseEpoxyAdapterInterface baseEpoxyAdapterInterface;
    private FirstItem firstItem;
    private LoadingItem loadingItem;
    private NetworkErrorItem networkErrorItem;
    private int lastAddedItemPosition;

    BaseEpoxyAdapter(BaseEpoxyAdapterInterface baseEpoxyAdapterInterface) {
        this.baseEpoxyAdapterInterface = baseEpoxyAdapterInterface;
        enableDiffing();
        firstItem = new FirstItem();
        addModel(firstItem);
        loadingItem = new LoadingItem();
        networkErrorItem = new NetworkErrorItem_().isInGrid(false);

    }

    BaseEpoxyAdapter(BaseEpoxyAdapterInterface baseEpoxyAdapterInterface, boolean forGrid) {
        this.baseEpoxyAdapterInterface = baseEpoxyAdapterInterface;
        enableDiffing();
        if (forGrid) firstItem = new FirstItem_().shouldChangeTopMargin(true);
        loadingItem = new LoadingItem();
        networkErrorItem = new NetworkErrorItem_().isInGrid(true);
    }

    BaseEpoxyAdapter(BaseEpoxyAdapterInterface baseEpoxyAdapterInterface, boolean forGrid, boolean forSearch) {
        this.baseEpoxyAdapterInterface = baseEpoxyAdapterInterface;
    }


    void addItem(List<T> items) {
        addModels(items);

    }

    void addItem(T item) {
        addModel(item);
        lastAddedItemPosition = getModelPosition(item);
        //Log.d(TAG, "Last added Item Positon = " + lastAddedItemPosition);
    }

    void addItemAtTop(T item) {
        insertModelAfter(item, firstItem);
    }

    void updateItem(final T item) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                notifyModelChanged(item);
            }
        });
    }

    @Deprecated
    void delayedUpdateItem(final T item) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                notifyModelChanged(item);
            }
        });
    }


    void removeItem(T item) {
        removeModel(item);
    }

    void removeAllItems() {
        if (getModelPosition(firstItem) >= 0) removeAllAfterModel(firstItem);
    }

    void removeAllItemsCompletely() {
        removeAllModels();
    }


    void showLoadingMore() {
        //Log.d(TAG, "Loading item position = " + getModelPosition(loadingItem));
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (getModelPosition(loadingItem) == -1) {
                    addModel(loadingItem);
                    lastAddedItemPosition = getModelPosition(loadingItem);
                    //Log.d(TAG, "Last added Item Positon = " + lastAddedItemPosition);
                }
            }
        });
    }

    void showLoadingMoreAfterNetworkError() {
        //Log.d(TAG, "Loading item position = " + getModelPosition(loadingItem));
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (getModelPosition(loadingItem) == -1) {
                    addModel(loadingItem);
                    lastAddedItemPosition = getModelPosition(loadingItem);
                    //Log.d(TAG, "Last added Item Positon = " + lastAddedItemPosition);
                    baseEpoxyAdapterInterface.notifyLoadingMoreAdded(getModelPosition(loadingItem));
                }
            }
        });
    }

    void hideLoadingMore() {
        //Log.d(TAG, "hideLoadingMore()");
        removeModel(loadingItem);
    }

    public int getLoadingMorePosition() {
        return getModelPosition(loadingItem);
    }

    public int getNetworkErrorPosition() {
        return getModelPosition(networkErrorItem);
    }

    void showNetworkError() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (getModelPosition(networkErrorItem) == -1) {
                    addModel(networkErrorItem);
                    lastAddedItemPosition = getModelPosition(networkErrorItem);
                    //Log.d(TAG, "Last added Item Positon = " + lastAddedItemPosition);
                }
                if (getModelPosition(loadingItem) != -1) hideLoadingMore();

            }
        });
    }

    void hideNetworkError() {
        removeModel(networkErrorItem);
    }

    int getLastAddedItemPosition() {
        return lastAddedItemPosition;
    }

    int getItemPosition(T item) {
        return getModelPosition(item);
    }

    interface BaseEpoxyAdapterInterface {
        void notifyLoadingMoreAdded(int position);
    }
}
