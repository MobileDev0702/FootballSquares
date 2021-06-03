package com.moyersoftware.contender.login;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.moyersoftware.contender.R;
import com.moyersoftware.contender.login.data.User;
import com.moyersoftware.contender.menu.MainActivity;
import com.moyersoftware.contender.util.Util;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class RegisterActivity extends AppCompatActivity {

    Button btn_register;
    TextView txt_signin2, txt_goback2;
    EditText mNameEditTxt, mEmailEditTxt, mPasswordEditTxt, mRepeatPasswordEditTxt;

    // Usual variables
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    String mUserId;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        btn_register        =   findViewById(R.id.btn_register);
        txt_signin2         =   findViewById(R.id.txt_signin2);
        mNameEditTxt        =   findViewById(R.id.register_name_edit_txt);
        mEmailEditTxt       =   findViewById(R.id.register_email_edit_txt);
        mPasswordEditTxt    =   findViewById(R.id.register_password_edit_txt);
        mRepeatPasswordEditTxt = findViewById(R.id.register_repeat_password_edit_txt);

        overrideActivityAnimation();
        initAuth();
        initStatusBar();

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRegisterButtonClicked();
            }
        });


        txt_signin2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });
    }

    private void initAuth() {
        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    mUserId = user.getUid();
                    Map<String,Object> user_ = new HashMap<>();
                    user_.put("name", mNameEditTxt.getText().toString());
                    user_.put("email", mEmailEditTxt.getText().toString());
                    user_.put("id", mUserId);
                    fStore.collection("users").document(mUserId)
                            .set(user_)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("START","success");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("START","error");
                                }
                            });


                        finish();
//                        LoadingActivity.sActivity.finish();
                        Util.setCurrentPlayerId(FirebaseAuth.getInstance()
                                .getCurrentUser().getUid());

                        startActivity(new Intent(RegisterActivity.this, FindFriendsActivity.class));
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /**
     * Creates a cross fade effect between loading and registration screens.
     */
    private void overrideActivityAnimation() {
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_hold);
    }

    private void onRegisterButtonClicked() {
        String email = mEmailEditTxt.getText().toString();
        String password = mPasswordEditTxt.getText().toString();
        String repeatedPassword = mRepeatPasswordEditTxt.getText().toString();
        if (TextUtils.isEmpty(mNameEditTxt.getText().toString()) || TextUtils.isEmpty(email)
                || TextUtils.isEmpty(password)) {
            Toast.makeText(RegisterActivity.this, "Some fields are empty!", Toast.LENGTH_SHORT)
                    .show();
        } else if (!password.equals(repeatedPassword)) {
            Toast.makeText(RegisterActivity.this, "Passwords don't match!", Toast.LENGTH_SHORT)
                    .show();
        } else {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this,
                    new OnCompleteListener<AuthResult>() {
                        @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                if (task.getException() != null && task.getException()
                                        .getMessage() != null) {
                                    Toast.makeText(RegisterActivity.this, task.getException()
                                            .getMessage(), Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(RegisterActivity.this, "Authentication failed",
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
        }
    }

    /**
     * Makes the status bar translucent.
     */
    private void initStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

}
