package id.digilabyte.nibchat.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import id.digilabyte.nibchat.R;
import id.digilabyte.nibchat.utils.UserPreferences;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText txtUserLogin, txtPassLogin;
    private Button btnSignInLogin, btnSignUpLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        txtUserLogin = findViewById(R.id.txt_user_login);
        txtPassLogin = findViewById(R.id.txt_pass_login);
        btnSignInLogin = findViewById(R.id.btn_sign_in_login);
        btnSignUpLogin = findViewById(R.id.btn_sign_up_login);

        btnSignInLogin.setOnClickListener(this);
        btnSignUpLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sign_up_login :
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                break;
            case R.id.btn_sign_in_login:
                loginRequest();
                break;
            default:
                break;
        }
    }

    private void loginRequest() {

        final String userLogin = txtUserLogin.getText().toString();
        final String passLogin = txtPassLogin.getText().toString();

        if (userLogin.isEmpty()) {
            txtUserLogin.setError("User harus diisi");
            txtUserLogin.requestFocus();
        } else if (passLogin.isEmpty()) {
            txtPassLogin.setError("User harus diisi");
            txtPassLogin.requestFocus();
        } else {

            QBUser qbUser = new QBUser();
            qbUser.setLogin(userLogin);
            qbUser.setPassword(passLogin);

            QBUsers.signIn(qbUser).performAsync(new QBEntityCallback<QBUser>() {
                @Override
                public void onSuccess(QBUser qbUser, Bundle bundle) {
                    Toast.makeText(LoginActivity.this, "Login successfully", Toast.LENGTH_SHORT).show();

                    UserPreferences up = new UserPreferences(LoginActivity.this);
                    up.setFirst(false);
                    up.setUser(userLogin);
                    up.setPass(passLogin);

                    Intent intent = new Intent(LoginActivity.this, ChatDialogActivity.class);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onError(QBResponseException e) {
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }
}
