package group.csm117.findyou;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


public class InvitationsActivity extends ActionBarActivity
        implements AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    final private List<Event> mEvents = new ArrayList();

    private TextView mHeaderTextView;
    private RefreshableListViewWrapper mRefreshWrapper;
    private EventListAdapter mListAdapter;


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Create

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Views
        setContentView(R.layout.activity_invitations);

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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // List

    @Override
    public void onRefresh() {
        Event.getQuery().whereEqualTo("invited", ParseUser.getCurrentUser())
                .findInBackground(new FindCallback<Event>() {
                    @Override
                    public void done(List<Event> list, ParseException e) {
                        if (e == null) {
                            mListAdapter.clear();
                            mListAdapter.addAll(list);
                            mHeaderTextView.setText("No invites");
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
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int cellPosition, long id) {
        final int position = cellPosition - 1;
        final Event event = mEvents.get(position);

        // Alert to respond to invitation
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Accept invitation?");
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case AlertDialog.BUTTON_POSITIVE: // Accept
                        event.join();
                        onRespondToInvitation(event, position);
                        break;
                    case AlertDialog.BUTTON_NEGATIVE: // Reject
                        event.uninvite(ParseUser.getCurrentUser());
                        onRespondToInvitation(event, position);
                        break;
                }
                dialog.dismiss();
            }
        };
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Accept", listener);
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Reject", listener);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Ignore", listener);
        alertDialog.show();
    }

    private void onRespondToInvitation(final Event event, final int position) {
        event.saveInBackground();
        mEvents.remove(position);
        mListAdapter.notifyDataSetChanged();
    }

}
