package com.magomed.gamzatov.universalmarket.entity;

import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

/**
 * Created by MacBookAir on 11.03.16.
 */
public interface FileUploadService {
    @Multipart
    @POST("/TestTomcat-1.0-SNAPSHOT/setProduct")
    Call<String> uploadImage(@PartMap() Map<String, RequestBody> mapFileAndName,
                             @Part("description") RequestBody description,
                             @Part("brand") RequestBody brand,
                             @Part("typeId") RequestBody typeId,
                             @Part("shopId") RequestBody shopId,
                             @Part("price") RequestBody price);
}
