package com.example.pathfinder;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class StateAdapter  extends RecyclerView.Adapter<StateAdapter.ViewHolder>{
    interface OnStateClickListener{
        void onStateClick(Marker marker, int position);
    }

    private final OnStateClickListener onClickListener;
    private final LayoutInflater inflater;
    private final List<Marker> markers;
    private final Context cont;

    StateAdapter(Context context, List<Marker> markers, OnStateClickListener onClickListener) {
        this.onClickListener = onClickListener;
        this.markers = markers;
        this.inflater = LayoutInflater.from(context);
        this.cont = context;
    }
    @Override
    public StateAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.points_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StateAdapter.ViewHolder holder, int position) {
        Marker marker = markers.get(position);
        holder.nameView.setText(marker.getName());
        holder.descriptionView.setText(marker.getDescription());
        Integer color = ColorUtils.blendARGB(Color.parseColor(marker.getPriority()), Color.BLACK, 0.25f); //MAKING COLOR FILTER
        holder.background.setBackgroundTintList(ColorStateList.valueOf(color)); //CHANGING BACKGROUND COLOR
        FirebaseDatabase.getInstance().getReference().child("Users").child(marker.getOwner())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String profileImage = snapshot.child("thumbnail").getValue().toString();
                        if (profileImage != null && !profileImage.isEmpty()) {
                            Glide.with(cont)
                                    .load(profileImage)
                                    .into(holder.picView);
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        holder.elementLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                onClickListener.onStateClick(marker, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return markers.size();
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