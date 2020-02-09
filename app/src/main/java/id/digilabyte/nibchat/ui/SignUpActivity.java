package id.digilabyte.nibchat.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import id.digilabyte.nibchat.R;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    TextInputEditText txtFullNameSignUp, txtUserSignUp, txtPasswordSignUp, txtConfirmSignUp;
    Button btnLoginSignUp, btnRegisterSignUp;
    ProgressDialog mLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        txtFullNameSignUp   = findViewById(R.id.edt_full_name_register);
        txtUserSignUp       = findViewById(R.id.edt_user_register);
        txtPasswordSignUp   = findViewById(R.id.edt_password_register);
        txtConfirmSignUp    = findViewById(R.id.edt_password_confirm_register);
        btnLoginSignUp      = findViewById(R.id.btn_login_signup);
        btnRegisterSignUp   = findViewById(R.id.btn_register_signup);

        btnLoginSignUp.setOnClickListener(this);
        btnRegisterSignUp.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login_signup :
                signUpRequest();
                break;
            case R.id.btn_register_signup:
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                break;
        }
    }

    private void showLoading() {
        mLoading = new ProgressDialog(SignUpActivity.this, ProgressDialog.THEME_HOLO_LIGHT);
        mLoading.setMessage("Please wait ...");
        mLoading.show();
    }

    private void signUpRequest() {

        if (txtFullNameSignUp.getText().toString().isEmpty()) {
            txtFullNameSignUp.setError("Nama Lengkap Harus Di Isi");
            txtFullNameSignUp.requestFocus();
        } else if (txtUserSignUp.getText().toString().isEmpty()) {
            txtUserSignUp.setError("User Harus Di Isi");
            txtUserSignUp.requestFocus();
        } else if (txtPasswordSignUp.getText().toString().isEmpty()) {
            txtPasswordSignUp.setError("Password Harus Di Isi");
            txtPasswordSignUp.requestFocus();
        } else if (txtConfirmSignUp.getText().equals(txtPasswordSignUp.getText())) {
            txtPasswordSignUp.setError("Password Tidak Cocok");
            txtPasswordSignUp.requestFocus();
        } else {
            showLoading();
            String fullName = txtFullNameSignUp.getText().toString();
            String userLogin = txtUserSignUp.getText().toString();
            String userPass = txtPasswordSignUp.getText().toString();

            QBUser qbUser = new QBUser();
            qbUser.setFullName(fullName);
            qbUser.setLogin(userLogin);
            qbUser.setPassword(userPass);

            QBUsers.signUp(qbUser).performAsync(new QBEntityCallback<QBUser>() {
                @Override
                public void onSuccess(QBUser qbUser, Bundle bundle) {
                    mLoading.dismiss();
                    Toast.makeText(SignUpActivity.this, "Registered successfully", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onError(QBResponseException e) {
                    Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }
}
