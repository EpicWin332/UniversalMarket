package com.magomed.gamzatov.universalmarket.activity;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.magomed.gamzatov.universalmarket.R;
import com.magomed.gamzatov.universalmarket.adapter.RVAdapter;
import com.magomed.gamzatov.universalmarket.network.VolleySingleton;
import com.squareup.picasso.Picasso;
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
        final String imgUrl = getIntent().getStringExtra("image");
        startAnim();
        Picasso.with(this).load(imgUrl)
                .error(R.mipmap.no_image)
                .into(imagePreview, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        stopAnim();
                        mAttacher = new PhotoViewAttacher(imagePreview);
                    }

                    @Override
                    public void onError() {
                        stopAnim();
                        Toast.makeText(ImagePreview.this , "Error loading image from url " + imgUrl, Toast.LENGTH_LONG).show();
                    }
                });

    }

    private void startAnim(){
        avLoadingIndicatorView.setVisibility(View.VISIBLE);
    }

    private void stopAnim(){
        avLoadingIndicatorView.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(mAttacher!=null)
            mAttacher.cleanup();
    }

    @Override
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            supportFinishAfterTransition();
        }
        super.onBackPressed();
    }


}
