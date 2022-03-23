package se.umu.cs.khalil.picchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import se.umu.cs.khalil.picchat.Adapters.MyAdapterForSendingActivity;

public class SendingImageActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private DatabaseReference mDatabaseFriendListReference;
    private MyAdapterForSendingActivity mAdapter;
    private FirebaseUser mCurrentUser;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_and_fragment_for_list_of_users);

        mTextView = (TextView) findViewById(R.id.textView);
        mTextView.setText("Send to: ");

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        //Referens till currentUser´s friendslist
        mDatabaseFriendListReference = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(mCurrentUser.getUid()).child("friendsList");

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        //Bilden som ska skickas iväg sparas i byteArray
        byte[] byteArray = getIntent().getByteArrayExtra("image");

        //konfigurerar listan med element som ska visas i en recycler
        //i detta fall alla element i currentUser´s friendlist
        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(mDatabaseFriendListReference, User.class)
                .build();

        //initierar en adapter med "MyAdapterForSendingActivity"
        mAdapter = new MyAdapterForSendingActivity(options, SendingImageActivity.this, byteArray);
        mAdapter.startListening();
        mRecyclerView.setAdapter(mAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_in_sending_activity, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
            if(item.getItemId() == R.id.exit){
                onBackPressed();
            }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}