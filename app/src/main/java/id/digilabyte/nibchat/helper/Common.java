package id.digilabyte.nibchat.helper;

import com.quickblox.users.model.QBUser;

import id.digilabyte.nibchat.holder.QBUsersHolder;

public class Common {

    public static final String DIALOG_EXTRA = "dialogs";
    public static final String EXTRA_QB_USERS_LIST = "users";
    public static final String EXTRA_IS_INCOMING_CALL = "income_call_fragment";

    public static String createChatDialogName (String input) {
        StringBuilder name = new StringBuilder();
        name.append(input);
        if (name.length() > 30) {
            name = name.replace(30, name.length() -1, "...");
        }
        return name.toString();
    }
}
