package com.magomed.gamzatov.universalmarket.network;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by MacBookAir on 09.04.17.
 */

public interface ShopsQuery {
    @GET(ServiceGenerator.API_PREFIX_URL+ "/shops")
    Call<List<Map<String, String>>> getShops();
}
