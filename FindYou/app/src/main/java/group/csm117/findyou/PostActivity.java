package group.csm117.findyou;


import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import android.app.ProgressDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import com.parse.ParseACL;
import com.parse.SaveCallback;

import java.util.ArrayList;

/**
 * Activity which displays a login screen to the user, offering registration as well.
 */
public class PostActivity extends Activity {
  // UI references.
  //private SupportMapFragment mapFragment;
  private EditText postEditText;
  private TextView characterCountTextView;
  private Button postButton;
  private ArrayList<String> users;

  private int maxCharacterCount = Application.getConfigHelper().getPostMaxCharacterCount();
  private ParseGeoPoint geoPoint;
  private String event;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_post);

    Intent intent = getIntent();
    event = (String) intent.getExtras().getString("event");
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

    characterCountTextView = (TextView) findViewById(R.id.character_count_textview);

    postButton = (Button) findViewById(R.id.post_button);
    postButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        post();
      }
    });

    updatePostButtonState();
    updateCharacterCountTextViewText();


  }

  private void post () {
    String text = postEditText.getText().toString().trim();

    // Set up a progress dialog
    final ProgressDialog dialog = new ProgressDialog(PostActivity.this);
    dialog.setMessage("Creating Detail...");
    dialog.show();

    // Create a post.
    AnywallPost post = new AnywallPost();

    // Set the location to the current user's location

    users = getIntent().getStringArrayListExtra("users");
    post.setUsersThatCanSee(users);
    post.setLocation(geoPoint);
    if (event == null)
      post.setEvent("DETAIL");
    else post.setEvent(event);
    post.setTitle("DETAIL");
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
    boolean enabled = length > 0 && length < maxCharacterCount;
    postButton.setEnabled(enabled);
  }

  private void updateCharacterCountTextViewText () {
    String characterCountString = String.format("%d/%d", postEditText.length(), maxCharacterCount);
    characterCountTextView.setText(characterCountString);
  }
}
