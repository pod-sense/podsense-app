package de.danoeh.antennapod.ui.screen.home;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import de.danoeh.antennapod.ui.common.ColorUtils;
import de.danoeh.antennapod.ui.glide.PaletteTransformation;
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
        
        // Set initial fallback color immediately
        int initialColor = ColorUtils.extractBackgroundColor(null, podcast.feedUrl);
        holder.colorBackground.setBackgroundColor(initialColor);
        
        // Load image with color extraction
        Glide.with(context)
                .load(podcast.imageUrl)
                .transform(new PaletteTransformation(palette -> {
                    // Extract background color and apply it
                    int backgroundColor = ColorUtils.extractBackgroundColor(palette, podcast.feedUrl);
                    holder.colorBackground.setBackgroundColor(backgroundColor);
                }))
                .into(holder.podcastCover);

        // Set up play button to start streaming latest episode
        holder.playButton.setOnClickListener(v -> {
            // For discovery podcasts, open the feed view to allow subscription first
            Intent intent = new OnlineFeedviewActivityStarter(context, podcast.feedUrl).getIntent();
            context.startActivity(intent);
        });

        // Set up options button
        holder.optionsButton.setOnClickListener(v -> showOptionsDialog(podcast));

        // Keep the original click behavior for the card
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new OnlineFeedviewActivityStarter(context, podcast.feedUrl).getIntent();
            context.startActivity(intent);
        });
    }

    private void showOptionsDialog(PodcastSearchResult podcast) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.podcast_options_dialog, null);
        
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setView(dialogView);
        
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        
        // Set up option click listeners
        dialogView.findViewById(R.id.option_subscribe).setOnClickListener(v -> {
            Intent intent = new OnlineFeedviewActivityStarter(context, podcast.feedUrl).getIntent();
            context.startActivity(intent);
            dialog.dismiss();
        });
        
        dialogView.findViewById(R.id.option_play_latest).setOnClickListener(v -> {
            Intent intent = new OnlineFeedviewActivityStarter(context, podcast.feedUrl).getIntent();
            context.startActivity(intent);
            dialog.dismiss();
        });
        
        dialogView.findViewById(R.id.option_view_episodes).setOnClickListener(v -> {
            Intent intent = new OnlineFeedviewActivityStarter(context, podcast.feedUrl).getIntent();
            context.startActivity(intent);
            dialog.dismiss();
        });
        
        dialogView.findViewById(R.id.option_share).setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, podcast.feedUrl);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, podcast.title);
            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_label)));
            dialog.dismiss();
        });
        
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return podcasts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView podcastCover;
        View colorBackground;
        ImageButton playButton;
        ImageButton optionsButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            podcastCover = itemView.findViewById(R.id.podcast_cover);
            colorBackground = itemView.findViewById(R.id.color_background);
            playButton = itemView.findViewById(R.id.play_button);
            optionsButton = itemView.findViewById(R.id.options_button);
        }
    }
}
