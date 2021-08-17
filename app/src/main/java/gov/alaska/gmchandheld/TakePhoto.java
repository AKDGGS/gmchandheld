package gov.alaska.gmchandheld;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

import gov.alaska.gmchandheld.BaseActivity;
import gov.alaska.gmchandheld.CameraToScanner;
import gov.alaska.gmchandheld.Configuration;
import gov.alaska.gmchandheld.ImageFileRequestBody;
import gov.alaska.gmchandheld.R;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class TakePhoto extends BaseActivity {

    private TextView imageViewTv;
    private ImageView uploadImageIv;
    private Button submitBtn;
    private EditText barcodeEt, descriptionEt;
    private String filename, barcode;
    private String description = null;
    private static final int CAM_REQUEST = 1;
    // 49374 return code is hardcoded into the Zxing file.
    // I need it here to capture the requestCode when IntentIntegrator is used for API <= 24
    private static final int SCAN_BARCODE_REQUEST = 49374;
    private static final int PERMISSION_CODE = 1000;
    private Uri image_uri;
    private IntentIntegrator qrScan;

    @Override
    public void onRestart() {
        super.onRestart();

        SharedPreferences sp = getSharedPreferences(Configuration.SHARED_PREFS, MODE_PRIVATE);
        boolean cameraOn = (sp.getBoolean("cameraOn", false));
        View v = findViewById(R.id.cameraBtn);
        if (!cameraOn) {
            v.setVisibility(View.GONE);
        }else{
            v.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Photo");
        toolbar.setBackgroundColor(Color.parseColor("#ff567b95"));
        SharedPreferences sp = getSharedPreferences(Configuration.SHARED_PREFS, MODE_PRIVATE);
        boolean cameraOn = (sp.getBoolean("cameraOn", false));
        Button cameraBtn = findViewById(R.id.cameraBtn);
        if (!cameraOn) {
            cameraBtn.setVisibility(View.GONE);
        } else {
            qrScan = new IntentIntegrator(this);
            qrScan.setOrientationLocked(false);
            qrScan.setBeepEnabled(true);
        }
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT <= 24) {
                    qrScan.initiateScan();
                } else {
                    Intent intent = new Intent(TakePhoto.this, CameraToScanner.class);
                    startActivityForResult(intent, SCAN_BARCODE_REQUEST);
                }
            }
        });
        barcodeEt = findViewById(R.id.barcodeET);
        descriptionEt = findViewById(R.id.descriptionET);
        uploadImageIv = findViewById(R.id.imageToUploadIv);
        imageViewTv = findViewById(R.id.imageViewTv);
        uploadImageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_DENIED ||
                        checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_DENIED) {
                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permission, PERMISSION_CODE);
                    } else {

                        if (barcodeEt.getText().toString().trim().length() != 0) {
                            barcode = barcodeEt.getText().toString().trim();
                            openCamera();
                        }
                    }
                } else {
                    if (barcodeEt.getText().toString().trim().length() != 0) {
                        barcode = barcodeEt.getText().toString().trim();
                        openCamera();
                    } else {
                        Toast.makeText(getApplicationContext(), "Barcode must be added before taking a photo.", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        submitBtn = findViewById(R.id.submitBtn);
        submitBtn.setEnabled(false);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File("/sdcard/DCIM/Camera/" + filename);
                if (file.exists()) {
                    new TakePhoto.UploadImage(TakePhoto.this).execute();
                } else {
                    Toast.makeText(TakePhoto.this, "There was a problem finding the image. Please take it again.", Toast.LENGTH_SHORT).show();
                    uploadImageIv.setImageDrawable(null);
                }
            }
        });
    }

    private void openCamera() {
        filename = "img_" + new SimpleDateFormat("yyyyMMddHHmm'.jpeg'").format(new Date());
        File file = new File("/sdcard/DCIM/Camera/" + filename);
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camera_intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(camera_intent, CAM_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CAM_REQUEST:
                if (resultCode == RESULT_OK) {
                    uploadImageIv.setImageURI(image_uri);
                    imageViewTv = findViewById(R.id.imageViewTv);
                    imageViewTv.setText("");
                    submitBtn.setEnabled(true);
                }
                break;
            case SCAN_BARCODE_REQUEST:
                if (Build.VERSION.SDK_INT <= 24) {
                    barcodeEt = findViewById(R.id.barcodeET);
                    IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                    barcodeEt.setText(result.getContents());
                } else {
                    if (resultCode == CommonStatusCodes.SUCCESS) {
                        Barcode barcode = data.getParcelableExtra("barcode");
                        EditText edit_text = findViewById(R.id.barcodeET);
                        if (null != barcode.displayValue) {
                            edit_text.setText(barcode.displayValue);
                        }
                    } else {
                        super.onActivityResult(requestCode, resultCode, data);
                    }
                }
                break;
        }
    }

    private class UploadImage extends AsyncTask<Void, Void, Integer> {
        private WeakReference<Context> mActivity;

        public UploadImage(Context context) {
            mActivity = new WeakReference<>(context);
        }
        @Override
        protected Integer doInBackground(Void... voids) {
            File file = new File("/sdcard/DCIM/Camera/" + filename);
            okhttp3.Response response = DoActualRequest(file);
            return response.code();
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result == 200 | result == 302) {
                Toast.makeText(getApplicationContext(), "Image Uploaded", Toast.LENGTH_SHORT).show();
                uploadImageIv.setImageDrawable(null);
                imageViewTv.setText("Click to add image");
            } else {
                Toast.makeText(getApplicationContext(), "Image wasn't uploaded.  Please take it again.", Toast.LENGTH_LONG).show();
                uploadImageIv.setImageDrawable(null);
                imageViewTv.setText("Click to add image");
            }
        }

        private okhttp3.Response DoActualRequest(File file) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                                                    .followRedirects(false)
                                                    .followSslRedirects(false)
                                                    .build();
            SharedPreferences sharedPreferences = mActivity.get().getSharedPreferences(Configuration.SHARED_PREFS, Context.MODE_PRIVATE);
            String urlBase = sharedPreferences.getString("urlText", "");
            String url = urlBase + "/upload.json";
            if (barcodeEt.getText().toString().trim().length() != 0) {
                barcode = barcodeEt.getText().toString().trim();
            }
            if (descriptionEt.getText().toString().trim().length() != 0) {
                description = descriptionEt.getText().toString().trim();
            }
            okhttp3.Response response = null;
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            builder.addFormDataPart("barcode", barcode);
            builder.addFormDataPart("content", file.getName(),
                                    RequestBody.create(MediaType.parse("Image/jpeg"), file));
            if (description != null) {
                builder.addFormDataPart("description", description);
            }
            MultipartBody body = builder.build();
            ImageFileRequestBody imageFileRequestBody = new ImageFileRequestBody(body);
            sharedPreferences = TakePhoto.this.getSharedPreferences(Configuration.SHARED_PREFS, Context.MODE_PRIVATE);
            String accessToken = sharedPreferences.getString("apiText", "");
//            String accessToken = "6Ve0DF0rRLH0RDDomchEdkCwU83prZbAEWqb27q9fs34o4zSisV6rgXSU3iLato9OlW6eXPBKyzj2x1OvMbv7WhANMKKjGgmJlNAkKQvR2s0SMmGN26m6hr3pbXp49NG";
            Request request = new Request.Builder()
                    .header("Authorization", "Token " + accessToken)
                    .url(url)
                    .post(imageFileRequestBody)
                    .build();
            try {
                response = client.newCall(request).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }
    }
}