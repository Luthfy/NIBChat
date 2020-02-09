package id.digilabyte.nibchat.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.quickblox.chat.model.QBChatDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import id.digilabyte.nibchat.R;
import id.digilabyte.nibchat.helper.Common;
import id.digilabyte.nibchat.holder.QBUnreadMessageHolder;
import id.digilabyte.nibchat.ui.ChatMessageActivity;

public class ChatDialogAdapter extends RecyclerView.Adapter<ChatDialogAdapter.ViewHolder> {

    private Context context;
    private ArrayList<QBChatDialog> qbChatDialogs;

    public ChatDialogAdapter(Context context, ArrayList<QBChatDialog> qbChatDialogs) {
        this.context = context;
        this.qbChatDialogs = qbChatDialogs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_chat_dialog, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindItem(qbChatDialogs.get(position));
    }

    @Override
    public int getItemCount() {
        return qbChatDialogs.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgChatIcon, imgUnread;
        TextView txtTitleName, txtShortMessage, txtDateLastMessage;
        LinearLayout llChatDialog;
        Integer position;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            llChatDialog        = itemView.findViewById(R.id.ll_chat_message_dialog);
            imgChatIcon         = itemView.findViewById(R.id.img_user_chat_dialog);
            imgUnread           = itemView.findViewById(R.id.img_unread_chat_dialog);
            txtTitleName        = itemView.findViewById(R.id.txt_name_chat_dialog);
            txtShortMessage     = itemView.findViewById(R.id.txt_short_message_chat_dialog);
            txtDateLastMessage  = itemView.findViewById(R.id.txt_time_chat_dialog);
        }

        public void bindItem(final QBChatDialog qbChatDialog) {

            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            String date;

            if (String.valueOf(qbChatDialog.getLastMessageDateSent()).isEmpty()) {
                date = format.format(qbChatDialog.getLastMessageDateSent());
            } else {
                date = format.format(qbChatDialog.getCreatedAt());
            }

            ColorGenerator generator = ColorGenerator.MATERIAL;
            int randomColor = generator.getRandomColor();

            TextDrawable.IBuilder builder = TextDrawable.builder().beginConfig()
                    .withBorder(4)
                    .endConfig()
                    .round();

            TextDrawable drawable = builder.build(qbChatDialog.getName().substring(0,1).toUpperCase(), randomColor);

            TextDrawable.IBuilder unread = TextDrawable.builder().beginConfig()
                    .withBorder(4)
                    .endConfig()
                    .round();

            Integer unReadCount = QBUnreadMessageHolder.getInstance().getBundle().getInt(qbChatDialog.getDialogId());

            if (unReadCount > 0) {
                TextDrawable text_drawable = unread.build(unReadCount.toString(), Color.RED);
                imgUnread.setImageDrawable(text_drawable);
            }

            imgChatIcon.setImageDrawable(drawable);
            txtTitleName.setText(qbChatDialog.getName());
            txtShortMessage.setText(qbChatDialog.getLastMessage());
            txtDateLastMessage.setText(date);

            llChatDialog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ChatMessageActivity.class);
                    intent.putExtra(Common.DIALOG_EXTRA, qbChatDialog);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            });
        }

    }
}
