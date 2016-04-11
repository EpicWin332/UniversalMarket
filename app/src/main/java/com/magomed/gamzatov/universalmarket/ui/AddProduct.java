package com.magomed.gamzatov.universalmarket.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.magomed.gamzatov.universalmarket.R;
import com.magomed.gamzatov.universalmarket.entity.FileUploadService;
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
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddProduct extends AppCompatActivity {

    private static final int REQUEST_CODE_SOME_FEATURES_PERMISSIONS = 1;
    private static final int REQUEST_TAKE_PHOTO = 2;
    private static final int RESULT_LOAD_IMG = 3;
    private String mCurrentPhotoPath;
    String imgDecodableString;
    ImageView imageButton;
    boolean photoAdded=false;
    private AVLoadingIndicatorView avLoadingIndicatorView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);
        initToolbar("Добавление");
        imageButton = (ImageView) findViewById(R.id.imageView2);
        final Button button = (Button) findViewById(R.id.button);
        final EditText editBrand = (EditText) findViewById(R.id.editBrand);
        final EditText editPrice = (EditText) findViewById(R.id.editPrice);
        final EditText editDescription = (EditText) findViewById(R.id.editDescription);
        avLoadingIndicatorView = (AVLoadingIndicatorView) findViewById(R.id.avloadingIndicatorView);

        stopAnim();

        imageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AddProduct.this);
                // Add the buttons
                builder.setPositiveButton("Сделать фото", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            photoPermissons();
                        } else {
                            dispatchTakePictureIntent();
                        }
                    }
                });
                builder.setNegativeButton("Взять из галереи", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });


        button.setOnClickListener(new View.OnClickListener() {
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

                imageButton.setEnabled(false);
                button.setEnabled(false);
                startAnim();

                FileUploadService service = ServiceGenerator.createService(FileUploadService.class);

                HashMap<String,RequestBody> map=new HashMap<>();
                RequestBody file=null;
                File f=null;

                if(photoAdded) {
                    //for(int i=0,size=listOfNames.size(); i<size;i++){
                    try {
                        f = new File(getApplicationContext().getCacheDir(), "file1.jpg");
                        FileOutputStream fos = new FileOutputStream(f);
                        //Bitmap bitmap = bitmapList.get(0);
                        Bitmap bitmap = ((BitmapDrawable)imageButton.getDrawable()).getBitmap();
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
                    map.put("file\"; filename=\"file1\"; fileExtension=\"jpg\"; ", file);
                    file = null;
                    f = null;
                    //}
                }

                RequestBody description = RequestBody.create(MediaType.parse("multipart/form-data"), editDescription.getText().toString());
                RequestBody brand = RequestBody.create(MediaType.parse("multipart/form-data"), editBrand.getText().toString());
                RequestBody typeId = RequestBody.create(MediaType.parse("multipart/form-data"), "1");
                RequestBody shopId = RequestBody.create(MediaType.parse("multipart/form-data"), "1");
                RequestBody price = RequestBody.create(MediaType.parse("multipart/form-data"), editPrice.getText().toString());

                service.uploadImage(map, description, brand, typeId, shopId, price).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        Log.v("Upload", "success " + response.code());
                        imageButton.setEnabled(true);
                        button.setEnabled(true);
                        stopAnim();
                        if(response.code()==200) {
                            Toast.makeText(AddProduct.this, "Успешно добавлено", Toast.LENGTH_SHORT).show();
                            onBackPressed();
                        }
                        else
                            Toast.makeText(AddProduct.this, "При добавлении возникла ошибка", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.e("Upload", t.getMessage());
                        imageButton.setEnabled(true);
                        button.setEnabled(true);
                        stopAnim();
                        Toast.makeText(AddProduct.this, "При добавлении возникла ошибка", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }

    private boolean isEmpty(EditText myEditText) {
        return myEditText.getText().toString().trim().length() == 0;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void photoPermissons() {
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
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);

        if(getSupportActionBar()!=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
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
        int targetW = imageButton.getWidth();
        int targetH = imageButton.getHeight();

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
        imageButton.setImageBitmap(bitmap);
        imageButton.setScaleType(ImageView.ScaleType.CENTER_CROP);
        photoAdded=true;
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
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgDecodableString = cursor.getString(columnIndex);
                cursor.close();

                setPic(imgDecodableString);

            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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
