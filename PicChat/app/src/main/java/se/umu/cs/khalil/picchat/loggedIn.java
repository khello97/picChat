package se.umu.cs.khalil.picchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class loggedIn extends AppCompatActivity {

   private BottomNavigationView mBottomNav;
   private FloatingActionButton mFAB;
   private Fragment selectedFragment;
   private Bitmap mBitmap;
   private static final int Image_Capture_Code = 1;
   private DatabaseReference RefRequests;
   private BadgeDrawable badgeDrawable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);

        initializeVariables();

        //Om det finns en savesInstanceState av vald fragment visas denna ist, annars visas default fragment, FriendListFragment
        if(savedInstanceState != null){
            selectedFragment = getSupportFragmentManager().getFragment(savedInstanceState, "selectedFragment");
            mFAB.setVisibility(savedInstanceState.getInt("FAB"));
        }

        //startar transaktionen mellan fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();


        //en Listener till bottomNavigationBar, där användaren kan navigera mellan två olika fragment,
        //FriendListFragment eller ProfileFragment
        mBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){
                    case R.id.nav_friends:
                        selectedFragment = new FriendListFragment();
                        mFAB.setVisibility(View.VISIBLE);
                        break;

                    case R.id.nav_profile:
                        selectedFragment = new ProfileFragment();
                        mFAB.setVisibility(View.GONE);
                        break;

                }
                //startar transaktionen mellan fragment
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();

                return true;
            }
        });

        //Startar helt enkelt en ny aktivitet (ACTION_IMAGE_CAPTURE) där användaren får möjlighet att ta bild
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, Image_Capture_Code);

            }
        });

    }

    //Initierar nödvändiga variabler
    private void initializeVariables() {
        RefRequests = FirebaseDatabase.getInstance().getReference().child("Requests")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        mFAB = (FloatingActionButton) findViewById(R.id.floating_camera_button);
        mBottomNav = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        mBottomNav.setSelectedItemId(R.id.nav_friends);
        selectedFragment = new FriendListFragment();

        badgeDrawable = mBottomNav.getOrCreateBadge(R.id.nav_profile);
        amountRequestsBadger();
    }

    //En funktion som räknar ut antalet request (vänförfrågningar) currentUser har
    //och sedan visar dessa som en badge vid profil-ikonen i bottomNavigationBar
    private void amountRequestsBadger(){

        RefRequests.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mBottomNav.removeBadge(R.id.nav_profile);
                badgeDrawable = mBottomNav.getOrCreateBadge(R.id.nav_profile);
                int requests = 0;

                for(DataSnapshot shot: snapshot.getChildren()){
                        requests++;
                }

                if(requests == 0){
                    badgeDrawable.setVisible(false);
                }
                else {
                    badgeDrawable.setVisible(true);
                    badgeDrawable.setNumber(requests);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    //Denna funktion körs efter "Kamera-Intent"
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Image_Capture_Code && resultCode == RESULT_OK && data != null){

            //lagrar bilden i en Bitmap
            mBitmap = (Bitmap) data.getExtras().get("data");

            //konverterar bitmap till en JPEG bild som sedan lagras i en byteArray
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            mBitmap.compress(Bitmap.CompressFormat.JPEG,100, stream);
            byte[] byteArray = stream.toByteArray();

            //stänger av stream och återvinner bitmap så jag friar resurser
            try {
                mBitmap.recycle();
                stream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            //skickar iväg byteArrayen (som innehåller bilden) med intent till en ny aktivitet
            Intent i = new Intent(loggedIn.this, SendingImageActivity.class);
            i.putExtra("image",byteArray);
            startActivity(i);



        }

    }

    //tar bort back-knappens funktionalitet, eftersom användaren ska endast kunna gå tillbaka till
    //LoggaIn-vyn genom att logga ut
    @Override
    public void onBackPressed() {

    }

    //sparar valda fragment´s state, samt FABs visibility state
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        getSupportFragmentManager().putFragment(outState, "selectedFragment", selectedFragment);

        outState.putInt("FAB", mFAB.getVisibility());

    }

    //lägger till en actionbar_menu genom att infiltrera nuvarande menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    //Finns endast ett element i menu, vilket är "addFriend" item
    //ifall användaren klickar på elementen kommer enheten att föras vidare till AddFriendsActivity
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.add_friend){
            startActivity(new Intent(loggedIn.this, AddFriendsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}