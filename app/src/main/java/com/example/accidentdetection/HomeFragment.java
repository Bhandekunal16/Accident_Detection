package com.example.accidentdetection;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.accidentdetection.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements
        OnMapReadyCallback {
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng currentLatLng;
    private Button btnSafe;
    private RecyclerView recyclerPlaces;
    private List<PlaceModel> placeList;
    private PlaceAdapter placeAdapter;
    private DatabaseReference contactRef;
    private List<Contact> contactList = new ArrayList<>();

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        btnSafe = view.findViewById(R.id.btnSafe);
        recyclerPlaces = view.findViewById(R.id.recyclerPlaces);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        placeList = new ArrayList<>();
        placeAdapter = new PlaceAdapter(placeList);
        recyclerPlaces.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerPlaces.setAdapter(placeAdapter);
        String uid = FirebaseAuth.getInstance().getUid();
        contactRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("contacts");
        fetchContacts();
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        btnSafe.setOnClickListener(v -> {
            sendSafeSmsToAllContacts();
            fetchNearbyPlaces();
        });
        return view;
    }

    private void fetchContacts() {
        contactRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contactList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    contactList.add(snap.getValue(Contact.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load contacts",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendSafeSmsToAllContacts() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[] {
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 200);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null && !contactList.isEmpty()) {
                String message = "Hey, Accident happen. I need help! My current location: https://maps.google.com/?q=" +
                        location.getLatitude() + "," + location.getLongitude();
                SmsManager smsManager = SmsManager.getDefault();
                for (Contact contact : contactList) {
                    try {
                        smsManager.sendTextMessage(contact.number, null, message,
                                null, null);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Failed to send to: " +
                                contact.number, Toast.LENGTH_SHORT).show();
                    }
                }
                Toast.makeText(getContext(), "Safe SMS sent to all contacts",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Location or contact list is empty",
                        Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to fetch location",
                Toast.LENGTH_SHORT).show());
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    currentLatLng = new LatLng(location.getLatitude(),
                            location.getLongitude());

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng,
                            15f));
                }
            });
            mMap.setOnInfoWindowClickListener(marker -> {
                LatLng latLng = marker.getPosition();
                String name = marker.getTitle();
                String uri = "http://maps.google.com/maps?q=loc:" + latLng.latitude
                        + "," + latLng.longitude + " (" + name + ")";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            });
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 101);
        }
    }

    private void fetchNearbyPlaces() {
        if (currentLatLng == null) {
            Toast.makeText(getContext(), "Location not available",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mMap.clear();
        placeList.clear();
        placeAdapter.notifyDataSetChanged();
        mMap.addMarker(new MarkerOptions().position(currentLatLng).title("You"));
        String[] types = { "police", "hospital" };
        for (String type : types) {
            String url = getNearbyPlacesUrl(currentLatLng.latitude,
                    currentLatLng.longitude, type);
            new PlaceTask().execute(url, type);
        }
    }

    private String getNearbyPlacesUrl(double lat, double lng, String placeType) {
        String apiKey = "AIzaSyAfWwxNGWagAh8MHKezAouLu6n708DUbQ"; // Replace with your actual API key
        return "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=" + lat + "," + lng +
                "&radius=5000&type=" + placeType +
                "&sensor=true&key=" + apiKey;
    }

    private class PlaceTask extends AsyncTask<String, Integer, String> {
        String type;

        @Override
        protected String doInBackground(String... strings) {
            type = strings[1];
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                InputStreamReader reader = new InputStreamReader(conn.getInputStream());
                int ch;
                while ((ch = reader.read()) != -1) {
                    result.append((char) ch);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return result.toString();
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String s) {

            try {

                JSONObject jsonObject = new JSONObject(s);
                JSONArray results = jsonObject.getJSONArray("results");

                for (int i = 0; i < results.length(); i++) {

                    JSONObject place = results.getJSONObject(i);
                    String name = place.getString("name");

                    JSONObject location = place.getJSONObject("geometry")
                            .getJSONObject("location");

                    double lat = location.getDouble("lat");
                    double lng = location.getDouble("lng");

                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lat, lng))
                            .title(name)
                            .icon(BitmapDescriptorFactory.defaultMarker(
                                    type.equals("police")
                                            ? BitmapDescriptorFactory.HUE_RED
                                            : BitmapDescriptorFactory.HUE_BLUE)));

                    placeList.add(new PlaceModel(name, type, lat, lng));
                }

                placeAdapter.notifyDataSetChanged();

            } catch (Exception e) { // ✅ Proper catch block
                e.printStackTrace();
            }
        }
    }
}