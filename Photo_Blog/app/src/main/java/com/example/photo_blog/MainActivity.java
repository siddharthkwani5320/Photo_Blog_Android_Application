package com.example.photo_blog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Set;


import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Toolbar mainToolbar;
    private FirebaseAuth mauth;
    private FirebaseFirestore fstore;

    private FloatingActionButton addpostbtn;

    private String curruser_id;

    private BottomNavigationView mainbottomnav;

    private RecyclerView recyclerView;

    ArrayList<BlogPost> blog_list;
    BlogRecyclerAdapter blogRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mauth=FirebaseAuth.getInstance();
        fstore=FirebaseFirestore.getInstance();

        recyclerView=findViewById(R.id.blog_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        blog_list=new ArrayList<BlogPost>();
        blogRecyclerAdapter=new BlogRecyclerAdapter(MainActivity.this,blog_list);

        recyclerView.setAdapter(blogRecyclerAdapter);

        EventChangeListner();

        mainToolbar=(Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);

        getSupportActionBar().setTitle("Photo Blog");

        mainbottomnav=findViewById(R.id.main_bottom_nav);

        mainbottomnav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.bottom_home:
                        return true;
                    case R.id.botton_account:
                        Intent accIntent=new Intent(MainActivity.this,SetupActivity.class);
                        startActivity(accIntent);
                        finish();
                        return true;
                    case R.id.bottom_noti:
                        return true;
                    default:
                        return false;
                }
            }
        });

        addpostbtn=findViewById(R.id.add_post_button);

        addpostbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newIntent=new Intent(MainActivity.this,NewPostActivity.class);
                startActivity(newIntent);
                finish();
            }
        });

    }

    private void EventChangeListner() {

        fstore.collection("Posts")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error!=null){
                            Toast.makeText(MainActivity.this, "Firestore fetching error"+error.getMessage(), Toast.LENGTH_SHORT).show();
                        }else{
                            if(value!=null) {
                                for (DocumentChange doc : value.getDocumentChanges()) {
                                    if (doc.getType() == DocumentChange.Type.ADDED) {

                                        blog_list.add(doc.getDocument().toObject(BlogPost.class));

                                    }
                                    blogRecyclerAdapter.notifyDataSetChanged();
                                }
                            }
                            else{
                                Toast.makeText(MainActivity.this, "Fetch Error", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

    }


    @Override
    protected void onStart() {
        super.onStart();
        Intent loginIntent=new Intent(MainActivity.this,LoginActivity.class);
        startActivity( loginIntent );
        finish();
        FirebaseUser currentUser = mauth.getCurrentUser();
        if(currentUser==null){
            sendToLogin();
        }
        else{
            curruser_id=mauth.getCurrentUser().getUid();
            fstore.collection("Users").document(curruser_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        if(!task.getResult().exists()){
                            Intent setupIntent=new Intent(MainActivity.this,SetupActivity.class);
                            startActivity(setupIntent);
                        }
                    }else{
                        String error = task.getException().getMessage();
                        Toast.makeText(MainActivity.this, "Current Error:" + error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                logOut();
                return true;
            case R.id.account:
                Intent settingIntent=new Intent(MainActivity.this, SetupActivity.class);
                startActivity(settingIntent);
                return true;
            default:
                return false;
        }


    }

    private void logOut() {
        mauth.signOut();
        sendToLogin();
    }

    private void sendToLogin() {
        Intent intent=new Intent(MainActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

}