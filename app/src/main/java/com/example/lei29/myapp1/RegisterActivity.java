package com.example.lei29.myapp1;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {


    private EditText regEmailText;
    private EditText regPassText;
    private EditText regPassconfirmText;
    private Button regBtn;
    private Button regloginBtn;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        regEmailText = (EditText) findViewById(R.id.signup_email_text);
        regPassText = (EditText) findViewById(R.id.signup_password_text);
        regPassconfirmText = (EditText) findViewById(R.id.pass_confirm_text);
        regBtn = (Button) findViewById(R.id.signup_confirm_btn);
        regloginBtn = (Button) findViewById(R.id.already_signup_btn);

        regloginBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        regBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                final String Email = regEmailText.getText().toString();
                final String Pass = regPassText.getText().toString();
                final String confirm_Pass = regPassconfirmText.getText().toString();

                if(!TextUtils.isEmpty(Email) &&!TextUtils.isEmpty(Pass) && !TextUtils.isEmpty(confirm_Pass)){
                    mAuth.signInWithEmailAndPassword(Email,Pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(Pass.equals(confirm_Pass)){
                                mAuth.createUserWithEmailAndPassword(Email,Pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if(task.isSuccessful()){
                                            System.out.println("lolololollllllllllll");
                                            Intent setupIntent = new Intent(RegisterActivity.this,SetupActivity.class);
                                            Toast.makeText(RegisterActivity.this, "sETUP INTENT CALLED", Toast.LENGTH_LONG).show();
                                            startActivity(setupIntent);
                                            finish();
                                        }else{
                                            String e = task.getException().getMessage();
                                            Toast.makeText(RegisterActivity.this, "Error: "+e, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }else{
                                //String e = task.getException().getMessage();
                                Toast.makeText(RegisterActivity.this, "error: password and confirm password are not match",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser !=null){

            sendToMain();
        }


    }


    private void sendToMain(){
        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    private void sendToLogin(){
        Intent loginIntent = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }
}
