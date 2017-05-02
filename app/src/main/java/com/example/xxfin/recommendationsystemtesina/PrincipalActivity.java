package com.example.xxfin.recommendationsystemtesina;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/*FireBase Imports*/
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.xxfin.recommendationsystemtesina.objects.Place_Info;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

/*Storage*/
import com.google.api.services.storage.StorageScopes;

/*Utilities imports*/
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static java.lang.System.getProperties;

public class PrincipalActivity extends AppCompatActivity {
    private static final String LOG_TAG = "Principal Activity";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;

    // Internal Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /*Storage references*/
    private static RecyclerView.LayoutManager.Properties properties;
    private static Storage storage;
    private static final String PROJECT_ID_PROPERTY = "tesina-159419";
    private static final String APPLICATION_NAME_PROPERTY = "tesina";
    private static final String ACCOUNT_ID_PROPERTY = "account.id";
    private static final String PRIVATE_KEY_PATH_PROPERTY = "private.key.path";

    /*Images references*/
    private static final int GALLERY_REQUEST = 1; // Codigo para identificar la llamada a la aplicación de galeria
    private String rutaImagen;
    private double latitud = 0; // Variable para guardar latitud
    private double longitud = 0; // Variable para guardar longitud
    private Uri uriImagen;
    private String typeLocation;
    private String placeId;
    private JSONArray arrayPlaceId;
    private ByteArrayOutputStream bytes;
    private Bitmap photo;
    private TextView viewPlace;

    /*Objects variables*/
    private Place_Info placeInfo = new Place_Info();
    private HashMap mapNearbyPlaces = new HashMap();
    private LinkedList listNearbyPlaces = new LinkedList();
    private HashMap mapEmotionsPlace = new HashMap();
    private LinkedList listEmotions = new LinkedList();
    private HashMap correlationsFound = new HashMap();

    /*Constants for search*/
    private static final int SEARCH_RADIOUS = 50; //Radio aproximado de búsqueda para Geocoder
    private static final int NEARBY_RADIOUS = 10000; //Radio para buscar alrededor de la ciudad
    private static final String API_KEY = "AIzaSyALTyezzge7Tz1HdQMfBrUyfkJMWdk_RCE";

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

        checkPermissions();

