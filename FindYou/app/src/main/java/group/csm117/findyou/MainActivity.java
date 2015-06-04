package group.csm117.findyou;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MainActivity extends FragmentActivity implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnMarkerDragListener  {

    /*
 * Define a request code to send to Google Play services This code is returned in
 * Activity.onActivityResult
 */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /*
   * Constants for location update parameters
   */
    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;

    // The update interval
    private static final int UPDATE_INTERVAL_IN_SECONDS = 1;

    // A fast interval ceiling
    private static final int FAST_CEILING_IN_SECONDS = 1;

    // Update interval in milliseconds
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
            * UPDATE_INTERVAL_IN_SECONDS;

    // A fast ceiling of update intervals, used when the app is visible
    private static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
            * FAST_CEILING_IN_SECONDS;


    /*
  * Constants for handling location results
  */
    // Conversion from feet to meters
    private static final float METERS_PER_FEET = 0.3048f;

    // Conversion from kilometers to meters
    private static final int METERS_PER_KILOMETER = 100;

    // Initial offset for calculating the map bounds
    private static final double OFFSET_CALCULATION_INIT_DIFF = 1.0;

    // Accuracy for calculating the map bounds
    private static final float OFFSET_CALCULATION_ACCURACY = 0.01f;

    // Maximum results returned from a Parse query
    private static final int MAX_POST_SEARCH_RESULTS = 20;

    // Maximum post search radius for map in kilometers
    private static final int MAX_POST_SEARCH_DISTANCE = 100;

    // Map fragment
    private SupportMapFragment mapFragment;

    // Represents the circle around a map
    private Circle mapCircle;

    // Fields for the map radius in feet
    private float radius;
    private float lastRadius;

    // Fields for helping process map and location changes
    private final Map<String, Marker> mapMarkers = new HashMap<String, Marker>();
    private int mostRecentMapUpdate;
    private boolean hasSetUpInitialLocation;
    private String selectedPostObjectId;
    private Marker selectedMarker;
    private Location lastLocation;
    private Location currentLocation;
    private Event curEvent;
    private FindYouPost det;

    // A request to connect to Location Services
    private LocationRequest locationRequest;

    // Stores the current instantiation of the location client in this object
    private GoogleApiClient locationClient;

    // Adapter for the Parse query
    private ParseQueryAdapter<FindYouPost> postsQueryAdapter;



    @Override
    public void onMarkerDrag(Marker marker) {
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        LatLng pos = marker.getPosition();
        final ParseGeoPoint geoPoint = new ParseGeoPoint(pos.latitude, pos.longitude);
        String post = null;

        // get post id of the marker
        for(String postID: mapMarkers.keySet()) {
            if (mapMarkers.get(postID).getId().equals(marker.getId())) {
                post = postID;
                break;
            }
        }

        ParseQuery<FindYouPost> mapQuery = FindYouPost.getQuery();
        mapQuery.getInBackground(post, new GetCallback<FindYouPost>() {
            public void done(FindYouPost post, ParseException e) {
                if (e == null) {
                    post.put("location", geoPoint);
                    post.saveInBackground();
                }
            }
        });
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        radius = Application.getSearchDistance();
        lastRadius = radius;
        setContentView(R.layout.activity_main);

        // Create a new global location parameters object
        locationRequest = LocationRequest.create();

        // Set the update interval
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Use high accuracy
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //Set the interval ceiling to one minute
        locationRequest.setFastestInterval(FAST_INTERVAL_CEILING_IN_MILLISECONDS);

//        Create a new location client, using the enclosing class to handle callbacks.
        locationClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        // Enable the current location "blue dot"
        mapFragment.getMap().setMyLocationEnabled(true);
        // Set up the camera change handler
        mapFragment.getMap().setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            public void onCameraChange(CameraPosition position) {
                // Run the map query
                doMapQuery();
                doListQuery();
            }
        });

        mapFragment.getMap().setOnMarkerDragListener(this);

        mapFragment.getMap().setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

                                                          @Override public boolean onMarkerClick(Marker marker) {
                                                              selectedMarker = marker;
                                                              marker.showInfoWindow();
                                                              return true;
                                                          }

                                                      }
        );

        // Set up the event title at the top of screen
        Intent intent = getIntent();
        String event = intent.getStringExtra("event");

        ParseQuery<Event> mapQuery = Event.getQuery();
        mapQuery.getInBackground(event, new GetCallback<Event>() {
            public void done(Event ev, ParseException e) {
                if (e == null) {
                    TextView textv = (TextView) findViewById(R.id.textView);
                    textv.setText(ev.getTitle());
                    TextView textvw = (TextView) findViewById(R.id.textView2);
                    textvw.setText(ev.getDescription());
                    curEvent = ev;
                }
            }
        });

        // Set up the handler for the post button click
        Button postButton = (Button) findViewById(R.id.post_button);
        postButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Only allow posts if we have a location
                Location myLoc = (currentLocation == null) ? lastLocation : currentLocation;
                if (myLoc == null) {
                    Toast.makeText(MainActivity.this,
                            "Please try again after your location appears on the map.", Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent = new Intent(MainActivity.this, PostActivity.class);
                intent.putExtra(Application.INTENT_EXTRA_LOCATION, myLoc);
                Intent i = getIntent();
                String eve = i.getStringExtra("event");
                intent.putExtra("event", eve);
                startActivity(intent);
            }
        });

        // Set up the handler for the post button click
        Button inviteButton = (Button) findViewById(R.id.invite_button);
        inviteButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddFriendsToEventActivity.class);
                Intent i = getIntent();
                String eve = i.getStringExtra("event");
                intent.putExtra("event", eve);
                startActivity(intent);
            }
        });

        // Set up the handler for the delete button click
        Button deleteButton = (Button) findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // get post id of the marker
                String detail = null;
                for(String postID: mapMarkers.keySet()) {
                    if (mapMarkers.get(postID).getId().equals(selectedMarker.getId())) {
                        detail = postID;
                        break;
                    }
                }

                ParseQuery<FindYouPost> mapQuery = FindYouPost.getQuery();
                mapQuery.getInBackground(detail, new GetCallback<FindYouPost>() {
                    public void done(FindYouPost post, ParseException e) {
                        if (e == null) {
                            det = post;
                            if ((det == null) || (det.getEvent().equals("USER")) || det.getIsEvent() || (det.getEvent().equals("ERROR"))) {
                                Toast.makeText(MainActivity.this,
                                        "Please select a detail to delete on the map.", Toast.LENGTH_LONG).show();
                                return;
                            }
                            if (!det.getUser().hasSameId(ParseUser.getCurrentUser()))  {
                                Toast.makeText(MainActivity.this,
                                        "Error: You did not create this detail.", Toast.LENGTH_LONG).show();
                                return;
                            }

                            ParseQuery<FindYouPost> MQ = FindYouPost.getQuery();
                            MQ.getInBackground(det.getObjectId(), new GetCallback<FindYouPost>() {
                                        public void done(FindYouPost post, ParseException e) {
                                            if (e == null) {
                                                // delete the detail
                                                final FindYouPost p = post;
                                                try {
                                                    post.delete();
                                                } catch (Exception ex) {
                                                    Toast.makeText(MainActivity.this,
                                                            "Cannot delete.", Toast.LENGTH_LONG).show();
                                                }
                                                mapMarkers.get(post.getObjectId()).remove();
                                            }
                                        }
                                    }
                            );
                        }
                    }
                });

            }
        });

    }

    /*
  * Called when the Activity is no longer visible at all. Stop updates and disconnect.
  */
    @Override
    public void onStop() {
        // If the client is connected
        if (locationClient.isConnected()) {
            stopPeriodicUpdates();
        }

        // After disconnect() is called, the client is considered "dead".
        locationClient.disconnect();

        super.onStop();
    }

    /*
       * Called when the Activity is restarted, even before it becomes visible.
       */
    @Override
    public void onStart() {
        super.onStart();

        // Connect to the location services client
        locationClient.connect();
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);

        Application.getConfigHelper().fetchConfigIfNeeded();

        // Get the latest search distance preference
        radius = Application.getSearchDistance();
        // Checks the last saved location to show cached data if it's available
        if (lastLocation != null) {
            LatLng myLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            // If the search distance preference has been changed, move
            // map to new bounds.
            if (lastRadius != radius) {
                updateZoom(myLatLng);
            }
            // Update the circle map
            updateCircle(myLatLng);
        }
        // Save the current radius
        lastRadius = radius;
        // Query for the latest data to update the views.
        doMapQuery();
        doListQuery();

    }

    /*
   * Handle results returned to this Activity by other Activities started with
   * startActivityForResult(). In particular, the method onConnectionFailed() in
   * LocationUpdateRemover and LocationUpdateRequester may call startResolutionForResult() to start
   * an Activity that handles Google Play services problems. The result of this call returns here,
   * to onActivityResult.
   */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // Choose what to do based on the request code
        switch (requestCode) {

            // If the request code matches the code sent in onConnectionFailed
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:

                switch (resultCode) {
                    // If Google Play services resolved the problem
                    case Activity.RESULT_OK:

                        if (Application.APPDEBUG) {
                            // Log the result
                            Log.d(Application.APPTAG, "Connected to Google Play services");
                        }

                        break;

                    // If any other result was returned by Google Play services
                    default:
                        if (Application.APPDEBUG) {
                            // Log the result
                            Log.d(Application.APPTAG, "Could not connect to Google Play services");
                        }
                        break;
                }

                // If any other request code was received
            default:
                if (Application.APPDEBUG) {
                    // Report that this Activity received an unknown requestCode
                    Log.d(Application.APPTAG, "Unknown request code received for the activity");
                }
                break;
        }

    }

    /*
   * Verify that Google Play services is available before making a request.
   *
   * @return true if Google Play services is available, otherwise false
   */
    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            if (Application.APPDEBUG) {
                // In debug mode, log the status
                Log.d(Application.APPTAG, "Google play services available");
            }
            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {
            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getSupportFragmentManager(), Application.APPTAG);
            }
            return false;
        }

    }

    /*
       * Called by Location Services when the request to connect the client finishes successfully. At
       * this point, you can request the current location or start periodic updates
       */
    public void onConnected(Bundle bundle) {
        if (Application.APPDEBUG) {
            Log.d("Connected loc services", Application.APPTAG);
        }
        currentLocation = getLocation();
        startPeriodicUpdates();

    }

    /*
       * Called by Location Services if the connection to the location client drops because of an error.
       */
    public void onDisconnected() {
        if (Application.APPDEBUG) {
            Log.d("Disconnect locServices", Application.APPTAG);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(Application.APPTAG, "GoogleApiClient connection has been suspend");
    }

    /*
     * Called by Location Services if the attempt to Location Services fails.
     */
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Google Play services can resolve some errors it detects. If the error has a resolution, try
        // sending an Intent to start a Google Play services activity that can resolve error.
        if (connectionResult.hasResolution()) {
            try {

                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

            } catch (IntentSender.SendIntentException e) {

                if (Application.APPDEBUG) {
                    // Thrown if Google Play services canceled the original PendingIntent
                    Log.d(Application.APPTAG, "An error occurred when connecting to location services.", e);
                }
            }
        } else {
            // If no resolution is available, display a dialog to the user with the error.
            showErrorDialog(connectionResult.getErrorCode());
        }

    }

    /*
     * Report location updates to the UI.
     */
    public void onLocationChanged(Location location) {
        currentLocation = location;
        if (lastLocation != null
                && geoPointFromLocation(location)
                .distanceInKilometersTo(geoPointFromLocation(lastLocation)) < 0.01) {
            // If the location hasn't changed by more than 10 meters, ignore it.
            return;
        }
        lastLocation = location;
        LatLng myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (!hasSetUpInitialLocation) {
            // Zoom to the current location.
            updateZoom(myLatLng);
            hasSetUpInitialLocation = true;
        }
        // Update map radius indicator
        updateCircle(myLatLng);
        doMapQuery();
        doListQuery();

    }

    /*
       * In response to a request to start updates, send a request to Location Services
       */
    private void startPeriodicUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                locationClient, locationRequest, this);
    }

    /*
     * In response to a request to stop updates, send a request to Location Services
     */
    private void stopPeriodicUpdates() {
        locationClient.disconnect();
    }

    /*
     * Get the current location
     */
    private Location getLocation() {
        // If Google Play Services is available
        if (servicesConnected()) {
            // Get the current location
            return LocationServices.FusedLocationApi.getLastLocation(locationClient);
        } else {
            return null;
        }

    }

    /*
     * Helper method to clean up old markers
     */
    private void cleanUpMarkers(Set<String> markersToKeep) {
        for (String objId : new HashSet<String>(mapMarkers.keySet())) {
            if (!markersToKeep.contains(objId)) {
                Marker marker = mapMarkers.get(objId);
                marker.remove();
                mapMarkers.get(objId).remove();
                mapMarkers.remove(objId);
            }
        }
    }

    /*
     * Helper method to get the Parse GEO point representation of a location
     */
    private ParseGeoPoint geoPointFromLocation(Location loc) {
        return new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());
    }

    private void doListQuery() {/*
        Location myLoc = (currentLocation == null) ? lastLocation : currentLocation;
        if (myLoc != null) {
            postsQueryAdapter.loadObjects();
        }*/

    }

    private void doMapQuery() {
        final int myUpdateNumber = ++mostRecentMapUpdate;
        // 1
        Location myLoc = (currentLocation == null) ? lastLocation : currentLocation;
        if (myLoc == null) {
            cleanUpMarkers(new HashSet<String>());
            return;
        }
        // 2
        final ParseGeoPoint myPoint = geoPointFromLocation(myLoc);
        // 3
        ParseQuery<FindYouPost> mapQuery = FindYouPost.getQuery();
        // 4
        //mapQuery.whereWithinKilometers("location", myPoint, MAX_POST_SEARCH_DISTANCE);
        // 5
        mapQuery.include("user");
        mapQuery.orderByDescending("createdAt");
        mapQuery.setLimit(MAX_POST_SEARCH_RESULTS);
        // 6
        mapQuery.findInBackground(new FindCallback<FindYouPost>() {
            @Override
            public void done(List<FindYouPost> objects, ParseException e) {
                // Handle the results
                if (e != null) {
                    if (Application.APPDEBUG) {
                        Log.d(Application.APPTAG, "An error occurred while querying for map posts.", e);
                    }
                    return;
                }

                /*
                 * Make sure we're processing results from
                 * the most recent update, in case there
                 * may be more than one in progress.
                 */
                if (myUpdateNumber != mostRecentMapUpdate) {
                    return;
                }

                // No errors, process query results
                // 1
                Set<String> toKeep = new HashSet<String>();
                FindYouPost myLocation = null;
                FindYouPost myEvent = null;

                // loop through results of query
                for (FindYouPost post : objects) {

                    // check if this is the user's current location marker
                    if (post.getEvent() != null) {
                        if (post.getEvent().equals("USER") && post.getUser().getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
                            myLocation = post;
                            continue;
                        }
                    }

                    // check if this is the current Event marker
                    if (post.getEvent() != null) {
                        if (post.getEvent().equals(curEvent.getObjectId()) && post.getIsEvent()) {
                            myEvent = post;
                        }
                    }

                    //Only show posts of the currently selected event or locations of other users
                    if (!post.getEvent().equals(curEvent.getObjectId()) && !(post.getEvent().equals("USER")))
                        continue;

                    // If it is a location, only show it if that user is part of the event.
                    // TODO: use Event#getJoined()
                    List<ParseUser> joined = curEvent.getJoined_DEPRECATED();
                    if (post.getEvent().equals("USER")) {
                        boolean wasInvited = false;
                        for (ParseUser user : joined) {
                            if (user.hasSameId(post.getUser())) {
                                wasInvited = true;
                                break;
                            }
                        }
                        if (!wasInvited)
                            continue;
                    }

                    toKeep.add(post.getObjectId());
                    Marker oldMarker = mapMarkers.get(post.getObjectId());
                    MarkerOptions markerOpts =
                            new MarkerOptions().position(new LatLng(post.getLocation().getLatitude(), post
                                    .getLocation().getLongitude()));

                    // skip marker update if position is the same
                    if (oldMarker != null)
                        if ((oldMarker.getPosition().latitude == post.getLocation().getLatitude()) && (oldMarker.getPosition().longitude == post.getLocation().getLongitude()))
                            continue;

                    // Set up an in-range marker
                    if (oldMarker != null) {
                        //                          if (oldMarker.getSnippet() != null) {
                        //                            continue;
                        //                      } else {
                        oldMarker.remove();
//                            }
                    }
                    // Get facebook name
                    final FindYouPost p = post;
                    if (!(p.getUser().getUsername().equals(ParseUser.getCurrentUser().getUsername()))) {
                        GraphRequest request = GraphRequest.newMyFriendsRequest(
                                AccessToken.getCurrentAccessToken(),
                                new GraphRequest.GraphJSONArrayCallback() {
                                    @Override
                                    public void onCompleted(
                                            JSONArray rows,
                                            GraphResponse response) {
                                        // Application code for users friends
                                        if (rows == null) {
                                            return;
                                        }
                                        try {
                                            Log.d("MyApp", "attemping to grab friends");
                                            if (rows != null)
                                                Log.d("MyAPP", "number of rows: " + rows.length());
                                            if (rows != null)
                                                for (int i = 0; i < rows.length(); i++) {

                                                    Log.d("MyApp", "Inside loop ");
                                                    JSONObject e = rows.getJSONObject(i);

                                                    if (p.getUser().get("fbid").equals(e.optString("id"))) {
                                                        try {
                                                            String name = e.optString("name");
                                                            MarkerOptions markerOpts =
                                                                    new MarkerOptions().position(new LatLng(p.getLocation().getLatitude(), p
                                                                            .getLocation().getLongitude()));
                                                            // allow markers to only be draggable if you created them. Color according to type.
                                                            if (p.getEvent().equals("USER")) {
                                                                markerOpts =
                                                                        markerOpts.draggable(false).title(p.getText())
                                                                                .icon(BitmapDescriptorFactory.defaultMarker(
                                                                                        BitmapDescriptorFactory.HUE_VIOLET));
                                                            } else if (p.getUser().getUsername().equals(ParseUser.getCurrentUser().getUsername()) && !p.getIsEvent()) {
                                                                markerOpts =
                                                                        markerOpts.draggable(true).title(p.getText())
                                                                                .snippet(name)
                                                                                .icon(BitmapDescriptorFactory.defaultMarker(
                                                                                        BitmapDescriptorFactory.HUE_GREEN));
                                                            } else if (p.getUser().getUsername().equals(ParseUser.getCurrentUser().getUsername()) && p.getIsEvent()) {
                                                                markerOpts =
                                                                        markerOpts.draggable(true).title(curEvent.getDescription())
                                                                                .snippet(name)
                                                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.event));
                                                            } else if (p.getIsEvent()) {
                                                                markerOpts =
                                                                        markerOpts.draggable(false).title(curEvent.getDescription())
                                                                                .snippet(name)
                                                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.event));
                                                            } else {
                                                                markerOpts =
                                                                        markerOpts.draggable(false).title(p.getText())
                                                                                .snippet(name)
                                                                                .icon(BitmapDescriptorFactory.defaultMarker(
                                                                                        BitmapDescriptorFactory.HUE_RED));
                                                            }
                                                            //     }
                                                            // 7
                                                            Marker marker = mapFragment.getMap().addMarker(markerOpts);
                                                            mapMarkers.put(p.getObjectId(), marker);
                                                        } catch (Exception ex) {
                                                        }
                                                    }

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
                    } else {
                        GraphRequest request = GraphRequest.newMeRequest(
                                AccessToken.getCurrentAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(
                                            JSONObject object,
                                            GraphResponse response) {
                                        try {
                                            String name = object.optString("name");
                                            MarkerOptions markerOpts =
                                                    new MarkerOptions().position(new LatLng(p.getLocation().getLatitude(), p
                                                            .getLocation().getLongitude()));
                                            // allow markers to only be draggable if you created them. Color according to type.
                                            if (p.getEvent().equals("USER")) {
                                                markerOpts =
                                                        markerOpts.draggable(false).title(p.getText())
                                                                .icon(BitmapDescriptorFactory.defaultMarker(
                                                                        BitmapDescriptorFactory.HUE_VIOLET));
                                            } else if (p.getUser().getUsername().equals(ParseUser.getCurrentUser().getUsername()) && !p.getIsEvent()) {
                                                markerOpts =
                                                        markerOpts.draggable(true).title(p.getText())
                                                                .snippet(name)
                                                                .icon(BitmapDescriptorFactory.defaultMarker(
                                                                        BitmapDescriptorFactory.HUE_GREEN));
                                            } else if (p.getUser().getUsername().equals(ParseUser.getCurrentUser().getUsername()) && p.getIsEvent()) {
                                                markerOpts =
                                                        markerOpts.draggable(true).title(curEvent.getDescription())
                                                                .snippet(name)
                                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.event));
                                            } else if (p.getIsEvent()) {
                                                markerOpts =
                                                        markerOpts.draggable(false).title(curEvent.getDescription())
                                                                .snippet(name)
                                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.event));
                                            } else {
                                                markerOpts =
                                                        markerOpts.draggable(false).title(p.getText())
                                                                .snippet(name)
                                                                .icon(BitmapDescriptorFactory.defaultMarker(
                                                                        BitmapDescriptorFactory.HUE_RED));
                                            }
                                            //     }
                                            // 7
                                            Marker marker = mapFragment.getMap().addMarker(markerOpts);
                                            mapMarkers.put(p.getObjectId(), marker);
                                        } catch (Exception e) {
                                        }
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "name");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }
                }

                // make the marker for the event
                if (myEvent == null) {
                    myEvent = new FindYouPost();
                    myEvent.setLocation(myPoint);
                    myEvent.setText(curEvent.getTitle());
                    myEvent.setEvent(curEvent.getObjectId());
                    myEvent.setIsEvent(true);
                    myEvent.setUser(ParseUser.getCurrentUser());
                    ParseACL acl = new ParseACL();
                    acl.setPublicReadAccess(true);
                    acl.setPublicWriteAccess(true);
                    myEvent.setACL(acl);
                    myEvent.saveInBackground();

                    MarkerOptions markerOpts =
                            new MarkerOptions().position(new LatLng(myEvent.getLocation().getLatitude(), myEvent
                                    .getLocation().getLongitude()));
                    markerOpts =
                            markerOpts.title(myEvent.getText()).draggable(true)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.event));
                    Marker marker = mapFragment.getMap().addMarker(markerOpts);
                    mapMarkers.put(myEvent.getObjectId(), marker);
                    toKeep.add(myEvent.getObjectId());

                }


                //make sure the user's current location is updated and displayed.
        if (myLocation != null) {
          // Check for an existing marker for this post, need to update location if so
          Marker oldMarker = mapMarkers.get(myLocation.getObjectId());
          if (oldMarker != null)
            oldMarker.remove();
          ParseQuery<FindYouPost> mapQuery = FindYouPost.getQuery();
          mapQuery.getInBackground(myLocation.getObjectId(), new GetCallback<FindYouPost>() {
            public void done(FindYouPost post, ParseException e) {
              if (e == null) {
                post.put("location", myPoint);
                post.saveInBackground();
              }
            }
          });
        } else { // create the location marker
          myLocation = new FindYouPost();
            myLocation.setLocation(myPoint);
            myLocation.setText(ParseUser.getCurrentUser().getUsername() + " is here.");
            myLocation.setEvent("USER");
            myLocation.setIsEvent(false);
          myLocation.setUser(ParseUser.getCurrentUser());
          ParseACL acl = new ParseACL();
          acl.setPublicReadAccess(true);
          acl.setPublicWriteAccess(true);
          myLocation.setACL(acl);
          myLocation.saveInBackground();

        }
                final FindYouPost p = myLocation;
                // Get facebook name
                GraphRequest request = GraphRequest.newMeRequest(
                        AccessToken.getCurrentAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {
                                try {
                                    p.setText(object.optString("name") + " is here.");
                                    p.saveInBackground();
                                    MarkerOptions markerOpts =
                                            new MarkerOptions().position(new LatLng(p.getLocation().getLatitude(), p
                                                    .getLocation().getLongitude()));
                                    markerOpts =
                                            markerOpts.title(p.getText()).draggable(false)
                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                                    Marker marker = mapFragment.getMap().addMarker(markerOpts);
                                    mapMarkers.put(p.getObjectId(), marker);

                                } catch (Exception e) {
                                    p.setText("No one is here.");
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "name");
                request.setParameters(parameters);
                request.executeAsync();


                toKeep.add(myLocation.getObjectId());

                cleanUpMarkers(toKeep);
            }

        });

    }

    /*
   * Displays a circle on the map representing the search radius
   */
    private void updateCircle(LatLng myLatLng) {
        /*if (mapCircle == null) {
            mapCircle =
                    mapFragment.getMap().addCircle(
                            new CircleOptions().center(myLatLng).radius(radius * METERS_PER_FEET));
            int baseColor = Color.DKGRAY;
            mapCircle.setStrokeColor(baseColor);
            mapCircle.setStrokeWidth(2);
            mapCircle.setFillColor(Color.argb(50, Color.red(baseColor), Color.green(baseColor),
                    Color.blue(baseColor)));
        }
        mapCircle.setCenter(myLatLng);
        mapCircle.setRadius(radius * METERS_PER_FEET); // Convert radius in feet to meters.*/
    }

    /*
       * Zooms the map to show the area of interest based on the search radius
       */
    private void updateZoom(LatLng myLatLng) {
        // Get the bounds to zoom to
        LatLngBounds bounds = calculateBoundsWithCenter(myLatLng);
        // Zoom to the given bounds
        mapFragment.getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 5));
    }

    /*
     * Helper method to calculate the offset for the bounds used in map zooming
     */
    private double calculateLatLngOffset(LatLng myLatLng, boolean bLatOffset) {
        // The return offset, initialized to the default difference
        double latLngOffset = OFFSET_CALCULATION_INIT_DIFF;
        // Set up the desired offset distance in meters
        float desiredOffsetInMeters = radius * METERS_PER_FEET;
        // Variables for the distance calculation
        float[] distance = new float[1];
        boolean foundMax = false;
        double foundMinDiff = 0;
        // Loop through and get the offset
        do {
            // Calculate the distance between the point of interest
            // and the current offset in the latitude or longitude direction
            if (bLatOffset) {
                Location.distanceBetween(myLatLng.latitude, myLatLng.longitude, myLatLng.latitude
                        + latLngOffset, myLatLng.longitude, distance);
            } else {
                Location.distanceBetween(myLatLng.latitude, myLatLng.longitude, myLatLng.latitude,
                        myLatLng.longitude + latLngOffset, distance);
            }
            // Compare the current difference with the desired one
            float distanceDiff = distance[0] - desiredOffsetInMeters;
            if (distanceDiff < 0) {
                // Need to catch up to the desired distance
                if (!foundMax) {
                    foundMinDiff = latLngOffset;
                    // Increase the calculated offset
                    latLngOffset *= 2;
                } else {
                    double tmp = latLngOffset;
                    // Increase the calculated offset, at a slower pace
                    latLngOffset += (latLngOffset - foundMinDiff) / 2;
                    foundMinDiff = tmp;
                }
            } else {
                // Overshot the desired distance
                // Decrease the calculated offset
                latLngOffset -= (latLngOffset - foundMinDiff) / 2;
                foundMax = true;
            }
        } while (Math.abs(distance[0] - desiredOffsetInMeters) > OFFSET_CALCULATION_ACCURACY);
        return latLngOffset;
    }

    /*
     * Helper method to calculate the bounds for map zooming
     */
    LatLngBounds calculateBoundsWithCenter(LatLng myLatLng) {
        // Create a bounds
        LatLngBounds.Builder builder = LatLngBounds.builder();

        // Calculate east/west points that should to be included
        // in the bounds
        double lngDifference = calculateLatLngOffset(myLatLng, false);
        LatLng east = new LatLng(myLatLng.latitude, myLatLng.longitude + lngDifference);
        builder.include(east);
        LatLng west = new LatLng(myLatLng.latitude, myLatLng.longitude - lngDifference);
        builder.include(west);

        // Calculate north/south points that should to be included
        // in the bounds
        double latDifference = calculateLatLngOffset(myLatLng, true);
        LatLng north = new LatLng(myLatLng.latitude + latDifference, myLatLng.longitude);
        builder.include(north);
        LatLng south = new LatLng(myLatLng.latitude - latDifference, myLatLng.longitude);
        builder.include(south);

        return builder.build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        menu.findItem(R.id.action_settings).setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            }
        });
        return true;
    }

    /*
     * Show a dialog returned by Google Play services for the connection error code
     */
    private void showErrorDialog(int errorCode) {
        // Get the error dialog from Google Play services
        Dialog errorDialog =
                GooglePlayServicesUtil.getErrorDialog(errorCode, this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {

            // Create a new DialogFragment in which to show the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();

            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);

            // Show the error dialog in the DialogFragment
            errorFragment.show(getSupportFragmentManager(), Application.APPTAG);
        }
    }

    /*
     * Define a DialogFragment to display the error dialog generated in showErrorDialog.
     */
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;

        /**
         * Default constructor. Sets the dialog field to null
         */
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        /*
         * Set the dialog to display
         *
         * @param dialog An error dialog
         */
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        /*
         * This method must return a Dialog to the DialogFragment.
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

}
