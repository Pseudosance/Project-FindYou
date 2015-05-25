package group.csm117.findyou;


import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.List;

/**
 * ListView wrapped in a SwipeRefreshLayout
 */
public class RefreshableListViewWrapper extends SwipeRefreshLayout implements AbsListView.OnScrollListener {

    private ListView mListView;
    public ListView getListView() {
        return mListView;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Init

    public RefreshableListViewWrapper(Context context) {
        super(context);
        init(null);
    }

    public RefreshableListViewWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        mListView = new ListView(getContext());
        addView(mListView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mListView.setOnScrollListener(this);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // OnScrollListener

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // Only enable swipe to refresh when already at top
        if (scrollState == SCROLL_STATE_IDLE) {
            setEnabled(mListView.getFirstVisiblePosition() <= 0
                    && mListView.getChildAt(0).getTop() == 0);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // nothing
    }

}
