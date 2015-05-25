package group.csm117.findyou;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * Created by Mitchell on 5/7/2015.
 */
//Data model for a post
    @ParseClassName("Posts")
public class FindYouPost extends ParseObject {
    public String getText(){
        return getString("text");
    }
    public void setText(String value){
        put("text", value);
    }

    public String getEvent() {
        return getString("Event");
    }

    public void setEvent(String value) {
        put("Event", value);
    }

    public JSONArray getUsersThatCanSee() {
        return getJSONArray("canSee");
    }

    public void setUsersThatCanSee(ArrayList<String> value) {
        put("canSee", value);
    }

    public Boolean getDraggable() {
        return getBoolean("draggable");
    }

    public void setDraggable(Boolean b) {
        put("draggable", b);
    }

    public String getTitle() {
        return getString("title");
    }

    public ParseUser getUser(){
        return getParseUser("user");
    }
    public void setUser(ParseUser value) {
        put("user", value);
    }

    public void setTitle(String value) {
        put("title", value);
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint("location");
    }

    public void setLocation(ParseGeoPoint value) {
        put("location", value);
    }

    public static ParseQuery<FindYouPost> getQuery() {
        return ParseQuery.getQuery(FindYouPost.class);
    }
}
