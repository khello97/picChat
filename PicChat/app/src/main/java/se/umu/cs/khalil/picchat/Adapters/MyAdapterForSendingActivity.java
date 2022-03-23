package se.umu.cs.khalil.picchat.Adapters;

import android.content.Context;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;


import se.umu.cs.khalil.picchat.R;
import se.umu.cs.khalil.picchat.User;

public class MyAdapterForSendingActivity extends FirebaseRecyclerAdapter<User, MyAdapterForSendingActivity.MyViewHolder> {

    private final Context mContext;
    private final DatabaseReference RefCurrentUserFriendsList, RefSent, RefUserName;
    private final StorageReference mStorageReference;
    private final byte[] imageByte;
    private boolean sendEvent = false;
    private static String currentUserName;

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public MyAdapterForSendingActivity(@NonNull FirebaseRecyclerOptions<User> options, Context context, byte[] imageByte) {
        super(options);
        this.mContext = context;
        this.imageByte = imageByte;

        //Initierar databas referenser
        RefUserName = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("userName");

        RefCurrentUserFriendsList = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("friendsList");

        RefSent = FirebaseDatabase.getInstance().getReference().child("Sent");

        //Initierar storage referens
        mStorageReference = FirebaseStorage.getInstance().getReference().child("Sent");

        getCurrentUserName();

    }


    @Override
    protected void onBindViewHolder(@NonNull MyAdapterForSendingActivity.MyViewHolder myViewHolder, int i, @NonNull User user) {

        //Tar info från user som kommer från databasen och binder ihop valda attribut med valda komponenter i viewholder
        myViewHolder.fullName.setText(user.getFullName());
        myViewHolder.userName.setText(user.getUserName());

    }

    @NonNull
    @Override
    public MyAdapterForSendingActivity.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_friend, parent, false);

        MyAdapterForSendingActivity.MyViewHolder myViewHolder = new MyAdapterForSendingActivity.MyViewHolder(view);


            myViewHolder.sendButton.setVisibility(View.VISIBLE);

            //En ClickListener som lyssnar på knappen senButton
            myViewHolder.sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setEnabled(false);

                    sendEvent = true;
                    sendImage(myViewHolder.userName.getText().toString());

                }
            });




        return myViewHolder;
    }

    //Funktionen nedan skickar iväg en bild till databasen som endast mottagaren har åtkomst till
    private void sendImage(String mUserName) {

        if(imageByte != null) {
            RefCurrentUserFriendsList.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(sendEvent) {
                        //Går igenom alla användare i currentUser´s friendList i databasen
                        for (DataSnapshot shot : snapshot.getChildren()) {

                            String Uid = shot.getKey();
                            User friend = shot.getValue(User.class);

                            //Om användarens username i databasen matchar username i viewHolder
                            //kommer en bild till databasen som endast mottagaren har åtkomst till
                            if (friend.getUserName().toLowerCase().trim().equals(mUserName.toLowerCase().trim())) {

                                String ImageUid = UUID.randomUUID().toString();

                                RefSent.child(Uid).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child("userName").setValue(currentUserName);

                                RefSent.child(Uid).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("messages")
                                        .child(ImageUid).setValue("imageMessage");

                                mStorageReference.child(Uid).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child(ImageUid).putBytes(imageByte);

                                Toast.makeText(mContext, "Sent", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                    sendEvent = false;

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
    }

    //En funktion som sparar currentUser´s användarnamn i den globala String variabeln currentUserName
    private void getCurrentUserName(){
        RefUserName.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                currentUserName = snapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    class MyViewHolder extends RecyclerView.ViewHolder{

        public TextView userName, fullName;
        public Button sendButton;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            //Initierar komponenter i viewHolder
            userName = (TextView) itemView.findViewById(R.id.username_item);
            fullName = (TextView) itemView.findViewById(R.id.full_name_item);
            sendButton = (Button) itemView.findViewById(R.id.send_button);

        }
    }
}