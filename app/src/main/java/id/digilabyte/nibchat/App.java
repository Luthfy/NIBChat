package id.digilabyte.nibchat;

import android.app.Application;

import com.quickblox.auth.session.QBSettings;
import com.quickblox.chat.QBChatService;

public class App extends Application {

    private final String APP_ID      = "80077";
    private final String AUTH_KEY    = "dBKvzaeXmck24Jq";
    private final String AUTH_SECRET = "TMvfP-QJTfxK5Rj";
    private final String ACCOUNT_KEY = "J3XxssfXpDgh3Q5TS73f";

    @Override
    public void onCreate() {
        super.onCreate();
        initializeFramework();
    }

    private void initializeFramework() {
        QBChatService.setDebugEnabled(true);
        QBChatService.setDefaultPacketReplyTimeout(10000);
        
        QBSettings.getInstance().init(getApplicationContext(), APP_ID, AUTH_KEY, AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(ACCOUNT_KEY);
    }
}
