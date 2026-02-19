package com.example.accidentdetection;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.ViewHolder> {

    private final List<PlaceModel> placeList;

    public PlaceAdapter(List<PlaceModel> placeList) {
        this.placeList = placeList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView typeText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.placeName);
            typeText = itemView.findViewById(R.id.placeType);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.place_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlaceModel place = placeList.get(position);
        holder.nameText.setText(place.getName());
        holder.typeText.setText(place.getType());
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }
}

