package com.example.app.vizbee.homesso

import com.example.app.vizbee.applifecycle.AppReadyModel
import com.example.app.vizbee.applifecycle.VizbeeAppLifecycleAdapter
import org.json.JSONObject
import tv.vizbee.screen.homesso.IVizbeeHomeSSOAdapter
import tv.vizbee.screen.homesso.VizbeeSignInStatusCallback
import tv.vizbee.screen.homesso.model.VizbeeSenderSignInInfo
import tv.vizbee.screen.homesso.model.VizbeeSignInStatus.Failure
import tv.vizbee.screen.homesso.model.VizbeeSignInStatus.Progress
import tv.vizbee.screen.homesso.model.VizbeeSignInStatus.Success
import tv.vizbee.utils.ICommandCallback

/**
 * #VizbeeGuide Implement (1) SUPPORTED_SIGN_IN_TYPES (2) isSignedIn(signInType) (3) onStartSignIn() as described.
 * Create an instance of this class in Application class and pass it to VizbeeWrapper.
 *
 * This class implements Vizbee HomeSSO SDK's IVizbeeHomeSSOAdapter to handle the sign in requests
 * coming from mobile.
 *
 * @property application application object
 * @property appLifecycleAdapter implementation of VizbeeAppLifecycleAdapter
 */
