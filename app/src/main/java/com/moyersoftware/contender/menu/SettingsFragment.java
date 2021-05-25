package com.moyersoftware.contender.menu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.moyersoftware.contender.R;
import com.moyersoftware.contender.util.Util;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SettingsFragment extends Fragment {

    // Constants
    private static final int PICK_IMAGE_CODE = 0;
    private static final int USER_PHOTO_SIZE_PX = 500;

    // Views
    @BindView(R.id.settings_name_txt)
    TextView mNameTxt;
    //@BindView(R.id.settings_username_txt)
    //TextView mUsernameTxt;
    @BindView(R.id.settings_email_txt)
    TextView mEmailTxt;
    //@BindView(R.id.settings_photo_img)
    //ImageView mPhotoImg;
    //@BindView(R.id.settings_photo_btn)
    //Button mPhotoBtn;
    //@BindView(R.id.editUsernameImg)
    //View mEditUsernameImg;
    @BindView(R.id.txt_version)
    TextView txtVer;

    // Usual variables
    private Bitmap mBitmap;
    private StorageReference mImageRef;
    private String mUserId;
    private String mUsername;
    private final Target mTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mBitmap = bitmap;

            uploadImage();
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    };

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);

        initMyAccount();
        initStorage();
        initSettings();
        setVersion();

        return view;
    }

    private void initSettings() {
        //mEditUsernameImg.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View view) {
        //        editUsername();
        //    }
        //});
    }

    private void editUsername() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity(), R.style.MaterialDialog);
        @SuppressLint("InflateParams") View dialogView = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_edit_username, null);
        dialog.setTitle(R.string.edit_username);
        final EditText username = dialogView.findViewById(R.id.username);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            username.setText(mUsername);
            username.setSelection(mUsername.length());
        }
        dialog.setView(dialogView);
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (!TextUtils.isEmpty(username.getText())) {
                    FirebaseDatabase.getInstance().getReference().child("users").child(mUserId)
                            .child("username").setValue(username.getText().toString());
                    //mUsernameTxt.setText(username.getText().toString());
                }
            }
        });
        dialog.setNegativeButton("Cancel", null);
        dialog.show();
    }

    private void initMyAccount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            mUserId = user.getUid();
            mNameTxt.setText(Util.getDisplayName());
            try {
                mUsername = Util.parseUsername(user);
            } catch (Exception e) {
                mUsername = "";
            }
            //mUsernameTxt.setText(mUsername);
            mEmailTxt.setText(user.getEmail());
            //Picasso.get().load(Util.getPhoto())
            //        .placeholder(R.drawable.avatar_placeholder).fit().into(mPhotoImg);
            //mPhotoBtn.setOnClickListener(new View.OnClickListener() {
            //    @Override
            //    public void onClick(View v) {
            //        onUpdatePhotoClicked();
            //    }
            //});

            FirebaseDatabase.getInstance().getReference().child("users").child(mUserId)
                    .child("username")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            try {
                                if (dataSnapshot != null
                                        && !TextUtils.isEmpty(dataSnapshot.getValue().toString())) {
                                    mUsername = dataSnapshot.getValue().toString();
                                    //mUsernameTxt.setText(mUsername);
                                }
                            } catch (Exception e) {
                                //mUsernameTxt.setText("");
                                // Can't retrieve Firebase profile
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
        }
    }

    public void onUpdatePhotoClicked() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select a game image"),
                PICK_IMAGE_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                //Picasso.get().load(data.getData()).placeholder(android.R.color.white)
                //        .centerCrop().fit().into(mPhotoImg);

                //Picasso.get().load(data.getData()).centerCrop().resize(USER_PHOTO_SIZE_PX,
               //         USER_PHOTO_SIZE_PX).into(mTarget);
            }
        }
    }

    private void uploadImage() {
        initStorage();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] data = outputStream.toByteArray();

        UploadTask uploadTask = mImageRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Util.Log("File uploading failure: " + exception);

                Toast.makeText(getActivity(), "Can't upload image", Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                @SuppressWarnings("ConstantConditions")
                Task<Uri> downUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                downUrl.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        savePhoto(uri.toString());
                    }
                });
            }
        });
    }

    private void savePhoto(String photo) {
        Util.setPhoto(photo);

        FirebaseDatabase.getInstance().getReference().child("users").child(mUserId).child("image")
                .setValue(photo);
    }

    private void initStorage() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReferenceFromUrl
                ("gs://contender-3ef7d.appspot.com");

        // Create a reference the photo
        mImageRef = storageRef.child(mUserId + ".jpg");
    }

    private void setVersion(){

        try {
            PackageInfo pInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            String version = pInfo.versionName;
            txtVer.setText("version "+version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


}
