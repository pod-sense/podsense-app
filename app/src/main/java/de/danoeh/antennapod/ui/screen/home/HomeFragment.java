package de.danoeh.antennapod.ui.screen.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.databinding.HomeFragmentBinding;
import de.danoeh.antennapod.event.FeedListUpdateEvent;
import de.danoeh.antennapod.event.FeedUpdateRunningEvent;
import de.danoeh.antennapod.ui.screen.download.CompletedDownloadsFragment;
import de.danoeh.antennapod.ui.screen.queue.QueueFragment;
import de.danoeh.antennapod.ui.screen.subscriptions.SubscriptionFragment;
import de.danoeh.antennapod.model.feed.FeedItemFilter;
import de.danoeh.antennapod.net.discovery.ItunesTopListLoader;
import de.danoeh.antennapod.net.discovery.PodcastSearchResult;
import de.danoeh.antennapod.net.download.serviceinterface.FeedUpdateManager;
import de.danoeh.antennapod.storage.database.DBReader;
import de.danoeh.antennapod.storage.preferences.UserPreferences;
import de.danoeh.antennapod.ui.screen.AllEpisodesFragment;
import de.danoeh.antennapod.ui.screen.InboxFragment;
import de.danoeh.antennapod.ui.screen.SearchFragment;
import de.danoeh.antennapod.ui.view.LiftOnScrollListener;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Locale;
import androidx.recyclerview.widget.RecyclerView;
import de.danoeh.antennapod.ui.common.ThemeUtils;

/**
 * Shows unread or recently published episodes
 */
public class HomeFragment extends Fragment implements Toolbar.OnMenuItemClickListener {

    public static final String TAG = "HomeFragment";
    public static final String PREF_NAME = "PrefHomeFragment";
    public static final String PREF_HIDE_ECHO = "HideEcho";

    private static final String KEY_UP_ARROW = "up_arrow";
    private boolean displayUpArrow;
    private HomeFragmentBinding viewBinding;
    private final CompositeDisposable disposable = new CompositeDisposable();
    private Disposable welcomeScreenDisposable;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        viewBinding = HomeFragmentBinding.inflate(inflater);
        viewBinding.toolbar.inflateMenu(R.menu.home);
        viewBinding.toolbar.setOnMenuItemClickListener(this);
        if (savedInstanceState != null) {
            displayUpArrow = savedInstanceState.getBoolean(KEY_UP_ARROW);
        }
        viewBinding.homeScrollView.setOnScrollChangeListener(new LiftOnScrollListener(viewBinding.appbar));
        ((MainActivity) requireActivity()).setupToolbarToggle(viewBinding.toolbar, displayUpArrow);
        updateWelcomeScreenVisibility();

        viewBinding.swipeRefresh.setDistanceToTriggerSync(getResources().getInteger(R.integer.swipe_refresh_distance));
        viewBinding.swipeRefresh.setOnRefreshListener(() ->
                FeedUpdateManager.getInstance().runOnceOrAsk(requireContext()));

        setupClickListeners();
        loadAllCategories();
        return viewBinding.getRoot();
    }

    private void setupClickListeners() {
        viewBinding.subscriptionsCard.setOnClickListener(v ->
                ((MainActivity) getActivity()).loadChildFragment(new SubscriptionFragment()));
        viewBinding.popularShowsArrow.setOnClickListener(v -> {
            // TODO: Navigate to a full list of popular shows
        });
        viewBinding.newsArrow.setOnClickListener(v -> {
            // TODO: Navigate to a full list of news shows
        });
        viewBinding.comedyArrow.setOnClickListener(v -> {
            // TODO: Navigate to a full list of comedy shows
        });
        viewBinding.technologyArrow.setOnClickListener(v -> {
            // TODO: Navigate to a full list of technology shows
        });
        viewBinding.businessArrow.setOnClickListener(v -> {
            // TODO: Navigate to a full list of business shows
        });
    }

    private void loadAllCategories() {
        loadCategory(null, viewBinding.popularShowsRecycler);
        loadCategory("1489", viewBinding.newsRecycler);
        loadCategory("1303", viewBinding.comedyRecycler);
        loadCategory("1318", viewBinding.technologyRecycler);
        loadCategory("1321", viewBinding.businessRecycler);
    }

    private void loadCategory(String genre, RecyclerView recyclerView) {
        disposable.add(Observable.fromCallable(() -> {
                    ItunesTopListLoader loader = new ItunesTopListLoader(getContext());
                    String country = Locale.getDefault().getCountry();
                    if (country.isEmpty()) {
                        country = "US";
                    }
                    return loader.loadToplist(country, genre, 12, DBReader.getFeedList());
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(podcastSearchResults -> {
                    PopularPodcastAdapter adapter = new PopularPodcastAdapter(getContext(), podcastSearchResults);
                    recyclerView.setAdapter(adapter);
                }, error -> Log.e(TAG, "Error loading category: " + Log.getStackTraceString(error))));
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEventMainThread(FeedUpdateRunningEvent event) {
        viewBinding.swipeRefresh.setRefreshing(event.isFeedUpdateRunning);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.refresh_item) {
            FeedUpdateManager.getInstance().runOnceOrAsk(requireContext());
            return true;
        } else if (item.getItemId() == R.id.action_search) {
            ((MainActivity) getActivity()).loadChildFragment(SearchFragment.newInstance());
            return true;
        }
        return false;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(KEY_UP_ARROW, displayUpArrow);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        disposable.clear();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFeedListChanged(FeedListUpdateEvent event) {
        updateWelcomeScreenVisibility();
    }

    private void updateWelcomeScreenVisibility() {
        if (welcomeScreenDisposable != null) {
            welcomeScreenDisposable.dispose();
        }
        welcomeScreenDisposable = Observable.fromCallable(() -> DBReader.getTotalEpisodeCount(FeedItemFilter.unfiltered()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(numEpisodes -> {
                    boolean hasEpisodes = numEpisodes != 0;
                    viewBinding.welcomeContainer.setVisibility(hasEpisodes ? View.GONE :
                            View.VISIBLE);
                    viewBinding.homeScrollView.setVisibility(hasEpisodes ? View.VISIBLE : View.GONE);
                    viewBinding.swipeRefresh.setVisibility(hasEpisodes ? View.VISIBLE : View.GONE);
                    if (!hasEpisodes) {
                        viewBinding.homeScrollView.setScrollY(0);
                    }
                }, error -> Log.e(TAG, Log.getStackTraceString(error)));
        disposable.add(welcomeScreenDisposable);
    }

}
