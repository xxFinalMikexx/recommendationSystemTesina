package com.example.xxfin.recommendationsystemtesina;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

/*FireBase Imports*/
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

/*Utilities imports*/
import java.util.LinkedList;

public class PrincipalActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        //Initialize authentication
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
            }
        };
    }

    public void showResultsHistory() {

    }

    public LinkedList checkHistory() {
        LinkedList resultados = new LinkedList();

        return resultados;
    }

    public void detectFaces(View v) {
        try {
            Intent intentFaces = new Intent(PrincipalActivity.this, DetectFacesActivity.class);
            startActivity(intentFaces);
        } catch (Exception e) {
            Toast.makeText(PrincipalActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
