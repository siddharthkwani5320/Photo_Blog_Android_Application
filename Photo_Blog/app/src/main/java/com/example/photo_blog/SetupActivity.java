package com.example.photo_blog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.usage.StorageStats;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ActionMenuView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.BaseRequestOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firestore.v1.FirestoreGrpc;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private CircleImageView setupImage;
    private Uri mainImageUri=null;

    private String user_id;
    private String download_uri;
    private boolean isChanged=false;

    private EditText setup_name;
    private Button setupbtn;
    private ProgressBar setup_progress;

    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore fstore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        Toolbar setToolbar = findViewById(R.id.setup_toolbar);
        setSupportActionBar(setToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseAuth=FirebaseAuth.getInstance();

        user_id=firebaseAuth.getCurrentUser().getUid();

        fstore=FirebaseFirestore.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference();


        setupImage=findViewById(R.id.profile_picture);
        setup_name=findViewById(R.id.setup_hint_name);
        setupbtn=findViewById(R.id.setup_btn);
        setup_progress=findViewById(R.id.setup_progress);

        setup_progress.setVisibility(View.VISIBLE);
        setupbtn.setEnabled(false);

        fstore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        String name=task.getResult().getString("name");
                        String image=task.getResult().getString("image");
                        mainImageUri = Uri.parse(image);
                        setup_name.setText(name);
                        RequestOptions placeholder=new RequestOptions();
                        placeholder.placeholder(R.drawable.profile);
                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholder).load(image).into(setupImage);

                    }
                }else{
                    String error=task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "Error:"+error, Toast.LENGTH_SHORT).show();
                }
                setup_progress.setVisibility(View.INVISIBLE);
                setupbtn.setEnabled(true);

            }
        });

        setupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user_name = setup_name.getText().toString();


                if (!TextUtils.isEmpty(user_name) && mainImageUri != null) {
                    setup_progress.setVisibility(View.VISIBLE);
                    if (isChanged) {
                        //Toast.makeText(SetupActivity.this, "Upload Success", Toast.LENGTH_SHORT).show();

                        StorageReference image_path = storageReference.child("profile_images").child(user_id + ".jpg");
                        image_path.putFile(mainImageUri)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        getDownloadUrl(image_path, user_name);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        String error = e.getMessage();
                                        Toast.makeText(SetupActivity.this, "Error:" + error, Toast.LENGTH_SHORT).show();
                                        setup_progress.setVisibility(View.INVISIBLE);
                                    }
                                });

                    }else {
                        getDownloadUrl(null, user_name);
                    }
                }else {
                    Toast.makeText(SetupActivity.this, "Fill the details", Toast.LENGTH_SHORT).show();
                }

            }
        });
        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(SetupActivity.this , "Permission Denied", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(SetupActivity.this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},1);
                    }
                    else{
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1,1)
                                .start(SetupActivity.this);
                    }
                }


            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        startActivity(new Intent(SetupActivity.this,MainActivity.class));
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageUri= result.getUri();
                setupImage.setImageURI(mainImageUri);
                isChanged=true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
    private void getDownloadUrl(StorageReference reference,String user_name){

        if(reference!=null) {
            reference.getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            download_uri = uri.toString();
                            Map<String, String> userMap = new HashMap<>();
                            userMap.put("name", user_name);
                            userMap.put("image", download_uri.toString());
                            fstore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        setup_progress.setVisibility(View.INVISIBLE);
                                        Toast.makeText(SetupActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                                        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                                        startActivity(mainIntent);
                                        finish();
                                    } else {
                                        String error = task.getException().getMessage();
                                        Toast.makeText(SetupActivity.this, "Error:" + error, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
        }
        else{
            download_uri =mainImageUri.toString();
            Map<String, String> userMap = new HashMap<>();
            userMap.put("name", user_name);
            userMap.put("image", download_uri.toString());
            fstore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        setup_progress.setVisibility(View.INVISIBLE);
                        Toast.makeText(SetupActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                        startActivity(mainIntent);
                        finish();
                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error:" + error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }
}