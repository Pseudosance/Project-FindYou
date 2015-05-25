package group.csm117.findyou;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;

import com.facebook.share.model.AppInviteContent;
import com.facebook.share.widget.AppInviteDialog;


public class InviteContactsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_contacts);

        String appLinkUrl, previewImageUrl;

        appLinkUrl = "https://fb.me/517655461722207";
        previewImageUrl = "http://i378.photobucket.com/albums/oo224/BaconLord/FindYou_Icon.png?t=1432441586";

        if (AppInviteDialog.canShow()) {
            AppInviteContent content = new AppInviteContent.Builder()
                    .setApplinkUrl(appLinkUrl)
                    .setPreviewImageUrl(previewImageUrl)
                    .build();
            AppInviteDialog.show(this, content);
        }

        // Set up the submit button click handler
        Button mActionButton = (Button) findViewById(R.id.continue_button);
        mActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                Intent intent = new Intent(InviteContactsActivity.this, DispatchActivity.class);
                startActivity(intent);
            }
        });


    }


}
