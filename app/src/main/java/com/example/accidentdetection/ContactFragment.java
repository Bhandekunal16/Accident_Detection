package com.example.accidentdetection;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.example.accidentdetection.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.*;

public class ContactFragment extends Fragment {
    private static final int PICK_CONTACT = 1;
    private static final int PERMISSION_REQUEST_CODE = 101;
    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private Button sendLocationBtn;
    private List<Contact> contactList = new ArrayList<>();
    private DatabaseReference contactRef;
    private FusedLocationProviderClient locationClient;

    public ContactFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_contact, container, false);
        recyclerView = v.findViewById(R.id.recyclerViewContacts);
        fab = v.findViewById(R.id.fabAddContact);
        sendLocationBtn = v.findViewById(R.id.sendLocationBtn);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        String uid = FirebaseAuth.getInstance().getUid();
        contactRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("contacts");
        locationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        fab.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[] { Manifest.permission.READ_CONTACTS },
                        PERMISSION_REQUEST_CODE);
            } else {
                pickContact();
            }
        });
        sendLocationBtn.setOnClickListener(v1 -> {
            if (checkAndRequestSmsLocationPermissions()) {
                sendLocationToContacts();
            }
        });
        contactRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contactList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    contactList.add(snap.getValue(Contact.class));
                }
                recyclerView.setAdapter(new ContactAdapter(contactList,
                        contactRef));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        return v;
    }

    private void pickContact() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (reqCode == PICK_CONTACT && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            Cursor cursor = requireActivity().getContentResolver().query(uri, null, null,
                    null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int numberIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                if (nameIdx != -1 && numberIdx != -1) {
                    String name = cursor.getString(nameIdx);
                    String number = cursor.getString(numberIdx).replaceAll("[^\\d+]",
                            ""); // clean number
                    String key = contactRef.push().getKey();
                    contactRef.child(key).setValue(new Contact(key, name, number));
                }
                cursor.close();
            }
        }
    }

    private boolean checkAndRequestSmsLocationPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
            permissionsNeeded.add(Manifest.permission.SEND_SMS);
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)

            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)

            permissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(requireActivity(),
                    permissionsNeeded.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @SuppressLint("MissingPermission")
    private void sendLocationToContacts() {
        locationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null && !contactList.isEmpty()) {
                String message = "Hey, Accident happen, I need help! My current location: https://maps.google.com/?q=" +
                        location.getLatitude() + "," + location.getLongitude();
                SmsManager smsManager = SmsManager.getDefault();
                for (Contact contact : contactList) {
                    try {
                        smsManager.sendTextMessage(contact.number, null, message, null, null);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Failed to send to: " + contact.number,
                                Toast.LENGTH_SHORT).show();
                    }
                }
                Toast.makeText(getContext(), "SMS sent to all contacts",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Location or contact list is empty",
                        Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to fetch location",
                Toast.LENGTH_SHORT).show());
    }
}