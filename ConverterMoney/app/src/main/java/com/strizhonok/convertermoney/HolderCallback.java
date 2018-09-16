package com.strizhonok.convertermoney;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;

public class HolderCallback extends SurfaceView implements SurfaceHolder.Callback
{
    final int CAMERA_ID = 0;

    Context context;
    SurfaceHolder holder;
    Camera camera;

    public HolderCallback(Context context)
    {
        super(context);
        this.context = context;

        holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
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
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        camera.stopPreview();
        setCameraDisplayOrientation(CAMERA_ID);
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    void setCameraDisplayOrientation(int cameraId)
    {
        try
        {
            // определяем насколько повернут экран от нормального положения
            int rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getOrientation();
            int degrees = 0;
            switch (rotation) {
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

            int result = 0;

            // получаем инфо по камере cameraId
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, info);

            result = ((360 - degrees) + info.orientation);
            result = result % 360;

            camera.setDisplayOrientation(result);
        }
        catch (NullPointerException e)
        {
            Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
        }

    }
}