class MyVizbeeHomeSSOAdapter(
    private val appLifecycleAdapter: VizbeeAppLifecycleAdapter
) : IVizbeeHomeSSOAdapter, VizbeeSignInStatusListener {

    companion object {
        // #VizbeeGuide replace "your_sign_in_type1" with your own string constant. The same string
        // should be used in Sender as well. You can add multiple sign in types.
        val SUPPORTED_SIGN_IN_TYPES = listOf("your_sign_in_type1")
    }

    // public vars exposed for video deeplink handling logic
    var isHomeSSOReady = false
    var homeSSOReadyListener: HomeSSOReadyListener? = null
    var isMobileSignedIn = false
    var isSignInInProgress = false
    var signInStatusListener: VizbeeSignInStatusListener? = null

    // HomeSSO's getSignInInfo query callbacks map
    private val pendingGetSignInInfoCallbacks = arrayListOf<ICommandCallback<List<VizbeeSignInInfo>>>()

    // HomeSSO's startSignIn request callback
    private var homeSSOSignInCallback: VizbeeSignInStatusCallback? = null

    // region Internal Initialisation | App/HomeSSO Readiness

    init {

        appLifecycleAdapter.addAppLifecycleListener(object :
            VizbeeAppLifecycleAdapter.AppLifecycleListener {

            override fun onAppReady(appReadyModel: AppReadyModel) {
                // 1. Fulfil any pending getSignInInfo requests
                pendingGetSignInInfoCallbacks.forEach { callback ->
                    sendSignInInfo(callback)
                }
                pendingGetSignInInfoCallbacks.clear()

                // 2. Inform listeners waiting for HomeSSO readiness
                isHomeSSOReady = true
                homeSSOReadyListener?.onHomeSSOReady()
            }

            override fun onAppUnReady() {
                isHomeSSOReady = false
            }
        })
    }

    /**
     * This interface is a listener to know if HomeSSO is ready to share the sign in status
     */
    interface HomeSSOReadyListener {
        fun onHomeSSOReady()
    }

    // endregion

    // region APIs Implementation

    /**
     * This method sends the sign in info to the SDK.
     * @param callback callback via which the app tells the SDK about its sign in status
     */
    override fun getSignInInfo(callback: ICommandCallback<List<VizbeeSignInInfo>>) {
        if (appLifecycleAdapter.isAppReady()) {
            sendSignInInfo(callback)
        } else {
            pendingGetSignInInfoCallbacks.add(callback)
        }
    }

    private fun sendSignInInfo(callback: ICommandCallback<List<VizbeeSignInInfo>>) {
        /* Example Code */
        /*
        val signedIn = isSignedIn(MVPD_SIGN_IN_TYPE)
        val email = SharedPreferencesManager(context).getStringValue("email")
        val mvpdSignInInfo = VizbeeSignInInfo(
            MVPD_SIGN_IN_TYPE,
            signedIn,
            userLogin = email,
            // userName = "fill with the user name",
            // userSubscriptionType = "premium/free",
            // userSubscriptionValue = "$ value such as $19",
            // userSubscriptionRenewalType = "monthly/yearly",
            // additionally, pass any custom info as JSON object
            // userAdditionalInfo = JSONObject().apply {
            // put("customKey1", "customValue1")
            // put("customKey2", "customValue2")
            // }
        )
        // if your app has additional sign in types, make a VizbeeSignInInfo object for each type and append to the list
        callback.onSuccess(listOf(mvpdSignInInfo))
        */
    }

    /**
     * #VizbeeGuide Implement this method to return if the user is signed for the given sign in.
     *
     * Returns whether the user is signed in with a given sign in or not.
     * @param signInType sign in type that app decides internally and uses consistently. eg., "MVPD"
     */
    fun isSignedIn(signInType: String): Boolean {
        /* Example Code */
        /*
        return appLifecycleAdapter.getAppReadyModel()?.appViewModel?.isUserSignedIn(signInType)
         */
    }

    /**
     * Returns whether the user is signed in with any type or not, eg. either via D2C or MVPD
     */
    fun isSignedIn(): Boolean {
        return SUPPORTED_SIGN_IN_TYPES.any { isSignedIn(it) }
    }

    /**
     * The SDK invokes this method to pass the start sign-in message that mobile sent.
     * App implements this method to kick start the sign in process
     * eg., generate reg code and start polling for its success.
     * @param senderSignInInfo sender's sign in info including knowledge about if it's signed in
     */
    override fun onStartSignIn(
        senderSignInInfo: VizbeeSenderSignInInfo,
        callback: VizbeeSignInStatusCallback
    ) {
        if (appLifecycleAdapter.isVideoPlaying()) {
            return
        }
        isSignInInProgress = true
        this.homeSSOSignInCallback = callback
        this.isMobileSignedIn = senderSignInInfo.isSignedIn

        // #VizbeeGuide Implement your own logic to start the sign in process and inform
        // its progress, success/failure to this class via callback.

        /*
            // Example Code
            if (senderSignInInfo.isSignedIn) {
                startBackgroundSignIn(MVPD_SIGN_IN_TYPE)
            } else {
                startForegroundSignIn(MVPD_SIGN_IN_TYPE)
            }
        */

        // Vizbee Recommendation:
        // (1) For approaches that need registration code authentication,
        //     (a) If mobile is NOT signed in, implement sign in by showing your app's regular sign in screen
        //     (b) If mobile is already signed in, implement the sign in the background without showing
        //         any UI because it happens quickly.
        //     (c) For both of these use cases, here’s how you need to callback on `MyVizbeeHomeSSOAdapter`
        //         so it maintains the state as well as takes care of informing the SDK about the sign in
        //         status via the SDK's `callback` sent with `onStartSignIn()`.
        //         (i) As soon as the reg code is generated, call onProgress(signInType, regCode).
        //         (ii) As soon as the sign in is successful, call onSuccess(signInType).
        //         (iii) If sign in fails or is cancelled, call onFailure(signInType, isCancelled).
        //         (iv) These callbacks ensure that the SDK passes information to the mobile and also
        //              shows appropriate UI modals.
        // (2) For approaches that do not need registration code authentication,
        //     implement the sign in the background without showing any UI because it happens quickly.
        //      (i) As soon as the sign in process starts, call onProgress(signInType).
        //      (ii) As soon as the sign in is successful, call onSuccess(signInType).
        //      (iii) If sign in fails or is cancelled, call onFailure(signInType, isCancelled).
        //      (iv) These callbacks ensure that the SDK passes information to the mobile and also
        //            shows appropriate UI modals.
    }

    override fun onProgress(signInType: String, regCode: String?) {
        regCode?.let {
            homeSSOSignInCallback?.onProgress(
                Progress(
                    signInType,
                    JSONObject().apply {
                        put("regcode", regCode)
                    })
            )
        } ?: run {
            homeSSOSignInCallback?.onProgress(
                Progress(
                    signInType,
                    JSONObject())
            )
        }
    }

    override fun onSuccess(signInType: String) {
        isSignInInProgress = false
        homeSSOSignInCallback?.onSuccess(Success(signInType))
        signInStatusListener?.onSuccess(signInType)
    }

    override fun onFailure(signInType: String, isCancelled: Boolean) {
        isSignInInProgress = false
        homeSSOSignInCallback?.onFailure(Failure(signInType, "", isCancelled, null))
        signInStatusListener?.onFailure(signInType, isCancelled)
    }

    // endregion
}