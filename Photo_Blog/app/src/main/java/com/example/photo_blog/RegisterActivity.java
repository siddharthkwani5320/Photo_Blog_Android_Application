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

public class RegisterActivity extends AppCompatActivity {
    private EditText reg_email;
    private EditText reg_password;
    private EditText reg_confirm_password;
    private Button reg_btn;
    private Button reg_login_btn;
    private ProgressBar reg_progress;

    FirebaseAuth mauth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mauth=FirebaseAuth.getInstance();

        reg_email=(EditText) findViewById(R.id.reg_email);
        reg_password=(EditText) findViewById(R.id.reg_password);
        reg_confirm_password=(EditText) findViewById(R.id.reg_confirm_password);
        reg_btn=(Button) findViewById(R.id.register_btn);
        reg_login_btn=(Button) findViewById(R.id.reg_login_btn);
        reg_progress=(ProgressBar) findViewById(R.id.reg_login_progress);

        reg_login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        reg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email= reg_email.getText().toString();
                String pass=reg_password.getText().toString();
                String confirm_pass=reg_confirm_password.getText().toString();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass) &&
                !TextUtils.isEmpty(confirm_pass)){
                    if(pass.equals(confirm_pass)){
                        reg_progress.setVisibility(View.VISIBLE);
                        mauth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    Intent setupIntent=new Intent(RegisterActivity.this,SetupActivity.class);
                                    startActivity(setupIntent);
                                    finish();
                                }
                                else{
                                    String error=task.getException().getMessage();
                                    Toast.makeText(RegisterActivity.this, "Error Occured: "+error, Toast.LENGTH_LONG).show();
                                }
                                reg_progress.setVisibility(View.INVISIBLE);
                            }
                        });

                    }
                    else{
                        Toast.makeText(RegisterActivity.this, "Confirm Password  " +
                                "and Password does not match", Toast.LENGTH_LONG  ).show();
                    }
                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentuser=mauth.getCurrentUser();
        if(currentuser!=null){
            sendtoMain();
        }
    }

    private void sendtoMain() {
        Intent mainintent=new Intent(RegisterActivity.this,MainActivity.class);
        startActivity(mainintent);
        finish();
    }
}