<p align="center">
  <img src="fastlane/metadata/android/en-US/images/icon.png" width="180" height="180" alt="Video Player app icon" />
</p>

<h1 align="center">Video Player</h1>

<p align="center">
  <b>Feature-rich, Efficient Powerful Android video player based on libmpv.</b>
  <br>
  <i>No ads. No trackers. No noise. Just a serious video player with a calmer surface and a sharper edge.</i>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-brightgreen.svg" />
  <img src="https://img.shields.io/badge/License-AGPL_v3-blue.svg" />
  <img src="https://img.shields.io/github/v/release/DARKNIGHT7473/VideoPlayer.svg?logo=github&label=Release&cacheSeconds=3600" />
  <img src="https://img.shields.io/github/downloads/DARKNIGHT7473/VideoPlayer/total?logo=github&cacheSeconds=3600" />
</p>

---

## Showcase

<div align="center">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/tanjiro-playback.jpg" width="100%" alt="Video playback in mpvRx">
</div>

<div align="center">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/player-controls.jpg" width="100%" alt="mpvRx player controls">
</div>

<div align="center">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/playlist-window.jpg" width="49%" alt="mpvRx visual playlist">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/chapter-navigation.jpg" width="49%" alt="mpvRx chapter navigation">
</div>


<div align="center">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/pip.png" width="32%" alt="mpvRx picture-in-picture mode">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/music-player.jpg" width="32%" alt="mpvRx music player">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/about-screen.jpg" width="32%" alt="mpvRx About screen">
</div>

---

## Features

mpvRx pushes the mpv-android experience further with deep customization, thermal-aware performance, and unique quality-of-life features. Here's what sets it apart:

<details close>
<summary><b>🎨 Theme & Visual System</b></summary>

| Feature | Description |
|---|---|
| **25+ Color Themes** | Default, Dynamic (Material You), Catppuccin, Nord, Tokyo Night, Rose Pine, Gruvbox, Dracula, and many more |
| **AMOLED Pure Black Mode** | Every theme has a dedicated variant with pure black backgrounds |
| **Always Dark Mode** | Option to keep player controls in dark theme regardless of app theme |
| **Themed Player Controls** | Adaptive controls that match your app theme or system accent |
| **Material 3 Expressive UI** | Expressive components, spring-based navigation, responsive grids, and polished predictive-back motion |

</details>

<details close>
<summary><b>🖐️ Gesture System</b></summary>

| Feature | Description |
|---|---|
| **Refined Tap Logic** | Configurable double-tap seek zones (left/center/right) with independently assignable actions |
| **Multi-Tap Continuous Seeking** | Triple/quadruple tap to keep seeking further without lifting |
| **Horizontal Swipe to Seek** | Swipe across video to seek with live time/delta overlay |
| **Long-Press Dynamic Speed** | Long-press activates configurable speed boost; swipe left/right to adjust across 8 presets |
| **Subtitle Drag Gesture** | Hold the center to move primary subtitles; hold either subtitle directly to move that track independently |
| **Subtitle Zoom & Dialogue Seek** | Pinch subtitle text to resize it and swipe across subtitles to seek between dialogue lines |
| **Pinch-to-Zoom with Pan** | Pinch to zoom (-1x to 3x) with simultaneous pan and single-finger pan after zoom |
| **Volume Boost via Gesture** | Vertical swipe volume can exceed 100% into configurable boost range |
| **Swap Volume/Brightness Sides** | Option to swap which screen side controls volume vs brightness |

</details>

<details close>
<summary><b>📺 HDR & Video Pipeline</b></summary>

