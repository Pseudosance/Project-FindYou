package group.csm117.findyou;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class UserContactsActivity extends Activity implements AbsListView.OnScrollListener {

    List user_friends;
    ListView lvFriends;
    User_friendListAdapterWithCache adapterFriends;

    private boolean lvBusy = false;

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_contacts);

        // populate data
        user_friends = new ArrayList();
        GraphRequest request = GraphRequest.newMyFriendsRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONArrayCallback() {
                    @Override
                    public void onCompleted(
                            JSONArray rows,
                            GraphResponse response) {
                        // Application code for users friends
                        try {
                            Log.d("MyApp", "attemping to grab friends");
                            Log.d("MyAPP", "number of rows: " + rows.length());
                            for(int i=0; i<rows.length(); i++){

                                Log.d("MyApp", "Inside loop ");
                                JSONObject e = rows.getJSONObject(i);

                                user_friends.add(new User_friend(e.optString("name"), e.getJSONObject("picture").getJSONObject("data").getString("url")));

                            }
                        } catch (JSONException e) {
                            Log.e("Error", "json " + e.toString());
                        }
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "picture.type(small),id,name,link");
        request.setParameters(parameters);
        request.executeAsync();

        // Populate list view with more to demonstrate scrolling because I only have 1 friend....
        user_friends.add(new User_friend("Orange", "http://farm5.staticflickr.com/4142/4787427683_3672f1db9a_s.jpg"));
        user_friends.add(new User_friend("Apple", "http://farm4.staticflickr.com/3139/2780642603_8d2c90e364_s.jpg"));
        user_friends.add(new User_friend("Pineapple", "http://farm2.staticflickr.com/1008/1420343003_13eeb0f9f3_s.jpg"));
        user_friends.add(new User_friend("Grape", "http://4.bp.blogspot.com/-M6qZctUebsU/VKQTpu_ARxI/AAAAAAAAAZQ/Wchoe_Jd1C0/s1600/grape%2B1.JPG"));
        user_friends.add(new User_friend("Cherry", "http://graphics8.nytimes.com/newsgraphics/2014/06/16/bittman-eat-cherry/ed5c4f4c098cd142650d7c00014e71abf85d2f86/eatopener_cherry.jpg"));
        user_friends.add(new User_friend("Pumpkin", "https://americanorchard.files.wordpress.com/2013/03/pumpkin-simple-image.jpg"));
        user_friends.add(new User_friend("Banana", "http://saltmarshrunning.com/wp-content/uploads/2014/09/bananasf.jpg"));
        user_friends.add(new User_friend("Strawberry", "http://www.adagio.com/images5/flavor_thumbnail/strawberry.jpg"));
        user_friends.add(new User_friend("Watermelon", "http://www.wzdm.com/wp-content/uploads/2014/08/d0e5Watermelon2.jpg"));

        lvFriends = (ListView) findViewById( R.id.listview);
        adapterFriends = new User_friendListAdapterWithCache(this, user_friends);
        lvFriends.setAdapter(adapterFriends);
        lvFriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                //args2 is the listViews Selected index
                // Send Invite
                Log.d("MyApp", "User clicked on list item!");

            }
        });

    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
            case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                lvBusy = false;
                adapterFriends.notifyDataSetChanged();
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                lvBusy = true;
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                lvBusy = true;
                break;
        }
    }


    public boolean isLvBusy(){
        return lvBusy;
    }

}
