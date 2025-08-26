package de.danoeh.antennapod.ui.screen.home;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import de.danoeh.antennapod.R;
import de.danoeh.antennapod.net.discovery.PodcastSearchResult;
import de.danoeh.antennapod.ui.appstartintent.OnlineFeedviewActivityStarter;
import java.util.List;

public class PopularPodcastAdapter extends RecyclerView.Adapter<PopularPodcastAdapter.ViewHolder> {

    private final Context context;
    private final List<PodcastSearchResult> podcasts;

    public PopularPodcastAdapter(Context context, List<PodcastSearchResult> podcasts) {
        this.context = context;
        this.podcasts = podcasts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.popular_podcast_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PodcastSearchResult podcast = podcasts.get(position);
        Glide.with(context)
                .load(podcast.imageUrl)
                .into(holder.podcastCover);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new OnlineFeedviewActivityStarter(context, podcast.feedUrl).getIntent();
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return podcasts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView podcastCover;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            podcastCover = itemView.findViewById(R.id.podcast_cover);
        }
    }
}
