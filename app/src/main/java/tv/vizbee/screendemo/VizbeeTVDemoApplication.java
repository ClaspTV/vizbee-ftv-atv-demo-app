package tv.vizbee.screendemo;

import android.app.Application;

import tv.vizbee.screendemo.vizbee.VizbeeWrapper;

public class VizbeeTVDemoApplication extends Application {

    public VizbeeWrapper vizbeeWrapper;

    @Override
    public void onCreate() {
        super.onCreate();

        // ---------------------------
        // Begin SDK Integration
        // ---------------------------
        vizbeeWrapper = new VizbeeWrapper();
        vizbeeWrapper.initialize(this);
        // ---------------------------
        // End SDK Integration
        // ---------------------------

    }
}
