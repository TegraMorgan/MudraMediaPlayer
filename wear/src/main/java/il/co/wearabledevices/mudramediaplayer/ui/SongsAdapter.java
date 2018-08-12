package il.co.wearabledevices.mudramediaplayer.ui;

import android.support.v7.widget.RecyclerView;
import android.support.wear.widget.WearableRecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import il.co.wearabledevices.mudramediaplayer.R;
import il.co.wearabledevices.mudramediaplayer.model.Song;

public class SongsAdapter extends WearableRecyclerView.Adapter<SongsAdapter.SongsViewHolder> {

    private final List<Song> mValues;
    private final SongsFragment.OnSongsListFragmentInteractionListener mListener;


    public SongsAdapter(List<Song> items,
                        SongsFragment.OnSongsListFragmentInteractionListener listener) {
        //this.setEdgeItemsCenteringEnabled(true);

        mValues = items;
        mListener = listener;

    }

    @Override
    public SongsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item_album, parent, false);
        return new SongsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SongsViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).getDisplayTitle());
        holder.mContentView.setText(mValues.get(position).getDisplayArtist());
        holder.getAdapterPosition();
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onSongsListFragmentInteraction(holder,position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class SongsViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public Song mItem;

        public SongsViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.album_id);
            mContentView = (TextView) view.findViewById(R.id.album_content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
