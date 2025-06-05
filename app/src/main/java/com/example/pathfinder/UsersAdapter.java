package com.example.pathfinder;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder>{
    interface OnUserClickListener{
        void onUserClick(User user, int position);
    }

    private final OnUserClickListener onClickListener;
    private final LayoutInflater inflater;
    private final List<User> users;
    private final Context cont;

    UsersAdapter(Context context, List<User> users, OnUserClickListener onClickListener) {
        this.onClickListener = onClickListener;
        this.users = users;
        this.inflater = LayoutInflater.from(context);
        this.cont = context;
    }
    @Override
    public UsersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.points_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UsersAdapter.ViewHolder holder, int position) {
        User user = users.get(position);
        FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUID())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String profileImage = snapshot.child("thumbnail").getValue().toString();
                        String name = snapshot.child("username").getValue().toString();
                        String telegram = snapshot.child("telegram").getValue().toString();
                        if (profileImage != null && !profileImage.isEmpty()) {
                            Glide.with(cont)
                                    .load(profileImage)
                                    .into(holder.picView);
                        }
                        holder.nameView.setText(name);
                        if (telegram.isEmpty()) {telegram = "Not enough information"; } else {telegram = "@" + telegram;}
                        holder.descriptionView.setText(telegram);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        holder.elementLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                onClickListener.onUserClick(user, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final CircleImageView picView;
        final View background;
        final TextView nameView, descriptionView;
        final FrameLayout elementLayout;
        ViewHolder(View view){
            super(view);
            picView = view.findViewById(R.id.button_profile);
            nameView = view.findViewById(R.id.pointname);
            elementLayout = view.findViewById(R.id.elementLayout);
            descriptionView = view.findViewById(R.id.pointdescription);
            background = view.findViewById(R.id.background);
        }
    }
}