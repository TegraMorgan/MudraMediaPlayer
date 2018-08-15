package il.co.wearabledevices.mudramediaplayer.ui;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.wear.widget.WearableLinearLayoutManager;
import android.support.wear.widget.WearableRecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import il.co.wearabledevices.mudramediaplayer.R;
import il.co.wearabledevices.mudramediaplayer.constants;
import il.co.wearabledevices.mudramediaplayer.model.Playlist;


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
    private static Playlist mPlayList;
    private WearableRecyclerView mRecyclerView;

    private OnSongsListFragmentInteractionListener mListener;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SongsFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static SongsFragment newInstance(int columnCount, Playlist playList) {
        SongsFragment fragment = new SongsFragment();
        Bundle args = new Bundle();
        args.putSerializable(constants.PLAY_LIST, playList);
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        mPlayList = playList;
        return fragment;
    }

//    public Album getAlbum() {
//        return album;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bdl = getArguments();
        if (getArguments() != null) {
            //mColumnCount = bdl.getInt(ARG_COLUMN_COUNT);
            mPlayList = (Playlist) bdl.getSerializable(constants.PLAY_LIST);
            //mSongs = album.getSongs();
            //Log.d("Is there any songs", (mSongs.isEmpty() ? "Yes" : "No"));

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album_list, container, false);
        Bundle bdl = getArguments();
        if (getArguments() != null) {
            //mColumnCount = bdl.getInt(ARG_COLUMN_COUNT);
            //album = (Album) bdl.getSerializable(SERIALIZE_ALBUM);
//            if (mSongs.isEmpty()) {
//                mSongs = album.getSongs();
//                Log.d("Is there any songs2", (mSongs.isEmpty() ? "Yes" : "No"));
//            }
            mPlayList = (Playlist) getArguments().getSerializable(constants.PLAY_LIST);

        }
        // Set the adapter
        if (view instanceof WearableRecyclerView) {
            Context context = view.getContext();
            WearableRecyclerView recyclerView = (WearableRecyclerView) view;
            recyclerView.setEdgeItemsCenteringEnabled(true);
            recyclerView.setCircularScrollingGestureEnabled(false);
            recyclerView.setBezelFraction(0.3f);
            recyclerView.setScrollDegreesPerScreen(230);


            /**using custom scrolling for selection*/
            CustomScrollingLayoutCallback2 customScrollingLayoutCallback =
                    new CustomScrollingLayoutCallback2();
            recyclerView.setLayoutManager(
                    new WearableLinearLayoutManager(context, customScrollingLayoutCallback));
            recyclerView.setAdapter(new SongsAdapter(mPlayList.getSongs(), mListener));
            /**using snap helper for better scrolling experience*/
            PagerSnapHelper snapHelper = new PagerSnapHelper();
            snapHelper.attachToRecyclerView(recyclerView);
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
                getCurrentItem() < (mRecyclerView.getAdapter().getItemCount() - 1);
    }

    public void preview() {
        int position = getCurrentItem();
        if (position > 0)
            setCurrentItem(position - 1, true);
    }

    public boolean next() {
        RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        if (adapter == null) {
            return false;
        }


        int position = getCurrentItem();
        int count = adapter.getItemCount();
        if (position < (count - 1))
            setCurrentItem(position + 1, true);
        return true;
    }


    private void setCurrentItem(int position, boolean smooth) {
        if (smooth) {
            mRecyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mRecyclerView.scrollToPosition(position);
                }
            }, 100);
        } else {
            mRecyclerView.scrollToPosition(position);
        }
    }


    public void scrollToPos(int position, boolean smooth) {
        RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        if (position <= adapter.getItemCount() - 1) {
            setCurrentItem(position, smooth);
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

    public class CustomScrollingLayoutCallback2 extends WearableLinearLayoutManager.LayoutCallback {
        /**
         * How much should we scale the icon at most.
         */
        private static final float MAX_ICON_PROGRESS = 0.65f;

        private float mProgressToCenter;

        @Override
        public void onLayoutFinished(View child, RecyclerView parent) {

            // Figure out % progress from top to bottom
            float centerOffset = ((float) child.getHeight() / 2.0f) / (float) parent.getHeight();
            float yRelativeToCenterOffset = (child.getY() / parent.getHeight()) + centerOffset;

            // Normalize for center
            mProgressToCenter = Math.abs(0.5f - yRelativeToCenterOffset);
            // Adjust to the maximum scale
            mProgressToCenter = Math.min(mProgressToCenter, MAX_ICON_PROGRESS);

            child.setScaleX(1 - mProgressToCenter);
            child.setScaleY(1 - mProgressToCenter);
//            child.setAlpha(0.5f);

            /**Item highlighting upon focus*/
            if ((int) 1 - mProgressToCenter == 1)
                child.setBackgroundColor((int) R.color.black);
            else
                child.setBackgroundColor(0);
        }
    }

}
