package tv.vizbee.screendemo.vizbee.homesso

object SignInCallbackHolder {
    private var listener: VizbeeSignInStatusListener? = null

    fun setListener(listener: VizbeeSignInStatusListener) {
        this.listener = listener
    }

    fun getListener(): VizbeeSignInStatusListener? {
        return listener
    }

    fun clearListener() {
        listener = null
    }
}