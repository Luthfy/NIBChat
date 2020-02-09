package id.digilabyte.nibchat.holder;

import android.util.SparseArray;

import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

public class QBUsersHolder {

    private static QBUsersHolder instance;

    private SparseArray<QBUser> qbUserSparseArray;

    public static synchronized QBUsersHolder getInstance() {
        if (instance == null) {
            instance = new QBUsersHolder();
        }
        return instance;
    }

    private QBUsersHolder() {
        qbUserSparseArray = new SparseArray<>();
    }

    public void putUsers(ArrayList<QBUser> users) {
        for (QBUser user : users) {
            putUser(user);
        }
    }

    private void putUser(QBUser user) {
        qbUserSparseArray.put(user.getId(), user);
    }

    public QBUser getUserById (int id) {
        return qbUserSparseArray.get(id);
    }

    public ArrayList<QBUser> getUserByIds(List<Integer> ids) {
        ArrayList<QBUser> qbUsers = new ArrayList<>();
        for (Integer id : ids) {
            QBUser user = getUserById(id);
            if (user != null) {
                qbUsers.add(user);
            }
        }
        return qbUsers;
    }
}
