package group.csm117.findyou;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/*

read more: http://developer.android.com/training/improving-layouts/smooth-scrolling.html
 */
public class User_friendListAdapterWithCache_MapInvites extends ArrayAdapter<User_friend> {
    private Context mContext;
    List<User_friend> mylist;
    public List<User_friend> selectedFriends;
    ImageDownloader mImageDownloader = new ImageDownloader();

    public User_friendListAdapterWithCache_MapInvites(Context _context, List<User_friend> _mylist) {
        super(_context, R.layout.list_item, _mylist);

        mContext = _context;
        this.mylist = _mylist;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        User_friend friend  = getItem(position);

        FriendViewHolder holder;

        if (convertView == null) {
            convertView = new LinearLayout(getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater vi = (LayoutInflater)getContext().getSystemService(inflater);
            convertView = vi.inflate(R.layout.list_item, parent, false);

            //
            holder = new FriendViewHolder();
            holder.img = (ImageView)convertView.findViewById(R.id.image);
            holder.title = (TextView)convertView.findViewById(R.id.title);
            holder.checkbox = (CheckBox)convertView.findViewById(R.id.checkbox);
            holder.checkbox.setVisibility(View.VISIBLE);

            //
            convertView.setTag(holder);
        }
        else{
            holder = (FriendViewHolder) convertView.getTag();
        }

        // Populate
        holder.title.setText(friend.title);
        if (!((AddFriendsToEventActivity) mContext).isLvBusy()){
            // download from internet
            // TODO: downloaded images may be too big, causing GC to clear heap and forget which
            // friends were selected. Or my test phone is just too underpowered...
            mImageDownloader.download(friend.img_url, holder.img);
        } else {
            holder.img.setImageResource(R.drawable.spinner);
        }
        holder.checkbox.setChecked(((AddFriendsToEventActivity) mContext).isLvPositionSelected(position));

        //
        return convertView;
    }

    static class FriendViewHolder {
        public ImageView img;
        public TextView title;
        public CheckBox checkbox;
    }

}