        getPhotoData();
    }

    public void getPhotoData() {
        Intent intentGaleria = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI); // Se crea el intent para abrir la aplicación de galería
        startActivityForResult(intentGaleria, GALLERY_REQUEST); // Inicia la aplicación de galeria
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        bytes = new ByteArrayOutputStream();

        if (requestCode == GALLERY_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            try {
                Uri selectedImage = data.getData();

                /*Ruta de la imagen para usar como referencia para storage*/
                this.rutaImagen = obtenerRutaRealUri(selectedImage);

                /*Obtener latitud y longitud*/
                tieneCoordenadasImagen(this.rutaImagen);

                /*Bitmap de la imagen seleccionada*/
                InputStream imageStream = getContentResolver().openInputStream(selectedImage);
                this.photo = BitmapFactory.decodeStream(imageStream);

                getPlacesResult();
            } catch(Exception e) {
                Toast.makeText(this, "Archivo no encontrado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public String obtenerRutaRealUri(Uri imagenSeleccionada){
        try {
            String[] informacion_imagen = {MediaStore.Images.Media.DATA}; // Obtener la metadata de todas las imagenes guardadas en el dispositivo
            Cursor cursor = getContentResolver().query(imagenSeleccionada, informacion_imagen, null, null, null); // Buscar la imagen que coincide con el Uri dado
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);  // Buscar la columna de url de imagen
            cursor.moveToFirst(); // Ir al primer elemento
            return cursor.getString(column_index); // Regresar ruta real
        } catch (Exception e) {
            return imagenSeleccionada.getPath(); // Regresar ruta decodificada
        }
    }

    public void showResultsHistory() {
        //TODO implement DB
    }

    public void getPlacesResult() {
        try {
            /*Obtener Place_Id del lugar usando coordenadas*/
            obtenerPlaceId();
        } catch(Exception e) {
            Log.e("Actividad Principal ", e.toString());
        }
    }

    public void obtenerPlaceId() {
        try {
            RequestQueue queue = Volley.newRequestQueue(this);

            StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
            googlePlacesUrl.append("location=").append(latitud).append(",").append(longitud);
            googlePlacesUrl.append("&radius=").append(SEARCH_RADIOUS);
            googlePlacesUrl.append("&types=").append("establishment");
            googlePlacesUrl.append("&key=").append(API_KEY);

            Log.e("Query to place", googlePlacesUrl.toString());

            this.viewPlace = (TextView)findViewById(R.id.placeId);
            this.viewPlace.setText(googlePlacesUrl);

            JsonObjectRequest placeRequest = new JsonObjectRequest (
                    Request.Method.GET,
                    googlePlacesUrl.toString(),
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            //Toast.makeText(DetectFacesActivity.this, response.toString(), Toast.LENGTH_LONG).show();
                            parsePlaceId(response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //Toast.makeText(PrincipalActivity.this, "Error volley " +error.toString(), Toast.LENGTH_LONG).show();
                    Log.e("Principal Activity", error.toString());
                }
            }
            );
            queue.add(placeRequest);

        } catch(Exception e) {
            //Toast.makeText(PrincipalActivity.this, "Place_ID error", Toast.LENGTH_LONG).show();
            Log.e("Principal ACtivity", e.toString());
        }

    }

    public void parsePlaceId(JSONObject result) {
        this.viewPlace = (TextView)findViewById(R.id.placeId);
        try {
            this.arrayPlaceId = result.getJSONArray("results");

            JSONObject place = this.arrayPlaceId.getJSONObject(0);
            this.placeId = place.getString("place_id");

            Log.e("Principal Place_id: ", this.placeId);

            obtenerInformacion(this.placeId);
        } catch(Exception e) {
            //Toast.makeText(PrincipalActivity.this, "Parsing PlaceId " + e.getMessage(),Toast.LENGTH_LONG).show();
            Log.e("Principal Activity", e.toString());
        }
    }

    public void obtenerInformacion(String placeId) {
        Toast.makeText(PrincipalActivity.this, "Obteniendo información de Lugar", Toast.LENGTH_LONG).show();
        try {
            RequestQueue queue = Volley.newRequestQueue(this);

            StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?");
            googlePlacesUrl.append("placeid=").append(this.placeId);
            googlePlacesUrl.append("&key=").append(API_KEY);
            Log.e("Specific Place", googlePlacesUrl.toString());

            final JsonObjectRequest placeRequest = new JsonObjectRequest (
                    Request.Method.GET,
                    googlePlacesUrl.toString(),
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            //Toast.makeText(DetectFacesActivity.this, response.toString(), Toast.LENGTH_LONG).show();
                            parsePlaceInformation(response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Principal Activity", "Error place:"+error.toString());
                }
            });
            queue.stop();
            queue.add(placeRequest);
            queue.start();
        } catch(Exception e) {
            Log.e("Principal Activity", "Información insuficiente. Intente con otro lugar."+e.toString());
        }
    }

    public void parsePlaceInformation(JSONObject result) {
        Toast.makeText(PrincipalActivity.this, "Parseando datos...",Toast.LENGTH_LONG).show();
        try {
            JSONObject jsonObject = result.getJSONObject("result");

            String nombre = jsonObject.getString("name");
            this.placeInfo.setName(nombre);

            this.placeInfo.setPlaceId(jsonObject.getString("place_id"));

            JSONObject geometry = jsonObject.getJSONObject("geometry").getJSONObject("location");
            LatLng coordinates = new LatLng(geometry.getDouble("lat"), geometry.getDouble("lng"));
            this.placeInfo.setLatlng(coordinates);

            this.placeInfo.setPlaceTypes(jsonObject.getJSONArray("types"));

            this.placeInfo.setRating(jsonObject.getDouble("rating"));

            Log.e("Place_Info", this.placeInfo.toString());

            obtenerResultadosSimilares(this.placeId);
        } catch(Exception e) {
            Toast.makeText(PrincipalActivity.this, "Error al parsear informacion del lugar\n"+e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("Principal Activity", e.toString());
        }
    }

    public void obtenerResultadosSimilares(String placeId) {
        RequestQueue queue = Volley.newRequestQueue(this);
        try {
            StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
            //location=51.503186,-0.126446&radius=5000&types=hospital&key=AIzaSyALTyezzge7Tz1HdQMfBrUyfkJMWdk_RCE
            googlePlacesUrl.append("location=").append(latitud).append(",").append(longitud);
            googlePlacesUrl.append("&radius=").append(NEARBY_RADIOUS);
            googlePlacesUrl.append("&types=").append(this.placeInfo.getPlaceTypes().get(0));
            googlePlacesUrl.append("&key=").append(API_KEY);

            Log.e("Nearby places", googlePlacesUrl.toString());

            final JsonObjectRequest detailsRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    googlePlacesUrl.toString(),
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            //Toast.makeText(DetectFacesActivity.this, response.toString(), Toast.LENGTH_LONG).show();
                            parseInformationDetail(response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(PrincipalActivity.this, "Error nearby: "+error.toString(), Toast.LENGTH_LONG).show();
                }
            });
            queue.add(detailsRequest);
        } catch(Exception e) {
            Toast.makeText(PrincipalActivity.this, "Error en string NearbySearch", Toast.LENGTH_LONG).show();
        }
    }

    public void parseInformationDetail(JSONObject result) {
        this.viewPlace = (TextView)findViewById(R.id.placeId);
        try {
            JSONArray jsonArray = result.getJSONArray("results");

            for (int i = 1; i < jsonArray.length(); i++) {
                Place_Info actualPlace = new Place_Info();

                JSONObject place = jsonArray.getJSONObject(i);
                actualPlace.setName(place.getString("name"));
                actualPlace.setPlaceTypes(place.getJSONArray("types"));

                JSONObject geometry = place.getJSONObject("geometry").getJSONObject("location");
                LatLng coordinates = new LatLng(geometry.getDouble("lat"), geometry.getDouble("lng"));
                actualPlace.setLatlng(coordinates);

                actualPlace.setPlaceId(place.getString("place_id"));

                /*Get ratings of each place*/
                try {
                    actualPlace.setRating(place.getDouble("rating"));
                } catch(Exception e) {
                    actualPlace.setRating(2.5);
                }

                mapNearbyPlaces.put(place.getString("place_id"), actualPlace);
                listNearbyPlaces.addLast(actualPlace);
                Log.e("Principal Activity", actualPlace.toString());
            }

        } catch (Exception e) {
            Log.e("Error nearby", e.toString());
        }
    }

    public LinkedList getRatingsPlace(JSONArray reviews) {
        LinkedList reviewsRatings = new LinkedList();
        try {
            for(int i = 0; i < reviews.length(); i++) {
                JSONObject placeReview = reviews.getJSONObject(i);
                reviewsRatings.addLast(placeReview.getJSONObject("aspects").getInt("rating"));
            }
            return reviewsRatings;
        } catch(Exception e) {
            Toast.makeText(PrincipalActivity.this, "Error al obtener los reviews", Toast.LENGTH_LONG).show();
            return null;
        }
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

    public void uploadTestImage() {
        try {
            HttpTransport httpTransport = new NetHttpTransport();
            JsonFactory jsonFactory = new JacksonFactory();

            List<String> scopes = new ArrayList<String>();
            scopes.add(StorageScopes.DEVSTORAGE_FULL_CONTROL);

            Credential credential = new GoogleCredential.Builder()
                    .setTransport(httpTransport)
                    .setJsonFactory(jsonFactory)
                    .setServiceAccountId(
                            getProperties().getProperty(ACCOUNT_ID_PROPERTY))
                    .setServiceAccountPrivateKeyFromP12File(
                            new File(getProperties().getProperty(
                                    PRIVATE_KEY_PATH_PROPERTY)))
                    .setServiceAccountScopes(scopes).build();
        } catch (Exception e) {
            Toast.makeText(PrincipalActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public boolean tieneCoordenadasImagen(String rutaImagen){
        try {
            float[] coordenadas = new float[2]; // Variable para guardar las coordenadas de la imagen
            ExifInterface exifInterface = new ExifInterface(rutaImagen); // Crear objeto para leer metadata de imagen
            if(exifInterface.getLatLong(coordenadas)) {
                this.latitud = (double) coordenadas[0];
                this.longitud = (double) coordenadas[1];
                return true;
            }
        } catch (IOException e) {
            Toast.makeText(PrincipalActivity.this, e.toString(), Toast.LENGTH_LONG).show();
        }

        return false;
    }

    public void checkPermissions() {
        try {
            int permissionCheck = ContextCompat.checkSelfPermission(PrincipalActivity.this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(PrincipalActivity.this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_EXTERNAL_STORAGE);
            }
        } catch(Exception e) {
            //this.viewPlace.setText("Error en permisos" + e.getMessage());
            Log.e("Principal Activity", e.toString());
        }
    }
}