| Feature | Description |
|---|---|
| **Shader-Based HDR Pipeline** | Powered by [hdr-toys](https://github.com/natural-harmonia-gropius/hdr-toys) — 83 bundled GLSL shaders |
| **Four HDR Modes** | BT.2100 PQ (HDR10), BT.2100 HLG, BT.2020 gamut mapping, Linear HDR |
| **SDR-to-HDR Boost** | Boost SDR content into HDR range when using Linear HDR pipeline |
| **GPU Deband** | CPU (gradfun) or GPU deband with configurable iterations, threshold, range, grain |
| **Smart Render Backend** | Auto-selects between OpenGL/Vulkan and gpu/gpu-next based on device support |
| **Automatic Black-Bar Cropping** | Optional analysis detects and removes encoded black borders on supported videos |

</details>

<details close>
<summary><b>🔥 Thermal & Battery Management</b></summary>

| Feature | Description |
|---|---|
| **ThermalMonitor** | Samples thermal headroom every 10s during playback |
| **Adaptive Shader Budget** | Ambient shader budget auto-capped based on thermal headroom |
| **Anime4K Proactive Throttling** | Auto-downgrades Anime4K quality when thermal headroom drops below 40% |
| **Background Poll Optimization** | Position poll interval doubles when controls are hidden, cutting JNI wake-ups 50% |
| **Stats Poll Backoff** | Stats page poll loop backs off from 1s to 2s when playback is paused |

</details>

<details close>
<summary><b>🧩 Anime4K & Upscaling</b></summary>

| Feature | Description |
|---|---|
| **7 Preset Quality Tiers** | Off, A, B, C, A+, B+, C+ with clean switching |
| **4K/8K Safety Guard** | Auto-disables Anime4K for high-resolution content |
| **Thermal-Guarded Selection** | Auto-downgrades quality tier under thermal pressure before frame drops |

</details>

<details close>
<summary><b>💡 Ambient Mode</b></summary>

| Feature | Description |
|---|---|
| **Two Visual Modes** | GLOW and FRAME_EXTEND — both rendered via custom GLSL at runtime |
| **15+ Configurable Parameters** | Blur samples, glow intensity, saturation, warmth, vignette, dither noise, and more |
| **Shader Recompilation Caching** | Skips recompilation when parameters match last compiled version |

</details>

<details close>
<summary><b>📝 Subtitle System</b></summary>

| Feature | Description |
|---|---|
| **Dual Subtitle Support** | Primary + secondary with auto-offset to prevent overlap |
| **ASS Override Modes** | Smart force/scale handling for secondary subtitles |
| **Comprehensive Styling** | Font, size, bold, italic, border, shadow, colors, justification, scale by window |
| **Subtitle Font Manager** | Choose a font directory, reload fonts, clear the cache, and select a default subtitle font |
| **Explicit Subtitle Off** | Disable subtitles directly without cycling through every available track |

</details>

<details close>
<summary><b>🎮 Player Controls</b></summary>

| Feature | Description |
|---|---|
| **Fully Customizable Layout** | Four configurable zones (top-left/right, bottom-left/right) + portrait bottom row |
| **25+ Button Types** | Cast, Mirror, Vertical Flip, A-B Loop, Custom Skip, Background Playback, Ambient, and more |
| **Custom User Buttons** | Create arbitrary buttons executing Lua, JavaScript, or mpv commands |
| **Landscape/Portrait Adaptive Layouts** | Completely different control layouts per orientation |
| **One-Tap Control Unlock** | When playback controls are locked, tap the unlock button at the top-left or top-right according to the side you touch |
| **Double-Back Unlock** | While controls are locked, the first Back press shows a reminder; a second quick press unlocks them |
| **App Icons** | Solid purple is the default across app, splash, Recents, website, and store artwork; gradient and classic remain selectable in Appearance |
| **Hide Button Backgrounds** | Transparent buttons with only icons visible |
| **Centralized "More Sheet"** | Quick access to all player buttons and custom controls |
| **In-Player Settings** | Toggle 10+ settings (gestures, PiP, UI behavior) without leaving playback |

</details>

<details close>
<summary><b>📺 Google Cast</b></summary>

| Feature | Description |
|---|---|
| **Native Cast Button** | Standard stateful Google Cast route icon in both portrait and landscape player controls |
| **Device Discovery** | Google Cast framework discovery and native device chooser for Chromecast and Cast-enabled TVs |
| **Position Handoff** | Transfers the current title, play state, duration, and playback position to the receiver |
| **Local File Casting** | Tokenized temporary LAN server exposes `file://` and `content://` media with byte-range seeking and CORS headers |
| **Remote Stream Casting** | Direct handoff for receiver-accessible HTTP and HTTPS media URLs |
| **Expanded Remote Controls** | Cast SDK controller, notification, lock-screen actions, reconnection, and receiver volume controls |
| **Return to Phone** | Restores local playback at the receiver's latest position when the Cast session ends |

> Cast uses Google's Default Media Receiver. The TV/Chromecast must support the media container and codecs; mpv-only formats are not transcoded automatically.

</details>

<details close>
<summary><b>🧭 Smart Orientation</b></summary>

| Feature | Description |
|---|---|
| **8 Orientation Modes** | Free, Video (auto aspect ratio), Portrait, Reverse Portrait, Sensor Portrait, Landscape, Reverse Landscape, Sensor Landscape |
| **Persistent Per-Video** | Orientation remembered per-video across sessions |

</details>

<details close>
<summary><b>🔍 File Browser & Navigation</b></summary>

| Feature | Description |
|---|---|
| **Dual Browser Modes** | Album View (folder grid) and Tree View (file manager hierarchy) |
| **Folder Pinning** | Pin frequently accessed folders to top |
| **Single-Child Auto-Flatten** | Folders with one subfolder auto-flatten for faster browsing |
| **Auto-Scroll to Last Played** | Opens to the last played video position |
| **Recursive File/Folder Counts** | Shows total video count, duration, size computed recursively |
| **"NEW" Badges** | Configurable threshold for new video indicators |
| **Grid/List Layout** | Per-orientation column count settings |
| **Multi-Protocol Network** | Built-in SMB, FTP, and WebDAV clients |
| **Syncplay Rooms** | Join a Syncplay server room to synchronize pause, resume, seeking, and playback position with other viewers |
| **Responsive & Dual-Pane Layouts** | Automatic grid sizing plus optional folder/settings dual-pane views on tablets |
| **Audio Library Mode** | MediaStore and filesystem audio browsing with square artwork, metadata titles, and mixed sibling playlists |
| **Home-First Back Navigation** | Main tabs return to Home before Back exits the app; selection, search, and nested screens handle Back first |
| **Swipeable Main Tabs** | Swipe between main destinations with a sliding dock highlight; nested category tabs remain selectable by tapping |
</details>

<details close>
<summary><b>🎵 Music Library & Audio Player</b></summary>

| Feature | Description |
|---|---|
| **Local Music Library** | Browse songs, albums, artists, and audio playlists with cover art, search, sorting, and grid/list views |
| **Choose Your Music Source** | Switch the Music tab between local storage, Jellyfin, and Navidrome |
| **Library Filters** | Exclude selected audio folders and set a minimum track duration |
| **Dedicated Now Playing View** | Artwork, track metadata, playback controls, and an up-next queue in a music-focused layout |
| **Lyrics** | View local or embedded lyrics, fetch online lyrics from LRCLIB, and switch between available sources |
| **Audio Visualizer** | Ambient visualization with an optional audio-reactive wavy seekbar |
| **Audio Playlists & Favorites** | Create local audio playlists, keep favorite tracks together, and start normal or shuffled playback |
| **Audiobook Library** | Open the book icon in Music to import a file, selected files, or one book folder including disc subfolders; M4B is recognized |
| **Book Metadata** | Covers, author, narrator, series and edition details from embedded tags or optional metadata.json/OPF sidecars, with editable book details |
| **Audiobook Listening** | Book progress/resume through the normal audio player, shared playlist/speed controls, a whole-book seekbar with chapter markers, artwork, visualizers, configured seek gestures, pause rewind and sleep timers |
| **Playback Bookmarks** | In video and audio player layouts, tap the bookmark icon to open chapters/bookmarks; long-press it to add a named point. Custom points persist and appear on the existing seekbar, with rename and delete actions |
| **Audiobook Text** | Display supplied embedded lyrics or readable local LRC text in the existing lyrics view, without automatic online song matching |

</details>

<details close>
<summary><b>🎧 Navidrome / Subsonic Music Streaming</b></summary>

| Feature | Description |
|---|---|
| **Subsonic API Integration** | Stream music from Navidrome and compatible Subsonic servers using token/salt authentication |
| **Multiple Servers** | Save named servers, switch the active server, and manage connections from the app |
| **Flexible Sign-In** | Connect with a username and password, or a supported app password/token |
| **Music Discovery** | Quick mixes, recently added albums, artist browsing, and artwork-rich album and playlist detail views |
| **Library Search** | Search the server for songs, albums, and artists |
| **Server Playlists & Favorites** | Browse server playlists and sync favorite songs, albums, and artists; starred songs appear in a Favorites collection |
| **Play All & Shuffle** | Start an album or playlist as a playback queue with track titles, artists, and cover art |
| **Display Preferences** | Choose grid/list views and track sorting preferences |

> Requires a music-server account and a reachable server. Available functionality depends on the server's Subsonic API and authentication support.

</details>

<details close>
<summary><b>🍿 Jellyfin Client (Expressive Cinematic UI)</b></summary>

| Feature | Description |
|---|---|
| **Native Server Integration** | Direct connection with Jellyfin accounts, fast token authentication, and multi-server management |
| **Material 3 Expressive UI** | Cinematic interface with artwork-rich browsing, detail sheets, and dedicated music views |
| **Featured Hero Banner** | Auto-advancing 16:9 backdrop banner with smooth gradient scrims, ratings, badges, and quick play |
| **Continue Watching & Recently Watched** | Horizontal resume carousels with relative timestamps and progress bars tracking playback progress |
| **Library Filter Chips** | Instant switching between Movies, TV Shows, Anime, Music, and BoxSets with dynamic item counts |
| **Poster & Backdrop Cards** | 2:3 vertical posters and 16:9 backdrop cards with community ratings, release years, and unplayed badges |
| **Cinematic Detail Sheet** | Full-bleed modal sheet with backdrops, floating posters, storyline synopsis, and season/episode picker |
| **Server-Synced Favorites** | Heart action synced bidirectionally with your Jellyfin server account (`UserData.IsFavorite`) |
| **Direct & Transcoded Streaming** | High-performance direct stream playback with audio track and subtitle stream switching |

</details>

<details close>
<summary><b>⚡ Unified Media & Torrent Streaming</b></summary>

| Feature | Description |
|---|---|
| **Cinematic Discovery** | Hero banner carousel, Continue Watching row, and 2:3 vertical poster cards for saved media |
| **Automated TMDB Enrichment** | Fetches posters, backdrops, storyline synopsis, release years, and media types via Wyzie/TMDB |
| **Cinematic Media Details** | Modal bottom sheet with floating poster, metadata badges, "Watch Now / Resume", and "Copy Magnet" |
| **Intelligent Episode Parser** | Robust parser supporting anime numbering, season/episode formats (S01E02, 1x02), and quality tags |
| **Episode Search & Sorting** | Filter and sort episodes within multi-file torrents with watched status checkmarks |
| **Unified Media Hub** | Save direct streams, YouTube links, and torrents into your collection with one-tap ingestion |

</details>

<details close>
<summary><b>📜 Config Editor</b></summary>

| Feature | Description |
|---|---|
| **Sora Code Editor** | Built-in editor with TextMate syntax highlighting |
| **Config Editor** | Built-in editor for mpv.conf and input.conf |

</details>

<details close>
<summary><b>⚙️ Utilities</b></summary>

| Feature | Description |
|---|---|
| **Stats Page 6** | Live system monitor: FPS, dropped frames, codecs, network sparkline, battery |
| **Video Compressor** | Built-in FFmpeg-based compression with presets |
| **12 Video Filter Presets** | Vivid, Cinematic, Dramatic, Ghibli Style, Neon Pop, Deep Black, and more |
| **Custom Skip Segments** | Intro/outro/recap/credits/preview detection from IntroDB, TIDB v3, AniSkip v2, Anime Skip, and SkipDB (requires an IMDb ID). Hybrid uses one validated provider result; duration-aware caches keep release versions separate. |
| **A-B Loop** | In-player looping with visual markers on seekbar |
| **Frame Navigation** | Frame-by-frame forward/backward with frame number display |
| **Sleep Timer** | Built-in with quick presets (15/30/45/60 min) |
| **Adaptive Background Playback** | Auto-PiP on Home, auto-resume after screen unlock |
| **Unified Background Playback** | One persistent audio/video switch; Back can return to browser lists without stopping the current media |
| **Notification Styles** | None, Media, or Progress with Chapters (Android 16+) |
| **Safe Area / Window Offset** | Prevents camera notch overlap |
| **Display Cutout Mode** | Full-bleed on notch devices |
| **Remember Brightness** | Persists brightness level set during playback |
| **Automatic Local Playlists** | Discover M3U/M3U8 IPTV playlists in readable internal storage, SD cards, and granted local folders; refresh changed sources without duplicate entries |
| **Playlist Covers** | Show the first entry's IPTV logo or cached media thumbnail in list and grid layouts, with an icon fallback when artwork is unavailable |
| **yt-dlp Integration** | High-performance streaming support for YouTube, Twitch, Bilibili, and more via a native Python bridge (SDK 29+ bypass) |
| **yt-dlp Quality Controls** | Independent codec, resolution, FPS, HDR, container, and audio-bitrate preferences |
| **Dynamic Refresh Rate** | Matches supported display refresh rates to the current video's frame rate for smoother motion |
| **Secure Folder** | PIN-protected access with optional biometrics, media move/restore actions, and a hideable entry point |

Local playlist discovery runs when the Playlists page is first loaded and after media-index or storage-mount changes. Pull to refresh to rescan manually, or use **Playlist options > Add local playlist folder** to grant access to a folder. Android's restrictions on protected directories such as `Android/data` and `Android/obb` still apply; discovery cannot bypass them. HLS segment manifests are not imported as channel lists. Removing a discovered playlist does not delete its source file, and that source stays hidden until explicitly imported again. Remote video thumbnails respect the existing network-thumbnail setting.

</details>

---

## 🔋 Battery Optimization Guide for mpv

**Pro Tip:** Keep `mpv.conf` empty if you are new to mpv.

- **Use `gpu` not `gpu-next`** — `gpu-next` is a Vulkan-based renderer that keeps the GPU active unnecessarily during regular playback. The classic `gpu` backend is lighter and uses OpenGL, which on most Android devices has better power characteristics.
- **Disable Vulkan entirely.** Vulkan offers great video playback performance but can be power-intensive on mobile devices.
- **Use the `fast` mpv profile.** It's built into mpvRx: select MPV Profiles and set it to Default, or add `profile=fast` to `mpv.conf`.
- **Don't overuse shaders.** Shaders run on the GPU every single frame. Watching 24fps content with complex shader pipelines means 24 heavy compute passes per second for minimal perceptible difference on a phone screen.
- **Avoid bloated configurations.** Copying complex configs with excessive scalers (`scale=ewa_lanczossharp`, `dscale=mitchell`, `cscale=sinc`) and dozens of shaders forces your mobile device to render like a cinema projector on a small display.

**Summary:** mpv's default config with `profile=fast` and the `gpu` backend plays video with negligible battery impact — often **less** than OEM players because mpv doesn't have proprietary DRM modules, analytics SDKs, and ad frameworks running in the background.

_Tip: If your battery consumption stays within 200 mAh and below 0.9W (see Stats Page 6: Player More Settings -> Page 6), your mpv configuration is well-optimized for playback._

---

<div align="center">
  <a href="https://github.com/DARKNIGHT7473/VideoPlayer/releases">
    <img src="https://img.shields.io/badge/Download-Stable_Release-blue?style=for-the-badge&logo=github" alt="Stable Release">
  </a>
</div>

<!-- <div align="center">
  <i>Note: Previews may be unstable and are intended for testing purposes only.</i>
</div> -->

---

## Build

### Requirements

- JDK 17
- Android SDK with modern build tools installed
- Git

### Debug Build

```powershell
./gradlew.bat :app:assembleStandardDebug
```

### Release Variants

| Variant | Description |
|---|---|
| `standard` | Main release with hardware acceleration, custom shaders, yt-dlp, and torrent streaming |

### APK Variants

| Variant | Description |
|---|---|
| `arm64-v8a` | Optimized for modern 64-bit ARM Android devices |

---

## Release Notes For Maintainers

To cut a signed GitHub release through Actions, configure these repository secrets:

| Secret Name | Description |
|---|---|
| `SIGNING_KEYSTORE` | Base64-encoded keystore file (`.jks` or `.keystore`) |
| `SIGNING_KEY_ALIAS` | Key alias inside the keystore |
| `SIGNING_STORE_PASSWORD` | Password for the keystore |
| `KEY_PASSWORD` | Password for the signing key |

Then bump `versionCode` and `versionName` in `app/build.gradle.kts`, create a tag, and push it:

```bash
git tag -a v1.0.1 -m "Release version 1.0.1"
git push origin v1.0.1
```

---

## Acknowledgments

### Built with open source

Thank you to the player projects, music apps, libraries, and individual contributors whose work makes projects like this possible.

| Explore the credits | Includes |
|---|---|
| [Playback & player projects](CITATION.md#playback--player-projects) | mpv-android, mpvExtended, mpvKt, MpvRex, Next Player, Neo Player |
| [Music & media apps](CITATION.md#music--media-apps) | PixelPlayer, Gramophone, AFinity, Chora, BitChord |
| [Video processing & streaming](CITATION.md#video-processing--streaming) | hdr-toys, anacrolix/torrent |

> Special thanks to [SunnyVishnu3](https://github.com/SunnyVishnu3) for the `yt-dlp` native integration and SDK 29+ bypass logic.

[Full acknowledgments & citation information](CITATION.md)

---

## License

Distributed under **GNU Affero General Public License v3.0 (AGPL-3.0-or-later)**. See `LICENSE` for more information.
