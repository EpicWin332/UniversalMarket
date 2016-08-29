package com.magomed.gamzatov.universalmarket.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.magomed.gamzatov.universalmarket.R;
import com.magomed.gamzatov.universalmarket.network.FileUploadService;
import com.magomed.gamzatov.universalmarket.network.ServiceGenerator;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddProduct extends AppCompatActivity {

    private static final int REQUEST_CODE_SOME_FEATURES_PERMISSIONS = 1;
    private static final int REQUEST_TAKE_PHOTO = 2;
    private static final int RESULT_LOAD_IMG = 3;
    private static final int MAX_PHOTO_NUMBER = 3;

    private String mCurrentPhotoPath;
    private ImageView photoButton;
    private AVLoadingIndicatorView avLoadingIndicatorView;
    private Button addButton;
    private EditText editBrand;
    private EditText editPrice;
    private EditText editDescription;
    private ImageView clickedImageView;
    private LinearLayout containerLayout;
    private Set<ImageView> photoViewSet = new LinkedHashSet<>();
    private int totalImagePicker = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);
        initToolbar("Добавление");
        photoButton = (ImageView) findViewById(R.id.photoView);
        addButton = (Button) findViewById(R.id.addButton);
        editBrand = (EditText) findViewById(R.id.editBrand);
        editPrice = (EditText) findViewById(R.id.editPrice);
        editDescription = (EditText) findViewById(R.id.editDescription);
        avLoadingIndicatorView = (AVLoadingIndicatorView) findViewById(R.id.avloadingIndicatorView);
        containerLayout = (LinearLayout) findViewById(R.id.container);

        stopAnim();
        setFont();
        addImageOnClickListener(photoButton);
        addButtonOnClickListener();
    }

    private void addImagePicker() {

        totalImagePicker++;
        final LayoutInflater layoutInflater = getLayoutInflater();
        final View addView = layoutInflater.inflate(R.layout.row_add_image, containerLayout, false);
        final ImageView imageButton = (ImageView) addView.findViewById(R.id.photoView);
        ImageView cancealAddImage = (ImageView) addView.findViewById(R.id.cancelAddImage);

        cancealAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                totalImagePicker--;
                photoViewSet.remove(imageButton);
                ((LinearLayout)addView.getParent()).removeView(addView);
                if(totalImagePicker<=1) {
                    addImagePicker();
                }
            }
        });

        addImageOnClickListener(imageButton);
        containerLayout.addView(addView);
    }

    private void setFont() {
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/LatoLight.ttf");
        editBrand.setTypeface(custom_font);
        editPrice.setTypeface(custom_font);
        editDescription.setTypeface(custom_font);

        Typeface custom_font1 = Typeface.createFromAsset(getAssets(), "fonts/LatoRegular.ttf");
        addButton.setTypeface(custom_font1);
    }

    private void addButtonOnClickListener() {
        if (addButton != null) {
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(isEmpty(editBrand)){
                        Toast.makeText(AddProduct.this, "Не заполнен бренд", Toast.LENGTH_SHORT).show();
                        return;
                    } else if(isEmpty(editPrice)){
                        Toast.makeText(AddProduct.this, "Не заполнена цена", Toast.LENGTH_SHORT).show();
                        return;
                    } else if(isEmpty(editDescription)){
                        Toast.makeText(AddProduct.this, "Не заполнено описание", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    photoButton.setEnabled(false);
                    addButton.setEnabled(false);
                    startAnim();

                    FileUploadService service = ServiceGenerator.createService(FileUploadService.class);

                    HashMap<String,RequestBody> map=new HashMap<>();
                    RequestBody file=null;
                    File f=null;
                    int i = 0;
                    for (ImageView photoView : photoViewSet) {
                        i++;
                        try {
                            f = new File(getApplicationContext().getCacheDir(), "file" + i + ".jpg");
                            FileOutputStream fos = new FileOutputStream(f);
                            Bitmap bitmap = ((BitmapDrawable) photoView.getDrawable()).getBitmap();
                            if (bitmap != null) {
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 99 /*ignored for PNG*/, fos);
                                fos.flush();
                                fos.close();
                            } else {
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }

                        file = RequestBody.create(MediaType.parse("multipart/form-data"), f);
                        map.put("file\"; filename=\"file" + i +"\"; fileExtension=\"jpg\"; ", file);
                        file = null;
                        f = null;
                    }

                    RequestBody description = RequestBody.create(MediaType.parse("multipart/form-data"), editDescription.getText().toString());
                    RequestBody brand = RequestBody.create(MediaType.parse("multipart/form-data"), editBrand.getText().toString());
                    RequestBody typeId = RequestBody.create(MediaType.parse("multipart/form-data"), "1");
                    RequestBody shopId = RequestBody.create(MediaType.parse("multipart/form-data"), "1");
                    RequestBody price = RequestBody.create(MediaType.parse("multipart/form-data"), editPrice.getText().toString());

                    SharedPreferences sharedPreferences = getSharedPreferences("cookies", MODE_PRIVATE);
                    String cookie = sharedPreferences.getString("cookie", "");

                    Log.d("cookie", cookie);

                    service.uploadImage(cookie, map, description, brand, typeId, shopId, price).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            Log.v("Upload", "success " + response.code());
                            photoButton.setEnabled(true);
                            addButton.setEnabled(true);
                            stopAnim();
                            if(response.code()==200) {
                                Toast.makeText(AddProduct.this, "Успешно добавлено", Toast.LENGTH_SHORT).show();
                                onBackPressed();
                            }
                            else {
                                Toast.makeText(AddProduct.this, "При добавлении возникла ошибка", Toast.LENGTH_SHORT).show();
                                Log.d("onResponse", response.code() + " " + response.message()+ " " + response.body());
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Log.e("Upload", t.getMessage());
                            photoButton.setEnabled(true);
                            addButton.setEnabled(true);
                            stopAnim();
                            Toast.makeText(AddProduct.this, "При добавлении возникла ошибка", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            });
        }
    }

    private void addImageOnClickListener(final ImageView imageView) {
        imageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AddProduct.this);
                // Add the buttons
                builder.setPositiveButton("Сделать фото", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        clickedImageView = imageView;
                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            photoPermissions();
                        } else {
                            dispatchTakePictureIntent();
                        }
                    }
                });
                builder.setNegativeButton("Взять из галереи", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        clickedImageView = imageView;
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    private boolean isEmpty(EditText myEditText) {
        return myEditText.getText().toString().trim().length() == 0;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void photoPermissions() {
        int hasWriteExternalPermission = checkSelfPermission( Manifest.permission.WRITE_EXTERNAL_STORAGE );
        int hasReadExternalPermission = checkSelfPermission( Manifest.permission.READ_EXTERNAL_STORAGE );
        int hasCameraPermission = checkSelfPermission( Manifest.permission.CAMERA);
        List<String> permissions = new ArrayList<String>();
        if( hasWriteExternalPermission != PackageManager.PERMISSION_GRANTED ) {
            permissions.add( Manifest.permission.WRITE_EXTERNAL_STORAGE );
        }

        if( hasReadExternalPermission != PackageManager.PERMISSION_GRANTED ) {
            permissions.add( Manifest.permission.READ_EXTERNAL_STORAGE );
        }

        if( hasCameraPermission != PackageManager.PERMISSION_GRANTED ) {
            permissions.add( Manifest.permission.CAMERA );
        }

        if( !permissions.isEmpty() ) {
            requestPermissions( permissions.toArray( new String[permissions.size()] ), REQUEST_CODE_SOME_FEATURES_PERMISSIONS );
        } else {
            Log.d("Permission", "All permissions already granted");
            dispatchTakePictureIntent();
        }
    }

    private void initToolbar(String title) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(title);
        }
        setSupportActionBar(toolbar);

        if(getSupportActionBar()!=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e("IOException", Arrays.toString(ex.getStackTrace()));
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void setPic(String mCurrentPhotoPath) {
        // Get the dimensions of the View
        int targetW = clickedImageView.getWidth();
        int targetH = clickedImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        clickedImageView.setImageBitmap(bitmap);
        clickedImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        //photoAdded=true;
        if(photoViewSet.size() < MAX_PHOTO_NUMBER - 1 && !photoViewSet.contains(clickedImageView)) {
            addImagePicker();
        }
        photoViewSet.add(clickedImageView);
    }

    private void startAnim(){
        avLoadingIndicatorView.setVisibility(View.VISIBLE);
    }

    private void stopAnim(){
        avLoadingIndicatorView.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            galleryAddPic();
            setPic(mCurrentPhotoPath);
        }
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                if (cursor != null) {
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String imgDecodableString = cursor.getString(columnIndex);
                    cursor.close();

                    setPic(imgDecodableString);
                }

            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch ( requestCode ) {
            case REQUEST_CODE_SOME_FEATURES_PERMISSIONS: {
                for( int i = 0; i < permissions.length; i++ ) {
                    if( grantResults[i] == PackageManager.PERMISSION_GRANTED ) {
                        Log.d( "Permissions", "Permission Granted: " + permissions[i] );
                    } else if( grantResults[i] == PackageManager.PERMISSION_DENIED ) {
                        Log.d( "Permissions", "Permission Denied: " + permissions[i] );
                    }
                }
            }
            break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }
}
