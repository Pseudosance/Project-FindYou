package group.csm117.findyou;

import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class EventListAdapter extends ArrayAdapter {

    private int mResourceId;
    private LayoutInflater inflater;
    private Context context;

    public EventListAdapter (Context ctx, int resourceId, List objects) {
        super(ctx, resourceId, objects);
        mResourceId = resourceId;
        inflater = LayoutInflater.from(ctx);
        context = ctx;
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent) {
        Event event = (Event) getItem(position);

        // Get or create cached eventView
        EventListItemHolder eventView;
        if (convertView == null) {
            convertView = (LinearLayout) inflater.inflate(mResourceId, null);
            eventView = new EventListItemHolder(convertView);
            convertView.setTag(eventView);
        }
        else {
            eventView = (EventListItemHolder) convertView.getTag();
        }

        String title = event.getTitle();
        eventView.getTitleTextView().setText(title);

        // TODO: show friends, not all joined
        final EventListItemHolder fEventView = eventView;
        event.getJoined(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> joined, ParseException e) {
                fEventView.getFriendsTextView().setText(String.format("%d friends going", joined.size()));
            }
        });

        return convertView;
    }
}