package com.example.photo_blog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

public class NewPostActivity extends AppCompatActivity {

    private Toolbar newpostToolbar;

    private Uri postimage_uri=null;

    private ImageView newpostimage;
    private EditText newpostdesc;
    private Button postblogbtn;
    private ProgressBar post_progress;

    private StorageReference storageReference;
    private FirebaseFirestore fstore;
    private FirebaseAuth mauth;

    private String current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        newpostToolbar=findViewById(R.id.new_post_toolbar);
        setSupportActionBar(newpostToolbar);
        getSupportActionBar().setTitle("Add New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mauth=FirebaseAuth.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference();
        fstore=FirebaseFirestore.getInstance();

        current_user_id=mauth.getCurrentUser().getUid();

        newpostimage=findViewById(R.id.new_post_image);
        newpostdesc=findViewById(R.id.post_description);
        postblogbtn=findViewById(R.id.post_btn);
        post_progress=findViewById(R.id.post_progress);

        newpostimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512,512)
                        .setAspectRatio(1,1)
                        .start(NewPostActivity.this);
            }
        });

        postblogbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String desc=newpostdesc.getText().toString();
                if(!TextUtils.isEmpty(desc) && postimage_uri!=null){
                    post_progress.setVisibility(View.VISIBLE);
                    String randomName= FieldValue.serverTimestamp().toString();
                    StorageReference file_path=storageReference.child("post_images").child(randomName+".jpg");
                    file_path.putFile(postimage_uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                 storeFirestore(file_path,desc);
                            }else{
                                String error = task.getException().getMessage();
                                Toast.makeText(NewPostActivity.this, "Error:" + error, Toast.LENGTH_SHORT).show();
                                post_progress.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                }
            }
        });
    }

    private void storeFirestore(StorageReference reference,String desc) {
        reference.getDownloadUrl().
                addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Map<String,Object> postMap=new HashMap<>();
                        postMap.put("imageUrl",uri.toString());
                        postMap.put("description",desc);
                        postMap.put("userId",current_user_id);
                        fstore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(NewPostActivity.this, "Post Uploaded Successfully", Toast.LENGTH_SHORT).show();
                                    Intent mainIntent=new Intent(NewPostActivity.this,MainActivity.class);
                                    startActivity(mainIntent);
                                }else{
                                    String error = task.getException().getMessage();
                                    Toast.makeText(NewPostActivity.this, "Error:" + error, Toast.LENGTH_SHORT).show();
                                }
                                post_progress.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        startActivity(new Intent(NewPostActivity.this,MainActivity.class));
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                postimage_uri=result.getUri();
                newpostimage.setImageURI(postimage_uri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}