package com.moyersoftware.contender.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.moyersoftware.contender.R;
import com.moyersoftware.contender.login.data.User;
import com.moyersoftware.contender.menu.MainActivity;
import com.moyersoftware.contender.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Objects;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class LoadingActivity extends AppCompatActivity {

    public static LoadingActivity sActivity;
    private CallbackManager mCallbackManager;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String mFacebookName = null;
    private String mFacebookPhoto = null;
    private String mFacebookEmail = null;
    private Button btn_signin, btn_facebook;
    private TextView txt_create;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        btn_signin      =   findViewById(R.id.btn_signin);
        btn_facebook    =   findViewById(R.id.btn_facebook);
        txt_create      =   findViewById(R.id.txt_create);

        initActivity();
        initAuth();
        initStatusBar();
        initFacebook();

        btn_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoadingActivity.this, LoginActivity.class));
            }
        });

        txt_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoadingActivity.this, RegisterActivity.class));
            }
        });
    }

    private void initAuth() {
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    if (mFacebookName != null) {

                        FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid())
                                .child("name").setValue(mFacebookName);
                        FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid())
                                .child("image").setValue(mFacebookPhoto);
                        FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid())
                                .child("email").setValue(mFacebookEmail);

                        String id = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                        Util.setCurrentPlayerId(id);
                        if (!Util.findFriendsShown(id)) {
                            startActivity(new Intent(LoadingActivity.this,
                                    FindFriendsActivity.class));
                            Util.setFindFriendsShown(id);
                        } else {
                            startActivity(new Intent(LoadingActivity.this, MainActivity.class));
                        }
                        finish();
                    } else {
                        FirebaseDatabase.getInstance().getReference().child("users")
                                .child(user.getUid()).child("image").addListenerForSingleValueEvent
                                (new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            String oldPhoto = dataSnapshot.getValue(String.class);
                                            Util.setPhoto(oldPhoto);
                                            FirebaseDatabase.getInstance().getReference().child("users")
                                                    .child(user.getUid()).setValue(new User(user.getUid(),
                                                    user.getDisplayName(), Util.parseUsername(user), user.getEmail(),
                                                    oldPhoto, null));
                                        } else {
                                            Util.setPhoto(user.getPhotoUrl() + "");
                                            FirebaseDatabase.getInstance().getReference().child("users")
                                                    .child(user.getUid()).setValue(new User(user.getUid(),
                                                    user.getDisplayName(), Util.parseUsername(user), user.getEmail(),
                                                    user.getPhotoUrl() + "", null));
                                        }
                                        String id = Objects.requireNonNull(FirebaseAuth.getInstance()
                                                .getCurrentUser()).getUid();
                                        Util.setCurrentPlayerId(id);
                                        if (!Util.findFriendsShown(id)) {
                                            startActivity(new Intent(LoadingActivity.this,
                                                    FindFriendsActivity.class));
                                            Util.setFindFriendsShown(id);
                                        } else {
                                            startActivity(new Intent(LoadingActivity.this,
                                                    MainActivity.class));
                                        }
                                        finish();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                    }
                }
            }
        };
    }

    private void initFacebook() {
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().logOut();
        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject me, GraphResponse response) {
                                        if (response.getError() != null) {
                                            Util.Log("Can't retrieve Facebook info");
                                        } else {
                                            try {
                                                mFacebookName = response.getJSONObject()
                                                        .get("name").toString();
                                                mFacebookPhoto = "https://graph.facebook.com/" + response.getJSONObject()
                                                        .get("id").toString() + "/picture?type=large";
                                                mFacebookEmail = response.getJSONObject()
                                                        .get("email").toString();

                                                Util.setDisplayName(mFacebookName);
                                                Util.setPhoto(mFacebookPhoto);
                                            } catch (JSONException e) {
                                                Util.Log("Can't retrieve Facebook name: " + e + ", " + me.toString());
                                            }
                                        }
                                        handleFacebookAccessToken(loginResult.getAccessToken());
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(LoadingActivity.this, "Login Canceled", Toast.LENGTH_LONG)
                                .show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Util.Log("Facebook error: " + exception);
                        Toast.makeText(LoadingActivity.this, exception.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken accessToken) {
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken
                .getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoadingActivity.this, "Authentication failed: " + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void initActivity() {
        sActivity = this;
    }

    /**
     * Makes the status bar translucent.
     */
    private void initStatusBar() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    /**
     * Opens a log in screen.
     */
    public void onLoginButtonClicked(View view) {
        startActivity(new Intent(this, LoginActivity.class));
    }

    /**
     * Signs in with Facebook.
     */
    public void onFacebookButtonClicked(View view) {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile",
                "email"));
    }

    /**
     * Opens a registration screen.
     */
    public void onRegisterButtonClicked(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
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
     * Required for the calligraphy library.
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }
}
