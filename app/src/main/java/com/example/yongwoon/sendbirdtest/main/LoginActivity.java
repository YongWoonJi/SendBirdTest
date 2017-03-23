package com.example.yongwoon.sendbirdtest.main;

import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yongwoon.sendbirdtest.MainActivity_;
import com.example.yongwoon.sendbirdtest.PreferenceManager;
import com.example.yongwoon.sendbirdtest.R;
import com.google.firebase.iid.FirebaseInstanceId;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_login)
public class LoginActivity extends AppCompatActivity {

    @ViewById
    ConstraintLayout layout;

    @ViewById
    TextInputEditText editID, editNickname;

    @ViewById
    TextView textView;

    @ViewById
    Button btnLogin;

    @ViewById
    ContentLoadingProgressBar progressBar;


    @AfterViews
    void init() {
        editID.setText(PreferenceManager.getUserId());
        editNickname.setText(PreferenceManager.getNickname());
        String sdkVersion = String.format("OS v%1$s / SendBird v%2$s", SendBird.getOSVersion(), SendBird.getSDKVersion());
        textView.setText(sdkVersion);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (PreferenceManager.getConnected()) {
            connectToSendBird(PreferenceManager.getUserId(), PreferenceManager.getNickname());
        }
    }

    @Click
    void btnLogin() {
        String userId = editID.getText().toString();
        String nickname = editNickname.getText().toString();

        PreferenceManager.setUserId(userId);
        PreferenceManager.setNickname(nickname);

        connectToSendBird(userId, nickname);
    }

    private void connectToSendBird(String userId, final String nickname) {
        showProgressBar(true);
        btnLogin.setEnabled(false);

        SendBird.connect(userId, new SendBird.ConnectHandler() {
            @Override
            public void onConnected(User user, SendBirdException e) {
                showProgressBar(false);

                if (e != null) {
                    showSnackbar("로그인 실패 : " + e.getCode() + " - " + e.getMessage());
                    btnLogin.setEnabled(true);
                    PreferenceManager.setConnected(false);
                    return;
                }

                PreferenceManager.setConnected(true);

                updateCurrentUserInfo(nickname);
                updateCurrentUserPushToken();

                MainActivity_.intent(LoginActivity.this).start();
                finish();
            }
        });
    }

    private void updateCurrentUserPushToken() {
        SendBird.registerPushTokenForCurrentUser(FirebaseInstanceId.getInstance().getToken(), new SendBird.RegisterPushTokenWithStatusHandler() {
            @Override
            public void onRegistered(SendBird.PushTokenRegistrationStatus pushTokenRegistrationStatus, SendBirdException e) {
                if (e != null) {
                    showSnackbar(e.getCode() + " - " + e.getMessage());
                    return;
                }
                Toast.makeText(LoginActivity.this, "Push Token 등록 성공", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCurrentUserInfo(String nickname) {
        SendBird.updateCurrentUserInfo(nickname, null, new SendBird.UserInfoUpdateHandler() {
            @Override
            public void onUpdated(SendBirdException e) {
                if (e != null) {
                    showSnackbar("유저 업데이트 실패 : " + e.getCode() + " - " + e.getMessage());
                    return;
                }
            }
        });
    }

    private void showSnackbar(String s) {
        Snackbar snackbar = Snackbar.make(layout, s, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }


    private void showProgressBar(boolean tf) {
        if (tf) {
            progressBar.show();
        } else {
            progressBar.hide();
        }
    }


}
