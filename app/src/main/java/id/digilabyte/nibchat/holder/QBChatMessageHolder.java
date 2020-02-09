package id.digilabyte.nibchat.holder;

import com.quickblox.chat.model.QBChatMessage;

import java.util.ArrayList;
import java.util.HashMap;

public class QBChatMessageHolder {

    private static QBChatMessageHolder instance;
    private HashMap<String, ArrayList<QBChatMessage>> qbChatMessageArray;

    public static synchronized QBChatMessageHolder getInstance() {
        QBChatMessageHolder qbChatMessageHolder;
        synchronized (QBChatMessageHolder.class) {
            if (instance == null) {
                instance = new QBChatMessageHolder();
            }
            qbChatMessageHolder = instance;
        }
        return qbChatMessageHolder;
    }

    private QBChatMessageHolder() {
        this.qbChatMessageArray = new HashMap<>();
    }

    public void putMessages(String dialogId, ArrayList<QBChatMessage> qbChatMessages) {
        this.qbChatMessageArray.put(dialogId, qbChatMessages);
    }

    public void putMessage(String dialogId, QBChatMessage qbChatMessage) {
        ArrayList<QBChatMessage> result = this.qbChatMessageArray.get(dialogId);
        result.add(qbChatMessage);
        ArrayList<QBChatMessage> messages = new ArrayList(result.size());
        messages.addAll(result);
        putMessages(dialogId, messages);
    }

    public ArrayList<QBChatMessage> getChatMessageByDialogId(String dialogId) {
        return (ArrayList<QBChatMessage>)this.qbChatMessageArray.get(dialogId);
    }


}
