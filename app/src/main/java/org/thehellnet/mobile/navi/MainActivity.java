package org.thehellnet.mobile.navi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import org.thehellnet.mobile.navi.config.C;
import org.thehellnet.mobile.navi.service.PositionData;
import org.thehellnet.mobile.navi.service.PositionService;

public class MainActivity extends ActionBarActivity {
    private SharedPreferences sharedPreferences;

    private UpdateUiPing updateUiPing;

    private class UpdateUiPing extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            PositionData positionData = (PositionData) intent.getSerializableExtra("data");

            updateUi(positionData);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(C.config.PREFERENCES_NAME, MODE_PRIVATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra(C.config.USERNAME, sharedPreferences.getString(C.config.USERNAME, ""));
            intent.putExtra(C.config.DESCRIPTION, sharedPreferences.getString(C.config.DESCRIPTION, ""));
            startActivityForResult(intent, 1);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUiFromConfig();

        updateUiPing = new UpdateUiPing();
        registerReceiver(updateUiPing, new IntentFilter(C.intent.UPDATE_LOCATION));

        Switch serviceSwitch = (Switch) findViewById(R.id.serviceSwitch);
        serviceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                Intent intent = new Intent(getApplicationContext(), PositionService.class);
                intent.putExtra("status", isChecked);
                startService(intent);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(updateUiPing);
        updateUiPing = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    SharedPreferences.Editor edit = sharedPreferences.edit();
                    edit.putString(C.config.USERNAME, data.getStringExtra(C.config.USERNAME));
                    edit.putString(C.config.DESCRIPTION, data.getStringExtra(C.config.DESCRIPTION));
                    edit.commit();

                    updateUiFromConfig();
                }

                break;
        }
    }

    private void updateUiFromConfig() {
        TextView usernameText = (TextView) findViewById(R.id.usernameText);
        usernameText.setText(sharedPreferences.getString(C.config.USERNAME, ""));

        TextView descriptionText = (TextView) findViewById(R.id.descriptionText);
        descriptionText.setText(sharedPreferences.getString(C.config.DESCRIPTION, ""));
    }

    private void updateUi(PositionData positionData) {
        double latitude = positionData.getLatitude();
        double longitude = positionData.getLongitude();
        String type = positionData.getType();
        float accuracy = positionData.getAccuracy();

        TextView latitudeText = (TextView) findViewById(R.id.latitudeText);
        String latitudeLetter = "N";
        if (latitude < 0) {
            latitude *= -1;
            latitudeLetter = "S";
        }
        latitudeText.setText(String.format("%.08f deg %s", latitude, latitudeLetter));

        TextView longitudeText = (TextView) findViewById(R.id.longitudeText);
        String longitudeLetter = "E";
        if (longitude < 0) {
            longitude *= -1;
            longitudeLetter = "W";
        }
        longitudeText.setText(String.format("%.08f deg %s", longitude, longitudeLetter));

        TextView lastProviderText = (TextView) findViewById(R.id.lastProviderText);
        if (type.equals(LocationManager.GPS_PROVIDER)) {
            lastProviderText.setText(getString(R.string.ui_position_lastprovider_gps));
        } else if (type.equals(LocationManager.NETWORK_PROVIDER)) {
            lastProviderText.setText(getString(R.string.ui_position_lastprovider_network));
        } else {
            lastProviderText.setText("");
        }

        TextView accuracyText = (TextView) findViewById(R.id.accuracyText);
        accuracyText.setText(String.format("%.02f m", accuracy));
    }
}
