package com.magomed.gamzatov.universalmarket.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.magomed.gamzatov.universalmarket.network.LoginQuery;

import com.magomed.gamzatov.universalmarket.R;
import com.magomed.gamzatov.universalmarket.network.ServiceGenerator;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {

    private AVLoadingIndicatorView avLoadingIndicatorView;
    private EditText email;
    private EditText password;
    private Button login;
    private Button singup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initToolbar("Вход");

        avLoadingIndicatorView = (AVLoadingIndicatorView) findViewById(R.id.avloadingIndicatorView);
        stopAnim();

        email = (EditText) findViewById(R.id.editEmail);
        password = (EditText) findViewById(R.id.editPassword);
        login = (Button) findViewById(R.id.button2);
        singup = (Button) findViewById(R.id.button3);

        buttonsOnClick();
        setEditLayoutClickListener();
        setFont();
        hideKeyboard();
    }

    private void hideKeyboard() {
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
    }

    private void buttonsOnClick() {
        if (login != null&&singup != null) {
            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(email!=null&&password!=null) {

                        // Check if no view has focus:
                        View view = Login.this.getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }

                        login.setEnabled(false);
                        singup.setEnabled(false);
                        if (!isValidEmail(email.getText().toString())) {
                            Toast.makeText(Login.this, "Введен невалидный Email", Toast.LENGTH_SHORT).show();
                            login.setEnabled(true);
                            singup.setEnabled(true);
                            return;
                        }
                        if (password.getText().toString().length() == 0) {
                            Toast.makeText(Login.this, "Пароль не может быть пустой", Toast.LENGTH_SHORT).show();
                            login.setEnabled(true);
                            singup.setEnabled(true);
                            return;
                        }

                        startAnim();
                        LoginQuery service = ServiceGenerator.createService(LoginQuery.class);

                        service.login(email.getText().toString(), password.getText().toString()).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if(response.code()==200) {
                                    Toast.makeText(Login.this, "onResponse 200" + response.message() +
                                            " body: " + response.body(), Toast.LENGTH_LONG).show();
                                    Log.d("onResponse 200", "onResponse 200" + response.message() +
                                            " body: " + response.body());
                                    List<String> headers = response.headers().values("Set-Cookie");

                                    stopAnim();

                                    for(String cook: headers){
//                                        String cook = cookie.split(";")[0];
                                        if(cook.startsWith("hash")) {
                                            Log.d("cookie", cook);
                                            SharedPreferences sPref = getSharedPreferences("cookies", MODE_PRIVATE);
                                            SharedPreferences.Editor ed = sPref.edit();
                                            ed.putString("cookie", cook);
                                            ed.putString("email", email.getText().toString());
                                            ed.apply();

                                            onBackPressed();
                                            return;
                                        }
                                    }

                                    login.setEnabled(true);
                                    singup.setEnabled(true);
                                }
                                else {
                                    Toast.makeText(Login.this, "onResponse " + response.code() + " " + response.message()
                                            + " body: " + response.body(), Toast.LENGTH_LONG).show();
                                    Log.d("onResponse " + response.code(), "onResponse 200" + response.message() +
                                            " body: " + response.body());
                                    login.setEnabled(true);
                                    singup.setEnabled(true);
                                    stopAnim();
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(Login.this, "onFailure " + t.getMessage(), Toast.LENGTH_LONG).show();
                                if (t.getMessage() != null) {
                                    Log.d("onFailure", t.getMessage());
                                    Log.d("onFailure", Log.getStackTraceString(t));
                                }
                                login.setEnabled(true);
                                singup.setEnabled(true);
                                stopAnim();
                            }
                        });

                    }
                }
            });

            singup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Login.this, Registration.class);
                    startActivity(intent);
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
                    InputMethodManager lManager = (InputMethodManager) Login.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    lManager.showSoftInput(email, 0);
                }
            });
        }

        if (layoutPassword != null) {
            layoutPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    password.requestFocusFromTouch();
                    InputMethodManager lManager = (InputMethodManager) Login.this.getSystemService(Context.INPUT_METHOD_SERVICE);
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
        login.setTypeface(custom_font1);
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
