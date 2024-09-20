# Vizbee Fire TV and Android TV Demo App
Vizbee Fire TV and Android TV Demo App demonstrates how to integrate Vizbee casting functionality into an FireTV and AndroidTV receiver app.

# Integration Steps for your Fire TV and Android TV mobile app
Look for the block comments with text "[BEGIN] Vizbee Integration" and "[END] Vizbee Integration" in the code for an easy understanding of the integration.

## Setup
### Build Setup
Use the following code to pull the latest Vizbee Receiver HomeSSO SDK into your app using gradle dependency management.

#### Continuity
Ensure you have completed the `Build Setup` guide provided [here](https://github.com/ClaspTV/vizbee-ftv-atv-demo-app/blob/develop/README.md#build-setup), as HomeSSO depends on the continuity setup.

#### Gradle
Add the Gradle dependency for the Vizbee Receiver HomeSSO SDK to your app/module-level `build.gradle` file. The HomeSSO dependency is shared by both receiver SDKs (Android TV & Fire TV), so regardless of whether you have a mono repo for both or separate repositories, you can simply add the dependency using implementation without needing a product flavor prefix.

**Note:** Refer to the [Release Notes](https://console.vizbee.tv/app/vzb6102589938/develop/releases/android-releases) for the latest SDK versions.

### Code Setup
To make the Vizbee integration easier, we have separated out the template files for smooth integration. Copy all the contents under the package [vizbee](app/src/main/java/tv/vizbee/screendemo/vizbee) to your app under an appropriate package. 

#### Components of the Template Files
1. These contains files related to continuity integration, please refer to `Code Setup` guide provided [here](https://github.com/ClaspTV/vizbee-ftv-atv-demo-app/blob/develop/README.md#code-setup), for more understanding of the continuity setup.
2. Continuing from the Continuity Setup, you will see some additional files for HomeSSO integration. Out of them, the following files need modifications from app side to align with app needs
    * [AppReadyModel](app/src/main/java/tv/vizbee/screendemo/vizbee/applifecycle/AppReadyModel.kt) - Create an instance of this class by passing the objects necessary to handle deep link and sign in requests.
    * [MyVizbeeHomeSSOAdapter](app/src/main/java/tv/vizbee/screendemo/vizbee/homesso/MyVizbeeHomeSSOAdapter.kt) - Implement (1) SUPPORTED_SIGN_IN_TYPES (2) isSignedIn(signInType) (3) onStartSignIn() as described.
    * [MyVizbeeDeeplinkManager](app/src/main/java/tv/vizbee/screendemo/vizbee/video/deeplink/MyVizbeeDeeplinkManager.kt) - Implement deeplinkVideo() method as described.
    * [MyVizbeePlayerAdapterHandler](app/src/main/java/tv/vizbee/screendemo/vizbee/video/playback/MyVizbeePlayerAdapterHandler.kt) - Implement setPlayerAdapter() and getVideoInfo() methods as described.
3. If your app already has continuity integration, update the `onStart()` method in the `MyVizbeeAppAdapter` as shown in the updated file to support HomeSSO integration.

**Naming:** For consistency with the rest of your application, prepend your app name to the above template file names. For example, if your app is named ‘Netflix’, rename ‘MyVizbeeHomeSSOAdapter’ to ‘NetflixVizbeeHomeSSOAdapter’.  

## SDK Initialisation
1. In your [application](app/src/main/java/tv/vizbee/screendemo/VizbeeTVDemoApplication.java) class, initialise Vizbee SDK & HomeSSO SDK via [VizbeeWrapper](app/src/main/java/tv/vizbee/screendemo/vizbee/VizbeeWrapper.kt) utility file.

## Handle Sign In Request
1. `isSignedIn(signInType, callback):` Update this method in MyVizbeeHomeSSOAdapter class to specify if the receiver is already signed in, which will be used by Vizbee SDK.
2. `onStartSignIn(senderSignInInfo, callback):` The SDK invokes this method to initiate sign in process on the receiver if not signed in already. From here, start initialising the sign in process on app side.
3. Update the corresponding callback methods `onProgress(signInType, regCode)`, `onSuccess(signInType)` and `onFailure(signInType, isCancelled)` in MyVizbeeHomeSSOAdapter class to manage the sign-in process updates by the SDK.

## Handle Start Video during Sign-In progress
When handling both the start video request and sign-in process in parallel, ensure that the sign-in is completed first before processing the start video request.

### Migrating from legacy Vizbee Continuity integration to HomeSSO SDK
If you have the legacy integration of the Vizbee Continuity SDK and are now upgrading to the HomeSSO SDK, follow these steps:

1. `Deep linking:` Move video deep linking code from MyVizbeeAppAdapter to MyDeeplinkManager::deeplinkVideo(), if not handled in the continuity integration.
2. `Sign-in handling:` Remove the VizbeeAppAdapter::onEvent() implementation if it was used for Vizbee sign-in.

   * Note: Keep this implementation until all your mobile apps are upgraded to support HomeSSO. This ensures backwards compatibility during the transition period.
3. `Adapter replacement:` Replace your existing MyVizbeeAppAdapter with the newer version provided in the HomeSSO template files.

   * The new adapter includes additional checks using MyVizbeeDeeplinkSignallingManager.
   
These changes ensure a smooth transition to the HomeSSO SDK while maintaining compatibility with your existing integration.

### Delay Start Video Request
If the start video request is received during the sign-in process, the receiver should delay handling the video request until the sign-in is complete. We’ve added an initial delay, but if your app requires more time for the receiver to sign in while the mobile is already signed in, consider extending the delay accordingly.   

## Documentation
Please refer to the following documentation for more details:
* [Vizbee Fire TV And Android TV Developer Guide](https://console.vizbee.tv/app/vzb2000001/develop/guides/firetv-androidtv-snippets)
* [Code Snippets](https://console.vizbee.tv/app/vzb2000001/develop/guides/firetv-androidtv-snippets)
* [Troubleshooting](https://console.vizbee.tv/app/vzb2000001/develop/guides/firetv-androidtv-troubleshooting-snippets)
