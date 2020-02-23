package id.digilabyte.nibchat.helper;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.QBWebRTCSignaling;
import com.quickblox.chat.connections.tcp.QBTcpChatConnectionFabric;
import com.quickblox.chat.connections.tcp.QBTcpConfigurationBuilder;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCConfig;

public class CallService extends Service {
    private static final String TAG = CallService.class.getSimpleName();
    private QBChatService chatService;
    private QBRTCClient rtcClient;
    private PendingIntent pendingIntent;
    private int currentCommand;
    private QBUser currentUser;

    public static void start(Context context, QBUser qbUser, PendingIntent pendingIntent) {
        Intent intent = new Intent(context, CallService.class);
        intent.putExtra(Common.EXTRA_COMMAND_TO_SERVICE, Common.COMMAND_LOGIN);
        intent.putExtra(Common.EXTRA_QB_USERS_LIST, qbUser);
        intent.putExtra(Common.EXTRA_PENDING_INTENT, pendingIntent);
        context.startService(intent);
    }

    public static void start(Context context, QBUser qbUser) {
        start(context, qbUser, null);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        createChatService();

        Log.d(TAG, "Service onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");

        parseIntentExtras(intent);

        startSuitableActions();

        return START_REDELIVER_INTENT;
    }

    private void parseIntentExtras(Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            currentCommand = intent.getIntExtra(Common.EXTRA_COMMAND_TO_SERVICE, Common.COMMAND_NOT_FOUND);
            pendingIntent = intent.getParcelableExtra(Common.EXTRA_PENDING_INTENT);
            currentUser = (QBUser) intent.getSerializableExtra(Common.EXTRA_QB_USERS_LIST);
        }
    }

    private void startSuitableActions() {
        if (currentCommand == Common.COMMAND_LOGIN) {
            startLoginToChat();
        } else if (currentCommand == Common.COMMAND_LOGOUT) {
            logout();
        }
    }

    private void createChatService() {
        if (chatService == null) {
            QBTcpConfigurationBuilder configurationBuilder = new QBTcpConfigurationBuilder();
            configurationBuilder.setSocketTimeout(0);
            QBChatService.setConnectionFabric(new QBTcpChatConnectionFabric(configurationBuilder));

            QBChatService.setDebugEnabled(true);
            chatService = QBChatService.getInstance();
        }
    }

    private void startLoginToChat() {
        if (!chatService.isLoggedIn()) {
            loginToChat(currentUser);
        } else {
            sendResultToActivity(true, null);
        }
    }

    private void loginToChat(QBUser qbUser) {
        chatService.login(qbUser, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                Log.d(TAG, "login onSuccess");
                startActionsOnSuccessLogin();
            }

            @Override
            public void onError(QBResponseException e) {
                Log.d(TAG, "login onError " + e.getMessage());
                sendResultToActivity(false, e.getMessage() != null
                        ? e.getMessage()
                        : "Login error");
            }
        });
    }

    private void startActionsOnSuccessLogin() {
//        initPingListener();
        initQBRTCClient();
        sendResultToActivity(true, null);
    }

//    private void initPingListener() {
//        ChatPingAlarmManager.onCreate(this);
//        ChatPingAlarmManager.getInstanceFor().addPingListener(new PingFailedListener() {
//            @Override
//            public void pingFailed() {
//                Log.d(TAG, "Ping chat server failed");
//            }
//        });
//    }

    private void initQBRTCClient() {
        rtcClient = QBRTCClient.getInstance(getApplicationContext());
        // Add signalling manager
        chatService.getVideoChatWebRTCSignalingManager().addSignalingManagerListener(new QBVideoChatSignalingManagerListener() {
            @Override
            public void signalingCreated(QBSignaling qbSignaling, boolean createdLocally) {
                if (!createdLocally) {
                    rtcClient.addSignaling(qbSignaling);
                }
            }
        });

        // Configure
        QBRTCConfig.setDebugEnabled(true);
//        SettingsUtil.configRTCTimers(CallService.this);

        // Add service as callback to RTCClient
//        rtcClient.addSessionCallbacksListener(WebRtcSessionManager.getInstance(this));
        rtcClient.prepareToProcessCalls();
    }

    private void sendResultToActivity(boolean isSuccess, String errorMessage) {
        if (pendingIntent != null) {
            Log.d(TAG, "sendResultToActivity()");
            try {
                Intent intent = new Intent();
//                intent.putExtra(Common.EXTRA_LOGIN_RESULT, isSuccess);
//                intent.putExtra(Consts.EXTRA_LOGIN_ERROR_MESSAGE, errorMessage);

                pendingIntent.send(CallService.this, 0, intent);
            } catch (PendingIntent.CanceledException e) {
                String errorMessageSendingResult = e.getMessage();
                Log.d(TAG, errorMessageSendingResult != null
                        ? errorMessageSendingResult
                        : "Error sending result to activity");
            }
        }
    }

    public static void logout(Context context) {
        Intent intent = new Intent(context, CallService.class);
        intent.putExtra(Common.EXTRA_COMMAND_TO_SERVICE, Common.COMMAND_LOGOUT);
        context.startService(intent);
    }

    private void logout() {
        destroyRtcClientAndChat();
    }

    private void destroyRtcClientAndChat() {
        if (rtcClient != null) {
            rtcClient.destroy();
        }
//        ChatPingAlarmManager.onDestroy();
        if (chatService != null) {
            chatService.logout(new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid, Bundle bundle) {
                    chatService.destroy();
                }

                @Override
                public void onError(QBResponseException e) {
                    Log.d(TAG, "logout onError " + e.getMessage());
                    chatService.destroy();
                }
            });
        }
        stopSelf();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service onDestroy()");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Service onBind)");
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "Service onTaskRemoved()");
        super.onTaskRemoved(rootIntent);
        destroyRtcClientAndChat();
    }
}