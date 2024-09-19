# Vizbee Fire TV and Android TV Demo App
Vizbee Fire TV and Android TV Demo App demonstrates how to integrate Vizbee casting functionality into an FireTV and AndroidTV receiver app.

# Integration Steps for your Fire TV and Android TV mobile app
Look for the block comments with text "[BEGIN] Vizbee Integration" and "[END] Vizbee Integration" in the code for an easy understanding of the integration.

## Setup
### Build Setup
1. Add the Vizbee repository to your Android mobile app’s root [settings.gradle](settings.gradle).
2. Add Vizbee SDK dependency to your app module's [build.gradle](/app/build.gradle).

### AndroidManifest Setup
#### Fire TV
1. Copy the file named [whisperplay.xml](/app/src/firetv/res/xml/whisperplay.xml) into res/xml folder of your app's code repository.
2. Update the dialid tag in whisperplay file with the DIAL name for your FireTV app. This should be the same DIAL name configured in Vizbee Continuity configuration in the Vizbee console.
3. Update your [AndroidManifest.xml](/app/src/firetv/AndroidManifest.xml) to add the `<meta-data>` tag to refer whisperplay.
4. Ensure that the primary/main activity has the DEFAULT category in the intent filter.
5. Add `android:usesCleartextTraffic="true"` attribute to application in the AndroidManifest file.

#### Android TV
1. Update your [AndroidManifest.xml](/app/src/androidtv/AndroidManifest.xml) to add the `LAUNCH` and `LOAD` intents required by the google cast framework. The LAUNCH intent is for launching the application and LOAD intent is for handling the deep linked content.
2. Update your [MainActivity](/app/src/main/java/tv/vizbee/screendemo/ui/home/MainActivity.kt) to handle the LOAD intent and pass it to the MediaManager in the CastReceiverContext.
3. Create and implement a [ReceiverOptionsProvider](/app/src/androidtv/java/tv/vizbee/screendemo/MyAppReceiverOptionsProvider.kt) to provide CastReceiverOptions and specify this in your AndroidManifest file.

### Code Setup
Copy the files under [vizbee package](app/src/main/java/tv/vizbee/screendemo/vizbee) to your app under an appropriate package. The following are the key components of above files

#### VizbeeWrapper
The `VizbeeWrapper.kt` acts as the primary interface for integrating Vizbee into your app. It manages SDK initialization and offers methods to seamlessly handle all aspects of Vizbee’s integration.

#### App Lifecycle Management
The `applifecycle` package includes files responsible for managing the app’s readiness for Vizbee deep linking and other integration features:

1. `AppReadyModel.kt:` Encapsulates objects necessary for managing deep linking and sign-in requests.
2. `VizbeeAppLifecycleAdapter.kt:` Provides an abstraction for managing the AppReadyModel lifecycle.
3. `MyVizbeeAppLifecycleAdapter.kt:` Implements `VizbeeAppLifecycleAdapter`, customized for your app.

#### Video Handling
The video package is divided into two sub-packages:

##### Deeplink
1. `MyVizbeeAppAdapter.kt:` Implements VizbeeAppAdapter to handle "start or deeplink to a new video" commands sent by your mobile app.
2. `MyVizbeeDeeplinkManager.kt:` Manages deep links and their potential failures, enabling you to customize the deeplinkVideo() method according to your app’s specific requirements.

##### Playback
1. `MyVizbeePlayerAdapterHandler.kt:` A utility class for interacting with the Vizbee API, allowing you to set and reset the player adapter that shares video playback details with the mobile application.
2. `MyVizbeeMediaSessionCompatPlayerAdapter.kt:` Bridges your app’s player with the Vizbee SDK, managing player commands and status updates.

## SDK Initialisation
1. In your [application](app/src/main/java/tv/vizbee/screendemo/VizbeeTVDemoApplication.java) class, initialise Vizbee SDK via [VizbeeWrapper](app/src/main/java/tv/vizbee/screendemo/vizbee/VizbeeWrapper.kt) utility file.

## App Adapter
1. Use the onStart method of [MyVizbeeAppAdapter](app/src/main/java/tv/vizbee/screendemo/vizbee/video/deeplink/MyVizbeeAppAdapter.kt) file to see the deep-linked content and add logging, which is invoked first when mobile/sender casts content.

## Player Adapter
### Creation
1. Use the [MyVizbeeMediaSessionCompatPlayerAdapter](app/src/main/java/tv/vizbee/screendemo/vizbee/video/playback/MyVizbeeMediaSessionCompatPlayerAdapter.kt) file to customize player adapter implementation.
2. Ensure that you are using your MyVizbeeMediaSessionCompatPlayerAdapter in the [MyVizbeePlayerAdapterHandler](app/src/main/java/tv/vizbee/screendemo/vizbee/video/playback/MyVizbeePlayerAdapterHandler.kt).

### Set & Reset
1. Invoke the setPlayerAdapter as soon as video begins to load like `vizbeePlayerAdapterHandler.setPlayerAdapter(video, mediaSession, playerListener)` in the [ExoPlayerActivity](app/src/main/java/tv/vizbee/screendemo/ui/video/ExoPlayerActivity.kt) (Setting the adapter soon after invoking playback ensures that there are no race conditions with previously playing video and also the mobile user sees all initial loading/buffering states on mobile UI)
2. Invoke the resetPlayerAdapter as soon as a video ends or interrupted like `vizbeePlayerAdapterHandler.resetPlayerAdapter()` in the [ExoPlayerActivity](app/src/main/java/tv/vizbee/screendemo/ui/video/ExoPlayerActivity.kt) (Resetting the adapter ensures that the player is ready for the next video playback)

## Documentation
* [Vizbee Fire TV And Android TV Developer Guide](https://console.vizbee.tv/app/vzb2000001/develop/guides/firetv-androidtv-snippets)
* [Code Snippets](https://console.vizbee.tv/app/vzb2000001/develop/guides/firetv-androidtv-snippets)
* [Troubleshooting](https://console.vizbee.tv/app/vzb2000001/develop/guides/firetv-androidtv-troubleshooting-snippets)
