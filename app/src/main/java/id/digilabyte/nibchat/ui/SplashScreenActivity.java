package id.digilabyte.nibchat.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import id.digilabyte.nibchat.R;
import id.digilabyte.nibchat.utils.UserPreferences;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class SplashScreenActivity extends AppCompatActivity {

    UserPreferences user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        user = new UserPreferences(SplashScreenActivity.this);

        Log.d("_SPLASH", user.getUser()+" "+user.getPass());

        Handler handler = new Handler();
        if (user.getFirst()) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
                    finish();
                }
            }, 1500);
        } else {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (user.getUser().isEmpty() && user.getPass().isEmpty()) {
                        startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        startActivity(new Intent(SplashScreenActivity.this, ChatDialogActivity.class));
                        finish();
                    }
                }
            }, 1500);

        }
    }
}
