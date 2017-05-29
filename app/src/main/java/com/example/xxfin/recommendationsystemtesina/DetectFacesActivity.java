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

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.android.http.AndroidHttp;
//import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
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
import java.io.File;
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
import java.util.Random;
import java.util.Scanner;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

/*Firebase imports*/
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import static java.lang.System.getProperties;

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
    public TextView photoPath;

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

        this.photoPath = (TextView)findViewById(R.id.result);

        Intent intent = getIntent();
        Uri image = (Uri)intent.getData();

        analizarDatosImagen(image);
    }

    private void processValue(String result) {
        Toast.makeText(DetectFacesActivity.this, result, Toast.LENGTH_LONG).show();
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
        enviarRequestVision(imagen);

        /*Correlación Pearson entre resultados de imagen vs resultados similares
        * == 1 => correlación positiva perfecta
        * > 0.7 => correlación positiva alta
        * 0.1 < > 0.6 correlación positiva baja. No se toman en cuenta
        * < 0 correlación negativa. No se toman en cuenta
        * */
        correlacionPearson();

        Intent intentResult = new Intent(DetectFacesActivity.this, ResultsActivity.class);
        intentResult.putExtra("Correlaciones", this.correlationsFound);
        intentResult.putExtra("Nearby", this.listNearbyPlaces);
        LinkedList placeInfoList = new LinkedList();
        placeInfoList.add(this.placeInfo);
        intentResult.putExtra("Place", placeInfoList);
        startActivity(intentResult);
    }

    public void enviarRequestVision(Uri imagen) {
        try {
            if (imagen != null) {
                Bitmap bitmap = scaleBitmapDown(MediaStore.Images.Media.getBitmap(getContentResolver(), imagen), 1200);
                callCloudVision(bitmap);
            } else {
                Log.d("Detection Face", "Imagen seleccionada nula");
                Toast.makeText(DetectFacesActivity.this, "Error al seleccionar imágen", Toast.LENGTH_LONG).show();
            }
        } catch(Exception e) {
            Toast.makeText(DetectFacesActivity.this, "Error al recuperar emociones", Toast.LENGTH_SHORT).show();
            Log.e("Detection Face", e.getMessage());
        }
    }

    private void callCloudVision(final Bitmap bitmap) throws IOException {
        // Switch text to loading
        Log.e("Detect Faces", "Cargando imagen...");
        //mImageDetails.setText(R.string.loading_message);

        // Do the real work in an async task, because we need to use the network anyway
        new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object... params) {
                try {
                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    VisionRequestInitializer requestInitializer =
                            new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                                /**
                                 * We override this so we can inject important identifying fields into the HTTP
                                 * headers. This enables use of a restricted cloud platform API key.
                                 */
                                @Override
                                protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                                        throws IOException {
                                    super.initializeVisionRequest(visionRequest);

                                    String packageName = getPackageName();
                                    visionRequest.getRequestHeaders().set("", packageName);

                                    String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);

                                    visionRequest.getRequestHeaders().set("", sig);
                                }
                            };

                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(requestInitializer);

                    Vision vision = builder.build();

                    BatchAnnotateImagesRequest batchAnnotateImagesRequest = new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
                        AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                        // Add the image
                        Image base64EncodedImage = new Image();
                        // Convert the bitmap to a JPEG
                        // Just in case it's a format that Android understands but Cloud Vision
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                        byte[] imageBytes = byteArrayOutputStream.toByteArray();

                        // Base64 encode the JPEG
                        base64EncodedImage.encodeContent(imageBytes);
                        annotateImageRequest.setImage(base64EncodedImage);

                        // add the features we want
                        annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                            Feature faceDetection = new Feature();
                            faceDetection.setType("FACE_DETECTION");
                            add(faceDetection);
                        }});

                        // Add the list of one thing to the request
                        add(annotateImageRequest);
                    }});

                    Vision.Images.Annotate annotateRequest =
                            vision.images().annotate(batchAnnotateImagesRequest);
                    // Due to a bug: requests to Vision API containing large images fail when GZipped.
                    annotateRequest.setDisableGZipContent(true);
                    Log.d("Detect FAces", "created Cloud Vision request object, sending request");

                    BatchAnnotateImagesResponse response = annotateRequest.execute();
                    //return convertResponseToString(response);
                    return response.toString();

                } catch (GoogleJsonResponseException e) {
                    Log.d("Detect FAces", "failed to make API request because " + e.getContent());
                } catch (IOException e) {
                    Log.d("Detect FAces", "failed to make API request because of other IOException " +
                            e.getMessage());
                }
                return "Cloud Vision API request failed. Check logs for details.";
            }

            protected void onPostExecute(String result) {
                //mImageDetails.setText(result);
                Toast.makeText(DetectFacesActivity.this, result.toString(), Toast.LENGTH_LONG).show();
                Log.e("Detect Faces", result.toString());
            }
        }.execute();
    }


    public Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
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

    public void correlacionPearson() {
        double emotionsAverage = 0, nearbyAverage = 0;
        try {
            for (int i = 0; i < this.listEmotions.size(); i++) {
                Place_Info actualPlace = (Place_Info) this.listEmotions.get(i);
                emotionsAverage += actualPlace.getRating();
            }
            emotionsAverage = emotionsAverage / this.listEmotions.size();

            /*x = emotionRating - median(emotionRating)*/
            double xmedianX[] = new double[this.listEmotions.size()];
            for(int i = 0; i < xmedianX.length; i++) {
                Place_Info emotionInfo = (Place_Info) this.listEmotions.get(i);
                xmedianX[i] = emotionInfo.getRating() - emotionsAverage;
            }
            /*x^2*/
            double powX = 0;
            for(int i = 0; i < xmedianX.length; i++) {
                powX += xmedianX[i] * xmedianX[i];
            }

            for(int j = 0; j < listNearbyPlaces.size(); j++) {
                Place_Info actualNearbyPlace = (Place_Info) listNearbyPlaces.get(j);
                LinkedList placesRating = actualNearbyPlace.getRatingList();
                String actualPlaceId = actualNearbyPlace.getPlaceId();

                int nearbyCount = 0;
                HashMap visitados = new HashMap();
                double usedNearbyPlaces[] = new double[this.listEmotions.size()];
                Random rand = new Random(System.currentTimeMillis());

                int Low = 0, High = placesRating.size();
                while (nearbyCount < listEmotions.size()) {
                    int actualRand = rand.nextInt(High - Low) + Low;
                    if (visitados.get(actualRand) == null) {
                        visitados.put(rand, placesRating.get(actualRand));
                        nearbyAverage += (int)placesRating.get(actualRand);
                        nearbyCount++;
                    }
                }
                nearbyAverage = nearbyAverage / this.listEmotions.size();

                /*y = nearbyRating - mean(nearbyRating)*/
                double ymedianY[] = new double[this.listEmotions.size()];
                for(int i = 0; i < ymedianY.length; i++) {
                    ymedianY[i] = usedNearbyPlaces[i] - nearbyAverage;
                }

                 /*y^2*/
                double powY = 0;
                for(int i = 0; i < ymedianY.length; i++) {
                    powY += ymedianY[i] * ymedianY[i];
                }

                /*xy*/
                double xySum = 0;
                for(int i = 0; i < xmedianX.length; i++) {
                    xySum += xmedianX[i] - ymedianY[i];
                }
                double correlacion = xySum / Math.sqrt((powX) * (powY));
                if(correlacion > 0.8) {
                    this.correlationsFound.put(this.placeId, actualPlaceId);
                }
            }
            Toast.makeText(DetectFacesActivity.this, "Correlación: " + this.correlacionPearson, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(DetectFacesActivity.this, "Error al calcular Pearson", Toast.LENGTH_LONG).show();
        }
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
                                + "\"gs://recommendationsystem-ba351.appspot.com/images/20170418_135602.jpg\"" + "}}}]}");
                String request = "{\"requests\":  [{ \"features\":  [ {\"type\": \"FACE_DETECTION\""
                        + "}], \"image\": {\"source\": { \"gcsImageUri\":"
                        + "\"gs://recommendationsystem-ba351.appspot.com/images/20170418_135602.jpg\"" + "}}}]}";

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
                }
                httpResponseScanner.close();

                return resp;
            } catch (Exception e) {
                String request = "Error al enviar HTTP: "+e.getLocalizedMessage();
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

    public void test() {

    }
}
