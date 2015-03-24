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

import org.joda.time.format.DateTimeFormat;
import org.thehellnet.mobile.navi.config.C;
import org.thehellnet.mobile.navi.service.PositionService;
import org.thehellnet.mobile.navi.service.UpdateUiData;

public class MainActivity extends ActionBarActivity {
    private SharedPreferences sharedPreferences;

    private UpdateUiPing updateUiPing;

    private class UpdateUiPing extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            UpdateUiData updateUiData = (UpdateUiData) intent.getSerializableExtra("data");

            updateUi(updateUiData);
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

    private void updateUi(UpdateUiData updateUiData) {
        double latitude = updateUiData.getLatitude();
        double longitude = updateUiData.getLongitude();
        String type = updateUiData.getType();
        float accuracy = updateUiData.getAccuracy();

        TextView latitudeText = (TextView) findViewById(R.id.latitudeText);
        String latitudeLetter = "N";
        if (latitude < 0) {
            latitude *= -1;
            latitudeLetter = "S";
        }
        latitudeText.setText(String.format("%.07f deg %s", latitude, latitudeLetter));

        TextView longitudeText = (TextView) findViewById(R.id.longitudeText);
        String longitudeLetter = "E";
        if (longitude < 0) {
            longitude *= -1;
            longitudeLetter = "W";
        }
        longitudeText.setText(String.format("%.07f deg %s", longitude, longitudeLetter));

        TextView lastProviderText = (TextView) findViewById(R.id.lastProviderText);
        switch (type) {
            case LocationManager.GPS_PROVIDER:
                lastProviderText.setText(getString(R.string.ui_position_lastprovider_gps));
                break;
            case LocationManager.NETWORK_PROVIDER:
                lastProviderText.setText(getString(R.string.ui_position_lastprovider_network));
                break;
            default:
                lastProviderText.setText("");
                break;
        }

        TextView accuracyText = (TextView) findViewById(R.id.accuracyText);
        accuracyText.setText(String.format("%.01f m", accuracy));

        TextView dateTimeText = (TextView) findViewById(R.id.dateTimeText);
        dateTimeText.setText(DateTimeFormat.forPattern(getString(R.string.ui_position_datetime_tag))
                .print(updateUiData.getDateTime()));

        TextView queueSizeText = (TextView) findViewById(R.id.queueSizeText);
        String points = getString(R.string.ui_server_queuesize_manypoints);
        if (updateUiData.getQueueSize() == 1) {
            points = getString(R.string.ui_server_queuesize_onepoint);
        }
        queueSizeText.setText(String.format("%d %s", updateUiData.getQueueSize(), points));

        TextView connectedText = (TextView) findViewById(R.id.connectedText);
        String connected = getString(R.string.ui_server_connected_offline);
        if (updateUiData.isServerConnected()) {
            connected = getString(R.string.ui_server_connected_online);
        }
        connectedText.setText(connected);
    }
}
