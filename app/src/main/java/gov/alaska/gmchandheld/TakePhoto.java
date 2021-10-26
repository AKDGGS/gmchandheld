package gov.alaska.gmchandheld;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class TakePhoto extends BaseActivity implements RemoteAPIDownloadCallback {
    private static final int CAM_REQUEST = 1;
    private static final String PHOTO_PATH = "/sdcard/DCIM/Camera/";
    // 49374 return code is hardcoded into the Zxing file.
    // I need it here to capture the requestCode when IntentIntegrator is used for API <= 24
    private static final int SCAN_BARCODE_REQUEST = 49374;
    private TextView imageViewTV;
    private ImageView uploadImageIV;
    private Button submitBtn;
    private EditText barcodeET, descriptionET;
    private File file;
    private String barcode, description;
    private Uri image_uri;
    private boolean cameraOn;
    private ArrayList<File> fileList;

    @Override
    public int getLayoutResource() {
        return R.layout.activity_take_photo;
    }

    @Override
    public void onRestart() {
        super.onRestart();
        cameraOn = sp.getBoolean("cameraOn", false);
        View v = findViewById(R.id.cameraBtn);
        if (!cameraOn) {
            v.setVisibility(View.GONE);
        } else {
            v.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Set<File> fileSet = new HashSet<>(fileList);
        for (File f : fileSet) {
            f.delete();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraOn = sp.getBoolean("cameraOn", false);
        Button cameraBtn = findViewById(R.id.cameraBtn);
        fileList = new ArrayList<>();
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
        file = new File(PHOTO_PATH + filename);
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

        // KeyListener listens if enter is pressed
        barcodeET.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                descriptionET.requestFocus();
                return true;
            }
            return false;
        });

        submitBtn = findViewById(R.id.submitBtn);
        submitBtn.setEnabled(false);
        submitBtn.setOnClickListener(view -> {
            if (file.exists()) {
                if (barcodeET.getText().toString().trim().length() != 0) {
                    barcode = barcodeET.getText().toString().trim();
                }
                if (descriptionET.getText().toString().trim().length() != 0) {
                    description = descriptionET.getText().toString().trim();
                }

                MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                builder.addFormDataPart("barcode", barcode);
                builder.addFormDataPart("content", file.getName(),
                        RequestBody.create(MediaType.parse("Image/jpeg"), file));
                if (description != null) {
                    builder.addFormDataPart("description", description);
                }

                RequestBody requestBody = builder.build();

                try {
                    remoteAPIDownload.setFetchDataObj(baseURL + "/upload.json",
                            BaseActivity.apiKeyBase,
                            requestBody,
                            this,
                            RemoteAPIDownload.POST);
                } catch (Exception e) {
                    displayException(e);
                }
                fileList.add(file);
            }
        });
    }

    private void openCamera(File f) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATA, f.getAbsolutePath());
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

    @Override
    public void displayData(String data, int responseCode, String responseMessage, int requestType) {
        runOnUiThread(() -> {
            if (responseCode == 200 | responseCode == 302) {
                Toast.makeText(TakePhoto.this, "The photo was uploaded.",
                        Toast.LENGTH_SHORT).show();
                barcodeET.setText("");
                descriptionET.setText("");
                uploadImageIV.setImageDrawable(null);
                imageViewTV.setText(R.string.click_to_add_image);
            } else {
                if (responseCode == 403) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(TakePhoto.this,
                                    "The token is not correct.", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(TakePhoto.this, GetToken.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            TakePhoto.this.startActivity(intent);
                        }
                    });
                } else {
                    Toast.makeText(TakePhoto.this,
                            "There was a problem finding the image. Please take it again.",
                            Toast.LENGTH_SHORT).show();
                    uploadImageIV.setImageDrawable(null);
                    barcodeET.requestFocus();
                }
            }
        });
    }

    @Override
    public void displayException(Exception e) {
        if (e.getMessage() != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}