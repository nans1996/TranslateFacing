package com.example.client.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.client.DAOClass;
import com.example.client.OkhttpClass;
import com.example.client.R;
import com.example.client.activity.helper.GraphicOverlay;
import com.example.client.model.ImageDataClass;
import com.example.client.repos.ImageInterface;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.List;

import dmax.dialog.SpotsDialog;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TranslateVideoActivity extends Activity  {

private Button faceDetectButton;
private GraphicOverlay graphicOverlay;
private CameraView cameraView;
AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate_video);

        faceDetectButton = findViewById(R.id.detectButton);
        graphicOverlay = findViewById(R.id.graphic_overlay);
        cameraView  = findViewById(R.id.camera_video);

        alertDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Please, wait...")
                .setCancelable(false)
                .build();

        faceDetectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.start();
                cameraView.captureImage();
                graphicOverlay.clear();
            }

        });

        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                alertDialog.show();
                Bitmap bitmap = cameraKitImage.getBitmap();
                bitmap = Bitmap.createScaledBitmap(bitmap, cameraView.getWidth(), cameraView.getHeight(), false);
                cameraView.stop();

                processFacedetection(bitmap);
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });
    }

    private void processFacedetection(Bitmap bitmap) {

        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionFaceDetectorOptions firebaseVisionFaceDetectorOptions =  new FirebaseVisionFaceDetectorOptions.Builder().build();

        FirebaseVisionFaceDetector firebaseVisionFaceDetector = FirebaseVision.getInstance()
                .getVisionFaceDetector(firebaseVisionFaceDetectorOptions);

        firebaseVisionFaceDetector.detectInImage(firebaseVisionImage)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                        postImage(bitmap);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(TranslateVideoActivity.this, "Error: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void postImage(Bitmap bitmap) {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://192.168.1.4:44387/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(OkhttpClass.getUnsafeOkHttpClient())
                .build();

        ImageInterface service = retrofit.create(ImageInterface.class);


        Call<String> call = service.translateImage(convertBitmapToBite(bitmap));
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call call, Response response) {
                try (ResponseBody responseBody = (ResponseBody) response.body()) {
                    if (response.isSuccessful()) {
                        System.out.println("запрос true");
                        System.out.println(response.body());
                        System.out.println(responseBody);
                        String result = ((ResponseBody) response.body()).string();
                        int index = result.indexOf(",");
                        String res = result.substring(index + 1, index + 3);
                        if (Integer.parseInt(res) > 50) {
                            String[] arr = result.split("");
                            DAOClass.Add(arr[2], result.substring(index), Calendar.getInstance().getTime().toString());
                            resultDialogVideo(result);
                        } else {
                            resultDialogVideo("Failed to recognize image");
                        }

                    } else {
                        System.out.println("запрос false");
                    }
                } catch (Exception e) {

                } finally {
                    response.raw().body().close();
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {

                System.out.println("mistake " + t);
            }
        });
    }

    protected ImageDataClass convertBitmapToBite(Bitmap selectBitmap) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(selectBitmap.getWidth() * selectBitmap.getHeight());
        selectBitmap.compress(Bitmap.CompressFormat.JPEG, 100, buffer);
        ImageDataClass img;

        img = new ImageDataClass(" ", buffer.toByteArray());

        return img;
    }

//    private void getFaceResults(List<FirebaseVisionFace> firebaseVisionFaces) {
//        for (FirebaseVisionFace face : firebaseVisionFaces) {
//            Rect rect = face.getBoundingBox();
//            RectOverlay rectOverlay = new RectOverlay(graphicOverlay, rect);
//
//            graphicOverlay.add(rectOverlay);
//
//
//        }
//        alertDialog.dismiss();
//    }

    public void resultDialogVideo(String result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(TranslateVideoActivity.this);
        builder.setTitle("Result")
                .setIcon(R.drawable.smile)
                .setMessage(result)
                .setPositiveButton("Ok", null)
                .create().show();
    }

    @Override
    protected void onPause() {
    super.onPause();

    cameraView.stop();
}

@Override
protected void onResume() {
super.onResume();
cameraView.start();
}
    public void resultDialog(String result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(TranslateVideoActivity.this);
        builder.setTitle("Result")
                .setIcon(R.drawable.smile)
                .setMessage(result)
                .setPositiveButton("Ok", null)
                .create().show();
    }

}