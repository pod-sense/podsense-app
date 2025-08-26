# Podsense Development Rules & Guidelines

This document outlines key architectural patterns, conventions, and potential pitfalls in the Podsense codebase. Following these rules will help maintain code quality and prevent common errors.

### 1. Project Structure

The project is divided into multiple Gradle modules. Here are some of the most important ones:
- `app`: The main application module, containing most of the UI and Activity classes.
- `model`: Defines the core data models for the app (e.g., `FeedItem`, `FeedMedia`).
- `playback`: Contains the services and logic for handling audio playback.
- `net`: Includes networking code for downloading feeds, episodes, and interacting with APIs.
- `ui/common`: Contains shared UI resources like drawables, styles, and attributes. **Always check this module for existing resources before creating new ones.**

### 2. UI Development & Resource Naming

**A series of build failures occurred due to incorrect resource naming. Be meticulous here.**

- **Verify Resource Names**: Before using a resource (`@drawable`, `@string`, `@attr`), physically check that it exists in the project. Do not assume its name.
- **Shared Resources**: Common icons, colors, and attributes are in `ui/common/src/main/res/`. For example, the correct attribute for themed icons is `?attr/action_icon_color`.
- **Data Binding**: The project uses data binding. The generated binding class names are derived directly from the layout file names. For example, `feeditemlist_item.xml` generates `FeeditemlistItemBinding`, while `feeditemlist_wide_item.xml` would generate `FeeditemlistWideItemBinding`. Using the wrong one will cause compilation errors.

### 3. Core Patterns & Classes

- **Episode Lists**:
    - To display a list of episodes, always use the shared `EpisodeItemViewHolder`. It is designed to work with the `feeditemlist_item.xml` layout and handles the complex logic of binding `FeedItem` data to the views.
    - **Do not create new, custom `ViewHolder` classes inside adapters for this purpose.**
    - Adapters for episode lists should extend `RecyclerView.Adapter<EpisodeItemViewHolder>`.

- **Starting Playback**:
    - To stream or play an episode, use the `PlaybackServiceStarter`.
    - Its constructor requires a `Playable` object. **Do not pass a `FeedItem` directly.** The correct object is the `FeedMedia` contained within the `FeedItem`, which you can get by calling `item.getMedia()`.

- **Showing the Subscription Screen**:
    - To show the podcast details/subscribe screen for a given feed URL, use the `OnlineFeedviewActivityStarter` class.
    - It is instantiated via `new OnlineFeedviewActivityStarter(context, feedUrl)`.

- **Handling Background Tasks**:
    - When performing background tasks in a Fragment using RxJava, use a `CompositeDisposable`. Add each new `Disposable` to it, and call `disposable.clear()` (or `dispose()`) in the `onStop()` or `onDestroyView()` lifecycle method to prevent memory leaks.

### 4. Common Pitfalls

- **Modifying Shared Layouts**: Be extremely careful when changing a shared layout file like `feeditemlist_item.xml`. These layouts are often used in unexpected places (e.g., dialogs like `SwipeActionsDialog`). A change can cause a cascade of build failures in other parts of the app.
- **Incorrect Imports**: Many of the modules have similar package names. After making changes, double-check that you are importing classes from the correct packages (e.g., `de.danoeh.antennapod.model.feed.FeedItem`, not a non-existent `core` package).

By following these guidelines, we can ensure the codebase remains stable and consistent as we continue to build out new features.
