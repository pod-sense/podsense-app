package de.danoeh.antennapod.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.danoeh.antennapod.R;
import de.danoeh.antennapod.model.feed.FeedItem;
import de.danoeh.antennapod.playback.service.PlaybackServiceStarter;
import de.danoeh.antennapod.ui.episodeslist.EpisodeItemViewHolder;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class AllEpisodesRecyclerAdapter extends RecyclerView.Adapter<EpisodeItemViewHolder> {
    private final WeakReference<Context> contextRef;
    private List<FeedItem> episodes = new ArrayList<>();

    public AllEpisodesRecyclerAdapter(Context context) {
        this.contextRef = new WeakReference<>(context);
    }

    @NonNull
    @Override
    public EpisodeItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new EpisodeItemViewHolder((android.app.Activity) contextRef.get(), parent);
    }

    @Override
    public void onBindViewHolder(@NonNull EpisodeItemViewHolder holder, int position) {
        FeedItem item = episodes.get(position);
        holder.bind(item);
        holder.itemView.findViewById(R.id.butPlay).setOnClickListener(v -> new PlaybackServiceStarter(contextRef.get(), item.getMedia())
                .callEvenIfRunning(true)
                .start());
    }

    @Override
    public int getItemCount() {
        return episodes.size();
    }

    public void updateItems(List<FeedItem> newEpisodes) {
        this.episodes = newEpisodes;
        notifyDataSetChanged();
    }
}
