package com.anselmendeke.beanconsimple;

import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;


public class BeaconActivity extends AppCompatActivity implements BeaconConsumer {

    public static final String TAG = "BeaconsEverywhere";
    private BeaconManager beaconManager;
    private Region mainRegion;
    private boolean isBluetoothActivated = false;
    private boolean isScanStarted = false;
    private TextView distanceValueTextView;
    private Button scanningButton;
    private RelativeLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon);

//        init widgets
        distanceValueTextView = (TextView) findViewById(R.id.distanceValueTextView);
        scanningButton = (Button) findViewById(R.id.scanningButton);
        layout = (RelativeLayout) findViewById(R.id.layout);

//        init beaconManager
        beaconManager = BeaconManager.getInstanceForApplication(this);

        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));

        beaconManager.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        mainRegion = new Region("myBeaons", Identifier.parse("3d4f13b4-d1fd-4049-80e5-d3edcc840b6f"), null, null);
        isBluetoothActivated = true;

        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                try {
                    Log.d(TAG, "didEnterRegion");
                    beaconManager.startRangingBeaconsInRegion(region);

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didExitRegion(Region region) {
                try {
                    Log.d(TAG, "didExitRegion");
                    beaconManager.stopRangingBeaconsInRegion(region);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                    Log.d(TAG, "The distance: " + oneBeacon.getDistance() + " id:" + oneBeacon.getId1() + "/" + oneBeacon.getId2() + "/" + oneBeacon.getId3());
                            distanceValueTextView.setText("No beacons found.");
                        }
                    });

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {

            }
        });

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                for (final Beacon oneBeacon : beacons) {
                    saveFingerprint(oneBeacon);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    public void startScanning(View view) {
        try {
            if (isBluetoothActivated) {
                if (isScanStarted) {
//                    clicked on STOP
                    isScanStarted = false;
                    beaconManager.stopRangingBeaconsInRegion(mainRegion);
                    scanningButton.setText("Start scanning");
                    distanceValueTextView.setText("Stopped.");

                } else {
//                    clicked on START
                    isScanStarted = true;
                    scanningButton.setText("Stop scanning");
                    distanceValueTextView.setText("Scanning...");
                    try {
                        beaconManager.startRangingBeaconsInRegion(mainRegion);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                }
            } else {
                Toast.makeText(getApplicationContext(), "Please enable Bluetooth", Toast.LENGTH_LONG).show();
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void saveFingerprint(final Beacon oneBeacon) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                    Log.d(TAG, "The distance: " + oneBeacon.getDistance() + " id:" + oneBeacon.getId1() + "/" + oneBeacon.getId2() + "/" + oneBeacon.getId3());
                distanceValueTextView.setText(String.valueOf(oneBeacon.getDistance()));
            }
        });
    }

}
