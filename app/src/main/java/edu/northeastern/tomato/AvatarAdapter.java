package edu.northeastern.tomato;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class AvatarAdapter extends RecyclerView.Adapter<AvatarAdapter.AvatarViewHolder> {

    private List<Integer> avatarList;
    private OnAvatarClickListener listener;

    public interface OnAvatarClickListener {
        void onAvatarClick(int avatarResId);
    }

    public AvatarAdapter(List<Integer> avatarList, OnAvatarClickListener listener) {
        this.avatarList = avatarList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AvatarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_avatar, parent, false);
        return new AvatarViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AvatarViewHolder holder, int position) {
        int avatarResId = avatarList.get(position);
        Glide.with(holder.itemView.getContext())
                .load(avatarResId)
                .circleCrop()
                .into(holder.imageViewAvatar);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAvatarClick(avatarResId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return avatarList.size();
    }

    public static class AvatarViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewAvatar;

        public AvatarViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewAvatar = itemView.findViewById(R.id.imageView_avatar_item);
        }
    }
}

