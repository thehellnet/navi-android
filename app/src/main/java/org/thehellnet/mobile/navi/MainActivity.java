package org.thehellnet.mobile.navi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
    private SharedPreferences sharedPreferences;

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
        updateUi();
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

                    updateUi();
                }

                break;
        }
    }

    private void updateUi() {
        TextView usernameText = (TextView) findViewById(R.id.usernameText);
        usernameText.setText(sharedPreferences.getString(getString(R.string.preferences_username), ""));

        TextView descriptionText = (TextView) findViewById(R.id.descriptionText);
        descriptionText.setText(sharedPreferences.getString(getString(R.string.preferences_description), ""));
    }
}
