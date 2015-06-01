package group.csm117.findyou;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;


public class SettingsActivity extends Activity {

    private List<Float> availableOptions = Application.getConfigHelper().getSearchDistanceAvailableOptions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);


        // Set up the log out button click handler
        Button logoutButton = (Button) findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ParseFacebookUtils.unlinkInBackground(ParseUser.getCurrentUser(), new SaveCallback() {
                    @Override
                    public void done(ParseException ex) {
                        if (ex == null) {
                            Log.d("MyApp", "The user is no longer associated with their Facebook account.");
                        }
                    }
                });
                // Call the Parse log out method
                ParseUser.logOut();
                // Start and intent for the dispatch activity
                Intent intent = new Intent(SettingsActivity.this, DispatchActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }
}
