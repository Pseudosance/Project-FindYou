package group.csm117.findyou;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.FindCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * An activity representing a list of Events.
 * <p/>
 * The activity makes use of fragments. The list of items is a
 * {@link EventListFragment}.
 * <p/>
 * This activity also implements the required
 * {@link EventListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class EventListActivity extends ActionBarActivity
        implements EventListFragment.Callbacks {

    private List<Event> mEvents;
    public List<Event> getEvents() {
        return mEvents;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mEvents = Event.getQuery().find();
        } catch (ParseException e) {
            mEvents = new ArrayList();
        }
        setContentView(R.layout.activity_event_list);
    }

    /**
     * Callback method from {@link EventListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(int position) {
        Event post = mEvents.get(position);
//        Intent detailIntent = new Intent(this, EventDetailActivity.class);
//        detailIntent.putExtra(EventDetailFragment.ARG_EVENT_ID, post.getObjectId());
//        startActivity(detailIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_event_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        final int menuId = item.getItemId();
        if (menuId == R.id.action_friends) {
            openFriends();
            return true;
        } else if (menuId == R.id.action_new) {
            openNew();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void openFriends() {
    }

    private void openNew() {
        // TODO: open event creation activity
        ParseUser currentUser = ParseUser.getCurrentUser();
        final Event newEvent = new Event();
        newEvent.setTitle("New Event");
        newEvent.setDescription("Description");
        newEvent.setCreator(currentUser);
        newEvent.join();
        ParseUser.getQuery().whereNotEqualTo("objectId", currentUser.getObjectId())
                .findInBackground(new FindCallback<ParseUser>() {
                    public void done(List<ParseUser> users, ParseException e) {
                        for (ParseUser user : users) {
                            newEvent.invite(user);
                        }
                        newEvent.saveInBackground();
                    }
                });
    }
}
