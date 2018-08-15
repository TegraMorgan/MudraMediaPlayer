package il.co.wearabledevices.mudramediaplayer.ui;

/**
 * Created by Baselscs on 15/08/2018.
 */

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.ArrayMap;
import android.util.Log;
import android.view.ViewGroup;

import il.co.wearabledevices.mudramediaplayer.model.MusicActivity;

import android.support.wear.widget.WearableRecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

import il.co.wearabledevices.mudramediaplayer.R;
import il.co.wearabledevices.mudramediaplayer.model.Album;
import il.co.wearabledevices.mudramediaplayer.model.Playlist;
import il.co.wearabledevices.mudramediaplayer.ui.dummy.AlbumsDummyContent;

/**
 * {@link RecyclerView.Adapter} that can display a {@link AlbumsDummyContent.AlbumsDummyItem} and makes a call to the
 * specified {@link AlbumsFragment.OnAlbumsListFragmentInteractionListener}.
 */
public class PlayListAdapter extends WearableRecyclerView.Adapter<PlayListAdapter.ViewHolder> {

    private final ArrayList<Playlist> mValues;
    private final PlayListFragment.OnPlayListFragmentInteractionListener mListener;


    public PlayListAdapter(ArrayList<Playlist> items,
                           PlayListFragment.OnPlayListFragmentInteractionListener listener) {
        //this.setEdgeItemsCenteringEnabled(true);

        mValues = items;
        mListener = listener;
        Log.d("Basel", "PlayListAdapter: " + items.size());
        for(Playlist p : mValues){
            if(p == null){
                Log.d("Basel-Loop", "Found null in PlayList");
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        String aTitle = mValues.get(position).getPlaylistDisplayName() == null? "null":mValues.get(position).getPlaylistDisplayName();
        holder.mIdView.setText(aTitle);
        //holder.mContentView.setText(mValues.valueAt(position).getActivityDisplayName());
        holder.mIcon.setImageBitmap(mValues.get(position).getAlbumArt());
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onPlayListFragmentInteraction(holder,position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends WearableRecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public final ImageView mIcon;
        public Playlist mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = view.findViewById(R.id.song_id);
            mContentView = view.findViewById(R.id.song_content);
            mIcon = view.findViewById(R.id.song_icon);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }



}
