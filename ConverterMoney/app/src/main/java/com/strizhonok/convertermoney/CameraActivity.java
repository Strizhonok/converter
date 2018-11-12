package com.strizhonok.convertermoney;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.AutoFocusCallback, Camera.PictureCallback
{
    final int REQUEST_CODE_PERMISSION_CAMERA = 1;
    final int CAMERA_ID = 0;

    protected SurfaceHolder holder;
    protected SurfaceView surfaceView;
    protected Camera camera;
    protected FrameLayout cameraLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_camera);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        cameraLayout = findViewById(R.id.layout_camera);

        //запрос на разрешение работы с камерой
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA},
                    REQUEST_CODE_PERMISSION_CAMERA);
        } else {
            startCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCamera();
                } else {
                    Toast.makeText(this, R.string.not_premission, Toast.LENGTH_LONG).show();
                }
        }
    }

    private void startCamera() {
        Log.d("my_c", "start");

        surfaceView = new SurfaceView(this);
        holder = surfaceView.getHolder();
        holder.addCallback(this);
        cameraLayout.addView(surfaceView);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d("my_c", "create");

        try {
            camera = Camera.open(CAMERA_ID);
            camera.setPreviewDisplay(holder);
            camera.startPreview();
            setCameraDisplayOrientation(CAMERA_ID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.d("my_c", "change");

        camera.stopPreview();
        setCameraDisplayOrientation(CAMERA_ID);
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
            camera.autoFocus(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder)
    {
        Log.d("my_c", "delete");
        if (camera != null) {
            camera.release();
        }
    }

    void setCameraDisplayOrientation(int cameraId) {
        // определяем насколько повернут экран от нормального положения
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) { //потом пригодится, пока всегда 0
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        // получаем инфо по камере cameraId
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        int result = 0;
        result = ((360 - degrees) + info.orientation);
        result = result % 360;

        camera.setDisplayOrientation(result);
    }

    public void clickCameraImage(View view) {
        Log.d("my_c", "on click");

        camera.autoFocus(this);
    }

    @Override
    public void onAutoFocus(boolean b, final Camera myCamera) {
        Log.d("my_c", "on focus");

        if (myCamera != null && (Camera.Parameters.FOCUS_MODE_AUTO.equals(myCamera.getParameters().getFocusMode()) ||
                Camera.Parameters.FOCUS_MODE_MACRO.equals(myCamera.getParameters().getFocusMode())))
        {
            myCamera.autoFocus(CameraActivity.this);
            myCamera.takePicture(null, null, null, CameraActivity.this);
            myCamera.cancelAutoFocus();
        }
    }

    private Bitmap rotateImage(Bitmap source, float angle){
        Bitmap rotateBitmap;
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
       // matrix.postScale(source.getWidth(), source.getHeight());
        rotateBitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        return rotateBitmap;
    }

    @Override
    public void onPictureTaken(byte[] paramArrayOfByte, Camera myCamera) {
        Log.d("my_c", "on preview");

        Bitmap imageCamera = BitmapFactory.decodeByteArray(paramArrayOfByte, 0, paramArrayOfByte.length);

       // ImageView iv = findViewById(R.id.imageView);
      //  iv.setImageBitmap(rotateImage(imageCamera, 90));

        FirebaseVisionImage convertImage = FirebaseVisionImage.fromBitmap(imageCamera);//rotateImage(imageCamera, 180));
        FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();

        textRecognizer.processImage(convertImage)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText result) {
                        Toast.makeText(CameraActivity.this, "получилось", Toast.LENGTH_SHORT).show();

                        String resultText = result.getText();
                        for (FirebaseVisionText.TextBlock block: result.getTextBlocks()) {
                            String blockText = block.getText();
                            Float blockConfidence = block.getConfidence();
                            List<RecognizedLanguage> blockLanguages = block.getRecognizedLanguages();
                            Point[] blockCornerPoints = block.getCornerPoints();
                            Rect blockFrame = block.getBoundingBox();
                            for (FirebaseVisionText.Line line: block.getLines()) {
                                String lineText = line.getText();
                                Float lineConfidence = line.getConfidence();
                                List<RecognizedLanguage> lineLanguages = line.getRecognizedLanguages();
                                Point[] lineCornerPoints = line.getCornerPoints();
                                Rect lineFrame = line.getBoundingBox();
                                for (FirebaseVisionText.Element element: line.getElements()) {
                                    String elementText = element.getText();
                                    Float elementConfidence = element.getConfidence();
                                    List<RecognizedLanguage> elementLanguages = element.getRecognizedLanguages();
                                    Point[] elementCornerPoints = element.getCornerPoints();
                                    Rect elementFrame = element.getBoundingBox();
                                }
                            }
                        }

                        Log.d("my_c", "result " + resultText);
                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(CameraActivity.this, "ничего не получилось", Toast.LENGTH_SHORT).show();
                            }
                        });
    }
}
