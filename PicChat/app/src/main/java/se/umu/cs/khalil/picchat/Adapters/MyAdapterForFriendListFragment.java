package se.umu.cs.khalil.picchat.Adapters;

import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import se.umu.cs.khalil.picchat.R;
import se.umu.cs.khalil.picchat.ShowMessageActivity;
import se.umu.cs.khalil.picchat.User;

public class MyAdapterForFriendListFragment extends FirebaseRecyclerAdapter<User, MyAdapterForFriendListFragment.MyViewHolder> {

    private final Context mContext;
    private final DatabaseReference mRefCurrentUserFriendsList, mRefSent;
    private final StorageReference mStorageReference;
    private boolean runDeleteFriendEvent, deleteFromSent;

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public MyAdapterForFriendListFragment(@NonNull FirebaseRecyclerOptions<User> options, Context context) {
        super(options);
        this.mContext = context;

        //Initierar databas referenser samt storage referens
        mRefCurrentUserFriendsList = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("friendsList");

        mStorageReference = FirebaseStorage.getInstance().getReference().child("Sent")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        mRefSent = FirebaseDatabase.getInstance().getReference().child("Sent")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());


    }


    @Override
    protected void onBindViewHolder(@NonNull MyAdapterForFriendListFragment.MyViewHolder myViewHolder, int i, @NonNull User user) {

        //Tar info fr??n user som kommer fr??n databasen och binder ihop valda attribut med valda komponenter i viewholder
        myViewHolder.fullName.setText(user.getFullName());
        myViewHolder.userName.setText(user.getUserName());

        //En LongClickListener som lyssnar p?? "l??nga klick" p?? en element i v??nlistan
        //Detta g??r att en tabort-knapp dyker upp f??r den valda anv??ndaren
        myViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                myViewHolder.deleteFriendButton.setVisibility(View.VISIBLE);

                //En ClickListener som lyssnar p?? deleteFriendButton
                myViewHolder.deleteFriendButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        runDeleteFriendEvent = true;
                        deleteFriend(user.getUserName());
                        myViewHolder.deleteFriendButton.setVisibility(View.GONE);
                    }
                });

                return true;
            }
        });

        checkForIncomingMessages(myViewHolder, user.getUserName());

    }

    @NonNull
    @Override
    public MyAdapterForFriendListFragment.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_friend, parent, false);
        MyAdapterForFriendListFragment.MyViewHolder myViewHolder = new MyAdapterForFriendListFragment.MyViewHolder(view);

        return myViewHolder;
    }
    
//Funktionen nedan h??ller koll p?? v??ntande meddelanden
    private void checkForIncomingMessages(MyAdapterForFriendListFragment.MyViewHolder viewHolder, String mUserName) {

        mRefCurrentUserFriendsList.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                viewHolder.message.setVisibility(View.GONE);

                //g??r igenom alla anv??ndare i currentUser??s friendList
                for (DataSnapshot shot : snapshot.getChildren()) {

                    String friendUid = shot.getKey();

                    mRefSent.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            //g??r igenom alla "pending" meddelande till currentUser
                            for (DataSnapshot snapShot : snapshot.getChildren()) {

                                String senderUid = snapShot.getKey();

                                //Om Uid f??r anv??ndaren i friendsList matchar ett Uid i grenen "Sent/CurrentUserUid" i databasen
                                //h??mtas ett meddelande i taget fr??n s??ndaren med hj??lp av en for-loop
                                if (senderUid.equals(friendUid)) {
                                    String senderUserName = snapShot.child("userName").getValue(String.class);

                                    if (senderUserName.equals(mUserName)) {
                                        for (DataSnapshot imageShot : snapShot.child("messages").getChildren()) {

                                            String imageUid = imageShot.getKey();

                                            //En icon visas vid anv??ndaren som har ett "pending"-meddelande
                                            viewHolder.message.setVisibility(View.VISIBLE);

                                            //En ClickListener som lyssna p?? Viewn i viewHolder
                                            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    //Bilden h??mtas fr??n storage (FireBase) och sparar den i en variabel av typen byte[]
                                                    //sedan tar bilden bort fr??n b??de storage respektive databas referens
                                                    //d??refter skickas anv??ndaren till en sendingActivity d??r bilden visas i helsk??rm
                                                    final long FIVE_MEGABYTES = 1024 * 1024 * 5;
                                                    mStorageReference.child(senderUid).child(imageUid).getBytes(FIVE_MEGABYTES).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                                        @Override
                                                        public void onSuccess(byte[] bytes) {
                                                            Intent intent = new Intent(mContext, ShowMessageActivity.class);
                                                            intent.putExtra("data", bytes);
                                                            removeSeenImage(senderUid, imageUid);
                                                            mContext.startActivity(intent);

                                                        }
                                                    });

                                                }
                                            });
                                            break;
                                        }

                                    }
                                }
                            }

                        }


                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    //En funktion som tar bort en befintlig v??n
    private void deleteFriend(String userName){

        mRefCurrentUserFriendsList.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(runDeleteFriendEvent) {

                    //g??r igenom alla anv??ndare i currentUser??s friendList i databasen
                    for (DataSnapshot shot : snapshot.getChildren()) {

                        String friendUserName = shot.child("userName").getValue(String.class);

                        //Om anv??ndarens username i databasen matchar username i viewHolder
                        //kommer hen att tas bort fr??n CurrentUser??s friendList samt kommer
                        //CurrentUser??s att tas bort fr??n deras friendList
                        if (friendUserName.toLowerCase().equals(userName.toLowerCase())) {

                            String Uid = shot.getKey();
                            mRefCurrentUserFriendsList.child(Uid).removeValue();

                            if(!Uid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                FirebaseDatabase.getInstance().getReference().child("Users").child(Uid).child("friendsList")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
                            }
                        }
                    }
                    runDeleteFriendEvent = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    //Funktionen nedan tar bort en bild fr??n databasen samt storage i FireBase
    //Denna funktion anv??nds efter att en bild har ??ppnats/har blivit sedd
    private void removeSeenImage(String senderUid, String imageUid){
        mStorageReference.child(senderUid).child(imageUid).delete();
        mRefSent.child(senderUid).child("messages").child(imageUid).removeValue();

        deleteFromSent = true;
        mRefSent.child(senderUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(deleteFromSent){
                    int a = 0;
                    for(DataSnapshot shot: snapshot.child("messages").getChildren()){
                        a++;
                    }
                    if(a == 0){
                        mRefSent.child(senderUid).removeValue();
                    }
                }
                deleteFromSent = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    class MyViewHolder extends RecyclerView.ViewHolder{

        public TextView userName, fullName;
        public ImageView message;
        public Button deleteFriendButton;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            //Initierar komponenter i viewHolder
            message = (ImageView) itemView.findViewById(R.id.image_message);
            userName = (TextView) itemView.findViewById(R.id.username_item);
            fullName = (TextView) itemView.findViewById(R.id.full_name_item);
            deleteFriendButton = (Button) itemView.findViewById(R.id.delete_button);

        }
    }
}
