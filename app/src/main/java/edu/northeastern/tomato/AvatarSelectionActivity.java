package edu.northeastern.tomato;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import android.net.Uri; // 如果需要从Uri加载图片

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class AvatarSelectionActivity extends AppCompatActivity {

    private ImageView imageViewSelectedAvatar;
    private RecyclerView recyclerViewAvatars;
    private Button buttonSaveAvatar;

    private List<Integer> avatarList;
    private int selectedAvatarResId;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar_selection);

        // 初始化UI组件
        imageViewSelectedAvatar = findViewById(R.id.imageView_selected_avatar);
        recyclerViewAvatars = findViewById(R.id.recyclerView_avatars);
        buttonSaveAvatar = findViewById(R.id.button_save_avatar);

        // 初始化Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // 加载头像列表
        loadAvatarList();

        // 设置RecyclerView
        AvatarAdapter adapter = new AvatarAdapter(avatarList, new AvatarAdapter.OnAvatarClickListener() {
            @Override
            public void onAvatarClick(int avatarResId) {
                selectedAvatarResId = avatarResId;
                // 更新上方的选中头像显示
                Glide.with(AvatarSelectionActivity.this)
                        .load(selectedAvatarResId)
                        .circleCrop()
                        .into(imageViewSelectedAvatar);
            }
        });
        recyclerViewAvatars.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerViewAvatars.setAdapter(adapter);

        // 默认选中第一个头像
        if (!avatarList.isEmpty()) {
            selectedAvatarResId = avatarList.get(0);
            Glide.with(this)
                    .load(selectedAvatarResId)
                    .circleCrop()
                    .into(imageViewSelectedAvatar);
        }

        // 保存按钮点击事件
        buttonSaveAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSelectedAvatar();
            }
        });
    }

    private void loadAvatarList() {
        avatarList = new ArrayList<>();
        // 添加drawable中的头像资源ID
        avatarList.add(R.drawable.avatar0);
        avatarList.add(R.drawable.avatar1);
        avatarList.add(R.drawable.avatar2);
        avatarList.add(R.drawable.avatar3);
        avatarList.add(R.drawable.avatar4);
        avatarList.add(R.drawable.avatar5);
        avatarList.add(R.drawable.avatar6);
        avatarList.add(R.drawable.avatar7);
        avatarList.add(R.drawable.avatar8);
        avatarList.add(R.drawable.avatar9);
        avatarList.add(R.drawable.avatar10);
        avatarList.add(R.drawable.avatar11);

        // 根据您提供的头像数量继续添加
        // 请确保这些资源已经存在于drawable文件夹中
    }

    private void saveSelectedAvatar() {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String avatarName = getResources().getResourceEntryName(selectedAvatarResId);
            String avatarUrl = "drawable:" + avatarName;

            // 更新数据库中的avatarUrl字段
            usersRef.child(userId).child("profile").child("avatarUrl").setValue(avatarUrl)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Snackbar.make(buttonSaveAvatar, "头像已更新", Snackbar.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Snackbar.make(buttonSaveAvatar, "头像更新失败", Snackbar.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Snackbar.make(buttonSaveAvatar, "用户未登录", Snackbar.LENGTH_SHORT).show();
        }
    }
}
