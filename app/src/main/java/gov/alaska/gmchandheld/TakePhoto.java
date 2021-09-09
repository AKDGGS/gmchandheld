package gov.alaska.gmchandheld;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class TakePhoto extends BaseActivity {
    private TextView imageViewTV;
    private ImageView uploadImageIV;
    private Button submitBtn;
    private EditText barcodeET, descriptionET;
    private volatile File file;
    private String barcode, description;
    private static final int CAM_REQUEST = 1;
    private static final String PHOTO_PATH = "/sdcard/DCIM/Camera/";
    private Uri image_uri;
    private boolean cameraOn;
    private Integer responseCode;
    // 49374 return code is hardcoded into the Zxing file.
    // I need it here to capture the requestCode when IntentIntegrator is used for API <= 24
    private static final int SCAN_BARCODE_REQUEST = 49374;

    @Override
    public int getLayoutResource() {
        return R.layout.activity_take_photo;
    }

    @Override
    public void onRestart() {
        super.onRestart();
        checkAPIkeyExists(this);
        cameraOn = sp.getBoolean("cameraOn", false);
        View v = findViewById(R.id.cameraBtn);
        if (!cameraOn) {
            v.setVisibility(View.GONE);
        } else {
            v.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                intent = new Intent(TakePhoto.this, CameraToScanner.class);
                startActivityForResult(intent, SCAN_BARCODE_REQUEST);
            }
        });
        barcodeET = findViewById(R.id.barcodeET);
        descriptionET = findViewById(R.id.descriptionET);
        uploadImageIV = findViewById(R.id.imageToUploadIv);
        imageViewTV = findViewById(R.id.imageViewTv);
        String filename = "img_" + new SimpleDateFormat("yyyyMMddHHmm'.jpeg'", Locale.US)
                .format(new Date());
        File file = new File(PHOTO_PATH + filename);
        uploadImageIV.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_DENIED ||
                        checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                                PackageManager.PERMISSION_DENIED) {
                    String[] permission = {
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    };
                    requestPermissions(permission, 1000);
                } else {
                    if (barcodeET.getText().toString().trim().length() != 0) {
                        barcode = barcodeET.getText().toString().trim();
                        openCamera(file);
                    }
                }
            } else {
                if (barcodeET.getText().toString().trim().length() != 0) {
                    barcode = barcodeET.getText().toString().trim();
                    openCamera(file);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Barcode must be added before taking a photo.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        submitBtn = findViewById(R.id.submitBtn);
        submitBtn.setEnabled(false);
        submitBtn.setOnClickListener(view -> {
            if (file.exists()) {
                String urlBase = BaseActivity.sp.getString("urlText", "");
                String url = urlBase + "/upload.json";
                if (barcodeET.getText().toString().trim().length() != 0) {
                    barcode = barcodeET.getText().toString().trim();
                }
                if (descriptionET.getText().toString().trim().length() != 0) {
                    description = descriptionET.getText().toString().trim();
                }
                Runnable runnable = () -> {
                    if (thread.isInterrupted()) {
                        return;
                    }
                    final ExecutorService service =
                            Executors.newFixedThreadPool(1);
                    final Future < Integer > task =
                            service.submit(new UploadImage(barcode, description, url, file));
                    try {
                        responseCode = task.get();
                        System.out.println("ResponseCode: " + responseCode);
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                    runOnUiThread(() -> {
                        if (responseCode == 200 | responseCode == 302) {
                            Toast.makeText(TakePhoto.this, "The photo was uploaded.",
                                    Toast.LENGTH_SHORT).show();
                            barcodeET.setText("");
                            descriptionET.setText("");
                            uploadImageIV.setImageDrawable(null);
                            imageViewTV.setText(R.string.click_to_add_image);
                        } else {
                            Toast.makeText(TakePhoto.this,
                                    "There was a problem finding the image. Please take it again.",
                                    Toast.LENGTH_SHORT).show();
                            uploadImageIV.setImageDrawable(null);
                            barcodeET.requestFocus();
                        }
                        file.delete();
                    });
                };
                thread = new Thread(runnable);
                thread.start();
            }
        });
    }

    private void openCamera(File file) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values);
        Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camera_intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(camera_intent, CAM_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 1000) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                System.out.println("File " + file);
                openCamera(file);
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CAM_REQUEST:
                if (resultCode == RESULT_OK) {
                    uploadImageIV.setImageURI(image_uri);
                    imageViewTV = findViewById(R.id.imageViewTv);
                    imageViewTV.setText("");
                    submitBtn.setEnabled(true);
                }
                break;
            case SCAN_BARCODE_REQUEST:
                if (Build.VERSION.SDK_INT <= 24) {
                    barcodeET = findViewById(R.id.barcodeET);
                    IntentResult result = IntentIntegrator.parseActivityResult(requestCode,
                            resultCode, data);
                    barcodeET.setText(result.getContents());
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

    private class UploadImage implements Callable < Integer > {
        private String url,
                barcode,
                description;
        private File file;

        public UploadImage(String barcode, String description, String url, File file) {
            this.barcode = barcode;
            this.description = description;
            this.url = url;
            this.file = file;
        }

        @Override
        public Integer call() throws Exception {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .followRedirects(false)
                    .followSslRedirects(false)
                    .build();
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
            return response.code();
        }
    }
}