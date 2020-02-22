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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.BaseSession;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCMediaConfig;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionEventsCallback;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback;
import com.quickblox.videochat.webrtc.view.QBRTCSurfaceView;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

import org.webrtc.EglBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import id.digilabyte.nibchat.R;
import id.digilabyte.nibchat.fragment.PreviewCallFragment;
import id.digilabyte.nibchat.helper.Common;
import id.digilabyte.nibchat.holder.QBUsersHolder;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class CallActivity extends AppCompatActivity implements QBRTCSessionStateCallback, QBRTCClientVideoTracksCallbacks, QBRTCSessionEventsCallback {

    private static final String TAG = CallActivity.class.getSimpleName();
    public static final int REQUEST_PERMISSION_SETTING = 545;

    private Button btnCall;
    private QBRTCSurfaceView qbrtcSurfaceViewOpponent, qbrtcSurfaceViewLocal;

    private QBRTCClient qbrtcClient;
    private QBChatService qbChatService;
    private QBRTCSession qbrtcSession;
    private static QBChatDialog qbChatDialog;
    private ArrayList<QBUser> qbUsers = new ArrayList<>();
    private QBRTCTypes.QBConferenceType qbConferenceType;
    protected ArrayList<Integer> userOpponents = new ArrayList<>();
    private boolean doCallUser;
    private EglBase eglContext;
    private String type;

    String[] perms = {
            Manifest.permission.CAMERA,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_WIFI_STATE
    };

    public static void toChatForOpenCall(Context context, boolean isIncomingCall) {
        Log.d("dialog_extra", qbChatDialog.getType().toString());
        Intent intent = new Intent(context, ChatMessageActivity.class);
        intent.putExtra(Common.DIALOG_EXTRA, qbChatDialog);
        intent.putExtra(Common.EXTRA_IS_INCOMING_CALL, isIncomingCall);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        boolean isHasPermission = EasyPermissions.hasPermissions(this, perms);

        if (!isHasPermission) {
            methodRequiresTwoPermission();
        } else {
            initField();
            initQBRTCClient();
        }

        qbrtcClient.prepareToProcessCalls();

        if (doCallUser) {
            qbrtcSession.startCall(new HashMap<>());
        } else {
            Toast.makeText(this, "In Coming Call", Toast.LENGTH_SHORT).show();
        }
    }

    private void initQBRTCClient() {
        qbrtcSession.addSessionCallbacksListener(this);
        qbrtcSession.addVideoTrackCallbacksListener(this);
        qbrtcClient.addSessionCallbacksListener(this);
    }

    private void initField() {

        Objects.requireNonNull(getSupportActionBar()).hide();

        if (getIntent().getExtras() == null) {
            onBackPressed();
            finish();
        }

        btnCall                     = findViewById(R.id.button_action_call);
        qbrtcSurfaceViewOpponent    = findViewById(R.id.opponent);
        qbrtcSurfaceViewLocal       = findViewById(R.id.local);

        // catch call action
        doCallUser = getIntent().getBooleanExtra(Common.IS_STARTED_CALL, true);

        if (doCallUser) {
            btnCall.setBackgroundResource(R.mipmap.ic_call_end);
        } else {
            btnCall.setBackgroundResource(R.mipmap.ic_call_start);
        }

        // catch user
        qbChatDialog = (QBChatDialog) getIntent().getSerializableExtra(Common.EXTRA_QB_USERS_LIST);

        if (qbChatDialog != null) {

            qbUsers = QBUsersHolder.getInstance().getUserByIds(qbChatDialog.getOccupants());

            TextView txtName = findViewById(R.id.user_opponent);
            txtName.setText(qbChatDialog.getName());

            for (QBUser qbUser : qbUsers) {
                userOpponents.add(qbUser.getId());
            }

        } else {
            Toast.makeText(this, "Illegal Exception", Toast.LENGTH_SHORT).show();
            finish();
        }


        // catch type
        if (getIntent().getStringExtra(Common.QBCONFRENCE_TYPE) != null)
            type = getIntent().getStringExtra(Common.QBCONFRENCE_TYPE);

        assert type != null;
        if (type.equals("audio")) {
            qbConferenceType = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;
            Toast.makeText(this, "Panggilan Suara Sedang Dijalankan", Toast.LENGTH_SHORT).show();
        } else if (type.equals("video")) {
            qbConferenceType = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO;
            Toast.makeText(this, "Panggilan Video Sedang Dijalankan", Toast.LENGTH_SHORT).show();
        }

        qbChatService   = QBChatService.getInstance();
        qbrtcClient     = QBRTCClient.getInstance(CallActivity.this);
        qbrtcSession    = qbrtcClient.createNewSessionWithOpponents(userOpponents, qbConferenceType);

        QBRTCConfig.setDialingTimeInterval(10000);
        QBRTCConfig.setMaxOpponentsCount(Common.USER_OPPONENT_MAKS);
        QBRTCConfig.setDebugEnabled(true);

        Log.d(TAG, "Session is created : "+qbrtcSession.getSessionID());
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

    // session call back
    @Override
    public void onStateChanged(BaseSession baseSession, BaseSession.QBRTCSessionState qbrtcSessionState) {

    }

    @Override
    public void onConnectedToUser(BaseSession baseSession, Integer integer) {

    }

    @Override
    public void onDisconnectedFromUser(BaseSession baseSession, Integer integer) {

    }

    @Override
    public void onConnectionClosedForUser(BaseSession baseSession, Integer integer) {

    }

    // session video call back
    @Override
    public void onLocalVideoTrackReceive(BaseSession baseSession, QBRTCVideoTrack qbrtcVideoTrack) {

    }

    @Override
    public void onRemoteVideoTrackReceive(BaseSession baseSession, QBRTCVideoTrack qbrtcVideoTrack, Integer integer) {

    }

    // client session call back
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
//
////    private void prepareToCall() {
////        QBRTCTypes.QBConferenceType qbConferenceType = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;
////
////        QBRTCSession session = QBRTCClient.getInstance(this).createNewSessionWithOpponents(qbUsers, qbConferenceType);
////
////        session.startCall(userInfo);
////    }
//
//    private void initQBRTCClient() {
//        rtcClient = QBRTCClient.getInstance(this);
//        QBChatService.getInstance().getVideoChatWebRTCSignalingManager().addSignalingManagerListener(this);
//
//        QBRTCConfig.setMaxOpponentsCount(QBRTCConfig.getMaxOpponentsCount());
//        setSettingForMultiCall();
//        QBRTCConfig.setDebugEnabled(true);
//
//        rtcClient.addSessionCallbacksListener(this);
//        rtcClient.prepareToProcessCalls();
//    }
//
//    private void setSettingForMultiCall() {
//        if (qbUsers.size() == 2) {
//            QBRTCMediaConfig.setVideoWidth(QBRTCMediaConfig.VideoQuality.VGA_VIDEO.width);
//            QBRTCMediaConfig.setVideoHeight(QBRTCMediaConfig.VideoQuality.VGA_VIDEO.height);
//        } else {
//            QBRTCMediaConfig.setVideoWidth(QBRTCMediaConfig.VideoQuality.QBGA_VIDEO.width);
//            QBRTCMediaConfig.setVideoHeight(QBRTCMediaConfig.VideoQuality.QBGA_VIDEO.height);
//            QBRTCMediaConfig.setVideoHWAcceleration(false);
//        }
//    }
//
//    private void initField() {
//        QBChatDialog qbChatDialog = (QBChatDialog) getIntent().getSerializableExtra(Common.EXTRA_QB_USERS_LIST);
//        qbUsers = QBUsersHolder.getInstance().getUserByIds(qbChatDialog.getOccupants());
//    }
//

//
//    @Override
//    public void signalingCreated(QBSignaling qbSignaling, boolean b) {
//        if (!b) {
//            rtcClient.addSignaling((QBSignaling) qbSignaling);
//        }
//    }
//
//    @Override
//    public void onUserNotAnswer(QBRTCSession qbrtcSession, Integer integer) {
//
//    }
//
//    @Override
//    public void onCallRejectByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
//
//    }
//
//    @Override
//    public void onCallAcceptByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
//
//    }
//
//    @Override
//    public void onReceiveHangUpFromUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
//
//    }
//
//    @Override
//    public void onSessionClosed(QBRTCSession qbrtcSession) {
//
//    }
//}
