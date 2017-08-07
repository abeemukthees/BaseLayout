package msa.baselayout;

import android.view.View;
import android.widget.TextView;

import com.airbnb.epoxy.EpoxyHolder;

/**
 * Created by Abhimuktheeswarar on 07-08-2017.
 */

class NetworkHolder extends EpoxyHolder {

    public View itemView;

    public TextView textView;

    @Override
    protected void bindView(View itemView) {
        this.itemView = itemView;
        textView = itemView.findViewById(R.id.text_noNetwork);
    }
}
