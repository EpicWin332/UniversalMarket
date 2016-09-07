package com.magomed.gamzatov.universalmarket.network;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by MacBookAir on 07.05.16.
 */
public interface LoginQuery {

    @FormUrlEncoded
    @POST(ServiceGenerator.API_PREFIX_URL + "/authentication")
    Call<Void> login(@Field("email") String email, @Field("password") String password);
}
