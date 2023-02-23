package de.schildbach.wallet.rates;

import com.squareup.moshi.Moshi;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.converter.moshi.MoshiConverterFactory;
import retrofit2.http.GET;

/**
 * @author Samuel Barbosa
 */
public class PozoqoCentralClient extends RetrofitClient {

    private static PozoqoCentralClient instance;

    public static PozoqoCentralClient getInstance() {
        if (instance == null) {
            instance = new PozoqoCentralClient("https://www.dashcentral.org/");
        }
        return instance;
    }

    private PozoqoCentralService service;

    private PozoqoCentralClient(String baseUrl) {
        super(baseUrl);

        Moshi moshi = moshiBuilder.add(new PozoqoCentralRateAdapter()).build();
        retrofit = retrofitBuilder.addConverterFactory(MoshiConverterFactory.create(moshi)).build();
        service = retrofit.create(PozoqoCentralService.class);
    }

    public Response<Rate> getPozoqoBtcPrice() throws IOException {
        return service.getPozoqoBtcPrice().execute();
    }

    private interface PozoqoCentralService {
        @GET("api/v1/public")
        Call<Rate> getPozoqoBtcPrice();
    }

}
