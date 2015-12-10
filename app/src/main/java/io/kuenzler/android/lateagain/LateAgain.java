package io.kuenzler.android.lateagain;

import android.app.Application;
import android.os.Bundle;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

/**
 * Created by Leonhard on 08.12.2015.
 */
@ReportsCrashes(mailTo = "leonhard.kuenzler@gmail.com", // my email here
        mode = ReportingInteractionMode.NOTIFICATION)
        //resToastText = R.string.crash_toast_text)
public class LateAgain extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
    }
}
