package com.example.xxfin.recommendationsystemtesina;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xxfin.recommendationsystemtesina.objects.*;

/*import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;*/

import com.google.api.client.extensions.android.http.AndroidHttp;
//import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
/*import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;*/

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

/*Firebase imports*/
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

//import com.google.android.gms.appdatasearch.GetRecentContextCall;

public class DetectFacesActivity extends AppCompatActivity {
    private static final int GALLERY_REQUEST = 1; // Codigo para identificar la llamada a la aplicación de galeria
    private static final int SEARCH_RADIOUS = 500; //Radio aproximado de búsqueda para Geocoder
    private static final int NEARBY_RADIOUS = 10000;
    private static final String API_KEY = "AIzaSyALTyezzge7Tz1HdQMfBrUyfkJMWdk_RCE";
    private static final String CLOUD_VISION_API_KEY = "AIzaSyCjh4AsNOB4sUyK_L46pXkYAajd832u96w";
    private static final String LOG_TAG = "DetectFaces Activity";

    private static String accessToken;
    static final int REQUEST_GALLERY_IMAGE = 10;
    static final int REQUEST_CODE_PICK_ACCOUNT = 11;
    static final int REQUEST_ACCOUNT_AUTHORIZATION = 12;
    static final int REQUEST_PERMISSIONS = 13;
    Account mAccount;
    private ImageView selectedImage;
    private TextView resultTextView;

    private String rutaImagen;
    private double latitud = 0; // Variable para guardar latitud
    private double longitud = 0; // Variable para guardar longitud
    private Uri uriImagen;
    private String typeLocation;

    //private GoogleApiClient mGoogleApiClient;

    private String placeId;
    private LinkedList jsonVision;
    private Places_Object placeData;
    private Place_Info placeInfo = new Place_Info();

    private HashMap mapNearbyPlaces = new HashMap();
    private LinkedList listNearbyPlaces = new LinkedList();
    private HashMap mapEmotionsPlace = new HashMap();
    private LinkedList listEmotions = new LinkedList();
    private HashMap correlationsFound = new HashMap();

    private double correlacionPearson = 0;

    private String lastimgdatetime;
    private ProgressDialog prDialog;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /*Variables para Firebase*/
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private StorageReference mStorageRef;
    private FirebaseStorage storage;
    public StorageReference downloadUrl;
    private Bitmap photo;
    private ByteArrayOutputStream bytes;

