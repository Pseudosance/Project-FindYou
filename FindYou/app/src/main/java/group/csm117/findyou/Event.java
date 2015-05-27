package group.csm117.findyou;

import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ParseRelation;
import java.util.List;
import java.util.ArrayList;

@ParseClassName("Event")
public class Event extends ParseObject {

    public static ParseQuery<Event> getQuery() {
        return ParseQuery.getQuery(Event.class);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Title

    public String getTitle() {
        return getString("title");
    }

    public void setTitle(String value) {
        put("title", value);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Description

    public String getDescription() {
        return getString("description");
    }

    public void setDescription(String value) {
        put("description", value);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Creator

    public ParseUser getCreator() {
        return getParseUser("creator");
    }

    public void setCreator(ParseUser value) {
        put("creator", value);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Joined

    private List<ParseUser> mJoined;

    private ParseRelation<ParseUser> getJoinedRelation() {
        return getRelation("joined");
    }

    public void getJoined(final FindCallback<ParseUser> callback) {
        getJoined(callback, false);
    }

    public void getJoined(final FindCallback<ParseUser> callback, boolean forceFetch) {
        if (forceFetch || mJoined == null) {
            getJoinedRelation().getQuery().findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> list, ParseException e) {
                    mJoined = list;
                    callback.done(mJoined, e);
                }
            });
        } else {
            callback.done(mJoined, null);
        }
    }

    // TODO: Remove
    public List<ParseUser> getJoined_DEPRECATED() {
        try {
            return getJoinedRelation().getQuery().find();
        } catch (ParseException e) {
            return new ArrayList();
        }
    }

    public void join() {
        ParseUser curUser = ParseUser.getCurrentUser();
        getJoinedRelation().add(curUser);
        getInvitedRelation().remove(curUser);
    }

    public void leave() {
        getJoinedRelation().remove(ParseUser.getCurrentUser());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Invited

    private List<ParseUser> mInvited;

    private ParseRelation<ParseUser> getInvitedRelation() {
        return getRelation("invited");
    }

    public void getInvited(final FindCallback<ParseUser> callback) {
        getInvited(callback, false);
    }

    public void getInvited(final FindCallback<ParseUser> callback, boolean forceFetch) {
        if (forceFetch || mInvited == null) {
            getInvitedRelation().getQuery().findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> list, ParseException e) {
                    mInvited = list;
                    callback.done(mInvited, e);
                }
            });
        } else {
            callback.done(mInvited, null);
        }
    }

    public void invite(ParseUser user) {
        getInvitedRelation().add(user);
    }

    public void uninvite(ParseUser user) {
        getInvitedRelation().remove(user);
    }

}
