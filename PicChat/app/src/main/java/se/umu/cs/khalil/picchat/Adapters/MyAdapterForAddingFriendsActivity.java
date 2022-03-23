package se.umu.cs.khalil.picchat.Adapters;

import android.content.Context;
import android.util.Log;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import se.umu.cs.khalil.picchat.R;
import se.umu.cs.khalil.picchat.User;

public class MyAdapterForAddingFriendsActivity extends FirebaseRecyclerAdapter <String, MyAdapterForAddingFriendsActivity.MyViewHolder> {

    private final Context mContext;
    private final DatabaseReference RefRequests, RefUserNames;
    private boolean runAddFriendEvent;
    private User currentUser;

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public MyAdapterForAddingFriendsActivity(@NonNull FirebaseRecyclerOptions<String> options, Context context) {
        super(options);
        this.mContext = context;

        getCurrentUser();

        //Initierar databas referenser
         RefRequests = FirebaseDatabase.getInstance().getReference().child("Requests");
         RefUserNames = FirebaseDatabase.getInstance().getReference().child("userNames");
    }

    @Override
    protected void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i, @NonNull String username) {

        //Tar info från user som kommer från databasen och binder ihop valda attribut med valda komponenter i viewholder
      //  myViewHolder.fullName.setText(user.getFullName());
        myViewHolder.userName.setText(username);

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_users, parent, false);

        MyViewHolder myViewHolder = new MyViewHolder(view);

        //En ClickListener som lyssnar på knappen addFriendButton
        myViewHolder.addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                runAddFriendEvent = true;
                v.setEnabled(addFriend(myViewHolder.userName.getText().toString()));

            }
        });

        return myViewHolder;
    }


    //En funktion som skickar en vänförfrågan
    private boolean addFriend(String userName) {

        final boolean[] added = new boolean[1];

        //En lyssnare som går igenom alla användare på databasen
        RefUserNames.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(runAddFriendEvent){

                    //går igenom alla användare i databasen
                    for (DataSnapshot shot : snapshot.getChildren()) {

                        String user = shot.getValue(String.class);
                        String userUid = shot.getKey();

                        //Om användarens username i databasen matchar username i viewHolder
                        //kommer en request (vänförfrågan) att registreras i databasen
                        //ifall nått går fel, kommer ett felmeddelande att visas
                        if (user.toLowerCase().equals(userName.toLowerCase())) {

                            RefRequests.child(userUid).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(currentUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        added[0] = true;
                                     //   Toast.makeText(mContext, "Friend request sent", Toast.LENGTH_SHORT).show();
                                        Toast.makeText(mContext,"Friend request sent", Toast.LENGTH_LONG).show();

                                    } else{
                                        added[0] = false;
                                        Toast.makeText(mContext, "Could not send request", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            break;

                        }

                    }
                    runAddFriendEvent = false;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return added[0];
    }


    //En funktion som sparar currentUser i den globala User() variabeln currentUser
    private void getCurrentUser(){

        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
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

        public TextView userName;
        public Button addFriendButton;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            //Initierar komponenter på viewHolder
            userName = (TextView) itemView.findViewById(R.id.username_item);
            addFriendButton = (Button) itemView.findViewById(R.id.add_friend_button);

        }
    }
}


