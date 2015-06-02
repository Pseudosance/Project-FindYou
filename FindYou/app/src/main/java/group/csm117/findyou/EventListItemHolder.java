package group.csm117.findyou;

import android.view.View;
import android.widget.TextView;

public class EventListItemHolder {

    private View mView;
    private TextView mTitleTextView;
    private TextView mDesciptionTextView;
    private TextView mFriendsTextView;

    public EventListItemHolder(View view) {
        mView = view;
    }

    public View getView() {
        return mView;
    }

    public TextView getTitleTextView () {
        if (mTitleTextView == null) {
            mTitleTextView = (TextView) mView.findViewById(R.id.title_text_view);
        }
        return mTitleTextView;
    }

    public TextView getDescriptionTextView () {
        if (mDesciptionTextView == null) {
            mDesciptionTextView = (TextView) mView.findViewById(R.id.description_text_view);
        }
        return mDesciptionTextView;
    }

    public TextView getFriendsTextView () {
        if (mFriendsTextView == null) {
            mFriendsTextView = (TextView) mView.findViewById(R.id.friends_text_view);
        }
        return mFriendsTextView;
    }

}
