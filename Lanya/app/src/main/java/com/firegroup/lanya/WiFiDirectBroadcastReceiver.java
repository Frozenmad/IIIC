package com.firegroup.lanya;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Used to create WifiDirect Broadcast
 */

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager.PeerListListener myPeerListListener;
    private Context myContext;

        public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, Context myContext, WifiP2pManager.PeerListListener myPeerListListener) {
            super();
            this.mManager = manager;
            this.mChannel = channel;
            this.myPeerListListener = myPeerListListener;
            this.myContext = myContext;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    // Wifi P2P is enabled
                    Log.e("BroadcastReceiver","WIFI_P2P_STATE_ENABLED");
                } else {
                    Toast.makeText(myContext,"Please turn on your WIFI",Toast.LENGTH_SHORT).show();
                }
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                Log.e("BroadcastReceiver","Get in the CHANGED_ACTION");
                if(mManager != null)
                {
                    mManager.requestPeers(mChannel,myPeerListListener);
                }
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                // Respond to new connection or disconnections
                Log.e("BroadcastReceiver","Get in the P2P_CHANGED_ACTION");
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                // Respond to this device's wifi state changing
                Log.e("BroadcastReceiver","Get in the THIS_DEVICE_CHANGED_ACTION");
            }
        }
    }