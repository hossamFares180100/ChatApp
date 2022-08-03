package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceIdReceiver;
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

public class SignInActivity extends AppCompatActivity implements TextWatcher {

    EditText email,password;
    TextInputLayout email_ADDress;
    TextView signUp,forgetPass;
    CircularProgressButton btn_google,signIn;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 123;
    SharedPreferences.Editor editor;
    SharedPreferences sharedPreferences1;
    Drawable d;
    public static String token="";
    private FirebaseFirestore firestoreInstance;

    DocumentReference currentUserDocRef,currentUserDocRef2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        init();
        getToken();

        email.addTextChangedListener(this);
        password.addTextChangedListener(this);
        firestoreInstance = FirebaseFirestore.getInstance();
        //Code of google start from here
        createRequest();
        btn_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_google.startAnimation();
                signInwithgoogle();
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent n=new Intent(SignInActivity.this,SignUpActivity.class);
                startActivity(n);
                finish();
            }
        });

        forgetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgetPass();
            }
        });

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn.startAnimation();

                final String mail = email.getText().toString().trim();
                final String pass = password.getText().toString().trim();

                if(mail.isEmpty()){
                    email.setError("Email is needed");
                    email.requestFocus();
                    signIn.revertAnimation();
                    return;

                }

                if(!Patterns.EMAIL_ADDRESS.matcher(mail).matches()){
                    email.setError("Please enter a valid email.");
                    email.requestFocus();
                    signIn.revertAnimation();
                    return;
                }

                if(pass.isEmpty()){
                    password.setError("password is needed");
                    password.requestFocus();
                    signIn.revertAnimation();
                    return;
                }

                if(password.length()<6){
                    password.setError("password should be at least 6 char long");
                    password.requestFocus();
                    signIn.revertAnimation();
                    return;
                }
                userLogin(mail,pass);

            }
        });


    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void init(){
        email = findViewById(R.id.editTextTextEmail);
        password = findViewById(R.id.editTextPass);
        signUp = findViewById(R.id.txt_signUp);
        forgetPass = findViewById(R.id.txt_forget);
        btn_google = findViewById(R.id.btn_google);
        signIn = findViewById(R.id.btn_signIn);
        d = getResources().getDrawable(R.drawable.ic_baseline_done);
        mAuth = FirebaseAuth.getInstance();


    }


    private void userLogin(String mail, String password) {

        mAuth.signInWithEmailAndPassword(mail,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            currentUserDocRef2=FirebaseFirestore.getInstance().document("users/"+FirebaseAuth.getInstance().getCurrentUser().getUid());
                            currentUserDocRef2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()) {
                                        Map<String, Object> map = new HashMap<>();
                                        map = task.getResult().getData();
                                        //assert map != null;
                                        if (map != null) {
                                            map.put("token",token);
                                            currentUserDocRef2.update(map);
                                            signIn.doneLoadingAnimation(Color.parseColor("#0F2DCA"),drawableToBitmap(d));
                                            startMainActivity();
                                        }
                                    }
                                }
                            });


                        }else{
                            signIn.revertAnimation();
                            Toast.makeText(SignInActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    private void forgetPass(){

        AlertDialog.Builder alertBuilder= new AlertDialog.Builder(SignInActivity.this);
        View view= LayoutInflater.from(SignInActivity.this).inflate(R.layout.reset_passeord_layout,null,false);
        final CircularProgressButton reset=view.findViewById(R.id.btn_get_pass);
        email_ADDress=view.findViewById(R.id.edit_email_forget);
        alertBuilder.setView(view);
        final AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset.startAnimation();

                String emailAdd =email_ADDress.getEditText().getText().toString().trim();
                if(!isVaildEmailForget(emailAdd)){
                    reset.revertAnimation();
                    return;
                }else {
                    mAuth.sendPasswordResetEmail(emailAdd)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        reset.doneLoadingAnimation(Color.parseColor("#F44336"),drawableToBitmap(d));
                                        Toast.makeText(getApplicationContext(),"Check your email for reset",Toast.LENGTH_LONG).show();
                                        alertDialog.dismiss();
                                    }
                                    else {
                                        reset.revertAnimation();
                                        Toast.makeText(getApplicationContext(),"There is error...!",Toast.LENGTH_LONG).show();
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

        if(mAuth.getCurrentUser()!=null){
            startMainActivity();
        }

    }


    private void startMainActivity(){
        Intent n = new Intent(SignInActivity.this,MainActivity.class);
        n.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(n);
    }

    boolean isVaildEmailForget(String emailAdd){
        if(emailAdd.isEmpty()){
            email_ADDress.getEditText().setError("Email can’t be empty..!");
            email_ADDress.getEditText().requestFocus();
            return false;
        }
        if(emailAdd.length()>50){
            email_ADDress.getEditText().setError("Email is too long..!");
            return false;
        }
        return true;
    }



    //This function used to define account in google

    private void createRequest() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signInwithgoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @SuppressLint("CommitPrefEdits")
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            sharedPreferences1=getSharedPreferences("email_login",MODE_PRIVATE);
                            editor=sharedPreferences1.edit();
                            editor.putBoolean("login",true);
                            editor.apply();
                            currentUserDocRef = firestoreInstance.document("users/"+mAuth.getUid());
                            //Toast.makeText(SignInActivity.this,mAuth.getCurrentUser().getDisplayName() , Toast.LENGTH_SHORT).show();
                            currentUserDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()&&task.getResult().exists()){
                                            //Toast.makeText(SignInActivity.this, "bad", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            startActivity(intent);
                                        }else{
                                            User u = new User(Objects.requireNonNull(mAuth.getCurrentUser()).getDisplayName(), Objects.requireNonNull(mAuth.getCurrentUser().getPhotoUrl()).toString(),token);
                                            currentUserDocRef.set(u);
                                            btn_google.doneLoadingAnimation(Color.parseColor("#F44336"),drawableToBitmap(d));
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            startActivity(intent);
                                        }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    User u = new User(Objects.requireNonNull(mAuth.getCurrentUser()).getDisplayName(), Objects.requireNonNull(mAuth.getCurrentUser().getPhotoUrl()).toString(),token);
                                    currentUserDocRef.set(u);
                                    btn_google.doneLoadingAnimation(Color.parseColor("#F44336"),drawableToBitmap(d));
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                }
                            });


                        } else {

                            Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                            btn_google.revertAnimation();
                        }
                    }
                });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);

            } catch (ApiException e) {
                Log.e("error gmail",e.getMessage());
                Toast.makeText(getApplicationContext(), "Login failed."+e.getMessage(), Toast.LENGTH_SHORT).show();
                btn_google.revertAnimation();
            }
        }
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


    private void getToken(){


        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                token = s;
            }
        });

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        signIn.setEnabled(email.getText().toString().trim().length() > 0 && password.getText().toString().trim().length() > 0);

    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}