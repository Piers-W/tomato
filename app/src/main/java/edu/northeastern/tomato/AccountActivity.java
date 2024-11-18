package edu.northeastern.tomato;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.net.Uri;

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

public class AccountActivity extends AppCompatActivity {

    private ImageView imageViewAvatar;
    private TextView textViewChangePhoto;
    private TextView textViewUsernameValue;
    private TextView textViewEmailValue;
    private LinearLayout linearLayoutUsername;
    private LinearLayout linearLayoutEmail;
    private LinearLayout linearLayoutPassword;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        // 初始化UI组件
        imageViewAvatar = findViewById(R.id.imageView_avatar);
        textViewChangePhoto = findViewById(R.id.textView_change_photo);
        textViewUsernameValue = findViewById(R.id.textView_username_value);
        textViewEmailValue = findViewById(R.id.textView_email_value);
        linearLayoutUsername = findViewById(R.id.linearLayout_username);
        linearLayoutEmail = findViewById(R.id.linearLayout_email);
        linearLayoutPassword = findViewById(R.id.linearLayout_password);

        // 初始化Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // 更新UI
        updateUI();

        // 设置点击事件
        imageViewAvatar.setOnClickListener(view -> navigateToAvatarSelection());
        textViewChangePhoto.setOnClickListener(view -> navigateToAvatarSelection());

        linearLayoutUsername.setOnClickListener(view -> {
            // 进入修改用户名页面（待实现）
            Intent intent = new Intent(AccountActivity.this, ChangeUsernameActivity.class);
            startActivity(intent);
        });

        linearLayoutEmail.setOnClickListener(view -> {
            // 进入修改邮箱页面（待实现）
            Intent intent = new Intent(AccountActivity.this, ChangeEmailActivity.class);
            startActivity(intent);
        });

        linearLayoutPassword.setOnClickListener(view -> {
            // 进入修改密码页面（待实现）
            Intent intent = new Intent(AccountActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });
    }

    private void updateUI() {
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // 获取用户资料
            usersRef.child(userId).child("profile").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String username = snapshot.child("username").getValue(String.class);
                    String avatarUrl = snapshot.child("avatarUrl").getValue(String.class);

                    if (username != null) {
                        textViewUsernameValue.setText(username);
                    } else {
                        textViewUsernameValue.setText("未设置");
                    }

                    // 加载头像
                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        if (avatarUrl.startsWith("drawable:")) {
                            // 从drawable资源中加载头像
                            String avatarName = avatarUrl.substring("drawable:".length());
                            int resourceId = getResources().getIdentifier(avatarName, "drawable", getPackageName());
                            Glide.with(AccountActivity.this)
                                    .load(resourceId)
                                    .circleCrop()
                                    .placeholder(R.drawable.default_avatar)
                                    .into(imageViewAvatar);
                        } else {
                            // 从URL加载头像
                            Glide.with(AccountActivity.this)
                                    .load(avatarUrl)
                                    .circleCrop()
                                    .placeholder(R.drawable.default_avatar)
                                    .into(imageViewAvatar);
                        }
                    } else {
                        // 使用默认头像
                        Glide.with(AccountActivity.this)
                                .load(R.drawable.default_avatar)
                                .circleCrop()
                                .into(imageViewAvatar);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Snackbar.make(imageViewAvatar, "无法获取用户信息", Snackbar.LENGTH_SHORT).show();
                }
            });

            // 显示部分遮掩的邮箱
            String email = currentUser.getEmail();
            if (email != null) {
                textViewEmailValue.setText(maskEmail(email));
            } else {
                textViewEmailValue.setText("未设置");
            }

        } else {
            // 用户未登录，跳转到登录页面
            Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf("@");
        if (atIndex > 2) {
            String firstChar = email.substring(0,1);
            String lastChar = email.substring(atIndex -1, atIndex);
            String domain = email.substring(atIndex);
            return firstChar + "****" + lastChar + domain;
        } else {
            // 邮箱过短，无法遮掩
            return email;
        }
    }

    private void navigateToAvatarSelection() {
        // 进入头像选择页面（需要您实现AvatarSelectionActivity）
        Intent intent = new Intent(AccountActivity.this, AvatarSelectionActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 返回此页面时更新用户信息
        currentUser = mAuth.getCurrentUser();
        updateUI();
    }
}
