package com.example.accidentdetection;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.accidentdetection.R;
import com.google.firebase.database.DatabaseReference;
import java.util.List;

public class ContactAdapter extends
        RecyclerView.Adapter<ContactAdapter.ViewHolder> {
    private List<Contact> contactList;
    private DatabaseReference ref;

    public ContactAdapter(List<Contact> contactList, DatabaseReference ref) {
        this.contactList = contactList;
        this.ref = ref;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, numberText;
        Button deleteBtn;

        public ViewHolder(View view) {
            super(view);
            nameText = view.findViewById(R.id.nameText);
            numberText = view.findViewById(R.id.numberText);
            deleteBtn = view.findViewById(R.id.deleteBtn);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent,
                false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contact c = contactList.get(position);
        holder.nameText.setText(c.name);
        holder.numberText.setText(c.number);
        holder.deleteBtn.setOnClickListener(v -> {
            ref.child(c.id).removeValue();
            contactList.remove(position);
            notifyItemRemoved(position);
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }
}