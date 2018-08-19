package il.co.wearabledevices.mudramediaplayer.ui;


/**
 * Created by Baselscs on 15/08/2018.
 */

import android.support.v7.widget.RecyclerView;
import android.support.wear.widget.WearableRecyclerView;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import il.co.wearabledevices.mudramediaplayer.R;
import il.co.wearabledevices.mudramediaplayer.model.MusicActivity;
import il.co.wearabledevices.mudramediaplayer.ui.dummy.AlbumsDummyContent;

/**
 * {@link RecyclerView.Adapter} that can display a {@link AlbumsDummyContent.AlbumsDummyItem} and makes a call to the
 * specified {@link AlbumsFragment.OnAlbumsListFragmentInteractionListener}.
 */
public class MusicActivityAdapter extends WearableRecyclerView.Adapter<MusicActivityAdapter.ViewHolder> {

    private final ArrayMap<String, MusicActivity> mValues;
    private final MusicActivityFragment.OnMusicActivityFragmentInteractionListener mListener;


    public MusicActivityAdapter(ArrayMap<String, MusicActivity> items,
                                MusicActivityFragment.OnMusicActivityFragmentInteractionListener listener) {
        //this.setEdgeItemsCenteringEnabled(true);

        mValues = items;
        mListener = listener;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.valueAt(position);
        String aTitle = mValues.valueAt(position).getActivityDisplayName();
        holder.mIdView.setText(aTitle);
        //holder.mContentView.setText(mValues.valueAt(position).getActivityDisplayName());
        holder.mIcon.setImageBitmap(mValues.valueAt(position).decodeActivityIconFromResource(holder.mView.getContext()));
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onMusicActivityFragmentInteraction(holder, position);
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
        public MusicActivity mItem;

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
