package com.example.accidentdetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.example.accidentdetection.R;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainScreen extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {
    public DrawerLayout drawers;
    View headerViw1;
    CircleImageView admin_nav_image;
    private TextView nav_name;
    private TextView nav_email;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        // Firebase Initialization
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();

        databaseReference = firebaseDatabase.getReference("Users").child(firebaseAuth.getCurrentUser().getUid());
        databaseReference.keepSynced(true);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawers = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        // Drawer
        headerViw1 = navigationView.getHeaderView(0);
        nav_name = (TextView) headerViw1.findViewById(R.id.username);
        nav_email = (TextView) headerViw1.findViewById(R.id.usermail);
        admin_nav_image = headerViw1.findViewById(R.id.imageView);
        navigationView.setNavigationItemSelectedListener(this);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Model member = dataSnapshot.getValue(Model.class);
                nav_name.setText(member.getName());
                nav_email.setText(member.getEmail());
                /*
                 * Glide.with(MainScreen.this)
                 * .load(member.getImageurl())
                 * .into(admin_nav_image);
                 */
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainScreen.this, "Retrieve Failed !",
                        Toast.LENGTH_SHORT).show();
            }
        });
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                drawers, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawers.addDrawerListener(toggle);
        toggle.syncState();
        if (savedInstanceState == null) {

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment())
                    .commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (drawers.isDrawerOpen(GravityCompat.START)) {
            drawers.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();

        } else if (id == R.id.nav_contacts) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ContactFragment())
                    .commit();

        } else if (id == R.id.nav_chat) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AIAssistantFragment())
                    .commit();
            Toast.makeText(getApplicationContext(),
                    "Work Development is in progress...", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.nav_profiles) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment())
                    .commit();

        } else if (id == R.id.nav_sos) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SOSFragment())
                    .commit();

        } else if (id == R.id.nav_video) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new VideoFragment())
                    .commit();

        } else if (id == R.id.nav_privacy) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new PrivacyFragment())
                    .commit();

        } else if (id == R.id.nav_aboutapp) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AboutFragment())
                    .commit();

        } else if (id == R.id.nav_aboutdeveloper) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DeveloperFragment())
                    .commit();

        } else if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainScreen.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Toast.makeText(MainScreen.this, "User LogOut...",
                    Toast.LENGTH_SHORT).show();
            startActivity(intent);
            finish();
        }

        drawers.closeDrawer(GravityCompat.START);
        return true;
    }
}