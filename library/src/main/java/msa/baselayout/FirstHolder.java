package msa.baselayout;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.airbnb.epoxy.EpoxyHolder;

/**
 * Created by Abhimuktheeswarar on 07-08-2017.
 */

class FirstHolder extends EpoxyHolder {

    public View itemView;

    @Override
    protected void bindView(View itemView) {
        this.itemView = itemView;
    }

    void changeTopMargin() {
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) itemView.getLayoutParams();
        layoutParams.topMargin = (int) itemView.getContext().getResources().getDimension(R.dimen.item_first_top_margin);
        itemView.setLayoutParams(layoutParams);
    }


}