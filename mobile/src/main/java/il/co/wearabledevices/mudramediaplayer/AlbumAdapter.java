package il.co.wearabledevices.mudramediaplayer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Tegra on 11/03/2018.
 * Work in progress
 */

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.GenresViewHolder> {
    private static final String TAG = AlbumAdapter.class.getSimpleName();
    private int mGenreItems;

    public AlbumAdapter(int nGenres) {
        mGenreItems = nGenres;
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
        return mGenreItems;
    }

    class GenresViewHolder extends RecyclerView.ViewHolder {
        TextView listItemAlbum;

        private GenresViewHolder(View view) {
            super(view);

            listItemAlbum = view.findViewById(R.id.tv_album_name);
        }

        void bind(int listIndex) {
            listItemAlbum.setText(String.valueOf(listIndex));
        }
    }
}
