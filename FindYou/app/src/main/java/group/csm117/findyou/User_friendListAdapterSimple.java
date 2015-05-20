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

/**
 * Created by Mitchell on 5/19/2015.
 */
public class User_friendListAdapterSimple extends ArrayAdapter<User_friend> {

    List<User_friend> mylist;

    public User_friendListAdapterSimple(Context _context, List<User_friend> _mylist) {
        super(_context, R.layout.list_item, _mylist);

        this.mylist = _mylist;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = new LinearLayout(getContext());
        String inflater = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater vi = (LayoutInflater)getContext().getSystemService(inflater);
        convertView = vi.inflate(R.layout.list_item, parent, false);


        // Product object
        User_friend friend = getItem(position);


        //
        TextView txtTitle = (TextView) convertView.findViewById(R.id.title);
        txtTitle.setText(friend.title);

        // show image
        ImageView img = (ImageView)convertView.findViewById(R.id.image);

        // download image
        ImageDownloader imageDownloader = new ImageDownloader();
        imageDownloader.download(friend.img_url, img);

        return convertView;
    }

}
