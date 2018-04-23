package com.example.wangliming.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.wangliming.gifcode.AnimatedGifEncoder;
import com.example.wangliming.gifcode.GifFrame;
import com.example.wangliming.gifcode.GifImageDecoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public interface OnSoftKeyboardStateChangedListener {
        public void OnSoftKeyboardStateChanged(boolean isKeyBoardShow, int keyboardHeight);
    }

    //注册软键盘状态变化监听
    public void addSoftKeyboardChangedListener(OnSoftKeyboardStateChangedListener listener) {
        if (listener != null) {
            mKeyboardStateListeners = listener;
        }
    }


    //取消软键盘状态变化监听
    public void removeSoftKeyboardChangedListener(OnSoftKeyboardStateChangedListener listener) {
        mKeyboardStateListeners = null;
    }

    private OnSoftKeyboardStateChangedListener mKeyboardStateListeners;
    private ViewTreeObserver.OnGlobalLayoutListener mLayoutChangeListener;
    private boolean mIsSoftKeyboardShowing;
    private static final float MAX_SIZE = 3;
    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    TextView textview;
    ImageView image;
    ImageView imageView1;
    ImageView imageView2;
    String url = "http://p1.pstatp.com/large/166200019850062839d3";
    static final String TAG = "bilibili";
    static final int IMAGE_MAX_SIZE_1MB = 1024 * 1024 * 1024;
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textview = findViewById(R.id.textview);
        image = findViewById(R.id.image);
        imageView1 = findViewById(R.id.imageview1);
        imageView2 = findViewById(R.id.imageview2);
        btn = findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectAlbum();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // 检查该权限是否已经获取
            int i = ContextCompat.checkSelfPermission(this, permissions[0]);
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
            if (i != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                showDialogTipUserRequestPermission();
            }
        }

    }

    private void showDialogTipUserRequestPermission() {

        new AlertDialog.Builder(this)
                .setTitle("存储权限不可用")
                .setMessage("由于支付宝需要获取存储空间，为你存储个人信息；\n否则，您将无法正常使用支付宝")
                .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startRequestPermission();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setCancelable(false).show();
    }

    // 开始提交请求权限
    private void startRequestPermission() {
        ActivityCompat.requestPermissions(this, permissions, 321);
    }

    // 用户权限 申请 的回调方法
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 321) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // 判断用户是否 点击了不再提醒。(检测该权限是否还可以申请)
                    boolean b = shouldShowRequestPermissionRationale(permissions[0]);
                    if (!b) {
                        // 用户还是想用我的 APP 的
                        // 提示用户去应用设置界面手动开启权限
                        showDialogTipUserGoToAppSettting();
                    } else
                        finish();
                } else {
                    Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private AlertDialog dialog;


    // 提示用户去应用设置界面手动开启权限

    private void showDialogTipUserGoToAppSettting() {

        dialog = new AlertDialog.Builder(this)
                .setTitle("存储权限不可用")
                .setMessage("请在-应用设置-权限-中，允许支付宝使用存储权限来保存用户数据")
                .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 跳转到应用设置界面
                        goToAppSetting();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setCancelable(false).show();
    }

    // 跳转到当前应用的设置界面
    private void goToAppSetting() {
        Intent intent = new Intent();

        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);

        startActivityForResult(intent, 123);
    }


    public static int[] getScreenSize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return new int[]{outMetrics.widthPixels, outMetrics.heightPixels};
    }


    @Override
    protected void onResume() {
        super.onResume();
        /*  Glide.with(this).load(R.drawable.rename).asGif().into(image);
         */
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mKeyboardStateListeners != null) {
            mKeyboardStateListeners = null;
        }
    }

    public static final int REQUEST_ALBUM = 2;

    private void selectAlbum() {
        Intent albumIntent = new Intent(Intent.ACTION_PICK);
        albumIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(albumIntent, REQUEST_ALBUM);
    }

    private File mImageFile;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK != resultCode) {
            return;
        }
        switch (requestCode) {
            case REQUEST_ALBUM:
                //createImageFile();
               /* if (!mImageFile.exists()) {
                    return;
                }*/
                Uri uri = data.getData();
                Log.e("bilibili", uri.getPath());
                String realPath = getRealPathFromUriAboveApi19(this, uri);
                File cacheDir = getExternalCacheDir();
                Uri destUri = new Uri.Builder()
                        .scheme("file")
                        .appendPath(cacheDir.getAbsolutePath())
                        .appendPath(String.format(Locale.US, "%s.gif", System.currentTimeMillis()))
                        .build();
                File destFile = new File(destUri.getPath());
                showGif2(realPath);
                break;
            case (123): {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // 检查该权限是否已经获取
                    int i = ContextCompat.checkSelfPermission(this, permissions[0]);
                    // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
                    if (i != PackageManager.PERMISSION_GRANTED) {
                        // 提示用户应该去应用设置界面手动开启权限
                        showDialogTipUserGoToAppSettting();
                    } else {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }


    GifImageDecoder gifDecoder;

    void showGif2(String sourceImgPath) {
        gifDecoder = new GifImageDecoder();
        File sourceFile = new File(sourceImgPath);

        if (sourceFile == null || !sourceFile.exists()) {
            return;
        }
        InputStream is = null;
        try {
            is = new FileInputStream(sourceImgPath);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            gifDecoder.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "抽帧前" + "文件大小:  " + sourceFile.length() + "帧数：" + gifDecoder.getFrameCount());
        File cacheDir = getExternalCacheDir();
        Uri destUri = new Uri.Builder()
                .scheme("file")
                .appendPath(cacheDir.getAbsolutePath())
                .appendPath(String.format(Locale.US, "%s.gif", System.currentTimeMillis()))
                .build();



        int step = 1;
        ArrayList<GifFrame> listFrames = new ArrayList<>();
        File desFile;
        do {
            desFile = new File(destUri.getPath());
            listFrames.clear();
            step++;
            for (int i = 0; i < gifDecoder.getFrameCount(); i += step) {
                listFrames.add(gifDecoder.getmGifFrames().get(i));
            }
            compress(desFile, step, listFrames);
            Log.e(TAG, "抽帧后" + "文件大小:  " + desFile.length() + "帧数：" + gifDecoder.getFrameCount());
            Log.e(TAG,desFile.length() / 1024 / 1024+"");
        } while ((float)desFile.length() / 1024 / 1024 > MAX_SIZE);


        Glide.with(this).load(desFile).asGif().into(imageView1);
        Glide.with(this).load(desFile).asGif().into(imageView2);

    }

    private void compress(File desFile, int step, List<GifFrame> frames) {
        AnimatedGifEncoder gifEncoder = new AnimatedGifEncoder();
        OutputStream os = null;
        try {
            os = new FileOutputStream(desFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        gifEncoder.start(os);
        for (int i = 0; i < frames.size(); i++) {
            gifEncoder.addFrame(frames.get(i).image);
            gifEncoder.setDelay(gifDecoder.getDelay(i) * step);
            gifEncoder.setRepeat(0);
        }
        gifEncoder.finish();
    }


    /**
     * 抽帧的方式
     **/
   /* private static boolean compressGifImg(String sourceImgPath, File desFile) {
        File sourceFile = new File(sourceImgPath);
        if (sourceFile == null || !sourceFile.exists()) {
            return false;
        }
       if (sourceFile.length() < 2) {
        } else {
            //Toast.makeText(BusOnlineApp.mApp.getApplicationContext(),"Gif图片太大需要压缩",Toast.LENGTH_SHORT).show();
        }
        GifDecoder gifImageDecoder = new GifDecoder(new GifBitmapProvider());
        InputStream is = null;
        try {
            is = new FileInputStream(sourceFile);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (gifImageDecoder.read(is, 16384) != GifDecoder.STATUS_OK) {
            return false;
        }

        int step = 1;
        boolean status = false;
        int iCount = gifImageDecoder.getFrameCount();
        ArrayList<GifFrame> listFrams = new ArrayList<>();
        do {
            listFrams.clear();
            step++;
            for (int i = 0; i < iCount; i += step) {
                listFrams.add(gifImageDecoder.getHeader().getFrames().get(i));
            }
            status = makeGif(desFile, listFrams, step);
            if (status) {
                Log.i(TAG, "Gif图片压缩完成后: " + desFile.length() / 1024 + "KB");
            } else {
                Log.i(TAG, "Gif图片合成失败");
                break;
            }
        } while (desFile.length() > IMAGE_MAX_SIZE_1MB);

        gifImageDecoder.clear();

        return status;
    }*/
    private static boolean makeGif(File saveFile, ArrayList<GifFrame> gifFrames, int step) {
        AnimatedGifEncoder gifEncoder = new AnimatedGifEncoder();
        if (!saveFile.exists())
            try {
                saveFile.createNewFile();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        if (step > 3) {
            step--;
        }
        OutputStream os;
        try {
            os = new FileOutputStream(saveFile);
            gifEncoder.start(os);
            for (int i = 0; i < gifFrames.size(); i++) {
                gifEncoder.addFrame(gifFrames.get(i).image);
                gifEncoder.setDelay(gifFrames.get(i).delay * step);
                gifEncoder.setRepeat(0);
            }
            return gifEncoder.finish();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }


    @SuppressLint("NewApi")
    private static String getRealPathFromUriAboveApi19(Context context, Uri uri) {
        String filePath = null;
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // 如果是document类型的 uri, 则通过document id来进行处理
            String documentId = DocumentsContract.getDocumentId(uri);
            if (isMediaDocument(uri)) { // MediaProvider
                // 使用':'分割
                String id = documentId.split(":")[1];

                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = {id};
                filePath = getDataColumn(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs);
            } else if (isDownloadsDocument(uri)) { // DownloadsProvider
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId));
                filePath = getDataColumn(context, contentUri, null, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是 content 类型的 Uri
            filePath = getDataColumn(context, uri, null, null);
        } else if ("file".equals(uri.getScheme())) {
            // 如果是 file 类型的 Uri,直接获取图片对应的路径
            filePath = uri.getPath();
        }
        return filePath;
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is DownloadsProvider
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * 获取数据库表中的 _data 列，即返回Uri对应的文件路径
     *
     * @return
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        String path = null;

        String[] projection = new String[]{MediaStore.Images.Media.DATA};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
                path = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return path;
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is MediaProvider
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}