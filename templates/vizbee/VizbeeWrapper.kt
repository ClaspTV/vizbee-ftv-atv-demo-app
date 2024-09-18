package com.example.app.vizbee

import android.app.Application
import android.content.Context
import com.example.app.vizbee.homesso.MyVizbeeHomeSSOAdapter
import android.util.Log
import tv.vizbee.screen.api.Vizbee
import tv.vizbee.screen.api.adapter.VizbeeAppAdapter
import tv.vizbee.screen.homesso.VizbeeHomeSSOManager

/**
 * #VizbeeGuide Do not modify this file.
 *
 * This class is an entry point to the Vizbee integration. It has a utility method to initialise the
 * Vizbee SDKs: the Continuity SDK and the HomeSSO SDK. It has methods to know if Vizbee is enabled
 * by the app and some easy to access adapter objects via extension methods.
 */
class VizbeeWrapper {

    private var isVizbeeEnabled: Boolean = false
    val vizbeeAppLifecycleAdapter: VizbeeAppLifecycleAdapter by lazy { MyVizbeeAppLifecycleAdapter() }
    val vizbeeHomeSSOAdapter: MyVizbeeHomeSSOAdapter by lazy {
        MyVizbeeHomeSSOAdapter(
            vizbeeAppLifecycleAdapter
        )
    }

    val vizbeeAdapter: MyVizbeeAppAdapter by lazy {
        MyVizbeeAppAdapter(
            vizbeeAppLifecycleAdapter,
            vizbeeHomeSSOAdapter
        )
    }

    //------
    // Initialisation
    //------

    /**
     * Initializes Vizbee SDK with the app ID assigned for your app.
     *
     * Invoke this method in the onCreate()
     * lifecycle callback of your Application.
     *
     * @param app Application
     */
    fun initialize(app: Application) {

        isVizbeeEnabled = isVizbeeEnabled(app)
        if (!isVizbeeEnabled) {
            Log.i(
                "VizbeeWrapper",
                "Vizbee is not enabled. Not initialising. Define your_app_vizbee_app_id for app to enable Vizbee."
            )
            return
        }

        // Initialise Vizbee Continuity SDK
        vizbeeAdapter.context = app
        Vizbee.getInstance().initialize(app, getVizbeeAppId(app), vizbeeAdapter)

        // Initialise Vizbee HomeSSO SDK
        VizbeeHomeSSOManager.initialize(app, vizbeeHomeSSOAdapter)
    }

    //------
    // Helpers
    //------

    /**
     * This method returns true if Vizbee is enabled by the app. Returning false will disable all the Vizbee integration.
     *
     * @return true if Vizbee should be enabled in the app integration.
     */
    private fun isVizbeeEnabled(context: Context): Boolean {
        val vizbeeAppId = getVizbeeAppId(context)
        return vizbeeAppId.isNotEmpty()
    }

    /**
     * This method returns the vizbee_app_id defined the resources.
     *
     * #VizbeeGuide For best practices, define the resource in Gradle under product flavors to be able to change
     * it specific to a build variant.
     *
     * android {
     *      productFlavors {
     *          prod {
     *              ...
     *              resValue "string", "vizbee_app_id", "vzb********"
     *          }
     *      }
     * }
     *
     * @return returns the vizbee_app_id defined the resources.
     */
    private fun getVizbeeAppId(context: Context): String {
        val identifier = context.resources.getIdentifier(
            "vizbee_app_id",
            "string",
            context.packageName
        )
        if (identifier == 0) {
            return ""
        }
        return context.getString(identifier)
    }

    /**
     * #VizbeeGuide Make sure to create the VizbeeWrapper instance in Application class.
     *
     * This companion object defines the extension getters for a few major components in the integration.
     */
    companion object {
        private val Context.application get() = applicationContext as? com.example.app.Application

        val Context.vizbeeAppLifecycleAdapter
            get() = application?.vizbeeWrapper?.vizbeeAppLifecycleAdapter

        val Context.vizbeeHomeSSOAdapter
            get() = application?.vizbeeWrapper?.vizbeeHomeSSOAdapter

        val Context.vizbeeWrapper
            get() = application?.vizbeeWrapper

        val Context.isVizbeeEnabled
            get() = vizbeeWrapper?.isVizbeeEnabled ?: false
    }
}