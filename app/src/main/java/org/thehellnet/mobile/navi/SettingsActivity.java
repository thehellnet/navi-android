package org.thehellnet.mobile.navi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SettingsActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    @Override
    protected void onResume() {
        super.onResume();

        TextView usernameText = (TextView) findViewById(R.id.usernameText);
        usernameText.setText(getIntent().getStringExtra(getString(R.string.preferences_username)));

        TextView descriptionText = (TextView) findViewById(R.id.descriptionText);
        descriptionText.setText(getIntent().getStringExtra(getString(R.string.preferences_description)));

        Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeActivity(true);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        closeActivity(false);
    }

    private void closeActivity(boolean doSave) {
        Intent returnIntent = new Intent();
        int result = RESULT_CANCELED;

        if(doSave) {
            TextView usernameText = (TextView) findViewById(R.id.usernameText);
            returnIntent.putExtra(getString(R.string.preferences_username), usernameText.getText().toString());

            TextView descriptionText = (TextView) findViewById(R.id.descriptionText);
            returnIntent.putExtra(getString(R.string.preferences_description), descriptionText.getText().toString());

            result = RESULT_OK;
        }

        setResult(result, returnIntent);

        finish();
    }
}
