package com.example.client.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.client.DAOClass;
import com.example.client.OkhttpClass;
import com.example.client.R;
import com.example.client.model.ImageDataClass;
import com.example.client.repos.ImageInterface;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Calendar;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TranslateActivity extends AppCompatActivity {

    private ImageView imageview;
    private  final int Pick_image = 1;
    Bitmap selectBitmap;
    Bitmap cutBitmup;
 //   private FirebaseAuth mAuth;
    Button sendbutton;
    Button rotateButton;
    TextView textView1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate);

        imageview = (ImageView) findViewById(R.id.imageView);
        Button imagebutton = (Button)findViewById(R.id.buttonLoad);
        sendbutton = (Button) findViewById(R.id.buttonSend);
        rotateButton = (Button) findViewById(R.id.buttonRotate);
        textView1 = (TextView) findViewById(R.id.textView1);

        imagebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, Pick_image);
            }
        });

        sendbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postImage();
            }

        });

        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float a = imageview.getRotation();
                imageview.setRotation(a+90);
            }
        });
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent ImageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, ImageReturnedIntent);
        switch (requestCode) {
            case Pick_image:
                if (resultCode == RESULT_OK) {
                    try {
                        final Uri imageUri = ImageReturnedIntent.getData();
                        final InputStream imageinputStream = getContentResolver().openInputStream(imageUri);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inMutable = true;
                      // options.inJustDecodeBounds = true;
                      //  int imageWidth = Math.round(options.outWidth/10);
                      //  int imageHeight = Math.round(options.outHeight/10);

                        selectBitmap = BitmapFactory.decodeStream(imageinputStream, null, options);
                      //  selectBitmap = decodeSampledBitmapFromStream(imageinputStream);

                        cutBitmup = detectFaceInImage(imageview, selectBitmap);
                        //imageview.setImageBitmap(selectBitmap);
                        //imageview.setImageDrawable(new BitmapDrawable(getResources(), selectBitmap));
                        sendbutton.setVisibility(View.VISIBLE);
                        rotateButton.setVisibility(View.VISIBLE);

                    } catch (Exception e) {
                        e.getStackTrace();
                    }
                }
        }
    }

    public static Bitmap decodeSampledBitmapFromStream(InputStream imageinputStream) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inMutable = true;
        BitmapFactory.decodeStream(imageinputStream, null, options);
        int imageWidth = Math.round(options.outWidth/10);
        int imageHeight = Math.round(options.outHeight/10);
        //BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, 300, 300);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(imageinputStream, null, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    protected Bitmap detectFaceInImage(ImageView imageview, Bitmap bitmap) {
        Paint rectPaint = new Paint();
        rectPaint.setStrokeWidth(5);
        rectPaint.setColor(Color.RED);
        rectPaint.setStyle(Paint.Style.STROKE);

        Bitmap tempBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
        Canvas tempCanvas = new Canvas(tempBitmap);
        tempCanvas.drawBitmap(bitmap, 0, 0, null);

        FaceDetector faceDetector = new FaceDetector.Builder(getApplicationContext()).setTrackingEnabled(false).build();
            if(!faceDetector.isOperational()) {
                resultDialog("Could not set up the face detector!");
                return bitmap;
            }

        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Face> faces = faceDetector.detect(frame);

       // textView1.setText(faces.size());

        int x1 = 0;
        int x2 = 0;
        int y1 = 0;
        int y2 = 0;
            if (faces.size() > 0) {
                Face thisFace = faces.valueAt(0);
                x1 = Math.round(thisFace.getPosition().x);
                y1 = Math.round(thisFace.getPosition().y);
                x2 = x1 + Math.round(thisFace.getWidth());
                y2 = y1 + Math.round(thisFace.getHeight());
                tempCanvas.drawRoundRect(new RectF(x1, y1, x2, y2), 2, 2, rectPaint);
            }

        imageview.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));

        return  (x1 == x2) ? bitmap :  Bitmap.createBitmap(bitmap, x1, y1, x2-x1, y2-y1);
    }

    protected ImageDataClass convertBitmapToBite(Bitmap selectBitmap) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(selectBitmap.getWidth() * selectBitmap.getHeight());
        selectBitmap.compress(Bitmap.CompressFormat.JPEG, 100, buffer);
        ImageDataClass img;

        img = new ImageDataClass(" ", buffer.toByteArray());

        return img;
    }

    protected void postImage() {
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://192.168.1.4:44387/")
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(OkhttpClass.getUnsafeOkHttpClient())
                    .build();

            ImageInterface service = retrofit.create(ImageInterface.class);


            Call<String> call = service.translateImage(convertBitmapToBite(cutBitmup));
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call call, Response response)  {
                    try (ResponseBody responseBody = (ResponseBody) response.body()) {
                        if (response.isSuccessful()) {
                            System.out.println("запрос true");
                            System.out.println(response.body());
                            System.out.println(responseBody);
                            String result = ((ResponseBody) response.body()).string();
                            int index = result.indexOf(",");
                            String res = result.substring(index+1, index+3);
                            if (Integer.parseInt(res) > 50) {
                                String[] arr = result.split("");
                                DAOClass.Add(arr[2], result.substring(index), Calendar.getInstance().getTime().toString());
                                resultDialog(result.substring(index));
                            } else {
                                resultDialog("Failed to recognize image");
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

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


    public void resultDialog(String result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(TranslateActivity.this);
        builder.setTitle("Result")
                .setIcon(R.drawable.smile)
                .setMessage(result)
                .setPositiveButton("Ok", null)
                .create().show();
    }
}
