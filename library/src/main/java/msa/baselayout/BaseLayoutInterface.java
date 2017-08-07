package msa.baselayout;

/**
 * Created by Abhimuktheeswarar on 07-08-2017.
 */

public interface BaseLayoutInterface {

    void onLoadMore(int page);

    void onRefresh();

    void onError();
}
