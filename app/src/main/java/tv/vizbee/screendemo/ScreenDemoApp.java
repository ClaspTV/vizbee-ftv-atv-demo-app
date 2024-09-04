package tv.vizbee.screendemo;

import android.app.Application;

import tv.vizbee.screendemo.vizbee.VizbeeWrapper;

public class ScreenDemoApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // ---------------------------
        // Begin SDK Integration
        // ---------------------------
        VizbeeWrapper.INSTANCE.initialize(this);
        // ---------------------------
        // End SDK Integration
        // ---------------------------

    }
}
