package id.digilabyte.nibchat.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.request.QBMessageGetBuilder;
import com.quickblox.chat.request.QBMessageUpdateBuilder;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import java.util.ArrayList;
import id.digilabyte.nibchat.R;
import id.digilabyte.nibchat.adapter.ChatMessageAdapter;
import id.digilabyte.nibchat.helper.Common;
import id.digilabyte.nibchat.holder.QBChatMessageHolder;


public class ChatMessageActivity extends AppCompatActivity implements View.OnClickListener, QBChatDialogMessageListener {

    QBChatDialog qbChatDialog;
    RecyclerView rcChatMessage;
    ImageButton submitButton;
    EditText edtContent;

    ChatMessageAdapter adapter;

    boolean isEditMode = false;
    QBChatMessage editMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_message);

        QBChatDialog qbChatDialog = (QBChatDialog)getIntent().getSerializableExtra(Common.DIALOG_EXTRA);

        String nameReceiver = "";

        if (qbChatDialog.getType() == QBDialogType.PRIVATE) {
            nameReceiver = qbChatDialog.getName();
        } else if (qbChatDialog.getType() == QBDialogType.GROUP) {
            nameReceiver = qbChatDialog.getName();
        } else if (qbChatDialog.getType() == QBDialogType.PUBLIC_GROUP) {
            nameReceiver = qbChatDialog.getName();
        }

        getSupportActionBar();
        getSupportActionBar().setTitle(nameReceiver);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        edtContent = findViewById(R.id.edt_message_chat_message);
        submitButton = findViewById(R.id.btn_sent_chat_message);

        rcChatMessage = findViewById(R.id.rc_chat_message);
        rcChatMessage.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        llm.setReverseLayout(false);
        rcChatMessage.setLayoutManager(llm);

        initChatDialogs();
        retrieveMessage();

        edtContent.setText("");
        edtContent.setFocusable(true);

        submitButton.setOnClickListener(this);
    }

    private void retrieveMessage() {
        QBMessageGetBuilder qbMessageGetBuilder = new QBMessageGetBuilder();
        qbMessageGetBuilder.setLimit(500);
        qbMessageGetBuilder.sortAsc("last_message_date_sent");

        if(qbChatDialog != null) {
            QBRestChatService.getDialogMessages(qbChatDialog, qbMessageGetBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatMessage>>() {
                @Override
                public void onSuccess(ArrayList<QBChatMessage> qbChatMessages, Bundle bundle) {
                    QBChatMessageHolder.getInstance().putMessages(qbChatDialog.getDialogId(), qbChatMessages);

                    adapter = new ChatMessageAdapter(getBaseContext(), qbChatMessages);
                    rcChatMessage.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    rcChatMessage.scrollToPosition(adapter.getItemCount()-1);
                }

                @Override
                public void onError(QBResponseException e) {

                }
            });
        }
    }

    private void initChatDialogs() {
        qbChatDialog = (QBChatDialog)getIntent().getSerializableExtra(Common.DIALOG_EXTRA);
        qbChatDialog.initForChat(QBChatService.getInstance());

        QBIncomingMessagesManager incomingMessages = QBChatService.getInstance().getIncomingMessagesManager();
        incomingMessages.addDialogMessageListener(new QBChatDialogMessageListener() {
            @Override
            public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {

            }

            @Override
            public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

            }
        });

        if (qbChatDialog.getType() == QBDialogType.GROUP || qbChatDialog.getType() == QBDialogType.PUBLIC_GROUP) {
            DiscussionHistory discussionHistory = new DiscussionHistory();
            discussionHistory.setMaxStanzas(0);

            qbChatDialog.join(discussionHistory, new QBEntityCallback() {
                @Override
                public void onSuccess(Object o, Bundle bundle) {

                }

                @Override
                public void onError(QBResponseException e) {
                    Log.e("_ERROR_GROUP_", ""+e.getMessage());
                }
            });
        }

        qbChatDialog.addMessageListener(this);
    }

    @Override
    public void onClick(View v) {
        if (!isEditMode) {
            QBChatMessage qbChatMessage = new QBChatMessage();
            qbChatMessage.setBody(edtContent.getText().toString());
            qbChatMessage.setSenderId(QBChatService.getInstance().getUser().getId());
            qbChatMessage.setSaveToHistory(true);

            try {
                qbChatDialog.sendMessage(qbChatMessage);
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }

            if (qbChatDialog.getType() == QBDialogType.PRIVATE) {
                QBChatMessageHolder.getInstance().putMessage(qbChatDialog.getDialogId(), qbChatMessage);
                ArrayList<QBChatMessage> messages = QBChatMessageHolder.getInstance().getChatMessageByDialogId(qbChatDialog.getDialogId());
                adapter = new ChatMessageAdapter(ChatMessageActivity.this, messages);
                rcChatMessage.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            rcChatMessage.scrollToPosition(adapter.getItemCount() - 1);

            edtContent.setText("");
            edtContent.requestFocus();
        } else {
            final ProgressDialog loading = new ProgressDialog(this, ProgressDialog.THEME_HOLO_LIGHT);
            loading.setMessage("Please wait ...");
            loading.setCanceledOnTouchOutside(false);
            loading.show();

            QBMessageUpdateBuilder qbMessageUpdateBuilder = new QBMessageUpdateBuilder();
            qbMessageUpdateBuilder.updateText(edtContent.getText().toString()).markDelivered().markRead();

            QBRestChatService.updateMessage(editMessage.getId(), qbChatDialog.getDialogId(), qbMessageUpdateBuilder)
                    .performAsync(new QBEntityCallback<Void>() {
                        @Override
                        public void onSuccess(Void aVoid, Bundle bundle) {
                            loading.dismiss();
                            retrieveMessage();
                            isEditMode = false;

                            edtContent.setText("");
                            edtContent.setFocusable(true);
                        }

                        @Override
                        public void onError(QBResponseException e) {
                            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_user_chat, menu);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.chat_message_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.chat_message_update_menu:
                updateMessage();
                break;
            case R.id.chat_message_delete_menu:
                deleteMessage();
                break;
        }
        return true;
    }

    private void deleteMessage() {
        final ProgressDialog loading = new ProgressDialog(this, ProgressDialog.THEME_HOLO_LIGHT);
        loading.setMessage("Please wait ...");
        loading.setCanceledOnTouchOutside(false);
        loading.show();

        QBRestChatService.deleteMessage(getEditMessage().getId(), false).performAsync(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                loading.dismiss();
                retrieveMessage();
            }

            @Override
            public void onError(QBResponseException e) {
                Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMessage() {
        edtContent.setText(getEditMessage().getBody());
        isEditMode = true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_call:
                Intent intentCall = new Intent(ChatMessageActivity.this, CallActivity.class);
                intentCall.putExtra(Common.EXTRA_QB_USERS_LIST, qbChatDialog);
                startActivity(intentCall);
                return true;
            case R.id.menu_video_call:
                Intent intentVideo = new Intent(ChatMessageActivity.this, VideoCallActivity.class);
                intentVideo.putExtra(Common.EXTRA_QB_USERS_LIST, qbChatDialog);
                startActivity(intentVideo);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        finish();
        return true;
    }

    @Override
    public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
        QBChatMessageHolder.getInstance().putMessage(qbChatMessage.getDialogId(), qbChatMessage);
        ArrayList<QBChatMessage> messages = QBChatMessageHolder.getInstance().getChatMessageByDialogId(qbChatMessage.getDialogId());
        adapter = new ChatMessageAdapter(ChatMessageActivity.this, messages);
        rcChatMessage.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        rcChatMessage.scrollToPosition(adapter.getItemCount()-1);
    }

    @Override
    public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {
        Log.e("_ERROR_MESSAGE_", ""+e.getMessage());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        qbChatDialog.removeMessageListrener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        qbChatDialog.removeMessageListrener(this);
    }

    public QBChatMessage getEditMessage() {
        return editMessage;
    }

    public void setEditMessage(QBChatMessage editMessage) {
        this.editMessage = editMessage;
    }
}
