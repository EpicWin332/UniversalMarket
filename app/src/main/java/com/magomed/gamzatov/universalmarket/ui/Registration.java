package com.magomed.gamzatov.universalmarket.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.magomed.gamzatov.universalmarket.R;
import com.magomed.gamzatov.universalmarket.network.RegistrationQuery;
import com.magomed.gamzatov.universalmarket.network.ServiceGenerator;
import com.wang.avi.AVLoadingIndicatorView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Registration extends AppCompatActivity {

    private AVLoadingIndicatorView avLoadingIndicatorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);
        initToolbar("Регистрация");

        avLoadingIndicatorView = (AVLoadingIndicatorView) findViewById(R.id.avloadingIndicatorView);
        stopAnim();

        final EditText email = (EditText) findViewById(R.id.editEmail);
        final EditText password = (EditText) findViewById(R.id.editPassword);
        final Button singup = (Button) findViewById(R.id.button2);

        if (singup != null) {
            singup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(email!=null&&password!=null) {
                        singup.setEnabled(false);
                        if (!isValidEmail(email.getText().toString())) {
                            Toast.makeText(Registration.this, "Введен невалидный Email", Toast.LENGTH_SHORT).show();
                            singup.setEnabled(true);
                            return;
                        }
                        if (password.getText().toString().length()==0) {
                            Toast.makeText(Registration.this, "Пароль не может быть пустой", Toast.LENGTH_SHORT).show();
                            singup.setEnabled(true);
                            return;
                        }

                        startAnim();
                        RegistrationQuery service = ServiceGenerator.createService(RegistrationQuery.class);

                        service.createUser(email.getText().toString(), password.getText().toString()).enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(Call<String> call, Response<String> response) {
                                if(response.code()==200) {
                                    Toast.makeText(Registration.this, response.body(), Toast.LENGTH_SHORT).show();
                                    stopAnim();
                                    onBackPressed();
                                }
                                else {
                                    Toast.makeText(Registration.this, response.code() + " " + response.body(), Toast.LENGTH_SHORT).show();
                                    singup.setEnabled(true);
                                    stopAnim();
                                }
                            }

                            @Override
                            public void onFailure(Call<String> call, Throwable t) {
                                Toast.makeText(Registration.this, t.getMessage(), Toast.LENGTH_LONG).show();
                                singup.setEnabled(true);
                                stopAnim();
                            }
                        });

                    }
                }
            });
        }

    }

    private boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
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

    private void startAnim(){
        avLoadingIndicatorView.setVisibility(View.VISIBLE);
    }

    private void stopAnim(){
        avLoadingIndicatorView.setVisibility(View.GONE);
    }

}
