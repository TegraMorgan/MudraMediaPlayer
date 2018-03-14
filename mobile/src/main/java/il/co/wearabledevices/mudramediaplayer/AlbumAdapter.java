package il.co.wearabledevices.mudramediaplayer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import il.co.wearabledevices.mudramediaplayer.model.Album;

/**
 * Created by Tegra on 11/03/2018.
 * Work in progress
 */

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.GenresViewHolder> {
    private static final String TAG = AlbumAdapter.class.getSimpleName();
    private static final int ACCEPTABLE_LENGTH = 25;
    private int mAlbumsCount;
    private ArrayList<Album> albums;


    public AlbumAdapter(ArrayList<Album> theAlbums) {
        mAlbumsCount = theAlbums.size();
        albums = theAlbums;

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

    class GenresViewHolder extends RecyclerView.ViewHolder {
        TextView listItemAlbum;
        TextView listItemArtist;

        private GenresViewHolder(View view) {
            super(view);

            listItemAlbum = view.findViewById(R.id.tv_song_album);
            listItemArtist = view.findViewById(R.id.tv_song_artist);
        }

        void bind(int listIndex) {
            Album curr = albums.get(listIndex);
            String name = curr.getaName().trim();
            String art = curr.getaArtist().trim();
            if (name.length() > ACCEPTABLE_LENGTH) name = name.substring(0, ACCEPTABLE_LENGTH - 1);
            if (art.length() > ACCEPTABLE_LENGTH) art = art.substring(0, ACCEPTABLE_LENGTH - 1);
            listItemAlbum.setText(name + " - ");
            listItemArtist.setText(art);
        }
    }
}
