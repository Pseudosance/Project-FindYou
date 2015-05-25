package group.csm117.findyou;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.parse.ParseUser;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class DispatchActivity extends Activity {

   // Boolean test = true;

    public DispatchActivity(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try{
            PackageInfo info = getPackageManager().getPackageInfo(
                    "group.csm117.findyou", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));

            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

        //Testing purposes
        //Todo: delete
      //  if(test) {
   //         test = false;
    //        startActivity(new Intent(this, SettingsActivity.class));
  //      }else
        if
        // Check if there is current user info
        (ParseUser.getCurrentUser()!= null) {
            // Start an intent for the logged in activity
            startActivity(new Intent(this, EventListActivity.class));
        } else {
            // Sart and intent for the logged out activity
            startActivity(new Intent(this, WelcomeActivity.class));
        }
    }

}