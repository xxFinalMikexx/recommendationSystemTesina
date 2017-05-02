package com.example.xxfin.recommendationsystemtesina;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xxfin.recommendationsystemtesina.objects.*;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by xxfin on 01/05/2017.
 */

public class PlacesAdapter extends BaseAdapter {
    private Context context;
    private HashMap placesHash;

    public PlacesAdapter(Context context, HashMap placesHash) {
        this.context = context;
        this.placesHash = placesHash;
    }

    @Override
    public int getCount() {
        return this.placesHash.size();
    }

    @Override
    public long getItemId(int position) {
        long id = 0;
        ///return getItem(position).getId();
        return id;
    }

    @Override
    public Place_Info getItem(int placePosition) {
        Iterator iter = this.placesHash.entrySet().iterator();
        int actual = 0;
        if(this.placesHash.size() == 0) {
            return null;
        } else {
            try {
                while (iter.hasNext()) {
                    Place_Info place = (Place_Info)iter.next();
                    if(actual == placePosition) {
                        return place;
                    }
                    actual++;
                }
                return null;
            } catch (Exception e) {
                return null;
            }
        }
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.grid_item, viewGroup, false);
        }

        TextView nombreCoche = (TextView) view.findViewById(R.id.nombre_coche);

        final Place_Info place = getItem(position);
        nombreCoche.setText(place.getName());

        return view;
    }
}
