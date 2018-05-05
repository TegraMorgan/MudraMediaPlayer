package il.co.wearabledevices.mudramediaplayer.ui;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.wear.widget.WearableLinearLayoutManager;
import android.support.wear.widget.WearableRecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import il.co.wearabledevices.mudramediaplayer.R;
import il.co.wearabledevices.mudramediaplayer.model.Album;
import il.co.wearabledevices.mudramediaplayer.model.Song;
import il.co.wearabledevices.mudramediaplayer.ui.dummy.SongsDummyContent;

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
    private Album album;
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnSongsListFragmentInteractionListener mListener;
    ArrayList<Song> mSongs;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SongsFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static SongsFragment newInstance(int columnCount) {
        SongsFragment fragment = new SongsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bdl = getArguments();
        if (getArguments() != null) {
            mColumnCount = bdl.getInt(ARG_COLUMN_COUNT);
            album = (Album) bdl.getSerializable(SERIALIZE_ALBUM);
            mSongs = album.getaSongs();
        }

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
            recyclerView.setCircularScrollingGestureEnabled(true);
            //ecyclerView.setBezelFraction(0.1f);
            //recyclerView.setScrollDegreesPerScreen(30);

            if (mColumnCount <= 1) {
                CustomScrollingLayoutCallback customScrollingLayoutCallback =
                        new CustomScrollingLayoutCallback();
                recyclerView.setLayoutManager(
                        new WearableLinearLayoutManager(context, customScrollingLayoutCallback));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new SongsAdapter(mSongs, mListener));
        }
        return view;
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
        void onSongsListFragmentInteraction(Song item);
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
