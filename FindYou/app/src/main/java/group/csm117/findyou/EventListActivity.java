package group.csm117.findyou;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class EventListActivity extends ActionBarActivity
        implements AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    final private List<Event> mEvents = new ArrayList();

    private TextView mInvitationsBadgeView;
    private TextView mHeaderTextView;
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

        View headerView = (View)getLayoutInflater().inflate(R.layout.event_list_header, mRefreshWrapper.getListView(), false);
        mHeaderTextView = (TextView) headerView.findViewById(R.id.header_text_view);
        mHeaderTextView.setText("Loading...");
        mRefreshWrapper.getListView().addHeaderView(headerView, null, false);

        mListAdapter = new EventListAdapter(this, R.layout.event_list_item, mEvents);
        mRefreshWrapper.getListView().setAdapter(mListAdapter);

        // Load data
        mRefreshWrapper.setRefreshing(true);
        onRefresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
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

        // Custom invitation button
        final MenuItem invitationsMI = menu.findItem(R.id.action_invitations);
        invitationsMI.setActionView(R.layout.action_invitations);
        mInvitationsBadgeView = (TextView) invitationsMI.getActionView().findViewById(R.id.action_badge);
        mInvitationsBadgeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventListActivity.this.onOptionsItemSelected(invitationsMI);
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
        startActivity(new Intent(EventListActivity.this, EventCreationActivity.class));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // List

    @Override
    public void onRefresh() {
        // Get Joined
        Event.getQuery().whereEqualTo("joined", ParseUser.getCurrentUser())
                .findInBackground(new FindCallback<Event>() {
                    @Override
                    public void done(List<Event> list, ParseException e) {
                        if (e == null) {
                            mListAdapter.clear();
                            mListAdapter.addAll(list);
                            mHeaderTextView.setText("No events");
                            if (list.size() == 0) {
                                mHeaderTextView.setVisibility(View.VISIBLE);
                            } else {
                                mHeaderTextView.setVisibility(View.GONE);
                            }
                        } else {
                            mHeaderTextView.setVisibility(View.VISIBLE);
                            mHeaderTextView.setText("Error: " + e.getMessage());
                        }
                        mRefreshWrapper.setRefreshing(false);
                    }
                });
        // Get invitations count
        Event.getQuery().whereEqualTo("invited", ParseUser.getCurrentUser()).countInBackground(new CountCallback() {
            @Override
            public void done(int i, ParseException e) {
                mInvitationsBadgeView.setText(String.valueOf(i));
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int cellPosition, long id) {
        final int position = cellPosition - 1;
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
