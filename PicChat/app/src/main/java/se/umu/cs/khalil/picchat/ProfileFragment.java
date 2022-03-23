package se.umu.cs.khalil.picchat;

import android.content.Intent;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import se.umu.cs.khalil.picchat.Adapters.MyAdapterForProfileFragment;

public class ProfileFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private DatabaseReference mDatabaseRequestListReference, mRefCurrentUser;
    private MyAdapterForProfileFragment mAdapter;
    private TextView mEmail, mUserName, mFullName;
    private User mCurrentUser;
    private Button mSignOutButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initializeVariables(view);

        getCurrentUser();

        mSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Loggar ut från Firebase-systemet samt tar användaren tillbaka till MainActivity
                //dvs. där man loggar in
                //Alla aktiviteter i stacken rensas innan signOutIntent startas

                FirebaseAuth.getInstance().signOut();
                Intent signOutIntent = new Intent(getContext(), MainActivity.class);
                signOutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(signOutIntent);
            }
        });

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView_profile_fragment);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //konfigurerar listan med element som ska visas i en recycler med hjälp av en adapter
        //i detta fall visas alla vänförfrågningar (Requests) till currentUser
        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(mDatabaseRequestListReference, User.class)
                .build();
        //Initierar adaptern med MyAdapterForProfileFragment som i sin tur läggs till i Recyclern
        mAdapter = new MyAdapterForProfileFragment(options, getContext());
        mRecyclerView.setAdapter(mAdapter);


        return view;
    }

    private void initializeVariables(View view) {
        mRefCurrentUser = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        mDatabaseRequestListReference = FirebaseDatabase.getInstance().getReference().child("Requests")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        mUserName = (TextView) view.findViewById(R.id.profile_username);
        mFullName = (TextView) view.findViewById(R.id.profile_fullname);
        mSignOutButton = (Button) view.findViewById(R.id.signOut_button);
    }

//En funktion som hämtar all info om currentUser från databasen
    private void getCurrentUser(){
        mRefCurrentUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               mCurrentUser = snapshot.getValue(User.class);
                assert mCurrentUser != null;
                mUserName.setText(mCurrentUser.getUserName());
                mFullName.setText(mCurrentUser.getFullName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }
}
