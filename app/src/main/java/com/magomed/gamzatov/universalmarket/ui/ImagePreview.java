package com.magomed.gamzatov.universalmarket.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.magomed.gamzatov.universalmarket.R;
import com.magomed.gamzatov.universalmarket.network.VolleySingleton;
import com.wang.avi.AVLoadingIndicatorView;

import uk.co.senab.photoview.PhotoViewAttacher;

public class ImagePreview extends AppCompatActivity {

    private PhotoViewAttacher mAttacher;
    private AVLoadingIndicatorView avLoadingIndicatorView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_image_preview);

        avLoadingIndicatorView = (AVLoadingIndicatorView) findViewById(R.id.avloadingIndicatorView);
        final ImageView imagePreview = (ImageView) findViewById(R.id.imagePreview);
        String imgUrl = getIntent().getStringExtra("image");
        startAnim();
        ImageLoader imageLoader = VolleySingleton.getsInstance().getImageLoader();
        imageLoader.get(imgUrl, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                stopAnim();
                if (imagePreview != null) {
                    imagePreview.setImageBitmap(response.getBitmap());
                    mAttacher = new PhotoViewAttacher(imagePreview);
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                stopAnim();
                if (imagePreview != null) {
                    imagePreview.setImageResource(R.mipmap.no_image);
                }
                Toast.makeText(ImagePreview.this , "Error " + error, Toast.LENGTH_LONG).show();
            }
        });

    }

    private void startAnim(){
        avLoadingIndicatorView.setVisibility(View.VISIBLE);
    }

    private void stopAnim(){
        avLoadingIndicatorView.setVisibility(View.GONE);
    }


}
