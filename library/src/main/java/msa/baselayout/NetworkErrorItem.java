package msa.baselayout;

import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelWithHolder;

/**
 * Created by Abhimuktheeswarar on 07-08-2017.
 */

class NetworkErrorItem extends EpoxyModelWithHolder<NetworkHolder> {

    @EpoxyAttribute
    boolean isInGrid;

    @Override
    protected NetworkHolder createNewHolder() {
        return new NetworkHolder();
    }

    @Override
    protected int getDefaultLayout() {
        return R.layout.item_network_error;
    }

    @Override
    public void bind(NetworkHolder holder) {
        super.bind(holder);
    }

    @Override
    public void unbind(NetworkHolder holder) {
        super.unbind(holder);
    }

    @Override
    public int getSpanSize(int totalSpanCount, int position, int itemCount) {
        //Log.d(NetworkErrorItem.class.getSimpleName(), "Is in grid = " + isInGrid + " total span count = " + totalSpanCount);
        if (!isInGrid) return super.getSpanSize(totalSpanCount, position, itemCount);
        else return totalSpanCount;
    }
}
