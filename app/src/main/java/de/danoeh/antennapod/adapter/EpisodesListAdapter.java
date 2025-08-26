package de.danoeh.antennapod.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.danoeh.antennapod.R;
import de.danoeh.antennapod.actionbutton.ItemActionButton;
import de.danoeh.antennapod.model.feed.FeedItem;
import de.danoeh.antennapod.playback.service.PlaybackServiceStarter;
import de.danoeh.antennapod.ui.episodeslist.EpisodeItemViewHolder;
import java.util.List;

public class EpisodesListAdapter extends RecyclerView.Adapter<EpisodeItemViewHolder> {

    private final Context context;
    private final List<FeedItem> items;

    public EpisodesListAdapter(Context context, List<FeedItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public EpisodeItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new EpisodeItemViewHolder((android.app.Activity) context, parent);
    }

    @Override
    public void onBindViewHolder(@NonNull EpisodeItemViewHolder holder, int position) {
        FeedItem item = items.get(position);
        holder.bind(item);
        holder.itemView.findViewById(R.id.butPlay).setOnClickListener(v -> new PlaybackServiceStarter(context, item.getMedia())
                .callEvenIfRunning(true)
                .start());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
