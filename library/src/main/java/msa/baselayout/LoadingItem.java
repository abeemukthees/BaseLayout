package msa.baselayout;

import android.view.View;

import com.airbnb.epoxy.EpoxyModelWithHolder;

/**
 * Created by Abhimuktheeswarar on 07-08-2017.
 */

class LoadingItem extends EpoxyModelWithHolder<LoadingHolder> {

    @Override
    protected LoadingHolder createNewHolder() {
        return new LoadingHolder();
    }

    @Override
    protected int getDefaultLayout() {
        return R.layout.item_loading;
    }

    @Override
    public void bind(final LoadingHolder holder) {
        super.bind(holder);
        holder.progressBar.setVisibility(View.VISIBLE);

    }

    @Override
    public void unbind(LoadingHolder holder) {
        super.unbind(holder);
        holder.progressBar.setVisibility(View.GONE);
    }
}
