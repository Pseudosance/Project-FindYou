package group.csm117.findyou;

import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.parse.ParseUser;

public class EventListAdapter extends ArrayAdapter{

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
        /* create a new view of my layout and inflate it in the row */
        convertView = (LinearLayout) inflater.inflate(mResourceId, null);

        Event event = (Event) getItem(position);

        String title = event.getTitle();
        TextView titleTextView = (TextView) convertView.findViewById(R.id.titleTextView);
        titleTextView.setText(title);

        // TODO: show friends, not all joined
        List<ParseUser> joined = event.getJoined();
        TextView friendsTextView = (TextView) convertView.findViewById(R.id.friendsTextView);
        friendsTextView.setText(String.format("%d friends going", joined.size()));

        return convertView;
    }
}