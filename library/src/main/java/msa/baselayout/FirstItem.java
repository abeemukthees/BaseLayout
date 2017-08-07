package msa.baselayout;

import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelWithHolder;

/**
 * Created by Abhimuktheeswarar on 07-08-2017.
 */

class FirstItem extends EpoxyModelWithHolder<FirstHolder> {


    @EpoxyAttribute
    boolean shouldChangeTopMargin;

    @Override
    protected FirstHolder createNewHolder() {
        return new FirstHolder();
    }

    @Override
    protected int getDefaultLayout() {
        return R.layout.item_first;
    }

    @Override
    public void bind(FirstHolder holder) {
        super.bind(holder);
        if (shouldChangeTopMargin) holder.changeTopMargin();
    }
}