package com.example.app.vizbee.homesso

/**
 * #VizbeeGuide Do not modify this file.
 *
 * This interface is a listener to know success or failure of a sign in.
 */
interface VizbeeSignInStatusListener {

    /**
     * Called when a registration code is generated. Invoke this method only if your sign in requires a reg code to be authenticated.
     */
    fun onProgress(signInType: String, regCode: String? = null)

    /**
     * Called when the sign in is successful for the given sign in type
     * @param signInType sign in type that app decides internally and uses consistently. eg., "MVPD"
     */
    fun onSuccess(signInType : String)

    /**
     * Called when the sign in fails for the given sign in type
     * @param signInType sign in type that app decides internally and uses consistently. eg., "MVPD"
     * @param isCancelled true if it's explicitly cancelled by the user action, false if it fails for any other reason
     */
    fun onFailure(signInType : String, isCancelled: Boolean)
}