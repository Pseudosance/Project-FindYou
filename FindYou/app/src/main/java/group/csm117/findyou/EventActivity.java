package group.csm117.findyou;

/**
 * Created by Amy on 5/19/2015.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;


public class EventActivity extends Activity {
    // UI references.
    //private SupportMapFragment mapFragment;
    private EditText postEditText;
    private EditText titleEditText;
    private TextView characterCountTextView;
    private Button postButton;

    private int maxCharacterCount = Application.getConfigHelper().getPostMaxCharacterCount();
    private ParseGeoPoint geoPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_event);

        Intent intent = getIntent();
        Location location = intent.getParcelableExtra(Application.INTENT_EXTRA_LOCATION);
        geoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());

        postEditText = (EditText) findViewById(R.id.post_edittext);
        postEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updatePostButtonState();
                updateCharacterCountTextViewText();
            }
        });

        titleEditText = (EditText) findViewById(R.id.title_edittext);
        titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updatePostButtonState();
            }
        });

        characterCountTextView = (TextView) findViewById(R.id.character_count_textview);

        postButton = (Button) findViewById(R.id.post_button);
        postButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                post();
            }
        });

        updatePostButtonState();
        updateCharacterCountTextViewText();


    }

    private void post () {
        String text = postEditText.getText().toString().trim();
        String title = titleEditText.getText().toString().trim();

        // Set up a progress dialog
        final ProgressDialog dialog = new ProgressDialog(EventActivity.this);
        dialog.setMessage("Creating Event...");
        dialog.show();

        // Create a post.
        AnywallPost post = new AnywallPost();

        // Set the location to the current user's location
        post.setLocation(geoPoint);
        // add from friends list
        ArrayList<String> users = new ArrayList<String>();
        users.add(ParseUser.getCurrentUser().getUsername());
        post.setUsersThatCanSee(users);
        post.setEvent("EVENT");
        post.setTitle(title);
        post.setText(text);
        post.setDraggable(true);
        post.setUser(ParseUser.getCurrentUser());
        ParseACL acl = new ParseACL();

        // Give public access
        acl.setPublicReadAccess(true);
        acl.setPublicWriteAccess(true);
        post.setACL(acl);

        // Save the post
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                dialog.dismiss();
                finish();
            }
        });
    }

    private String getPostEditTextText () {
        return postEditText.getText().toString().trim();
    }

    private void updatePostButtonState () {
        int length = getPostEditTextText().length();
        int length2 = titleEditText.getText().toString().trim().length();
        boolean enabled = length > 0 && length < maxCharacterCount && length2 > 0;
        postButton.setEnabled(enabled);
    }

    private void updateCharacterCountTextViewText () {
        String characterCountString = String.format("%d/%d", postEditText.length(), maxCharacterCount);
        characterCountTextView.setText(characterCountString);
    }
}

