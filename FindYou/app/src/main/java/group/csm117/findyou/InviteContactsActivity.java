package group.csm117.findyou;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.ListView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class InviteContactsActivity extends Activity implements AbsListView.OnScrollListener {

    List user_friends;
    ListView lvFriends;
    User_friendListAdapterWithCache adapterFriends;

    private boolean lvBusy = false;

    /** Called when the activity is first created. */
   /* ListView list;
    private List<String> List_file;

    private void CreateListView()
    {
        List_file.add("Coderzheaven");
        List_file.add("Google");
        List_file.add("Android");
        List_file.add("iPhone");
        List_file.add("Apple");
        //Create an adapter for the listView and add the ArrayList to the adapter.
        list.setAdapter(new ArrayAdapter<String>(InviteContactsActivity.this, android.R.layout.simple_list_item_1,List_file));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3)
            {
                //args2 is the listViews Selected index
                // Send Invite

                Log.d("MyApp", "User clicked on list item!");

            }
        });
    }
*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_contacts);

        /*List_file = new ArrayList<String>();
        list = (ListView)findViewById(R.id.listview);

        CreateListView();*/

        // populate data
        user_friends = new ArrayList();


        user_friends.add(new User_friend("Orange", "http://farm5.staticflickr.com/4142/4787427683_3672f1db9a_s.jpg"));
        user_friends.add(new User_friend("Apple", "http://farm4.staticflickr.com/3139/2780642603_8d2c90e364_s.jpg"));
        user_friends.add(new User_friend("Pineapple", "http://farm2.staticflickr.com/1008/1420343003_13eeb0f9f3_s.jpg"));

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
                            Log.d("MyAPP", "number of rows" + rows.length());
                            for(int i=0; i<rows.length(); i++){
                                Log.d("MyApp", "Inside loop ");
                                JSONObject e = rows.getJSONObject(i);
                                user_friends.add(new User_friend("Apple", "http://farm4.staticflickr.com/3139/2780642603_8d2c90e364_s.jpg"));
                                user_friends.add(new User_friend(e.optString("name"), e.optString("img_url")));

                            }
                        } catch (JSONException e) {
                            Log.e("Error", "json " + e.toString());
                        }
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link");
        request.setParameters(parameters);
        request.executeAsync();

        // JSONArray rows = ...   // Data parsed from server
/*
        try {
            for(int i=0; i<rows.length(); i++){
                JSONObject e = rows.getJSONObject(i);

                user_friends.add(new User_friend(e.optString("name"), e.optString("img_url")));
            }
        } catch (JSONException e) {
            Log.e("Error", "json " +e.toString());
        }
*/
        lvFriends = (ListView) findViewById( R.id.listview);
        adapterFriends = new User_friendListAdapterWithCache(this, user_friends);
        lvFriends.setAdapter(adapterFriends);

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
