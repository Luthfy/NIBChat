package id.digilabyte.nibchat.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telecom.InCallService;
import android.util.Log;
import android.widget.Toast;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCMediaConfig;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionEventsCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import id.digilabyte.nibchat.R;
import id.digilabyte.nibchat.fragment.PreviewCallFragment;
import id.digilabyte.nibchat.helper.Common;
import id.digilabyte.nibchat.holder.QBUsersHolder;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class CallActivity extends AppCompatActivity implements QBVideoChatSignalingManagerListener, QBRTCSessionEventsCallback {

    private static final String TAG = CallActivity.class.getSimpleName();
    public static final String INCOME_CALL_FRAGMENT = "income_call_fragment";
    public static final int REQUEST_PERMISSION_SETTING = 545;

    private ArrayList<QBUser> qbUsers;
    private QBRTCClient rtcClient = null;

    private InCallService callService;

    String[] perms = {
            Manifest.permission.CAMERA,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_WIFI_STATE
    };

    public static void start(Context context, boolean isIncomingCall) {
        Intent intent = new Intent(context, CallActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Common.EXTRA_IS_INCOMING_CALL, isIncomingCall);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        Boolean isHasPermission = EasyPermissions.hasPermissions(this, perms);

        if (!isHasPermission) {
            methodRequiresTwoPermission();
        } else {
            initField();
            initQBRTCClient();
            prepareToCall();
        }
    }

    private void prepareToCall() {
        QBRTCTypes.QBConferenceType qbConferenceType = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("key", "value");
        QBRTCSession session = QBRTCClient.getInstance(this).createNewSessionWithOpponents(qbUsers, qbConferenceType);

        session.startCall(userInfo);
    }

    private void initQBRTCClient() {
        rtcClient = QBRTCClient.getInstance(this);
        QBChatService.getInstance().getVideoChatWebRTCSignalingManager().addSignalingManagerListener(this);

        QBRTCConfig.setMaxOpponentsCount(QBRTCConfig.getMaxOpponentsCount());
        setSettingForMultiCall();
        QBRTCConfig.setDebugEnabled(true);

        rtcClient.addSessionCallbacksListener(this);
        rtcClient.prepareToProcessCalls();
    }

    private void setSettingForMultiCall() {
        if (qbUsers.size() == 2) {
            QBRTCMediaConfig.setVideoWidth(QBRTCMediaConfig.VideoQuality.VGA_VIDEO.width);
            QBRTCMediaConfig.setVideoHeight(QBRTCMediaConfig.VideoQuality.VGA_VIDEO.height);
        } else {
            QBRTCMediaConfig.setVideoWidth(QBRTCMediaConfig.VideoQuality.QBGA_VIDEO.width);
            QBRTCMediaConfig.setVideoHeight(QBRTCMediaConfig.VideoQuality.QBGA_VIDEO.height);
            QBRTCMediaConfig.setVideoHWAcceleration(false);
        }
    }

    private void initField() {
        QBChatDialog qbChatDialog = (QBChatDialog) getIntent().getSerializableExtra(Common.EXTRA_QB_USERS_LIST);
        qbUsers = QBUsersHolder.getInstance().getUserByIds(qbChatDialog.getOccupants());
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_SETTING)
    private void methodRequiresTwoPermission() {
        if (EasyPermissions.hasPermissions(this, perms)) {
            Toast.makeText(this, "Izin didapatkan", Toast.LENGTH_SHORT).show();
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "Izin diperlukan", REQUEST_PERMISSION_SETTING, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void signalingCreated(QBSignaling qbSignaling, boolean b) {
        if (!b) {
            rtcClient.addSignaling((QBSignaling) qbSignaling);
        }
    }

    @Override
    public void onUserNotAnswer(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onCallRejectByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {

    }

    @Override
    public void onCallAcceptByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {

    }

    @Override
    public void onReceiveHangUpFromUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {

    }

    @Override
    public void onSessionClosed(QBRTCSession qbrtcSession) {

    }
}
