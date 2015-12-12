package com.example.administrator.work6;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class CameraActivity extends Activity implements View.OnClickListener, SurfaceHolder.Callback {
    private SurfaceView mSurfaceView;
    private ImageView mImageView;
    private SurfaceHolder mSurfaceHolder;
    private ImageView shutter;
    private android.hardware.Camera mCamera=null;
    private boolean mPreViewRunning;
    private static final int MENU_START=1;
    private static final int MENU_SENSOR=2;
    private Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);
        mSurfaceView= (SurfaceView) findViewById(R.id.camera);
        mImageView= (ImageView) findViewById(R.id.image);
        shutter= (ImageView) findViewById(R.id.shutter);
        shutter.setOnClickListener(this);
        mImageView.setVisibility(View.GONE);
        mSurfaceHolder=mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    @Override
    public void onClick(View view) {
        if (mPreViewRunning){
            shutter.setEnabled(false);
           mCamera.autoFocus(new Camera.AutoFocusCallback() {
               @Override
               public void onAutoFocus(boolean b, Camera camera) {
                    mCamera.takePicture(mShutterCallback,null,mPictureCallback);
               }
           });
        }
    }
    android.hardware.Camera.PictureCallback mPictureCallback=new android.hardware.Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes, android.hardware.Camera camera) {
                if (bytes!=null){
                    saveAndShow(bytes);
                }
        }
    };
    Camera.ShutterCallback mShutterCallback=new android.hardware.Camera.ShutterCallback(){

        @Override
        public void onShutter() {
            System.out.println("快照回调函数......");
        }
    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,MENU_START,0,"重拍");
        menu.add(0,MENU_SENSOR,0,"打开相册");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==MENU_START){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            return true;
        }else if (item.getItemId()==MENU_SENSOR){
            Intent intent=new Intent(this,AlbumActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
public void saveAndShow(byte[] data){
    try {
        String imageId=System.currentTimeMillis()+"";
        String pathName= android.os.Environment.getExternalStorageDirectory().getPath()+"/com.demo.pr4";
    File file=new File(pathName);
        if (file.exists()){
            file.mkdirs();
        }
    pathName="/"+imageId+".jpeg";
    file=new File(pathName);
    if (file.exists()){

            file.createNewFile();
    }
        FileOutputStream fos=new FileOutputStream(file);
        fos.write(data);
        fos.close();
        AlbumActivity album=new AlbumActivity();
        bitmap=album.loadImage(pathName);
        mImageView.setImageBitmap(bitmap);
        mImageView.setVisibility(View.VISIBLE);
        mSurfaceView.setVisibility(View.GONE);
        if (mPreViewRunning){
            mCamera.stopPreview();
            mPreViewRunning=false;
        }
        shutter.setEnabled(true);
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        setCameraParams();
    }

    public void setCameraParams() {
        if (mCamera!=null){
            return;
        }
        mCamera=Camera.open();
        Camera.Parameters parms=mCamera.getParameters();
        parms.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        parms.setPreviewFrameRate(3);
        parms.setPreviewFormat(PixelFormat.YCbCr_422_SP);
        parms.set("jpeg-quality", 85);
        List<Camera.Size> list=parms.getSupportedPictureSizes();
        Camera.Size size=list.get(0);
        int w=size.width;
        int h=size.height;
        parms.setPictureSize(w,h);
        parms.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        try {
        if (mPreViewRunning){
            mCamera.stopPreview();
        }
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
            mPreViewRunning=true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (mCamera!=null){
            mCamera.stopPreview();
            mPreViewRunning=false;
            mCamera.release();
            mCamera=null;
        }
    }
}
