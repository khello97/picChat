package se.umu.cs.khalil.picchat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import se.umu.cs.khalil.picchat.Adapters.MyAdapterForFriendListFragment;

public class FriendListFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private DatabaseReference mDatabaseFriendListReference;
    private MyAdapterForFriendListFragment mAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_and_fragment_for_list_of_users, container, false);

        //Initierar databas referens till currentUsers friendList
        mDatabaseFriendListReference = FirebaseDatabase.getInstance().getReference().child("Users")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("friendsList");

        //Initierar samt sätter layouten för RecyclerView
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //konfigurerar listan med element som ska visas i adaptern med hjälp av recycler ovan.
        //Koden nedan visar att recyclern kommer att innehålla element av typer User().
        //Elementen som kommer visas i adaptern finns befinner sig i databas referensen
        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(mDatabaseFriendListReference, User.class)
                        .build();

        //initierar adaptern med MyAdapterForFriendListFragment(), där den konfigurerade recyclerna kopplas med adaptern
        mAdapter = new MyAdapterForFriendListFragment(options, getContext());
        mRecyclerView.setAdapter(mAdapter);

        return v;
    }

    //adaptern börjar lyssna när fragmentet startas
    @Override
    public void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    //adaptern slutar lyssna när fragmentet stoppas, dvs när användaren byter fragment ellet aktivitet
    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

}
