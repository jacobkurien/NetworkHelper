package com.jacob.networkhelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	private static final String TAG = "WiFiDemo";
	WifiManager wifi;
	BroadcastReceiver receiver;

	TextView textStatus;
	Button buttonScan, buttonScanUnprotected, connectToStrongestUnprotected;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Setup UI
		textStatus = (TextView) findViewById(R.id.textStatus);
		buttonScan = (Button) findViewById(R.id.buttonScan);
		buttonScan.setOnClickListener(this);
		
		buttonScanUnprotected = (Button) findViewById(R.id.PasswordUnprotected);
		buttonScanUnprotected.setOnClickListener(this);
		
		connectToStrongestUnprotected = (Button) findViewById(R.id.ConnectToStrongestUnprotected);
		connectToStrongestUnprotected.setOnClickListener(this);

		// Setup WiFi
		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		// Get WiFi status
		if(wifi.isWifiEnabled()){
			WifiInfo info = wifi.getConnectionInfo();
			if(info != null)
			{
				textStatus.append("\n\nCurrently connected to: " + info.getSSID().toString());
			}
			
			//List all the networks in the location
			List<ScanResult> netWorks = wifi.getScanResults();
			for (ScanResult netWork : netWorks){
				textStatus.append("\n\n" + netWork.toString());
			}

			// List available networks
			//List<WifiConfiguration> configs = wifi.getConfiguredNetworks();
//			List<WifiConfiguration> configs = wifi.getConfiguredNetworks();
//			for (WifiConfiguration config : configs) {
//				textStatus.append("\n\n" + config.toString());
//			}

			// Register Broadcast Receiver
			if (receiver == null)
				receiver = new WiFiScanReceiver(this);

			registerReceiver(receiver, new IntentFilter(
					WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
			Log.d(TAG, "onCreate()");
		}
		
	}

	@Override
	public void onStop() {
		unregisterReceiver(receiver);
	}
	

	public void onClick(View view) {
		Toast.makeText(this, "On Click Clicked. Toast to that!!!",
				Toast.LENGTH_LONG).show();
		textStatus.setText("");
		if (view.getId() == R.id.buttonScan) {
			Log.d(TAG, "onClick() wifi.startScan()");
			
			wifi.startScan();
			textStatus.setText("");
			//List all the networks in the location
			List<ScanResult> netWorks = wifi.getScanResults();
			//List<> netWorksForSort = new List<ScanResult> () ;
			List<ScanResult> netWorksForSort = new ArrayList<ScanResult> ();
			 //new code (good way)
			ScanResult bestSignal = null;
			while(!netWorks.isEmpty()){
			 
			 	bestSignal = null;
			    for (ScanResult netWork : netWorks) {
			      if (bestSignal == null
			          || WifiManager.compareSignalLevel(bestSignal.level, netWork.level) < 0)
			        bestSignal = netWork;
			    }
			    netWorksForSort.add(bestSignal);
			    netWorks.remove(bestSignal);
		    
			}
			for (ScanResult netWork : netWorksForSort){
				textStatus.append("\n\n" + netWork.toString());
				
			}
		}
		
		if (view.getId() == R.id.PasswordUnprotected) {
			textStatus.setText("");
			//List all the networks in the location
			List<ScanResult> netWorks = wifi.getScanResults();
			List<ScanResult> netWorksUnprotected = new ArrayList<ScanResult> ();
			
			for (ScanResult netWork : netWorks) {
			      if (netWork.capabilities.startsWith("[WPS]"))
			      	netWorksUnprotected.add(netWork);
			      	
			    }
			
			for (ScanResult netWorkUp : netWorksUnprotected){
				textStatus.append("\n\n" + netWorkUp.toString());
				
			}
			
		}
		if (view.getId() == R.id.ConnectToStrongestUnprotected) {
			List<ScanResult> netWorks = wifi.getScanResults();
			List<ScanResult> netWorksUnprotected = new ArrayList<ScanResult> ();

			
			for (ScanResult netWork : netWorks) {
			      if (netWork.capabilities.startsWith("[WPS]"))
			      	netWorksUnprotected.add(netWork);
			      	
			}
			//Finding network with maximum signal strength
			ScanResult bestSignal = null;
			

			    for (ScanResult netWork : netWorksUnprotected) {
			      if (bestSignal == null
			          || WifiManager.compareSignalLevel(bestSignal.level, netWork.level) < 0)
			        bestSignal = netWork;
			    }
			    textStatus.setText("");
			    textStatus.append("\n\n" + bestSignal.toString());
			    
			 // Connect to the best network
			    
			   //WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		        // setup a wifi configuration
		        WifiConfiguration wc = new WifiConfiguration();
		        wc.SSID = bestSignal.SSID;
		        wc.preSharedKey = "";
		        wc.status = WifiConfiguration.Status.ENABLED;
		        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
		        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
		        wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
		        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
		        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
		        wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
		        wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
		        // connect to and enable the connection
		        int netId = wifi.addNetwork(wc);
		        
		     // Setup WiFi
				wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

				// Get WiFi status
				WifiInfo info = wifi.getConnectionInfo();
				if(info != null)
				{
					wifi.disconnect();
			        wifi.removeNetwork(info.getNetworkId());
				}
		        
		        //wifi.disconnect();
		        wifi.enableNetwork(netId, true);
		        wifi.setWifiEnabled(true);
			
			
			
			
		}
	}

}
