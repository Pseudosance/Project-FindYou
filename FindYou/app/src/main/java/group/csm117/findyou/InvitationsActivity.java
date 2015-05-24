package group.csm117.findyou;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


public class InvitationsActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {

    private List<Event> mEvents;
    public List<Event> getEvents() {
        return mEvents;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Data
        try {
            mEvents = Event.getQuery().whereEqualTo("invited", ParseUser.getCurrentUser()).find();
        } catch (ParseException e) {
            mEvents = new ArrayList();
        }

        // View
        setContentView(R.layout.activity_invitations);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(new EventListAdapter(this, R.layout.event_list_item, mEvents));
        listView.setOnItemClickListener(this);
    }

    // Callbacks

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Event event = mEvents.get(position);

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Accept invitation?");
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case AlertDialog.BUTTON_POSITIVE:
                        event.join();
                        event.saveInBackground();
                        break;
                    case AlertDialog.BUTTON_NEGATIVE:
                        event.uninvite(ParseUser.getCurrentUser());
                        event.saveInBackground();
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

}
