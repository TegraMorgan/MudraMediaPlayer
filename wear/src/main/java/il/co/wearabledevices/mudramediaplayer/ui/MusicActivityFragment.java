package il.co.wearabledevices.mudramediaplayer.ui;

/**
 * Created by Baselscs on 15/08/2018.
 */

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.wear.widget.WearableLinearLayoutManager;
import android.support.wear.widget.WearableRecyclerView;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import il.co.wearabledevices.mudramediaplayer.R;
import il.co.wearabledevices.mudramediaplayer.model.Album;
import il.co.wearabledevices.mudramediaplayer.model.MediaLibrary;
import il.co.wearabledevices.mudramediaplayer.model.MusicActivity;


/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link }
 * interface.
 */
public class MusicActivityFragment extends Fragment {

    private static final String TAG = SongsFragment.class.getSimpleName();
    private static ArrayMap<String, MusicActivity> mMusicAtivities;
    private WearableRecyclerView mRecyclerView;
    // TODO: Customize parameters
    private OnMusicActivityFragmentInteractionListener mListener;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MusicActivityFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static MusicActivityFragment newInstance(int columnCount, Album album) {
        MusicActivityFragment fragment = new MusicActivityFragment();
//        Bundle args = new Bundle();
//        args.putSerializable(SERIALIZE_MUSIC_ACTIVITY, mMusicAtivities);
//        args.putInt(ARG_COLUMN_COUNT, columnCount);
//        fragment.setArguments(args);
        mMusicAtivities = MediaLibrary.getMusicActivities();
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMusicAtivities = MediaLibrary.getMusicActivities();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album_list, container, false);

        // Set the adapter
        if (view instanceof WearableRecyclerView) {
            Context context = view.getContext();
            WearableRecyclerView recyclerView = (WearableRecyclerView) view;
            recyclerView.setEdgeItemsCenteringEnabled(true);
            recyclerView.setCircularScrollingGestureEnabled(false);
            recyclerView.setBezelFraction(0.3f);
            recyclerView.setScrollDegreesPerScreen(230);


            /**using custom scrolling for selection*/
            CustomScrollingLayoutCallback3 customScrollingLayoutCallback =
                    new CustomScrollingLayoutCallback3();
            recyclerView.setLayoutManager(
                    new WearableLinearLayoutManager(context, customScrollingLayoutCallback));
            recyclerView.setAdapter(new MusicActivityAdapter(mMusicAtivities, mListener));
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
        if (context instanceof OnMusicActivityFragmentInteractionListener) {
            mListener = (OnMusicActivityFragmentInteractionListener) context;
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

    public interface OnMusicActivityFragmentInteractionListener {
        // TODO: Update argument type and name
        void onMusicActivityFragmentInteraction(MusicActivityAdapter.ViewHolder item, int position);
    }

    public class CustomScrollingLayoutCallback3 extends WearableLinearLayoutManager.LayoutCallback {
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
            //child.setAlpha(0.5f);

            /**Item highlighting upon focus*/
            child.setBackgroundColor(R.color.black * (int) (1 - mProgressToCenter + 0.2));
        }
    }

}
