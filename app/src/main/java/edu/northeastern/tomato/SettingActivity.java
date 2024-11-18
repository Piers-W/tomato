package edu.northeastern.tomato;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SettingActivity extends AppCompatActivity {

    private ImageView imageViewAvatar;
    private TextView textViewUsername;
    private Button buttonLoginLogout;
    private LinearLayout linearLayoutAccount;
    private LinearLayout linearLayoutAbout;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // 初始化UI组件
        imageViewAvatar = findViewById(R.id.imageView_avatar);
        textViewUsername = findViewById(R.id.textView_username);
        buttonLoginLogout = findViewById(R.id.button_login_logout);
        linearLayoutAccount = findViewById(R.id.linearLayout_account);
        linearLayoutAbout = findViewById(R.id.linearLayout_about);

        // 初始化Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // 更新UI
        updateUI();

        // 登录/登出按钮点击事件
        buttonLoginLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleLoginLogout();
            }
        });

        // 账户设置点击事件
        linearLayoutAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleAccount();
            }
        });

        // 关于页面点击事件
        linearLayoutAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * 更新UI，根据用户的登录状态显示信息
     */
    private void updateUI() {
        if (currentUser != null) {
            // 用户已登录，从Firebase获取用户信息并更新UI
            String userId = currentUser.getUid();

            // 获取用户资料数据
            usersRef.child(userId).child("profile").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String username = snapshot.child("username").getValue(String.class);
                    String avatarUrl = snapshot.child("avatarUrl").getValue(String.class);

                    if (username != null) {
                        textViewUsername.setText(username);
                    } else {
                        textViewUsername.setText("未命名用户");
                    }

                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        if (avatarUrl.startsWith("drawable:")) {
                            // 从drawable资源加载头像
                            String avatarName = avatarUrl.substring("drawable:".length());
                            int resourceId = getResources().getIdentifier(avatarName, "drawable", getPackageName());
                            Glide.with(SettingActivity.this)
                                    .load(resourceId)
                                    .circleCrop()
                                    .placeholder(R.drawable.default_avatar)
                                    .into(imageViewAvatar);
                        } else {
                            // 从URL加载头像
                            Glide.with(SettingActivity.this)
                                    .load(avatarUrl)
                                    .circleCrop()
                                    .placeholder(R.drawable.default_avatar)
                                    .into(imageViewAvatar);
                        }
                    } else {
                        // 使用默认头像
                        imageViewAvatar.setImageResource(R.drawable.default_avatar);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Snackbar.make(buttonLoginLogout, "无法获取用户信息", Snackbar.LENGTH_SHORT).show();
                }
            });

            // 更新按钮文本为“登出”
            buttonLoginLogout.setText("登出");
        } else {
            // 用户未登录，显示默认信息
            textViewUsername.setText("未登录");
            imageViewAvatar.setImageResource(R.drawable.default_avatar);
            // 更新按钮文本为“登录”
            buttonLoginLogout.setText("登录");
        }
    }

    /**
     * 处理登录或登出操作
     */
    private void handleLoginLogout() {
        if (currentUser != null) {
            // 用户已登录，执行登出操作
            mAuth.signOut();
            currentUser = null;
            Snackbar.make(buttonLoginLogout, "登出成功", Snackbar.LENGTH_SHORT).show();
            updateUI();
        } else {
            // 用户未登录，导航到登录页面
            Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }

    /**
     * 处理账户设置点击事件
     */
    private void handleAccount() {
        if (currentUser != null) {
            // 用户已登录，导航到账户设置页面
            Intent intent = new Intent(SettingActivity.this, AccountActivity.class);
            startActivity(intent);
        } else {
            // 用户未登录，提示先登录
            Snackbar.make(buttonLoginLogout, "请先登录", Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 返回此页面时更新用户信息
        currentUser = mAuth.getCurrentUser();
        updateUI();
    }
}
