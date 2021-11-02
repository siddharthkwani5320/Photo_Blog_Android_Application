package com.example.photo_blog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private EditText loginemail;
    private EditText loginpass;
    private Button loginbtn;
    private Button regbtn;
    private FirebaseAuth mauth;
    private ProgressBar loginprogress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mauth=FirebaseAuth.getInstance();

        loginemail=(EditText) findViewById(R.id.reg_email);
        loginpass=(EditText) findViewById(R.id.reg_confirm_password);
        loginbtn=(Button) findViewById(R.id.register_btn);
        regbtn=(Button) findViewById(R.id.reg_login_btn);
        loginprogress=(ProgressBar) findViewById(R.id.reg_login_progress);

        regbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent regIntent=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(regIntent);
            }
        });
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email=loginemail.getText().toString();
                String pass=loginpass.getText().toString();
                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass)){
                    loginprogress.setVisibility(View.VISIBLE);
                    mauth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                sendtoMain();
                            }
                            else{
                                String error=task.getException().getMessage();
                                Toast.makeText(LoginActivity.this,"Error: "+error,
                                        Toast.LENGTH_LONG).show();
                            }
                            loginprogress.setVisibility(View.INVISIBLE);
                        }
                    });

                }
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mauth.getCurrentUser();
        if(currentUser!=null){
            sendtoMain();

        }
    }

    private void sendtoMain() {
        Intent mainintent=new Intent(LoginActivity.this,MainActivity.class);
        startActivity(mainintent);
        finish() ;
    }
}