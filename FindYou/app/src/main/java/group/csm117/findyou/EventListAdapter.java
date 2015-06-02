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

        // Fill data
        eventView.getTitleTextView().setText(event.getTitle());
        eventView.getDescriptionTextView().setText(event.getDescription());
        final EventListItemHolder fEventView = eventView;
        event.getJoined(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> joined, ParseException e) {
                int count = joined.size();
                String text;
                if (count == 1) {
                    text = "1 person going";
                } else {
                    text = String.format("%d people going", count);
                }
                fEventView.getFriendsTextView().setText(text);
            }
        });

        return convertView;
    }
}