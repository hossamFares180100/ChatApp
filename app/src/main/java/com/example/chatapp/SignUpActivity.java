package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

public class SignUpActivity extends AppCompatActivity implements TextWatcher {


    EditText et_name, et_email, et_pass;
    TextView signIn;
    CircularProgressButton btn_sign_up;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestoreInstance;
    DocumentReference currentUserDocRef;
    Drawable d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        init();

        et_name.addTextChangedListener(this);
        et_email.addTextChangedListener(this);
        et_pass.addTextChangedListener(this);

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent n=new Intent(SignUpActivity.this,SignInActivity.class);
                startActivity(n);
                finish();
            }
        });


        btn_sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            btn_sign_up.startAnimation();
            createUser();
            }
        });

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void init() {
        et_name = findViewById(R.id.editTextName);
        et_email = findViewById(R.id.editTextEmail);
        et_pass = findViewById(R.id.editTextPassword);
        signIn=findViewById(R.id.txt_sign_In);
        btn_sign_up = findViewById(R.id.buttonSignUp);
        d = getResources().getDrawable(R.drawable.ic_baseline_done);
        mAuth = FirebaseAuth.getInstance();
        firestoreInstance = FirebaseFirestore.getInstance();
        //firestoreInstance.collection("users").document(mAuth.getUid());
    }

    private void startMainActivity(){
        Intent n = new Intent(this,MainActivity.class);
        n.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(n);
    }

    private void createUser() {

        String name = et_name.getText().toString().trim();
        String email = et_email.getText().toString().trim();
        String pass = et_pass.getText().toString().trim();
        if(email.isEmpty()){
            et_email.setError("Email is needed");
            et_email.requestFocus();
            btn_sign_up.revertAnimation();
            return;

        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            et_email.setError("Please enter a valid email.");
            et_email.requestFocus();
            btn_sign_up.revertAnimation();
            return;
        }

        if(pass.isEmpty()){
            et_pass.setError("password is needed");
            et_pass.requestFocus();
            btn_sign_up.revertAnimation();
            return;
        }
        if(name.isEmpty()){
            et_name.setError("Name is needed");
            et_name.requestFocus();
            btn_sign_up.revertAnimation();
            return;
        }

        if(pass.length()<6){
            et_pass.setError("password should be at least 6 char long");
            et_pass.requestFocus();
            btn_sign_up.revertAnimation();
            return;
        }

        createNewUser(name,email,pass);
    }

    private void createNewUser(String name,String email,String pass)
    {
        mAuth.createUserWithEmailAndPassword(email,pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        currentUserDocRef = firestoreInstance.document("users/"+mAuth.getUid());
                        User u = new User(name,"",SignInActivity.token);
                        currentUserDocRef.set(u);
                        if(task.isSuccessful()){
                            btn_sign_up.doneLoadingAnimation(Color.parseColor("#0F2DCA"),drawableToBitmap(d));
                            startMainActivity();
                        }else{
                            if(task.getException() instanceof FirebaseAuthUserCollisionException){
                                userLogin(email,pass);
                            }else{
                                btn_sign_up.revertAnimation();
                                Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }

                    }
                });

    }

    private void userLogin(String mail, String password) {
        mAuth.signInWithEmailAndPassword(mail,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            btn_sign_up.doneLoadingAnimation(Color.parseColor("#0F2DCA"),drawableToBitmap(d));
                            startMainActivity();
                        }else{
                            btn_sign_up.revertAnimation();
                            Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }



    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        btn_sign_up.setEnabled(et_name.getText().toString().trim().length() > 0 && et_email.getText().toString().trim().length() > 0 && et_pass.getText().toString().trim().length() > 0);

    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}