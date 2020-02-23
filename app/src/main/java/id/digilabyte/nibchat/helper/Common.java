package id.digilabyte.nibchat.helper;

import com.quickblox.users.model.QBUser;

import id.digilabyte.nibchat.holder.QBUsersHolder;

public class Common {

    public static final String DIALOG_EXTRA = "dialogs";
    public static final String EXTRA_QB_USERS_LIST = "users";
    public static final String EXTRA_IS_INCOMING_CALL = "income_call_fragment";
    public static final String IS_STARTED_CALL = "is_star_call";
    public static final String QBCONFRENCE_TYPE = "qb_confrence_type";
    public static final Integer USER_OPPONENT_MAKS = 4;
    public static final String SESSION_CHAT = "qb_chat_session";
    public static final String EXTRA_PENDING_INTENT = "pending_intent";
    public static final String EXTRA_COMMAND_TO_SERVICE = "extra_command_service";
    public static final int COMMAND_LOGOUT = 2;
    public static final int COMMAND_LOGIN = 1;
    public static final int COMMAND_NOT_FOUND = 0;

    public static String createChatDialogName (String input) {
        StringBuilder name = new StringBuilder();
        name.append(input);
        if (name.length() > 30) {
            name = name.replace(30, name.length() -1, "...");
        }
        return name.toString();
    }
}
