package group.csm117.findyou;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;



public class AddFriendsToEventActivity extends ActionBarActivity implements AbsListView.OnScrollListener {
    private EditText EventTitleEditText;
    private EditText EventDescriptionEditText;
    private Event curEvent;

    List<User_friend> user_friends;
    List<User_friend> mInvitedFriends;
    ListView lvFriends;
    User_friendListAdapterWithCache_MapInvites adapterFriends;

    private boolean lvBusy = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);

        Intent intent = getIntent();
        String event = intent.getStringExtra("event");

        ParseQuery<Event> mapQuery = Event.getQuery();
        mapQuery.getInBackground(event, new GetCallback<Event>() {
            public void done(Event ev, ParseException e) {
                if (e == null) {
                    curEvent = ev;
                }
            }
        });

        EventTitleEditText = (EditText) findViewById(R.id.EventTitleEditText);
        EventDescriptionEditText = (EditText) findViewById(R.id.EventDescriptionEditText);

        Button createButton = (Button) findViewById(R.id.create_event_button);
        createButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                createEvent();
            }
        });

        // populate data
        user_friends = new ArrayList();
        mInvitedFriends = new ArrayList();
        GraphRequest request = GraphRequest.newMyFriendsRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONArrayCallback() {
                    @Override
                    public void onCompleted(
                            JSONArray rows,
                            GraphResponse response) {
                        // Application code for users friends
                        if (rows == null) {
                            return;
                        }
                        try {
                            Log.d("MyApp", "attemping to grab friends");
                            if (rows !=null)
                                Log.d("MyAPP", "number of rows: " + rows.length());
                            if (rows !=null)
                                for(int i=0; i<rows.length(); i++){

                                    Log.d("MyApp", "Inside loop ");
                                    final JSONObject j = rows.getJSONObject(i);

                                    // only add if not already invited

                                    if(curEvent == null)
                                        user_friends.add(new User_friend("Orange", "http://farm5.staticflickr.com/4142/4787427683_3672f1db9a_s.jpg"));
                                    else {
                                        List<ParseUser> joined = curEvent.getJoined_DEPRECATED();
                                        boolean alreadyInvited = false;
                                        for (ParseUser user : joined) {
                                            if (user.get("fbid").equals(j.optString("id")))
                                                alreadyInvited = true;
                                        }
                                        if (!alreadyInvited)
                                            user_friends.add(new User_friend(j.optString("id"), j.optString("name"), j.getJSONObject("picture").getJSONObject("data").getString("url")));
                                    }
                                }
                        } catch (JSONException e) {
                            Log.e("Error", "json " + e.toString());
                        }
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "picture.type(small),id,name,link");
        request.setParameters(parameters);
        request.executeAsync();


        lvFriends = (ListView) findViewById( R.id.invite_friends_listview);
        adapterFriends = new User_friendListAdapterWithCache_MapInvites(this, user_friends);
        lvFriends.setAdapter(adapterFriends);
        lvFriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                // Send Invite
                User_friend friend  = user_friends.get(position);
                CheckBox checkbox = (CheckBox)view.findViewById(R.id.checkbox);
                checkbox.setChecked(!checkbox.isChecked());
                if (checkbox.isChecked()) {
                    mInvitedFriends.add(friend);
                } else {
                    mInvitedFriends.remove(friend);
                }
            }
        });

    }

    public boolean isLvPositionSelected(int position) {
        boolean selected = mInvitedFriends.contains(user_friends.get(position));
        Log.d("selected?", "" + selected);
        return selected;
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
            case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                lvBusy = false;
                adapterFriends.notifyDataSetChanged();
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                lvBusy = true;
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                lvBusy = true;
                break;
        }
    }


    public boolean isLvBusy(){
        return lvBusy;
    }


    public void createEvent(){
        final ProgressDialog progress = ProgressDialog.show(this, "Sending Invites...", "", true, true);


        ParseUser currentUser = ParseUser.getCurrentUser();
        Intent intent = getIntent();
        String event = intent.getStringExtra("event");
                ParseQuery<Event> mapQuery = Event.getQuery();
        mapQuery.getInBackground(event, new GetCallback<Event>() {
            public void done(Event eve, ParseException e) {
                if (e == null) {
                    final Event ev = eve;
                    // Creator auto joins
                    ev.join();
                    // Invites
                    ArrayList<String> invitedFbids = new ArrayList();
                    for (User_friend friend : mInvitedFriends) {
                        if (friend.id != null) {
                            invitedFbids.add(friend.id);
                        }
                }
                    ParseUser.getQuery().whereContainedIn("fbid", invitedFbids)
                            .findInBackground(new FindCallback<ParseUser>() {
                                public void done(List<ParseUser> users, ParseException e) {
                                    for (ParseUser user : users) {
                                        ev.invite(user);
                                    }
                                    ev.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            progress.dismiss();
                                            if (e == null) {
                                                // Success. View event.
                                                Intent intent = new Intent(AddFriendsToEventActivity.this, MainActivity.class);
                                                intent.putExtra("event", ev.getObjectId());
                                                AddFriendsToEventActivity.this.finish();
                                                startActivity(intent);
                                            } else {
                                                // Failure.
                                                AlertDialog alert = new AlertDialog.Builder(AddFriendsToEventActivity.this).create();
                                                alert.setTitle("Error");
                                                alert.setMessage(e.getMessage());
                                                alert.setCancelable(true);
                                                alert.show();
                                            }
                                        }
                                    });
                                }
                            });
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event_creation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
