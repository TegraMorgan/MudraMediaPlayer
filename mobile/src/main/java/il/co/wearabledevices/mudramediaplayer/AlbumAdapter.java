package il.co.wearabledevices.mudramediaplayer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
    private int mAlbumsCount;
    private ArrayList<Album> albums;


    public AlbumAdapter(Context context, ArrayList<Album> theAlbums) {
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
        Log.d(TAG, "Binding " + position);
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mAlbumsCount;
    }

    class GenresViewHolder extends RecyclerView.ViewHolder {
        TextView listItemAlbum;

        private GenresViewHolder(View view) {
            super(view);

            listItemAlbum = view.findViewById(R.id.tv_song_album);
        }

        void bind(int listIndex) {
            listItemAlbum.setText(String.valueOf(listIndex));
        }
    }
}
