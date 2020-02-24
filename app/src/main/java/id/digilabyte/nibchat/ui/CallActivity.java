package id.digilabyte.nibchat.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.auth.session.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.AppRTCAudioManager;
import com.quickblox.videochat.webrtc.BaseSession;
import com.quickblox.videochat.webrtc.QBRTCAudioTrack;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.QBSignalingSpec;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientAudioTracksCallback;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionEventsCallback;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSignalingCallback;
import com.quickblox.videochat.webrtc.exception.QBRTCSignalException;
import com.quickblox.videochat.webrtc.view.QBRTCSurfaceView;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

import org.webrtc.EglBase;
import org.webrtc.VideoRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import id.digilabyte.nibchat.R;
import id.digilabyte.nibchat.helper.Common;
import id.digilabyte.nibchat.holder.QBUsersHolder;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class CallActivity extends AppCompatActivity implements QBRTCSessionStateCallback, QBRTCClientVideoTracksCallbacks, QBRTCSessionEventsCallback, QBRTCClientSessionCallbacks, QBRTCClientAudioTracksCallback {

    private static final String TAG = CallActivity.class.getSimpleName();
    public static final int REQUEST_PERMISSION_SETTING = 545;

    private Button btnCall;
    private QBRTCSurfaceView qbrtcSurfaceViewOpponent, qbrtcSurfaceViewLocal;

    private QBRTCClient qbrtcClient;
    private QBChatService qbChatService;
    private QBRTCSession qbrtcSession;
    private QBChatDialog qbChatDialog;
    private ArrayList<QBUser> qbUsers = new ArrayList<>();
    private QBRTCTypes.QBConferenceType qbConferenceType;
    protected ArrayList<Integer> userOpponents = new ArrayList<>();
    private boolean doCallUser;
    private EglBase eglContext;
    private String type;
    private AppRTCAudioManager audioManager;
    private boolean previousDeviceEarPiece;

    String[] perms = {
            Manifest.permission.CAMERA,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_WIFI_STATE
    };

    private boolean isVideoCall;
    private boolean headsetPlugged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        if (getIntent().getExtras() == null) {
            onBackPressed();
            finish();
        }

        initField();

    }

    private void initField() {

        Objects.requireNonNull(getSupportActionBar()).hide();

        btnCall                     = findViewById(R.id.button_action_call);
        qbrtcSurfaceViewOpponent    = findViewById(R.id.opponent);
        qbrtcSurfaceViewLocal       = findViewById(R.id.local);

        boolean isHasPermission = EasyPermissions.hasPermissions(this, perms);

        if (!isHasPermission) {
            methodRequiresTwoPermission();
        }

        qbChatService   = QBChatService.getInstance();
        qbrtcClient     = QBRTCClient.getInstance(CallActivity.this);
        eglContext      = qbrtcClient.getEglContext();

        // catch call action
        doCallUser = getIntent().getBooleanExtra(Common.IS_STARTED_CALL, true);

        if (doCallUser) {
            btnCall.setBackgroundResource(R.mipmap.ic_call_end);
            startCall();
        } else {
            btnCall.setBackgroundResource(R.mipmap.ic_call_start);
            openCall();
        }

        QBRTCConfig.setDialingTimeInterval(10000);
        QBRTCConfig.setMaxOpponentsCount(Common.USER_OPPONENT_MAKS);
        QBRTCConfig.setDebugEnabled(true);

        Log.d(TAG, "Init Field Activity, Do Call User : "+doCallUser);

        initAudioManager();
    }

    private void initAudioManager() {
        audioManager = AppRTCAudioManager.create(CallActivity.this, new AppRTCAudioManager.OnAudioManagerStateListener() {
            @Override
            public void onAudioChangedState(AppRTCAudioManager.AudioDevice audioDevice) {
                if (audioManager.getSelectedAudioDevice() == AppRTCAudioManager.AudioDevice.EARPIECE) {
                    previousDeviceEarPiece = true;
                } else if (audioManager.getSelectedAudioDevice() == AppRTCAudioManager.AudioDevice.SPEAKER_PHONE) {
                    previousDeviceEarPiece = false;
                }

            }
        });

        isVideoCall = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO.equals(qbConferenceType);
        if (isVideoCall) {
            audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
            Log.d(TAG, "AppRTCAudioManager.AudioDevice.SPEAKER_PHONE");
        } else {
            audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
            previousDeviceEarPiece = true;
            Log.d(TAG, "AppRTCAudioManager.AudioDevice.EARPIECE");
        }

        audioManager.setOnWiredHeadsetStateListener(new AppRTCAudioManager.OnWiredHeadsetStateListener() {
            @Override
            public void onWiredHeadsetStateChanged(boolean plugged, boolean hasMicrophone) {
                Log.d(TAG, "Plugged "+plugged+" Microphone "+hasMicrophone);
                headsetPlugged = plugged;
            }
        });

        audioManager.init();
    }

    private void openCall() {

        if (getIntent().getExtras() == null) {
            Toast.makeText(CallActivity.this, "Illegal Exception", Toast.LENGTH_SHORT).show();
        }

        String sessionId = getIntent().getStringExtra(Common.SESSION_CHAT);

        qbrtcSession        = qbrtcClient.getSession(sessionId);
        qbConferenceType    = qbrtcSession.getConferenceType();

        qbChatService.getVideoChatWebRTCSignalingManager().addSignalingManagerListener(new QBVideoChatSignalingManagerListener() {
            @Override
            public void signalingCreated(QBSignaling qbSignaling, boolean b) {
                Log.d(TAG, "signaling is created : "+b);
                if (!b) {
                    qbrtcClient.addSignaling(qbSignaling);
                }
            }
        });

        TextView username = findViewById(R.id.user_opponent);
        username.setText(QBUsersHolder.getInstance().getUserById(qbrtcSession.getCallerID()).getFullName());

        qbrtcClient.prepareToProcessCalls();

        qbrtcSession.addSessionCallbacksListener(this);
        qbrtcSession.addVideoTrackCallbacksListener(this);

        qbrtcClient.addSessionCallbacksListener(this);

//        qbrtcSurfaceViewOpponent.init(eglContext.getEglBaseContext(), null);
//        qbrtcSurfaceViewLocal.init(eglContext.getEglBaseContext(), null);


        btnCall.setOnClickListener(v -> {
            Log.d(TAG, "button Accept is clicked");
            qbrtcSession.acceptCall(new HashMap<>());
        });
    }

    private void startCall() {

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

        qbrtcSession    = qbrtcClient.createNewSessionWithOpponents(userOpponents, qbConferenceType);

        qbrtcSession.addSessionCallbacksListener(this);
        qbrtcSession.addVideoTrackCallbacksListener(this);

        qbrtcSession.startCall(new HashMap<>());


        qbrtcClient.addSessionCallbacksListener(this);

        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qbrtcSession.rejectCall(new HashMap<>());

                qbrtcSession.removeSignalingCallback(new QBRTCSignalingCallback() {
                    @Override
                    public void onSuccessSendingPacket(QBSignalingSpec.QBSignalCMD qbSignalCMD, Integer integer) {
                        Log.d(TAG, "onSuccessSendingPacket "+qbSignalCMD.getValue());
                    }

                    @Override
                    public void onErrorSendingPacket(QBSignalingSpec.QBSignalCMD qbSignalCMD, Integer integer, QBRTCSignalException e) {
                        Log.d(TAG, "onErrorSendingPacket "+qbSignalCMD.getValue());
                    }
                });

                onReleaseEgl();

                qbrtcSession.removeVideoTrackCallbacksListener(CallActivity.this);
                qbrtcSession.removeAudioTrackCallbacksListener(CallActivity.this);

                qbrtcClient.destroy();

                startActivity(new Intent(CallActivity.this, ChatDialogActivity.class));
                finish();
            }
        });

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

    // session state call back
    @Override
    public void onStateChanged(BaseSession baseSession, BaseSession.QBRTCSessionState qbrtcSessionState) {
        Log.d(TAG, "output from onStateChanged : "+baseSession.getSessionID());
    }

    @Override
    public void onConnectedToUser(BaseSession baseSession, Integer integer) {
        Log.d(TAG, "output from onConnectedToUser : "+baseSession.getSessionID());

        Toast.makeText(CallActivity.this, "Panggilan diterima", Toast.LENGTH_SHORT).show();

        if (!doCallUser) {
            btnCall.setBackgroundResource(R.mipmap.ic_call_end);
            btnCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onReleaseEgl();
                    qbrtcSession.removeVideoTrackCallbacksListener(CallActivity.this);
                    qbrtcSession.removeAudioTrackCallbacksListener(CallActivity.this);
                    qbrtcClient.destroy();
                    qbrtcSession.rejectCall(new HashMap<>());
                    onBackPressed();
                }
            });
        }
    }

    @Override
    public void onDisconnectedFromUser(BaseSession baseSession, Integer integer) {
        Log.d(TAG, "output from onDisconnectedFromUser : "+baseSession.getSessionID());

        Toast.makeText(CallActivity.this, "Panggilan dihentikan", Toast.LENGTH_SHORT).show();

        onBackPressed();
    }

    @Override
    public void onConnectionClosedForUser(BaseSession baseSession, Integer integer) {
        Log.d(TAG, "output from onConnectionClosedForUser : "+baseSession.getSessionID());

        Toast.makeText(CallActivity.this, "Panggilan dihentikan", Toast.LENGTH_SHORT).show();

        qbrtcSurfaceViewOpponent.release();
        qbrtcSurfaceViewLocal.release();
    }

    // session video call back
    @Override
    public void onLocalVideoTrackReceive(BaseSession baseSession, QBRTCVideoTrack qbrtcVideoTrack) {
        Log.d(TAG, "output from onLocalVideoTrackReceive : "+qbrtcVideoTrack.toString());

        QBRTCSession session = qbrtcClient.getSession(baseSession.getSessionID());

        Log.d(TAG, "onLocalVideoTrack "+QBUsersHolder.getInstance().getUserById(session.getCallerID()).getFullName());

        fillVideoView(session.getCallerID(), qbrtcSurfaceViewLocal, qbrtcVideoTrack);
    }

    @Override
    public void onRemoteVideoTrackReceive(BaseSession baseSession, QBRTCVideoTrack qbrtcVideoTrack, Integer integer) {
        Log.d(TAG, "output from onRemoteVideoTrackReceive : "+qbrtcVideoTrack.toString());

        QBRTCSession session = qbrtcClient.getSession(baseSession.getSessionID());

        for (int userId : session.getOpponents()) {
            Log.d(TAG, "onRemoteVideoTrackReceive : "+QBUsersHolder.getInstance().getUserById(userId).getFullName());
        }

        Log.d(TAG, "onRemoteVideoTrackReceive end : "+QBUsersHolder.getInstance().getUserById(session.getOpponents().get(0)).getFullName());

        fillVideoView(session.getOpponents().get(0), qbrtcSurfaceViewOpponent, qbrtcVideoTrack);
    }

    // session call back
    @Override
    public void onUserNotAnswer(QBRTCSession qbrtcSession, Integer integer) {
        Log.d(TAG, "output from onUserNotAnswer : "+qbrtcSession.getSessionID());

    }

    @Override
    public void onCallRejectByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        Log.d(TAG, "output from onCallRejectByUser : "+qbrtcSession.getSessionID());

        Toast.makeText(CallActivity.this, "Panggilan telah ditolak oleh ", Toast.LENGTH_SHORT).show();

        onBackPressed();
    }

    @Override
    public void onCallAcceptByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        Log.d(TAG, "output from onCallAcceptByUser : "+qbrtcSession.getSessionID());

        Toast.makeText(CallActivity.this, "Panggilan telah diterima oleh ", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onReceiveHangUpFromUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        Log.d(TAG, "output from onReceiveHangUpFromUser : "+qbrtcSession.getSessionID());

        Toast.makeText(CallActivity.this, "Panggilan telah diakhiri oleh ", Toast.LENGTH_SHORT).show();

        onBackPressed();
    }

    @Override
    public void onSessionClosed(QBRTCSession qbrtcSession) {
        Log.d(TAG, "output from onSessionClosed : "+qbrtcSession.getSessionID());
        onBackPressed();
    }

    // client
    @Override
    public void onReceiveNewSession(QBRTCSession session) {

        Log.d(TAG, "onReceiveNewSession : " + session.getSessionID());

        qbrtcSession = session;
    }

    @Override
    public void onUserNoActions(QBRTCSession qbrtcSession, Integer integer) {
        Log.d(TAG, "onUserNoActions : "+qbrtcSession.getSessionID());
    }

    @Override
    public void onSessionStartClose(QBRTCSession qbrtcSession) {
        Log.d(TAG, "onSessionStartClose : "+qbrtcSession.getSessionID());
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(CallActivity.this, ChatDialogActivity.class));
        finish();
    }

    private void fillVideoView(int userId, QBRTCSurfaceView videoView, QBRTCVideoTrack videoTrack) {
        videoTrack.addRenderer(new VideoRenderer(videoView));
    }

    private void onReleaseEgl() {
        qbrtcSurfaceViewOpponent.release();
        qbrtcSurfaceViewLocal.release();
    }

    @Override
    public void onLocalAudioTrackReceive(BaseSession baseSession, QBRTCAudioTrack qbrtcAudioTrack) {

    }

    @Override
    public void onRemoteAudioTrackReceive(BaseSession baseSession, QBRTCAudioTrack qbrtcAudioTrack, Integer integer) {

    }
}