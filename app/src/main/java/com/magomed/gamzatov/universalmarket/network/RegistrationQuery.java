package com.magomed.gamzatov.universalmarket.network;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by MacBookAir on 07.05.16.
 */
public interface RegistrationQuery {

    //@FormUrlEncoded
    @POST("/TestTomcat-1.0-SNAPSHOT/registration")
    Call<String> createUser(@Query("email") String email, @Query("password") String password);
}
