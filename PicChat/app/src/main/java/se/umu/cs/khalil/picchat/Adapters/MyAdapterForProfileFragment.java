package se.umu.cs.khalil.picchat.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import se.umu.cs.khalil.picchat.R;
import se.umu.cs.khalil.picchat.User;

public class MyAdapterForProfileFragment extends FirebaseRecyclerAdapter<User, MyAdapterForProfileFragment.MyViewHolder> {

    private final Context mContext;
    private final DatabaseReference RefRequests, RefCurrentUserFriendsList, RefAllUsers;
    private boolean runDeclineEvent, runAcceptEvent;
    private static User currentUser;


    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public MyAdapterForProfileFragment(@NonNull FirebaseRecyclerOptions<User> options, Context context) {
        super(options);
        this.mContext = context;

        //Initierar databas referenser samt storage referens
        RefCurrentUserFriendsList = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("friendsList");

        RefRequests = FirebaseDatabase.getInstance().getReference().child("Requests")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        RefAllUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        getCurrentUser();
    }


    @Override
    protected void onBindViewHolder(@NonNull MyAdapterForProfileFragment.MyViewHolder myViewHolder, int i, @NonNull User user) {

        //Tar info från user som kommer från databasen och binder ihop valda attribut med valda komponenter i viewholder
        myViewHolder.fullName.setText(user.getFullName());
        myViewHolder.userName.setText(user.getUserName());

    }

    @NonNull
    @Override
    public MyAdapterForProfileFragment.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_friend, parent, false);

        MyAdapterForProfileFragment.MyViewHolder myViewHolder = new MyAdapterForProfileFragment.MyViewHolder(view);


            myViewHolder.acceptButton.setVisibility(View.VISIBLE);
            myViewHolder.declineButton.setVisibility(View.VISIBLE);

            //ClickListeners till knapparna accept- respektive declineButton
            myViewHolder.acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    runAcceptEvent = true;
                    acceptRequest(myViewHolder.userName.getText().toString());
                }
            });

            myViewHolder.declineButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    runDeclineEvent = true;
                    declineRequest(myViewHolder.userName.getText().toString());
                }
            });



        return myViewHolder;
    }


    //En funktion som avslår/decline vänförfrågan
    private void declineRequest(String mUserName) {

            RefRequests.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(runDeclineEvent) {
                        //Går igenom alla requests för currentUser
                        for (DataSnapshot shot : snapshot.getChildren()) {

                            String friendUserName = shot.child("userName").getValue(String.class).toLowerCase().trim();
                            //Om användarnamnet från databasen(/Requests/currentUser) matchar användarnamnet i viewHolder
                            //Kommer vänförfrågan att tas bort från databasen
                            if (friendUserName.equals(mUserName.toLowerCase().trim())) {

                                RefRequests.child(shot.getKey()).removeValue();
                                Toast.makeText(mContext, "Request declined", Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                    runDeclineEvent = false;

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

    }

    //En funktion som accepterar vänförfrågan/request
    private void acceptRequest(String mUserName) {

        RefRequests.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(runAcceptEvent) {

                    //går igenom alla requests för currentUser
                    for (DataSnapshot shot : snapshot.getChildren()) {

                        String friendUserName = shot.child("userName").getValue(String.class).toLowerCase().trim();
                        String friendUid = shot.getKey();

                        //Om användarnamnet från databasen(/Requests/currentUserUid/friendUid) matchar användarnamnet i viewHolder
                        //kommer användaren att läggas till i currentUser´s friendList, currentUser kommer också att läggas till i
                        //den andra användarens friendList, därefter kommer denna request att tas bort från databasen
                        if (friendUserName.equals(mUserName.toLowerCase().trim())) {

                            User friendAccepted = shot.getValue(User.class);
                            RefCurrentUserFriendsList.child(friendUid).setValue(friendAccepted);

                            if(!friendUserName.equals(currentUser.getUserName())) {
                                RefAllUsers.child(friendUid).child("friendsList").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(currentUser);
                            }

                            RefRequests.child(friendUid).removeValue();
                            Toast.makeText(mContext, "Request accepted", Toast.LENGTH_SHORT).show();
                        }

                    }
                }
                runAcceptEvent = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //En funktion som sparar currentUser i den globala User() variabeln currentUser
    private void getCurrentUser(){

        RefAllUsers.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                currentUser = snapshot.getValue(User.class);


            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        public TextView userName, fullName;
        public Button acceptButton, declineButton;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            //Initierar komponenter i viewHolder
            acceptButton = (Button) itemView.findViewById(R.id.accept_button);
            declineButton = (Button) itemView.findViewById(R.id.decline_button);
            userName = (TextView) itemView.findViewById(R.id.username_item);
            fullName = (TextView) itemView.findViewById(R.id.full_name_item);


        }
    }
}