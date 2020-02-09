package id.digilabyte.nibchat.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.BaseService;
import com.quickblox.auth.session.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBSystemMessageListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import id.digilabyte.nibchat.R;
import id.digilabyte.nibchat.adapter.ChatDialogAdapter;
import id.digilabyte.nibchat.holder.QBChatDialogHolder;
import id.digilabyte.nibchat.holder.QBUnreadMessageHolder;
import id.digilabyte.nibchat.holder.QBUsersHolder;
import id.digilabyte.nibchat.utils.UserPreferences;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class ChatDialogActivity extends AppCompatActivity implements View.OnClickListener, QBSystemMessageListener, QBChatDialogMessageListener {

    FloatingActionButton floatingActionButton;
    RecyclerView rcChatDialog;
    UserPreferences up;

    private static final int ALL_PERMISSONS = 777;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_dialog);

        getSupportActionBar();
        getSupportActionBar().setTitle("Chat Dialogs");

        rcChatDialog = findViewById(R.id.rc_chat_dialog);
        rcChatDialog.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rcChatDialog.setLayoutManager(layoutManager);

        floatingActionButton = findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(this);

        methodRequiresTwoPermission();

        createSessionForChat();
        loadChatDialogs();

    }

    private void loadChatDialogs() {
        QBRequestGetBuilder qbRequestBuilder = new QBRequestGetBuilder();
        qbRequestBuilder.setLimit(100);
        qbRequestBuilder.sortDesc("last_message_date_sent");

        QBRestChatService.getChatDialogs(null, qbRequestBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(final ArrayList<QBChatDialog> qbChatDialogs, Bundle bundle) {
                QBChatDialogHolder.getInstance().putDialogs(qbChatDialogs);

                Set<String> setIds = new HashSet<>();
                for (QBChatDialog chatDialog : qbChatDialogs) {
                    setIds.add(chatDialog.getDialogId());
                }

                QBRestChatService.getTotalUnreadMessagesCount(setIds, QBUnreadMessageHolder.getInstance().getBundle()).performAsync(new QBEntityCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer integer, Bundle bundle) {
                        QBUnreadMessageHolder.getInstance().setBundle(bundle);

                        ChatDialogAdapter adapter = new ChatDialogAdapter(getBaseContext(), qbChatDialogs);
                        rcChatDialog.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(QBResponseException e) {

                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    private void createSessionForChat() {
        final ProgressDialog loading = new ProgressDialog(this, ProgressDialog.THEME_HOLO_LIGHT);
        loading.setMessage("Please wait ...");
        loading.setCanceledOnTouchOutside(false);
        loading.show();

        up = new UserPreferences(this);

        String user, password;

        user = up.getUser();
        password = up.getPass();

        Log.d("_chat_dialog_", user+" "+password);

        QBUsers.getUsers(null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> users, Bundle bundle) {
                QBUsersHolder.getInstance().putUsers(users);
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("_ERROR_GETUSER", e.getMessage());
            }
        });

        final QBUser qbUser = new QBUser();
        qbUser.setLogin(user);
        qbUser.setPassword(password);

        QBAuth.createSession(qbUser).performAsync(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                qbUser.setId(qbSession.getUserId());
                try {
                    qbUser.setPassword(BaseService.getBaseService().getToken());
                } catch (BaseServiceException e) {
                    e.printStackTrace();
                }

                QBChatService.getInstance().login(qbUser, new QBEntityCallback() {
                    @Override
                    public void onSuccess(Object o, Bundle bundle) {
                        loading.dismiss();

                        QBSystemMessagesManager qbSystemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
                        qbSystemMessagesManager.addSystemMessageListener(ChatDialogActivity.this);

                        QBIncomingMessagesManager qbIncomingMessagesManager = QBChatService.getInstance().getIncomingMessagesManager();
                        qbIncomingMessagesManager.addDialogMessageListener(ChatDialogActivity.this);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        loading.dismiss();
                        Log.e("_ERROR_LOGIN", e.getMessage());
                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {
                loading.dismiss();
                Log.e("_ERROR_SESSION", e.getMessage());
            }
        });
    }

    @Override
    public void onClick(View v) {
        startActivity(new Intent(ChatDialogActivity.this, ListUserActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChatDialogs();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_logout:
                logoutMethod();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logoutMethod() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("Logout");
        dialog.setMessage("Are you sure?");

        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                try {
                    QBChatService.getInstance().logout();
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }

                QBChatService.getInstance().destroy();
                up.clear();

                startActivity(new Intent(ChatDialogActivity.this, LoginActivity.class));
                finish();
            }
        });

        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void processMessage(QBChatMessage qbChatMessage) {
        QBRestChatService.getChatDialogById(qbChatMessage.getBody()).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                QBChatDialogHolder.getInstance().putDialog(qbChatDialog);
                ArrayList<QBChatDialog> adapterSource = QBChatDialogHolder.getInstance().getAllChatDialogs();
                ChatDialogAdapter adapter = new ChatDialogAdapter(getBaseContext(), adapterSource);
                rcChatDialog.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    @Override
    public void processError(QBChatException e, QBChatMessage qbChatMessage) {
        Log.d("_ERROR_", ""+e.getMessage());
    }

    @Override
    public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
        loadChatDialogs();
    }

    @Override
    public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

    }

    @AfterPermissionGranted(ALL_PERMISSONS)
    private void methodRequiresTwoPermission() {
        String[] perms = {
                Manifest.permission.CAMERA,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_WIFI_STATE
        };

        if (EasyPermissions.hasPermissions(this, perms)) {
            Toast.makeText(this, "Izin didapatkan", Toast.LENGTH_SHORT).show();
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "Izin diperlukan", ALL_PERMISSONS, perms);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}