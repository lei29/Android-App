package com.example.lei29.myapp1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.storage.StorageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;


public class SetupActivity extends AppCompatActivity {

    private CircleImageView setupImage;
    private Uri mainImageUri = null;
    private EditText setupName;
    private Button setupBtn;

    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String user_id;

    private boolean isChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);


        Toolbar setupToolbar = findViewById(R.id.setuptoolbar);
        setSupportActionBar(setupToolbar);
        getSupportActionBar().setTitle("Account Setup");

        firebaseAuth = FirebaseAuth.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();
        storageReference = FirebaseStorage.getInstance().getReference();
        setupImage = findViewById(R.id.setup_image);
        setupName = findViewById(R.id.setupName);
        setupBtn = findViewById(R.id.setupBtn);
        firebaseFirestore = FirebaseFirestore.getInstance();
        setupBtn.setEnabled(false);


        //get user id and image from firebase
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){
                    if(task.getResult().exists()){

                        Toast.makeText(SetupActivity.this, "Data exists", Toast.LENGTH_LONG).show();
                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");

                        mainImageUri = Uri.parse(image);
                        setupName.setText(name);
                        RequestOptions placeholder = new RequestOptions();
                        placeholder.placeholder(R.drawable.ic_search_black_24dp);
                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholder).load(image).into(setupImage);

                    }else{
                        Toast.makeText(SetupActivity.this, "Data dose not exists", Toast.LENGTH_LONG).show();

                    }
                }else{
                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "{FIRESTORE Error} : "+error, Toast.LENGTH_LONG).show();

                }
                setupBtn.setEnabled(true);
            }
        });

        //upload user name and image to firebase
        setupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String user_name = setupName.getText().toString();
                if(!TextUtils.isEmpty(user_name) && mainImageUri!=null) {
                    if(isChanged) {

                        //user_id = firebaseAuth.getCurrentUser().getUid();
                        final StorageReference image_path = storageReference.child("profile_image").child(user_id + ".jpg");

                        image_path.putFile(mainImageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                return image_path.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    storeFiretore(task, user_name);
                                } else {
                                    Toast.makeText(SetupActivity.this, "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });


                        image_path.putFile(mainImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    //Uri download_uri = task.getResult().getDownloadUrl();
                                    System.out.println("----------------------------------");
                                    System.out.println(task.getResult().getMetadata().toString());
                                    System.out.println("----------------------------------");
                                    Toast.makeText(SetupActivity.this, task.getResult().toString(), Toast.LENGTH_LONG).show();
                                    //Map<String, String> userMap = new HashMap<>();
                                    //userMap.put("name",user_name);
                                    //userMap.put("image", download_uri.toString());

                                    //firebaseFirestore.collection("Users").document(user_id);
                                } else {
                                    String e = task.getException().getMessage();
                                    Toast.makeText(SetupActivity.this, "Error: " + e, Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                    }else{
                        storeFiretore(null, user_name);
                    }
                }
            }
        });

        //set up image choose image from local
        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){

                        Toast.makeText(SetupActivity.this, "Permision Denied", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                    }else{

                        //Toast.makeText(SetupActivity.this, "You already got the permission", Toast.LENGTH_LONG).show();
                        bringImagePicker();
                    }
                }else{

                    bringImagePicker();

                }
            }
        });



    }

    private void bringImagePicker(){
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(SetupActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageUri = result.getUri();
                setupImage.setImageURI(mainImageUri);
                isChanged = true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void storeFiretore(@NonNull Task<Uri> task, String user_name){
        Uri downloadUri;
        Map<String, String> userMap = new HashMap<>();
        userMap.put("name", user_name);
        if(task != null) {
            downloadUri = task.getResult();

        }else{
            downloadUri = mainImageUri;
        }
        userMap.put("image", downloadUri.toString());
        firebaseFirestore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(SetupActivity.this, "User Setting are updated", Toast.LENGTH_LONG).show();
                    Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "{FIRESTORE Error} : " + error, Toast.LENGTH_LONG).show();
                }

            }
        });
    }



}
