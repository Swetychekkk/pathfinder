package com.example.pathfinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class StateAdapter  extends RecyclerView.Adapter<StateAdapter.ViewHolder>{

    private final LayoutInflater inflater;
    private final List<Marker> markers;

    StateAdapter(Context context, List<Marker> markers) {
        this.markers = markers;
        this.inflater = LayoutInflater.from(context);
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
    }

    @Override
    public int getItemCount() {
        return markers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final CircleImageView picView;
        final TextView nameView, descriptionView;
        ViewHolder(View view){
            super(view);
            picView = view.findViewById(R.id.button_profile);
            nameView = view.findViewById(R.id.pointname);
            descriptionView = view.findViewById(R.id.pointdescription);
        }
    }
}