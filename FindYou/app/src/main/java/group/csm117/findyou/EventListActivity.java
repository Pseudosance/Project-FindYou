package group.csm117.findyou;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class EventListActivity extends ActionBarActivity
        implements AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    final private List<Event> mEvents = new ArrayList();

    private RefreshableListViewWrapper mRefreshWrapper;
    private EventListAdapter mListAdapter;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Create

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Views
        setContentView(R.layout.activity_event_list);

        mRefreshWrapper = (RefreshableListViewWrapper) findViewById(R.id.refresh_wrapper);
        mRefreshWrapper.setOnRefreshListener(this);
        mRefreshWrapper.getListView().setOnItemClickListener(this);
        mListAdapter = new EventListAdapter(this, R.layout.event_list_item, mEvents);
        mRefreshWrapper.getListView().setAdapter(mListAdapter);

        // Load data
        mRefreshWrapper.setRefreshing(true);
        onRefresh();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event_list, menu);


        menu.findItem(R.id.action_settings).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                startActivity(new Intent(EventListActivity.this, SettingsActivity.class));
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        final int menuId = item.getItemId();
        if (menuId == R.id.action_friends) {
            openFriends();
        } else if (menuId == R.id.action_new) {
            openNew();
        } else if (menuId == R.id.action_invitations) {
             openInvitations();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void openFriends() {
        startActivity(new Intent(EventListActivity.this, UserContactsActivity.class));
    }

    private void openInvitations() {
        startActivity(new Intent(this, InvitationsActivity.class));
    }

    private void openNew() {
        // TODO: open event creation activity
        startActivity(new Intent(EventListActivity.this, EventCreationActivity.class));
       /* ParseUser currentUser = ParseUser.getCurrentUser();
        final Event newEvent = new Event();
        newEvent.setTitle("New Event");
        newEvent.setDescription("Description");
        newEvent.setCreator(currentUser);
        newEvent.join();
        mRefreshWrapper.setRefreshing(true);
        ParseUser.getQuery().whereNotEqualTo("objectId", currentUser.getObjectId())
                .findInBackground(new FindCallback<ParseUser>() {
                    public void done(List<ParseUser> users, ParseException e) {
                        for (ParseUser user : users) {
                            newEvent.invite(user);
                        }
                        newEvent.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    mListAdapter.insert(newEvent, 0);
                                }
                                mRefreshWrapper.setRefreshing(false);
                            }
                        });

                    }
                }); */
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // List

    @Override
    public void onRefresh() {
        Event.getQuery().whereEqualTo("joined", ParseUser.getCurrentUser())
                .findInBackground(new FindCallback<Event>() {
                    @Override
                    public void done(List<Event> list, ParseException e) {
                        if (e == null) {
                            mListAdapter.clear();
                            mListAdapter.addAll(list);
                        }
                        mRefreshWrapper.setRefreshing(false);
                    }
                });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        final Event event = mEvents.get(position);

        // TODO: go to edit view
        // For now this helps with testing
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Manage event");
        alertDialog.setCancelable(true);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case AlertDialog.BUTTON_POSITIVE: // Un-accept
                        event.leave();
                        event.invite(ParseUser.getCurrentUser());
                        event.saveInBackground();
                        mEvents.remove(position);
                        mListAdapter.notifyDataSetChanged();
                        break;
                    case AlertDialog.BUTTON_NEGATIVE: // Delete
                        event.deleteInBackground();
                        mEvents.remove(position);
                        mListAdapter.notifyDataSetChanged();
                        break;
                    case AlertDialog.BUTTON_NEUTRAL:
                        Intent intent = new Intent(EventListActivity.this, MainActivity.class);
                        intent.putExtra("event", event.getObjectId());
                        startActivity(intent);
                        break;
                }
                dialog.dismiss();
            }
        };
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Un-accept", listener);
        if (event.getCreator().hasSameId(ParseUser.getCurrentUser())) {
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Delete", listener);
        }
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "View", listener);
        alertDialog.show();
    }
}
