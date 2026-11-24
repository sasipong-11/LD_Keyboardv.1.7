package th.or.nectec.twskeyboard;

import android.app.Application;

public class AnalyticsApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        G2PApp.init(this);
    }
}
