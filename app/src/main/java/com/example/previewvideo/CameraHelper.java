package com.example.previewvideo;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowManager;

public class CameraHelper implements Camera.PreviewCallback{

    private final String TAG = "PREVIEW_DATA";
    private Camera camera = null;
    private int cameraOrientation = 90;
    private byte[] cameraPreviewBuffer;
    private int width = 0;
    private int height = 0;
    private Context context;

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public CameraHelper(Context context){
        this.context = context;
        width = ((WindowManager)context
                .getSystemService(context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
        height = ((WindowManager)context
                .getSystemService(context.WINDOW_SERVICE)).getDefaultDisplay().getHeight();
        //camera = Camera.open();
    }

    /**
     * 实时获取预览视频中的某一帧数据
     * @param bytes
     * @param camera
     */
    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        int frameWidth = parameters.getPreviewSize().width;
        int frameHeight = parameters.getPreviewSize().height;


        camera.addCallbackBuffer(cameraPreviewBuffer);
    }

    /**
     * 打开预览摄像头
     * @param surfaceHolder
     */
    public void openCamera(SurfaceHolder surfaceHolder){

        try {
            camera = Camera.open();
            if(camera != null) {
                camera.setPreviewDisplay(surfaceHolder);
                camera.setDisplayOrientation(cameraOrientation);
                final Camera.Parameters parameters = camera.getParameters();
                final Camera.Size size = getBestPreviewSize(width, height);
                Log.i(TAG,"size.width = " + size.width);
                Log.i(TAG,"size.height = " + size.height);
                parameters.setPreviewSize(size.width, size.height);
                parameters.setPictureSize(size.width, size.height);

                camera.setParameters(parameters);
                cameraPreviewBuffer = new byte[(int) (getPreviewWidth() * getPreviewHeight() * 1.5)];//1.5 for yuv image
                camera.addCallbackBuffer(cameraPreviewBuffer);
                camera.setPreviewCallback(this);
                camera.startPreview();
            }
        }catch (Exception e)
        {
            System.out.println("Camera opened: " + camera);
            System.out.println("Camera Exception: " + e);
            Log.d(TAG,"camera开启失败");
            Log.e(TAG, e.toString());
        }
    }

    /**
     * 释放相机资源
     */
    public void releaseCamera(){
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            Log.e(TAG, "releaseCamera: " );
            camera.release();//释放相机资源
            camera = null;
        }
    }

    /**
     * 获取相机最合适的预览尺寸
     * @param width
     * @param height
     * @return
     */
    private Camera.Size getBestPreviewSize(int width, int height) {
        Camera.Size result = null;
        final Camera.Parameters p = camera.getParameters();
        //特别注意此处需要规定rate的比是大的比小的，不然有可能出现rate = height/width，但是后面遍历的时候，current_rate = width/height,所以我们限定都为大的比小的。
        float rate = (float) Math.max(width, height)/ (float)Math.min(width, height);
        float tmp_diff;
        float min_diff = -1f;
        for (Camera.Size size : p.getSupportedPreviewSizes()) {
            float current_rate = (float) Math.max(size.width, size.height)/ (float)Math.min(size.width, size.height);
            tmp_diff = Math.abs(current_rate-rate);
            if( min_diff < 0){
                min_diff = tmp_diff ;
                result = size;
            }
            if( tmp_diff < min_diff ){
                min_diff = tmp_diff ;
                result = size;
            }
        }
        return result;
    }

    /**
     * 获取相机预览宽
     * @return
     */
    private int getPreviewWidth() {
        Camera.Parameters parameters = camera.getParameters();
        return parameters.getPreviewSize().width;

    }
    /**
     * 获取相机预览高
     * @return
     */
    private int getPreviewHeight() {
        Camera.Parameters parameters = camera.getParameters();
        return parameters.getPreviewSize().height;
    }
}