    /*Variables para HTTPs request*/
    private static final String TARGET_URL = "https://vision.googleapis.com/v1/images:annotate?";
    private static final String API_KEY_VISION = "key=AIzaSyCAxKbsPqfcZMPrJpcKD0nGvkqC_WDtAgI";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect_faces);

        checkPermissions();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        fotoPrueba();
    }

    public void fotoPrueba() {
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

                /*Bitmap de la imagen seleccionada*/
                InputStream imageStream = getContentResolver().openInputStream(selectedImage);
                this.photo = BitmapFactory.decodeStream(imageStream);

                /*Carga la foto al Storage de Firebase*/
                encodeBitmapAndSaveToFirebase(this.photo, this.rutaImagen);

                /*Envia request a Google Vision*/
                new UploadFileTask().execute();
            } catch(Exception e) {
                Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void processValue(String result) {
        TextView photoPath = (TextView)findViewById(R.id.result);
        photoPath.setText(result);
    }

    public void encodeBitmapAndSaveToFirebase(Bitmap bitmap, final String key) {
        /*Reference to Storage*/
        StorageReference storageRef = storage.getReferenceFromUrl("gs://recommendationsystem-ba351.appspot.com");
        /*Create reference for the image to upload*/
        String keyImage[] = key.split("/");
        StorageReference mountainsRef = storageRef.child("images/" + keyImage[keyImage.length - 1]);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] data = baos.toByteArray();

        /*Carga la imagen a Firebase y guarda la url para ser usada*/
        UploadTask uploadTask = mountainsRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(DetectFacesActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                downloadUrl = taskSnapshot.getStorage();
                Toast.makeText(DetectFacesActivity.this, "Imagen cargada: " + downloadUrl.getPath(), Toast.LENGTH_LONG).show();
                TextView photoPath = (TextView)findViewById(R.id.result);
                photoPath.setText(downloadUrl.toString());
            }
        });
    }

    public void checkPermissions() {
        int permissionCheck = ContextCompat.checkSelfPermission(DetectFacesActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DetectFacesActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_EXTERNAL_STORAGE);
        }
    }


    public void analizarDatosImagen(Uri imagen) {
        /*Enviar request para Google Vision, devuelve un JSON con información del análisis*/
        //enviarRequestVision(imagen);

        /*Obtener Place_Id del lugar usando coordenadas*/
        //obtenerPlaceId();

        /*Obtener información del lugar basado en el Place_Id y lo guarda en un objeto de Place_Info*/
        //obtenerInformación(this.placeId);

        /*Obtener información de los lugares similares al Place_Id*/
        //obtenerResultadosSimilares(this.placeId);

        /*Correlación Pearson entre resultados de imagen vs resultados similares
        * == 1 => correlación positiva perfecta
        * > 0.7 => correlación positiva alta
        * 0.1 < > 0.6 correlación positiva baja. No se toman en cuenta
        * < 0 correlación negativa. No se toman en cuenta
        * */
        //correlacionPearson();

        /*Intent intentResult = new Intent(DetectFacesActivity.this, ResultsActivity.class);
        intentResult.putExtra("Correlaciones", this.correlationsFound);
        intentResult.putExtra("Nearby", this.listNearbyPlaces);
        LinkedList placeInfoList = new LinkedList();
        placeInfoList.add(this.placeInfo);
        intentResult.putExtra("Place", placeInfoList);
        startActivity(intentResult);*/
    }

    private  Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        try {
            if (maxHeight > 0 && maxWidth > 0) {
                int width = image.getWidth();
                int height = image.getHeight();
                float ratioBitmap = (float) width / (float) height;
                float ratioMax = (float) maxWidth / (float) maxHeight;

                int finalWidth = maxWidth;
                int finalHeight = maxHeight;
                if (ratioMax > 1) {
                    finalWidth = (int) ((float) maxHeight * ratioBitmap);
                } else {
                    finalHeight = (int) ((float) maxWidth / ratioBitmap);
                }
                image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
                return image;
            } else {
                return image;
            }
        }catch (Exception e){
            Log.d("ActividadVistaPrevia", e.toString());
            return image;
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

    public boolean tieneCoordenadasImagen(String rutaImagen){
        try {
            float[] coordenadas = new float[2]; // Variable para guardar las coordenadas de la imagen
            ExifInterface exifInterface = new ExifInterface(rutaImagen); // Crear objeto para leer metadata de imagen
            if(exifInterface.getLatLong(coordenadas)) {
                this.latitud = (double) coordenadas[0];
                this.longitud = (double) coordenadas[1];
                Toast.makeText(DetectFacesActivity.this, "Coordenadas encontradas", Toast.LENGTH_LONG).show();
                return true;
            }
        } catch (IOException e) {
            Toast.makeText(DetectFacesActivity.this, e.toString(), Toast.LENGTH_LONG).show();
        }

        return false;
    }


    private class UploadFileTask extends AsyncTask<LinkedList, Integer, String> {
        protected String doInBackground(LinkedList... values) {
            try {
                StorageReference downloadUrl;
                LinkedList auxValues = (LinkedList) values[0];
                downloadUrl = (StorageReference) auxValues.get(0);

                URL serverUrl = new URL(TARGET_URL + API_KEY_VISION);
                URLConnection urlConnection = serverUrl.openConnection();
                HttpURLConnection httpConnection = (HttpURLConnection) urlConnection;

                httpConnection.setRequestMethod("POST");
                httpConnection.setRequestProperty("Content-Type", "application/json");

                httpConnection.setDoOutput(true);

                BufferedWriter httpRequestBodyWriter = new BufferedWriter(new
                        OutputStreamWriter(httpConnection.getOutputStream()));
                httpRequestBodyWriter.write
                        ("{\"requests\":  [{ \"features\":  [ {\"type\": \"FACE_DETECTION\""
                                + "}], \"image\": {\"source\": { \"gcsImageUri\":"
                                + downloadUrl.toString() + "}}}]}");
                String request = "{\"requests\":  [{ \"features\":  [ {\"type\": \"FACE_DETECTION\""
                        + "}], \"image\": {\"source\": { \"gcsImageUri\":"
                        + downloadUrl.toString() + "}}}]}";

                httpRequestBodyWriter.close();

                String response = httpConnection.getResponseMessage();

                if (httpConnection.getInputStream() == null) {
                    System.out.println("No stream");
                    return "Sin información...";
                }

                Scanner httpResponseScanner = new Scanner (httpConnection.getInputStream());
                String resp = "";
                while (httpResponseScanner.hasNext()) {
                    String line = httpResponseScanner.nextLine();
                    resp += line;
                    //System.out.println(line);  //  alternatively, print the line of response
                }
                httpResponseScanner.close();

                return resp;
            } catch (Exception e) {
                String request = e.getMessage();
                return request;
            }
        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(String result) {
            //showDialog("Downloaded " + result + " bytes");
            processValue(result);
        }
    }
}
