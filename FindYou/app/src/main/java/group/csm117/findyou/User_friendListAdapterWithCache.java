package group.csm117.findyou;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;


/*

read more: http://developer.android.com/training/improving-layouts/smooth-scrolling.html
 */
public class User_friendListAdapterWithCache extends ArrayAdapter<User_friend> {
    private Context mContext;
    List<User_friend> mylist;

    public User_friendListAdapterWithCache(Context _context, List<User_friend> _mylist) {
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



            //
            convertView.setTag(holder);
        }
        else{
            holder = (FriendViewHolder) convertView.getTag();
        }


        //
        holder.populate(friend, ((InviteToEventActivity)mContext).isLvBusy());

        //
        return convertView;
    }


    static class FriendViewHolder {
        public ImageView img;
        public TextView title;

        void populate(User_friend p) {
            title.setText(p.title);

            //
            ImageDownloader imageDownloader = new ImageDownloader();
            imageDownloader.download(p.img_url, img);

        }

        void populate(User_friend f, boolean isBusy) {
            title.setText(f.title);

            if (!isBusy){
                // download from internet
                ImageDownloader imageDownloader = new ImageDownloader();
                imageDownloader.download(f.img_url, img);
            }
            else{
                // set default image
                img.setImageResource(R.drawable.spinner);
            }
        }
    }

}