package group.csm117.findyou;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

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
    public Boolean getIsEvent(){
        return getBoolean("isEvent");
    }
    public void setIsEvent(Boolean value){
        put("isEvent", value);
    }
    public ParseUser getUser(){
        return getParseUser("user");
    }
    public void setUser(ParseUser value) {
        put("user", value);
    }


    public String getEvent() {
        return getString("Event");
    }

    public void setEvent(String value) {
        put("Event", value);
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
