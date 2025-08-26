package de.danoeh.antennapod.ui.screen.subscriptions;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.cardview.widget.CardView;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import de.danoeh.antennapod.ui.common.ColorUtils;
import de.danoeh.antennapod.ui.glide.PaletteTransformation;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.model.feed.Feed;
import de.danoeh.antennapod.model.feed.FeedItem;
import de.danoeh.antennapod.playback.service.PlaybackServiceStarter;
import de.danoeh.antennapod.storage.database.DBReader;
import de.danoeh.antennapod.ui.screen.feed.FeedItemlistFragment;
import de.danoeh.antennapod.ui.common.SquareImageView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.view.ContextMenu;
import android.view.MenuInflater;
import androidx.annotation.Nullable;

public class HorizontalFeedListAdapter extends RecyclerView.Adapter<HorizontalFeedListAdapter.Holder>
        implements View.OnCreateContextMenuListener  {
    private final WeakReference<MainActivity> mainActivityRef;
    private final List<Feed> data = new ArrayList<>();
    private int dummyViews = 0;
    private Feed longPressedItem;
    private @StringRes int endButtonText = 0;
    private Runnable endButtonAction = null;

    public HorizontalFeedListAdapter(MainActivity mainActivity) {
        this.mainActivityRef = new WeakReference<>(mainActivity);
    }

    public void setDummyViews(int dummyViews) {
        this.dummyViews = dummyViews;
    }

    public void updateData(List<Feed> newData) {
        data.clear();
        data.addAll(newData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View convertView = View.inflate(mainActivityRef.get(), R.layout.horizontal_feed_item, null);
        return new Holder(convertView);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        if (position == getItemCount() - 1 && endButtonAction != null) {
            holder.cardView.setVisibility(View.GONE);
            holder.actionButton.setVisibility(View.VISIBLE);
            holder.actionButton.setText(endButtonText);
            holder.actionButton.setOnClickListener(v -> endButtonAction.run());
            return;
        }
        holder.cardView.setVisibility(View.VISIBLE);
        holder.actionButton.setVisibility(View.GONE);
        if (position >= data.size()) {
            holder.itemView.setAlpha(0.1f);
            Glide.with(mainActivityRef.get()).clear(holder.imageView);
            holder.imageView.setImageResource(R.color.medium_gray);
            // Hide buttons for dummy views
            if (holder.playButton != null) holder.playButton.setVisibility(View.GONE);
            if (holder.optionsButton != null) holder.optionsButton.setVisibility(View.GONE);
            return;
        }

        holder.itemView.setAlpha(1.0f);
        final Feed podcast = data.get(position);
        holder.imageView.setContentDescription(podcast.getTitle());
        holder.imageView.setOnClickListener(v -> onClick(podcast));

        // Set initial fallback color immediately
        if (holder.colorBackground != null) {
            int initialColor = ColorUtils.extractBackgroundColor(null, podcast.getImageUrl());
            holder.colorBackground.setBackgroundColor(initialColor);
        }
        
        // Load image with color extraction
        Glide.with(mainActivityRef.get())
                .load(podcast.getImageUrl())
                .apply(RequestOptions.placeholderOf(R.color.light_gray))
                .transform(new PaletteTransformation(palette -> {
                    // Extract background color and apply it
                    int backgroundColor = ColorUtils.extractBackgroundColor(palette, podcast.getImageUrl());
                    if (holder.colorBackground != null) {
                        holder.colorBackground.setBackgroundColor(backgroundColor);
                    }
                }))
                .into(holder.imageView);

        // Show buttons for real feeds
        if (holder.playButton != null) {
            holder.playButton.setVisibility(View.VISIBLE);
            holder.playButton.setOnClickListener(v -> playLatestEpisode(podcast));
        }
        
        if (holder.optionsButton != null) {
            holder.optionsButton.setVisibility(View.VISIBLE);
            holder.optionsButton.setOnClickListener(v -> showOptionsDialog(podcast));
        }

        holder.imageView.setOnCreateContextMenuListener(this);
        holder.imageView.setOnLongClickListener(v -> {
            int currentItemPosition = holder.getBindingAdapterPosition();
            longPressedItem = data.get(currentItemPosition);
            return false;
        });

        Glide.with(mainActivityRef.get())
                .load(podcast.getImageUrl())
                .apply(new RequestOptions()
                        .placeholder(R.color.light_gray)
                        .fitCenter()
                        .dontAnimate())
                .into(holder.imageView);
    }

    private void playLatestEpisode(Feed feed) {
        Feed feedWithItems = DBReader.getFeed(feed.getId(), true, 0, 1);
        if (feedWithItems != null && !feedWithItems.getItems().isEmpty()) {
            FeedItem latestEpisode = feedWithItems.getItems().get(0);
            if (latestEpisode.getMedia() != null) {
                new PlaybackServiceStarter(mainActivityRef.get(), latestEpisode.getMedia())
                        .callEvenIfRunning(true)
                        .start();
            }
        } else {
            // No episodes available, just open the feed
            onClick(feed);
        }
    }

    private void showOptionsDialog(Feed feed) {
        View dialogView = LayoutInflater.from(mainActivityRef.get()).inflate(R.layout.podcast_options_dialog, null);
        
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(mainActivityRef.get());
        builder.setView(dialogView);
        
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        
        // Set up option click listeners
        dialogView.findViewById(R.id.option_subscribe).setOnClickListener(v -> {
            // Already subscribed, so open feed
            onClick(feed);
            dialog.dismiss();
        });
        
        dialogView.findViewById(R.id.option_play_latest).setOnClickListener(v -> {
            playLatestEpisode(feed);
            dialog.dismiss();
        });
        
        dialogView.findViewById(R.id.option_view_episodes).setOnClickListener(v -> {
            onClick(feed);
            dialog.dismiss();
        });
        
        dialogView.findViewById(R.id.option_share).setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, feed.getDownloadUrl());
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, feed.getTitle());
            mainActivityRef.get().startActivity(Intent.createChooser(shareIntent, mainActivityRef.get().getString(R.string.share_label)));
            dialog.dismiss();
        });
        
        dialog.show();
    }



    protected void onClick(Feed feed) {
        mainActivityRef.get().loadChildFragment(FeedItemlistFragment.newInstance(feed.getId()));
    }

    @Nullable
    public Feed getLongPressedItem() {
        return longPressedItem;
    }

    @Override
    public long getItemId(int position) {
        if (position >= data.size()) {
            return RecyclerView.NO_ID; // Dummy views
        }
        return data.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return dummyViews + data.size() + ((endButtonAction == null) ? 0 : 1);
    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        MenuInflater inflater = mainActivityRef.get().getMenuInflater();
        if (longPressedItem == null) {
            return;
        }
        inflater.inflate(R.menu.nav_feed_context, contextMenu);
        contextMenu.setHeaderTitle(longPressedItem.getTitle());
    }

    public void setEndButton(@StringRes int text, Runnable action) {
        endButtonAction = action;
        endButtonText = text;
        notifyDataSetChanged();
    }

    static class Holder extends RecyclerView.ViewHolder {
        SquareImageView imageView;
        View colorBackground;
        CardView cardView;
        Button actionButton;
        ImageButton playButton;
        ImageButton optionsButton;

        public Holder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.discovery_cover);
            imageView.setDirection(SquareImageView.DIRECTION_HEIGHT);
            colorBackground = itemView.findViewById(R.id.color_background);
            actionButton = itemView.findViewById(R.id.actionButton);
            cardView = itemView.findViewById(R.id.cardView);
            playButton = itemView.findViewById(R.id.play_button);
            optionsButton = itemView.findViewById(R.id.options_button);
        }
    }
}
