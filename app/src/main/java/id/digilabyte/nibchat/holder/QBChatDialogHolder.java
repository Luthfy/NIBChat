package id.digilabyte.nibchat.holder;

import com.quickblox.chat.QBChat;
import com.quickblox.chat.model.QBChatDialog;

import java.util.ArrayList;
import java.util.HashMap;

public class QBChatDialogHolder {

    private static QBChatDialogHolder instance;
    private HashMap<String, QBChatDialog> qbChatDialogHashMap;

    public static synchronized QBChatDialogHolder getInstance() {
        QBChatDialogHolder qbChatDialogHolder;
        synchronized (QBChatDialogHolder.class) {
            if (instance == null) {
                instance = new QBChatDialogHolder();
            }
            qbChatDialogHolder = instance;
        }
        return qbChatDialogHolder;
    }

    public QBChatDialogHolder() {
        this.qbChatDialogHashMap = new HashMap<>();
    }

    public void putDialogs (ArrayList<QBChatDialog> qbChatDialogs) {
        for (QBChatDialog qbChatDialog : qbChatDialogs) {
            putDialog(qbChatDialog);
        }
    }

    public void putDialog(QBChatDialog qbChatDialog) {
        this.qbChatDialogHashMap.put(qbChatDialog.getDialogId(), qbChatDialog);
    }

    public QBChatDialog getChatDialogById(String dialogId) {
        return (QBChatDialog)qbChatDialogHashMap.get(dialogId);
    }

    public ArrayList<QBChatDialog> getChatDialogsByIds (ArrayList<String> dialogIds) {
        ArrayList<QBChatDialog> chatDialogs = new ArrayList<>();
        for (String id : dialogIds) {
            QBChatDialog chatDialog = getChatDialogById(id);
            if (chatDialog != null) {
                chatDialogs.add(chatDialog);
            }
        }
        return chatDialogs;
    }

    public ArrayList<QBChatDialog> getAllChatDialogs () {
        ArrayList<QBChatDialog> qbChat = new ArrayList<>();
        for (String key : qbChatDialogHashMap.keySet()) {
            qbChat.add(qbChatDialogHashMap.get(key));
        }
        return qbChat;
    }
}
