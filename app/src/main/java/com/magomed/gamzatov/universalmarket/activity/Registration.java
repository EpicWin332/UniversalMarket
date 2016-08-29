package com.magomed.gamzatov.universalmarket.activity;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
    private EditText email;
    private EditText password;
    private Button singup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        initToolbar("Регистрация");

        avLoadingIndicatorView = (AVLoadingIndicatorView) findViewById(R.id.avloadingIndicatorView);
        stopAnim();

        email = (EditText) findViewById(R.id.editEmail);
        password = (EditText) findViewById(R.id.editPassword);
        singup = (Button) findViewById(R.id.button2);

        buttonSignUpOnClick();
        setEditLayoutClickListener();
        setFont();
        hideKeyboard();
    }

    private void hideKeyboard() {
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
    }

    private void buttonSignUpOnClick() {
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
                                    Toast.makeText(Registration.this, response.code() + " " + response.message()+ " " + response.body(), Toast.LENGTH_LONG).show();
                                    Log.e("Register error: ", response.code() + " " + response.message()+ " " + response.body());
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

    private void setEditLayoutClickListener() {
        LinearLayout layoutEmail = (LinearLayout) findViewById(R.id.layoutEmail);
        LinearLayout layoutPassword = (LinearLayout) findViewById(R.id.layoutPassword);

        if (layoutEmail != null) {
            layoutEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    email.requestFocusFromTouch();
                    InputMethodManager lManager = (InputMethodManager) Registration.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    lManager.showSoftInput(email, 0);
                }
            });
        }

        if (layoutPassword != null) {
            layoutPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    password.requestFocusFromTouch();
                    InputMethodManager lManager = (InputMethodManager) Registration.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    lManager.showSoftInput(password, 0);
                }
            });
        }
    }

    private void setFont() {
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/LatoLight.ttf");
        email.setTypeface(custom_font);
        password.setTypeface(custom_font);

        Typeface custom_font1 = Typeface.createFromAsset(getAssets(), "fonts/LatoRegular.ttf");
        singup.setTypeface(custom_font1);
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
