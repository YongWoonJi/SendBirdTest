package com.example.yongwoon.sendbirdtest.main;

import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.yongwoon.sendbirdtest.PreferenceManager;
import com.example.yongwoon.sendbirdtest.R;
import com.example.yongwoon.sendbirdtest.group.GroupChannelListFragment_;
import com.example.yongwoon.sendbirdtest.group.GroupChatFragment_;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    private static final String CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_MAIN";

    @Extra
    String groupChannelUrl;

    @ViewById
    Toolbar toolbar;

    @ViewById
    DrawerLayout drawer;

    @ViewById
    FrameLayout container;

    @ViewById
    NavigationView navi;

    private ActionBarDrawerToggle mDrawerToggle;

    OnBackPressListener listener;
    public interface OnBackPressListener {
        boolean onBack();
    }

    public void setOnBackPressListener(OnBackPressListener listener) {
        this.listener = listener;
    }

    @AfterViews
    void init() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        setUpNavigationDrawer();
        setUpDrawerToggle();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new GroupChannelListFragment_())
                .commit();
        navi.setCheckedItem(R.id.group);

        if (groupChannelUrl != null) {
            Fragment f = GroupChatFragment_.builder().mChannelUrl(groupChannelUrl).build();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, f)
                    .addToBackStack(null)
                    .commit();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        SendBird.addConnectionHandler(CONNECTION_HANDLER_ID, new SendBird.ConnectionHandler() {
            @Override
            public void onReconnectStarted() {}

            @Override
            public void onReconnectSucceeded() {}

            @Override
            public void onReconnectFailed() {}
        });
    }

    private void setUpDrawerToggle() {
        mDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.main_drawer_open, R.string.main_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        drawer.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                } else {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    mDrawerToggle.setDrawerIndicatorEnabled(true);
                    mDrawerToggle.syncState();
                }
            }
        });
    }

    private void setUpNavigationDrawer() {
        navi.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.group:
                        getSupportFragmentManager().popBackStack();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, new GroupChannelListFragment_())
                                .commit();
                        break;
                    case R.id.open:
                        Toast.makeText(MainActivity.this, "오픈 채널", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.disconnect:
                        disconnect();
                        break;
                }

                item.setChecked(true);
                drawer.closeDrawers();
                return false;
            }
        });
    }

    public void setActionBarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    private void disconnect() {
        SendBird.disconnect(new SendBird.DisconnectHandler() {
            @Override
            public void onDisconnected() {
                unregisterPushTokens();
                PreferenceManager.setConnected(false);
                LoginActivity_.intent(MainActivity.this).start();
                finish();
            }
        });
    }

    private void unregisterPushTokens() {
        SendBird.unregisterPushTokenAllForCurrentUser(new SendBird.UnregisterPushTokenHandler() {
            @Override
            public void onUnregistered(SendBirdException e) {
                if (e != null) {
                    // 에러
//                    Toast.makeText(MainActivity.this, "disconnect error : " + e.getCode() + " - " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    return;
                }
                Toast.makeText(MainActivity.this, "모든 Push Token 등록 해제", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawer.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (listener != null) {
            if (!listener.onBack()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }
}
