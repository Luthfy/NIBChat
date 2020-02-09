package id.digilabyte.nibchat.adapter;

import android.content.Context;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.library.bubbleview.BubbleTextView;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import java.util.ArrayList;
import id.digilabyte.nibchat.R;
import id.digilabyte.nibchat.holder.QBChatMessageHolder;
import id.digilabyte.nibchat.holder.QBUsersHolder;
import id.digilabyte.nibchat.ui.ChatMessageActivity;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ArrayList<QBChatMessage> qbChatMessages;

    private Integer SENDER = 1;
    private Integer RECEIVER = 2;

    public ChatMessageAdapter(Context context, ArrayList<QBChatMessage> qbChatMessages) {
        this.context = context;
        this.qbChatMessages = qbChatMessages;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (viewType == SENDER) {
            view = inflater.inflate(R.layout.list_send_chat, parent, false);
            return new SenderChat(view);
        } else {
            view = inflater.inflate(R.layout.list_receive_chat, parent, false);
            return new ReceiverChat(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == SENDER) {
            ((SenderChat)holder).bindItem(qbChatMessages.get(position));
            ((SenderChat)holder).qbChatMessageData = qbChatMessages.get(position);
            ((SenderChat)holder).pos = position;
        } else {
            ((ReceiverChat)holder).bindItem(qbChatMessages.get(position));
        }

    }

    @Override
    public int getItemCount() {
        return qbChatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(qbChatMessages.get(position).getSenderId().equals(QBChatService.getInstance().getUser().getId())) {
            return SENDER;
        } else {
            return RECEIVER;
        }
    }

    private class SenderChat extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        Integer pos;
        QBChatMessage qbChatMessageData;
        BubbleTextView bubbleMessage;
        String dateSender;

        public SenderChat(View view) {
            super(view);
            bubbleMessage = view.findViewById(R.id.txt_message_send_chat);
        }

        public void bindItem(final QBChatMessage qbChatMessage) {
            this.qbChatMessageData = qbChatMessage;
            bubbleMessage.setText(qbChatMessage.getBody());
            bubbleMessage.setOnCreateContextMenuListener(this);
            bubbleMessage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ((ChatMessageActivity)v.getContext()).setEditMessage(qbChatMessageData);
                    return false;
                }
            });
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(0, R.id.chat_message_update_menu, 0, "Update");
            menu.add(0, R.id.chat_message_delete_menu, 0, "Delete");
        }
    }

    private class ReceiverChat extends RecyclerView.ViewHolder {
        TextView txtName;
        BubbleTextView bubbleMessage;
        String dateSender;
        public ReceiverChat(View view) {
            super(view);
            bubbleMessage = view.findViewById(R.id.txt_message_receive_chat);
            txtName = view.findViewById(R.id.txt_name_receive_chat);
        }

        public void bindItem(QBChatMessage qbChatMessage) {
            txtName.setText(QBUsersHolder.getInstance().getUserById(qbChatMessage.getSenderId()).getFullName());
            bubbleMessage.setText(qbChatMessage.getBody());
        }
    }


}
