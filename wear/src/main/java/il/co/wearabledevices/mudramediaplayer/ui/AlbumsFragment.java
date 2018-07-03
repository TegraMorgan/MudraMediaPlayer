package il.co.wearabledevices.mudramediaplayer.ui;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.wear.widget.WearableLinearLayoutManager;
import android.support.wear.widget.WearableRecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Comparator;

import il.co.wearabledevices.mudramediaplayer.R;
import il.co.wearabledevices.mudramediaplayer.model.Album;
import il.co.wearabledevices.mudramediaplayer.model.MediaLibrary;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnAlbumsListFragmentInteractionListener}
 * interface.
 */
public class AlbumsFragment extends Fragment {
    private static final String TAG = AlbumsFragment.class.getSimpleName();
    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String LIST_TYPE = "albums";
    ArrayList<Album> mAlbums;
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnAlbumsListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AlbumsFragment() {
        // request all albums
        mAlbums = (ArrayList<Album>) MediaLibrary.getAlbums();
        Log.v(TAG, "Got " + mAlbums.size() + " albums from media library");
        // sort by album name
        mAlbums.sort(Comparator.comparing(Album::getaName));

    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static AlbumsFragment newInstance(int columnCount) {
        AlbumsFragment fragment = new AlbumsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
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
            recyclerView.setBezelFraction(0.5f);
            //recyclerView.setScrollDegreesPerScreen(30);

            if (mColumnCount <= 1) {
//                CustomScrollingLayoutCallback customScrollingLayoutCallback =
//                        new CustomScrollingLayoutCallback();
                recyclerView.setLayoutManager(
                        new WearableLinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new WearableLinearLayoutManager(context));
            }
            recyclerView.setAdapter(new AlbumsAdapter(mAlbums, mListener));
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAlbumsListFragmentInteractionListener) {
            mListener = (OnAlbumsListFragmentInteractionListener) context;
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
    public interface OnAlbumsListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onAlbumsListFragmentInteraction(Album item);
    }
//    public interface OnSongsListFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onListFragmentInteraction(SongsDummyItem item);
//    }


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
