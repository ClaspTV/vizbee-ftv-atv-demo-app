package tv.vizbee.screendemo;

import android.app.Application;

import tv.vizbee.screendemo.vizbee.VizbeeWrapper;

public class VizbeeTVDemoApplication extends Application {

    public VizbeeWrapper vizbeeWrapper;

    @Override
    public void onCreate() {
        super.onCreate();

        // ---------------------------
        // [BEGIN] Vizbee Integration
        // ---------------------------
        vizbeeWrapper = new VizbeeWrapper();
        vizbeeWrapper.initialize(this);
        // ---------------------------
        // [END] Vizbee Integration
        // ---------------------------

    }
}
