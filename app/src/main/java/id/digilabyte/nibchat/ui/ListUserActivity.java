package id.digilabyte.nibchat.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.utils.DialogUtils;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;

import java.util.ArrayList;

import id.digilabyte.nibchat.R;
import id.digilabyte.nibchat.adapter.ListUserAdapter;
import id.digilabyte.nibchat.helper.Common;
import id.digilabyte.nibchat.utils.UserPreferences;

public class ListUserActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView rcUserList;
    private Button btnStartChat;

    private ArrayList<QBUser> userChats = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_user);

        getSupportActionBar();
        getSupportActionBar().setTitle("List User Registered");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rcUserList = findViewById(R.id.rc_user_list_user);
        rcUserList.setHasFixedSize(true);

        rcUserList.setLayoutManager(new LinearLayoutManager(this));

        btnStartChat = findViewById(R.id.btn_start_chat_list_user);
        btnStartChat.setOnClickListener(this);

        retrieveUser();
    }

    private void retrieveUser() {

        final ProgressDialog loading = new ProgressDialog(this, ProgressDialog.THEME_HOLO_LIGHT);
        loading.setMessage("Please wait ...");
        loading.setCanceledOnTouchOutside(false);
        loading.show();

        QBUsers.getUsers(null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {

                Log.d("ListUserActivity", "list user : "+qbUsers.toString());

                ArrayList<QBUser> qbUsersWithoutCurrent = new ArrayList<>();

                for (int i = 0; i < qbUsers.size(); i++) {

                    QBUser user = qbUsers.get(i);

                    boolean getLogin = user.getLogin().equals(QBChatService.getInstance().getUser().getLogin());

                    Log.d("ListUserActivity", "user was login : " + getLogin);

                    if (!getLogin) {
                        Log.d("ListUserActivity", "list user : " + user.toString());
                        qbUsersWithoutCurrent.add(user);
                    }
                }

                ListUserAdapter adapter = new ListUserAdapter(ListUserActivity.this, qbUsersWithoutCurrent);
                rcUserList.setAdapter(adapter);

                adapter.notifyDataSetChanged();
                loading.dismiss();
            }

            @Override
            public void onError(QBResponseException e) {
                loading.dismiss();
                Log.d("_ERROR_LIST_USER_", e.getMessage());
            }
        });
    }

    public void userChat(QBUser user) {
        if (userChats.contains(user)) {
            userChats.remove(user);
        } else {
            userChats.add(user);
        }

        if (userChats.size() > 1) {
            btnStartChat.setText("START GROUP CHAT ("+userChats.size()+")");
        } else {
            btnStartChat.setText("START TO CHAT");
        }

        Log.d("_DEBUG_LST_USR_", String.valueOf(userChats.size()));
    }

    @Override
    public void onClick(View v) {
        Integer participant = userChats.size();
        if (participant == 1 ) {
            createPrivateChat();
        } else if (participant > 1) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
            dialog.setTitle("Tentukan Nama Grub?");

            final EditText inputGroubName = new EditText(this);

            inputGroubName.setHint("Masukan Nama Grub");
            inputGroubName.setHintTextColor(Color.GRAY);
            inputGroubName.setTextColor(Color.BLACK);

            dialog.setView(inputGroubName);

            dialog.setPositiveButton("CREATE GROUP", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    createGroupChat(inputGroubName.getText().toString());
                }
            });

            dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            dialog.show();

        } else {
            Toast.makeText(this, "Select friend for start to chat", Toast.LENGTH_SHORT).show();
        }
    }

    private void createGroupChat(String groupName) {
        final ProgressDialog loading = new ProgressDialog(this, ProgressDialog.THEME_HOLO_LIGHT);
        loading.setMessage("Please wait ...");
        loading.setCanceledOnTouchOutside(false);
        loading.show();

        ArrayList<Integer> occupantIdsList = new ArrayList<>();
        for (Integer i = 0; i < userChats.size(); i++) {
            occupantIdsList.add(userChats.get(i).getId());
        }

        QBChatDialog qbChatDialog = new QBChatDialog();
        qbChatDialog.setName(Common.createChatDialogName(groupName));
        qbChatDialog.setType(QBDialogType.GROUP);
        qbChatDialog.setOccupantsIds(occupantIdsList);

        QBRestChatService.createChatDialog(qbChatDialog).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                loading.dismiss();
                Toast.makeText(ListUserActivity.this, "Chat successfully to create", Toast.LENGTH_SHORT).show();

                QBSystemMessagesManager qbSystemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
                QBChatMessage qbChatMessage = new QBChatMessage();
                qbChatMessage.setBody(qbChatDialog.getDialogId());
                for (Integer i = 0; i < qbChatDialog.getOccupants().size(); i++) {
                    qbChatMessage.setRecipientId(qbChatDialog.getOccupants().get(i));
                    try {
                        qbSystemMessagesManager.sendSystemMessage(qbChatMessage);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }
                }
                finish();
            }

            @Override
            public void onError(QBResponseException e) {
                loading.dismiss();
                Log.e("_ERROR", e.getMessage());
            }
        });

    }

    private void createPrivateChat() {
        final ProgressDialog loading = new ProgressDialog(this, ProgressDialog.THEME_HOLO_LIGHT);
        loading.setMessage("Please wait ...");
        loading.setCanceledOnTouchOutside(false);
        loading.show();

        final Integer userId = userChats.get(0).getId();
        QBChatDialog chatDialog = DialogUtils.buildPrivateDialog(userId);

        QBRestChatService.createChatDialog(chatDialog).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                loading.dismiss();
                Toast.makeText(ListUserActivity.this, "Private Chat successfully to create", Toast.LENGTH_SHORT).show();

                QBSystemMessagesManager qbSystemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
                QBChatMessage qbChatMessage = new QBChatMessage();
                qbChatMessage.setRecipientId(userId);
                qbChatMessage.setBody(qbChatDialog.getDialogId());

                try {
                    qbSystemMessagesManager.sendSystemMessage(qbChatMessage);
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent(ListUserActivity.this, ChatMessageActivity.class);
                intent.putExtra(Common.DIALOG_EXTRA, qbChatDialog);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                finish();
            }

            @Override
            public void onError(QBResponseException e) {
                loading.dismiss();
                Log.e("_ERROR", e.getMessage());
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
