package gov.alaska.gmchandheld;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TakePhoto_Test extends BaseActivity {

    private TextView imageViewTv;
    private ImageView uploadImageIv;
    private Button submitBtn;
    private EditText barcodeEt, descriptionEt;
    private final int CAM_REQUEST = 1;
    private final String PHOTO_PATH = "/sdcard/DCIM/Camera/";
    // 49374 return code is hardcoded into the Zxing file.
    // I need it here to capture the requestCode when IntentIntegrator is used for API <= 24
    private final int SCAN_BARCODE_REQUEST = 49374;
    private Uri image_uri;
    boolean cameraOn;
    private String filename, barcode, description;
    private volatile okhttp3.Response responseCode;
    private Handler mainUIHandler = new Handler();

    @Override
    public int getLayoutResource() {
        return R.layout.activity_take_photo_test;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo_test);
        cameraOn = sp.getBoolean("cameraOn", false);
        Button cameraBtn = findViewById(R.id.cameraBtn);
        if (!cameraOn) {
            cameraBtn.setVisibility(View.GONE);
        } else {
            qrScan = new IntentIntegrator(this);
            qrScan.setOrientationLocked(false);
            qrScan.setBeepEnabled(true);
        }
        cameraBtn.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT <= 24) {
                qrScan.initiateScan();
            } else {
                intent = new Intent(TakePhoto_Test.this, CameraToScanner.class);
                startActivityForResult(intent, SCAN_BARCODE_REQUEST);
            }
        });
        barcodeEt = findViewById(R.id.barcodeET);
        descriptionEt = findViewById(R.id.descriptionET);
        uploadImageIv = findViewById(R.id.imageToUploadIv);
        imageViewTv = findViewById(R.id.imageViewTv);
        uploadImageIv.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_DENIED ||
                        checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                                PackageManager.PERMISSION_DENIED) {
                    String[] permission = {Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    requestPermissions(permission, 1000);
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
                    Toast.makeText(getApplicationContext(),
                            "Barcode must be added before taking a photo.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        submitBtn = findViewById(R.id.submitBtn);
        submitBtn.setEnabled(false);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                new Thread(new Runnable() {
//                    public void run() {
//                        runOnUiThread(new Runnable() {
//                            public void run() {
//                                Toast.makeText(getApplicationContext(),
//                                        "uploading started.....",
//                                        Toast.LENGTH_LONG).show();
//                            }
//                        });
//                        filename = "img_" + new SimpleDateFormat("yyyyMMddHHmm'.jpeg'", Locale.US)
//                                .format(new Date());
//                        File file = new File(PHOTO_PATH + filename);
//                        uploadFile(file);
//
//                    }
//                }).start();
                filename = "img_" + new SimpleDateFormat("yyyyMMddHHmm'.jpeg'", Locale.US)
                        .format(new Date());
                File file = new File(PHOTO_PATH + filename);

                ExampleThread photoThread = new ExampleThread(file);
                Thread thread = new Thread(photoThread);
                thread.start();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Response result = photoThread.getResponse();
                if (result.code() == 200 | result.code() == 302) {
                    Toast.makeText(TakePhoto_Test.this, "Image Uploaded",
                            Toast.LENGTH_SHORT).show();
                    barcodeEt.setText("");
                } else {
                    Toast.makeText(TakePhoto_Test.this,
                            "Image wasn't uploaded.  Please take it again. Error code: " +
                                    result + ".", Toast.LENGTH_LONG).show();
                }
                uploadImageIv.setImageDrawable(null);
                imageViewTv.setText(R.string.click_to_add_image);
            }
        });
    }

    private void openCamera() {
        filename = "img_" + new SimpleDateFormat("yyyyMMddHHmm'.jpeg'", Locale.US)
                .format(new Date());
        File file = new File(PHOTO_PATH + filename);
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values);
        Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camera_intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(camera_intent, CAM_REQUEST);
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
                    IntentResult result = IntentIntegrator.parseActivityResult(requestCode,
                            resultCode, data);
                    barcodeEt.setText(result.getContents());
                } else {
                    if (resultCode == CommonStatusCodes.SUCCESS) {
                        if (data != null) {
                            Barcode barcode = data.getParcelableExtra("barcode");
                            EditText edit_text = findViewById(R.id.barcodeET);
                            if (barcode != null) {
                                edit_text.setText(barcode.displayValue);
                            }
                        }
                    } else {
                        super.onActivityResult(requestCode, resultCode, data);
                    }
                }
                break;
        }
    }

    private okhttp3.Response DoActualRequest(File file) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build();
        String urlBase = BaseActivity.sp.getString("urlText", "");
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
        Request request = new Request.Builder()
                .header("Authorization", "Token " + BaseActivity.apiKeyBase)
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

    private class ExampleThread implements Runnable{
        File file;
        okhttp3.Response response;

        public ExampleThread( File file) {
            this.file = file;
        }

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "uploading started.....",
                            Toast.LENGTH_LONG).show();
                }
            });
            response = DoActualRequest(file);
        }

        public okhttp3.Response getResponse(){
            return response;
        }
    }
}