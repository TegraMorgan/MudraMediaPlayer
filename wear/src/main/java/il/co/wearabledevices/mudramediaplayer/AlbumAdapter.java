package il.co.wearabledevices.mudramediaplayer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import il.co.wearabledevices.mudramediaplayer.model.Album;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.GenresViewHolder> {
    private static final String TAG = AlbumAdapter.class.getSimpleName();
    private static final int ACCEPTABLE_LENGTH = 25;
    final private ListItemClickListener mOnClickListener;
    private int mAlbumsCount;
    private ArrayList<Album> albums;

    public AlbumAdapter(ArrayList<Album> theAlbums, ListItemClickListener listener) {
        mAlbumsCount = theAlbums.size();
        albums = theAlbums;
        mOnClickListener = listener;

    }

    @Override
    public GenresViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.album_list_item;
        LayoutInflater inf = LayoutInflater.from(context);

        View view = inf.inflate(layoutIdForListItem, parent, false);
        return new GenresViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GenresViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mAlbumsCount;
    }

    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }

    class GenresViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView listItemAlbum;
        TextView listItemArtist;

        private GenresViewHolder(View view) {
            super(view);

            listItemAlbum = view.findViewById(R.id.tv_song_album);
            listItemArtist = view.findViewById(R.id.tv_song_artist);
            itemView.setOnClickListener(this);
        }

        void bind(int listIndex) {
            Album curr = albums.get(listIndex);
            String name = curr.getaName();
            String art = curr.getaArtist();
            listItemAlbum.setText(name + " - ");
            listItemArtist.setText(art);
        }

        @Override
        public void onClick(View view) {
            int cliPos = getAdapterPosition();
            mOnClickListener.onListItemClick(cliPos);
        }
    }
}
