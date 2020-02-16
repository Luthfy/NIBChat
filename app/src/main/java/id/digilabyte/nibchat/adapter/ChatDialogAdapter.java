package id.digilabyte.nibchat.adapter;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.quickblox.chat.model.QBChatDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import id.digilabyte.nibchat.R;
import id.digilabyte.nibchat.helper.Common;
import id.digilabyte.nibchat.holder.QBUnreadMessageHolder;
import id.digilabyte.nibchat.holder.QBUsersHolder;
import id.digilabyte.nibchat.ui.ChatDialogActivity;
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


    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgChatIcon, imgUnread;
        TextView txtTitleName, txtShortMessage, txtDateLastMessage;
        LinearLayout llChatDialog;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            llChatDialog        = itemView.findViewById(R.id.ll_chat_message_dialog);
            imgChatIcon         = itemView.findViewById(R.id.img_user_chat_dialog);
            imgUnread           = itemView.findViewById(R.id.img_unread_chat_dialog);
            txtTitleName        = itemView.findViewById(R.id.txt_name_chat_dialog);
            txtShortMessage     = itemView.findViewById(R.id.txt_short_message_chat_dialog);
            txtDateLastMessage  = itemView.findViewById(R.id.txt_time_chat_dialog);
        }

        void bindItem(final QBChatDialog qbChatDialog) {

            @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");
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

            int unReadCount = QBUnreadMessageHolder.getInstance().getBundle().getInt(qbChatDialog.getDialogId());

            if (unReadCount > 0) {
                TextDrawable text_drawable = unread.build(Integer.toString(unReadCount), Color.RED);
                imgUnread.setImageDrawable(text_drawable);

                Integer unReadId = QBUsersHolder.getInstance().getUserById(qbChatDialog.getUserId()).getId();
                String unReadName = qbChatDialog.getName();
                String unReadContent = Common.createChatDialogName(qbChatDialog.getLastMessage());

                showNotifications(unReadId, unReadName, unReadContent);
            }

            imgChatIcon.setImageDrawable(drawable);
            txtTitleName.setText(qbChatDialog.getName());
            txtShortMessage.setText(qbChatDialog.getLastMessage());
            txtDateLastMessage.setText(date);

            llChatDialog.setOnClickListener(v -> {
                Intent intent = new Intent(context, ChatMessageActivity.class);
                intent.putExtra(Common.DIALOG_EXTRA, qbChatDialog);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            });
        }

        private void showNotifications(Integer unReadId, String unReadName, String unReadContent) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent intent = new Intent(context, ChatDialogActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

                int importance = NotificationManager.IMPORTANCE_DEFAULT;

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, unReadName)
                    .setSmallIcon(R.drawable.logo)
                    .setContentTitle(unReadName)
                    .setContentText(unReadContent)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setOngoing(false)
                    .setAutoCancel(true);

                NotificationChannel notificationChannel = new NotificationChannel(unReadName, unReadContent, importance);
                notificationChannel.enableLights(true);
                notificationChannel.enableVibration(true);
                notificationChannel.setLightColor(Color.GREEN);
                notificationChannel.setVibrationPattern(new long[] {500, 500, 500});
                notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);


                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(notificationChannel);
                }

                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
                notificationManagerCompat.notify(unReadId, builder.build());

            }
        }

    }
}
