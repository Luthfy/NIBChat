package id.digilabyte.nibchat.adapter;

import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

import id.digilabyte.nibchat.R;
import id.digilabyte.nibchat.ui.ListUserActivity;

public class ListUserAdapter extends RecyclerView.Adapter<ListUserAdapter.ViewHolder>{

    private Context context;
    private ArrayList<QBUser> qbUserArrayList;

    public ListUserAdapter(Context context, ArrayList<QBUser> qbUserArrayList) {
        this.context = context;
        this.qbUserArrayList = qbUserArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_users, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindItem(qbUserArrayList.get(position));
        holder.position = position;
    }

    @Override
    public int getItemCount() {
        return qbUserArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout llUserList;
        CheckedTextView txtName;
        ImageView imgName;
        Integer position;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            llUserList = itemView.findViewById(R.id.ll_list_user);
            txtName = itemView.findViewById(R.id.txt_name_list_user);
            imgName = itemView.findViewById(R.id.img_user_list_user);
        }

        public void bindItem(final QBUser qbUser) {

            ColorGenerator generator = ColorGenerator.MATERIAL;
            int randomColor = generator.getRandomColor();

            TextDrawable.IBuilder builder = TextDrawable.builder().beginConfig()
                    .withBorder(4)
                    .endConfig()
                    .round();

            TextDrawable drawable = builder.build(qbUser.getFullName().substring(0,1).toUpperCase(), randomColor);

            txtName.setText(qbUser.getFullName());
            imgName.setImageDrawable(drawable);

            llUserList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (txtName.isChecked()) {
                        llUserList.setBackgroundColor(Color.TRANSPARENT);
                        txtName.setCheckMarkDrawable(R.drawable.ic_check_box_outline_blank_black_24dp);
                        txtName.setChecked(false);
                    } else {
                        llUserList.setBackgroundColor(Color.LTGRAY);
                        txtName.setCheckMarkDrawable(R.drawable.ic_check_box_black_24dp);
                        txtName.setChecked(true);
                    }
                    ((ListUserActivity)context).userChat(qbUser);
                }
            });
        }
    }
}
