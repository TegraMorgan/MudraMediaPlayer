package il.co.wearabledevices.mudramediaplayer.ui;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.wear.widget.WearableLinearLayoutManager;
import android.support.wear.widget.WearableRecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

import il.co.wearabledevices.mudramediaplayer.MainActivity;
import il.co.wearabledevices.mudramediaplayer.R;
import il.co.wearabledevices.mudramediaplayer.model.Album;
import il.co.wearabledevices.mudramediaplayer.model.Song;

import static il.co.wearabledevices.mudramediaplayer.constants.SERIALIZE_ALBUM;


/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnSongsListFragmentInteractionListener}
 * interface.
 */
public class SongsFragment extends Fragment {

    private static final String TAG = SongsFragment.class.getSimpleName();
    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String LIST_TYPE = "songs";
    ArrayList<Song> mSongs;
    private WearableRecyclerView mRecyclerView;
    private Album album;
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnSongsListFragmentInteractionListener mListener;
    private int prevCenterPos; // Keep track the previous pos to dehighlight


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SongsFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static SongsFragment newInstance(int columnCount, Album album) {
        SongsFragment fragment = new SongsFragment();
        Bundle args = new Bundle();
        args.putSerializable(SERIALIZE_ALBUM, album);
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    public Album getAlbum() {
        return album;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bdl = getArguments();
        if (getArguments() != null) {
            mColumnCount = bdl.getInt(ARG_COLUMN_COUNT);
            album = (Album) bdl.getSerializable(SERIALIZE_ALBUM);
            mSongs = album.getaSongs();
            Log.d("Is there any songs", (mSongs.isEmpty() ? "Yes" : "No"));

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album_list, container, false);
        Bundle bdl = getArguments();
        if (getArguments() != null) {
            mColumnCount = bdl.getInt(ARG_COLUMN_COUNT);
            album = (Album) bdl.getSerializable(SERIALIZE_ALBUM);
            if (mSongs.isEmpty()) {
                mSongs = album.getaSongs();
                Log.d("Is there any songs2", (mSongs.isEmpty() ? "Yes" : "No"));
            }

        }
        // Set the adapter
        if (view instanceof WearableRecyclerView) {
            Context context = view.getContext();
            WearableRecyclerView recyclerView = (WearableRecyclerView) view;
            recyclerView.setEdgeItemsCenteringEnabled(true);
            recyclerView.setCircularScrollingGestureEnabled(false);
            recyclerView.setBezelFraction(0.3f);
            recyclerView.setScrollDegreesPerScreen(230);

            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(
                        new WearableLinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new SongsAdapter(mSongs, mListener));

            /*recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        int position = getCurrentItem();
                        onSelectedSongChanged(position);
                    }
                }
            });*/

            /*recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {


                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

//                    int firstVisible = mRecyclerView.get);
//                    int lastVisible = layoutManager.findLastVisibleItemPosition();
//                    int itemsCount = lastVisible - firstVisible + 1;

                    int center = (mRecyclerView.getTop() + mRecyclerView.getBottom()) / 2;
                    View centerView = mRecyclerView.findChildViewUnder(center, mRecyclerView.getTop());
                    int centerPos = mRecyclerView.getChildAdapterPosition(centerView);

                    if (prevCenterPos != centerPos) {
                        // dehighlight the previously highlighted view
                        View prevView = mRecyclerView.getLayoutManager().findViewByPosition(prevCenterPos);
                        if (prevView != null) {
                            View layout = prevView.findViewById(R.id.album_item);
                            //int white = ContextCompat.getColor(context, R.color.white);
                            layout.setBackgroundColor(Color.CYAN);;
                        }

                        // highlight view in the middle
                        if (centerView != null) {
                            View layout = centerView.findViewById(R.id.album_item);
                            //int highlightColor = ContextCompat.getColor(context, R.color.colorAccent);
                            layout.setBackgroundColor(Color.RED);
                        }

                        prevCenterPos = centerPos;
                    }
                }

            });*/

            PagerSnapHelper snapHelper = new PagerSnapHelper();
            snapHelper.attachToRecyclerView(recyclerView);
            recyclerView.setOnFlingListener(snapHelper);
            mRecyclerView = recyclerView;
            mRecyclerView.invalidate();
        }
        return view;
    }

    private void onSelectedSongChanged(int position) {
        //TODO change song playing
        Toast.makeText(this.getContext(), "Song # " + String.valueOf(position), Toast.LENGTH_SHORT);
        /*
        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(MainActivity.this);
            mediaController.getTransportControls().skipToQueueItem();
         */
    }

    public int getCurrentItem() {
        int pos = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                .findFirstVisibleItemPosition();
        return pos;
    }

    public boolean hasPreview() {
        return getCurrentItem() > 0;
    }

    public boolean hasNext() {
        return mRecyclerView.getAdapter() != null &&
                getCurrentItem() < (mRecyclerView.getAdapter().getItemCount()- 1);
    }

    public void preview() {
        int position = getCurrentItem();
        if (position > 0)
            setCurrentItem(position -1, true);
    }

    public boolean next() {
        RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        if (adapter == null){
            return false;
        }


        int position = getCurrentItem();
        int count = adapter.getItemCount();
        if (position < (count -1))
            setCurrentItem(position + 1, true);
        return true;
    }


    private void setCurrentItem(int position, boolean smooth){
        if (smooth) {
            mRecyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mRecyclerView.scrollToPosition(position);
                }
            },100);
        }
        else {
            mRecyclerView.scrollToPosition(position);
        }
    }


    public void scrollToPos(int position, boolean smooth){
        RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        if(position <= adapter.getItemCount() - 1) {
            setCurrentItem(position,smooth);
            //adapter.notifyDataSetChanged();
        }
        return;


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSongsListFragmentInteractionListener) {
            mListener = (OnSongsListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public WearableRecyclerView getRecycler() {
        return this.mRecyclerView;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */

    public interface OnSongsListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onSongsListFragmentInteraction(SongsAdapter.SongsViewHolder item, int position);
    }

    private class CustomScrollingLayoutCallback extends WearableLinearLayoutManager.LayoutCallback {

        private static final float MAX_ICON_PROGRESS = 2F;

        @Override
        public void onLayoutFinished(View child, RecyclerView parent) {

            float centerOffset = ((float) child.getHeight() / 2.0f) / (float) parent.getHeight();
            float yRelativeToCenterOffset = (child.getY() / parent.getHeight()) + centerOffset;

            float progresstoCenter = (float) Math.sin(yRelativeToCenterOffset * Math.PI);

            float mProgressToCenter = Math.abs(0.5f - yRelativeToCenterOffset);

            mProgressToCenter = Math.min(mProgressToCenter, MAX_ICON_PROGRESS);

            child.setScaleX(1 - mProgressToCenter);
            child.setScaleY(1 - mProgressToCenter);
            child.setX(+(1 - progresstoCenter) * 80);
        }

    }

}
