package com.example.app.vizbee.applifecycle

import com.example.app.ui.app.AppViewModel
import com.example.app.vizbee.video.deeplink.MyVizbeeDeeplinkManager

/**
 * #VizbeeGuide Create an instance of this class by passing the objects necessary to handle deep link and sign in requests.
 *
 * This class encapsulates all the objects necessary to (1) handle deep link (2) handle sign in
 * requests. The object of this class is eligible for garbage collection as soon as the activity
 * is destroyed.
 *
 * @property appViewModel this is a placeholder object. Replace this with the required object(s).
 */
class AppReadyModel(val appViewModel: AppViewModel) {
    val deeplinkManager = MyVizbeeDeeplinkManager(this)
}