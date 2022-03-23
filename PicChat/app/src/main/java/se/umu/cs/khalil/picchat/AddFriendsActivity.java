package se.umu.cs.khalil.picchat;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import se.umu.cs.khalil.picchat.Adapters.MyAdapterForAddingFriendsActivity;

public class AddFriendsActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private DatabaseReference mDatabase;
    private MyAdapterForAddingFriendsActivity mAdapter;
    private TextView mTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_and_fragment_for_list_of_users);

        mTextView = (TextView) findViewById(R.id.textView);
        mTextView.setText("All PicChat Users:");

        //Initierar databas referens, referensen i databasen innehåller alla användare i systemet
        mDatabase = FirebaseDatabase.getInstance().getReference().child("userNames");

        //Initierar samt sätter layouten för RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));


        //konfigurerar listan med element som ska visas i adaptern med hjälp av recycler ovan.
        //Koden nedan visar att recyclern kommer att innehålla element av typer User().
        //Elementen som kommer visas i adaptern befinner sig i databas referensen, mDataBase
        FirebaseRecyclerOptions<String> options =
                new FirebaseRecyclerOptions.Builder<String>()
                        .setQuery(mDatabase, String.class)
                        .build();

        //Initierar adaptern med MyAdapterForAddingFriendsActivity(), där den konfigurerade recyclerna, options, kopplas med adaptern
        mAdapter = new MyAdapterForAddingFriendsActivity(options, AddFriendsActivity.this);
        mAdapter.startListening();
        mRecyclerView.setAdapter(mAdapter);
    }

    //skapar en menu som innehåller ett element, search_view
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem item = menu.findItem(R.id.search_view);

        SearchView searchView = (SearchView) item.getActionView();

        //En Listener som lyssnar på när text matas in i sök-fältet
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchDatabase(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchDatabase(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    //Funktionen nedan körs när användaren matar in text i sök-fältet
    private void searchDatabase(String s) {

        //Recyclern konfigureras om på nytt där den filtrerar bort element (användare) som inte matchar
        //texten i sök-fältet.
        //"\uf8ff" står för "resten av ordet"
        FirebaseRecyclerOptions<String> options =
                new FirebaseRecyclerOptions.Builder<String>()
                        .setQuery(mDatabase.orderByValue().startAt(s).endAt(s + "\uf8ff"), String.class)
                        .build();

        //Sedan kopplas den nya konfigurationen av recyclern till adaptern och därefter börjar adaptern lyssna om på nytt
        mAdapter = new MyAdapterForAddingFriendsActivity(options, AddFriendsActivity.this);
        mAdapter.startListening();
        mRecyclerView.setAdapter(mAdapter);
    }
}
