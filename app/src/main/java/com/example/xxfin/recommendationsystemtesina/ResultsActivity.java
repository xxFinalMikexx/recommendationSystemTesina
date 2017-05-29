package com.example.xxfin.recommendationsystemtesina;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;

public class ResultsActivity extends AppCompatActivity {
    private ArrayList listNearbyPlaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        //Bundle extras = getIntent().getExtras();

        //this.listNearbyPlaces = (ArrayList) extras.get("Nearby");

        Log.e("Results", "Cargando resultados...");
    }
}
