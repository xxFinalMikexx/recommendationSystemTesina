package com.example.xxfin.recommendationsystemtesina;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.util.LinkedList;

public class PrincipalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
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
