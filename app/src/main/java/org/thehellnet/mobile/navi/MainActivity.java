package org.thehellnet.mobile.navi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import org.thehellnet.mobile.navi.service.PositionService;

public class MainActivity extends ActionBarActivity {
    private SharedPreferences sharedPreferences;

    private UpdateUiPing updateUiPing;

    private class UpdateUiPing extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Location location = intent.getParcelableExtra(getString(R.string.intent_update_location));
            String type = intent.getStringExtra(getString(R.string.intent_update_type));

            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            updateUi(latitude, longitude, type);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getPreferences(MODE_PRIVATE);
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
            intent.putExtra(getString(R.string.preferences_username),
                    sharedPreferences.getString(getString(R.string.preferences_username), ""));
            intent.putExtra(getString(R.string.preferences_description),
                    sharedPreferences.getString(getString(R.string.preferences_description), ""));
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
        registerReceiver(updateUiPing, new IntentFilter(getString(R.string.intent_update)));

        Switch serviceSwitch = (Switch) findViewById(R.id.serviceSwitch);
        serviceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent intent = new Intent(getApplicationContext(), PositionService.class);
                intent.putExtra(getString(R.string.intent_servicelaunch_status), isChecked);
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
                if(resultCode == RESULT_OK) {
                    SharedPreferences.Editor edit = sharedPreferences.edit();

                    edit.putString(getString(R.string.preferences_username),
                            data.getStringExtra(getString(R.string.preferences_username)));
                    edit.putString(getString(R.string.preferences_description),
                            data.getStringExtra(getString(R.string.preferences_description)));

                    edit.commit();

                    updateUiFromConfig();
                }

                break;
        }
    }

    private void updateUiFromConfig() {
        TextView usernameText = (TextView) findViewById(R.id.usernameText);
        usernameText.setText(sharedPreferences.getString(getString(R.string.preferences_username), ""));

        TextView descriptionText = (TextView) findViewById(R.id.descriptionText);
        descriptionText.setText(sharedPreferences.getString(getString(R.string.preferences_description), ""));
    }

    private void updateUi(double latitude, double longitude, String type) {
        TextView latitudeText = (TextView) findViewById(R.id.latitudeText);
        latitudeText.setText(String.format("%.06f", latitude));

        TextView longitudeText = (TextView) findViewById(R.id.longitudeText);
        longitudeText.setText(String.format("%.06f", longitude));

        TextView lastProviderText = (TextView) findViewById(R.id.lastProviderText);
        if(type.equals("gps")) {
            lastProviderText.setText(getString(R.string.ui_position_lastprovider_gps));
        } else if(type.equals("network")) {
            lastProviderText.setText(getString(R.string.ui_position_lastprovider_network));
        }
    }
}
