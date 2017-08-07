package msa.baselayout.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by Abhimuktheeswarar on 07-08-2017.
 */

public class NetworkStateReceiver extends BroadcastReceiver {

    private static final String TAG = NetworkStateReceiver.class.getSimpleName();
    private NetworkStateListener networkStateListener;

    public void setNetworkStateListener(NetworkStateListener networkStateListener) {
        this.networkStateListener = networkStateListener;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {

        //Log.d(TAG, "Network connectivity change");
        //if (networkStateListener != null) networkStateListener.onNetworkChange(true);

        if (intent.getExtras() != null) {
            final ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

            if (ni != null && ni.isConnected()) {
                if (networkStateListener != null) {
                    networkStateListener.onNetworkChange(true);
                    Log.i(TAG, "Network " + ni.getTypeName() + " connected");
                }

            } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                if (networkStateListener != null) {
                    networkStateListener.onNetworkChange(false);
                    Log.d(TAG, "There's no network connectivity");
                }

            }
        }
    }

    public interface NetworkStateListener {
        void onNetworkChange(boolean isNetworkAvailable);
    }
}