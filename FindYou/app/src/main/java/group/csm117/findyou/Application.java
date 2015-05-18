package group.csm117.findyou;

import android.content.Context;
import android.content.SharedPreferences;

import com.facebook.FacebookSdk;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;

/**
 * Created by Mitchell on 5/7/2015.
 */
public class Application extends android.app.Application {
    public static final boolean APPDEBUG = false;
    public static final String APPTAG = "FindYou";
    public static final String INTENT_EXTRA_LOCATION = "location";

    // Key for saving the search distance preference
    private static final String KEY_SEARCH_DISTANCE = "searchDistance";

    private static final float DEFAULT_SEARCH_DISTANCE = 250.0f;

    private static ConfigHelper configHelper;
    private static SharedPreferences preferences;

    public Application(){}

    @Override
    public void onCreate(){
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());
        ParseObject.registerSubclass(FindYouPost.class);
        Parse.initialize(this, "WyyPQ72c4Qk0U47BrnlrXCgXWVItNloBG94bqSzH", "6flz6M11tkjFpZVeDPFaGGE1cUmPoleMMrfxHKQh");
        ParseFacebookUtils.initialize(this);

        preferences = getSharedPreferences("group.csm117.findyou", Context.MODE_PRIVATE);

        configHelper = new ConfigHelper();
        configHelper.fetchConfigIfNeeded();

    }
    public static void setSearchDistance(float value) {
        preferences.edit().putFloat(KEY_SEARCH_DISTANCE, value).commit();
    }

    public static float getSearchDistance() {
        return preferences.getFloat(KEY_SEARCH_DISTANCE, DEFAULT_SEARCH_DISTANCE);
    }

    public static ConfigHelper getConfigHelper() {
        return configHelper;
    }
}

