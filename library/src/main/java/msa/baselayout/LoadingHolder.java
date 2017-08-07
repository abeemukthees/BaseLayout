package msa.baselayout;

import android.view.View;
import android.widget.ProgressBar;

import com.airbnb.epoxy.EpoxyHolder;

/**
 * Created by Abhimuktheeswarar on 07-08-2017.
 */

class LoadingHolder extends EpoxyHolder {

    public View itemView;

    ProgressBar progressBar;

    @Override
    protected void bindView(View itemView) {
        this.itemView = itemView;
        progressBar = itemView.findViewById(R.id.progressBar);
    }
}
