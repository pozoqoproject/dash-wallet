package de.schildbach.wallet.rates;

import com.squareup.moshi.Moshi;

import org.dash.wallet.common.data.BigDecimalAdapter;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.converter.moshi.MoshiConverterFactory;
import retrofit2.http.GET;

/**
 * @author Samuel Barbosa
 */
public class PozoqoCasaClient extends RetrofitClient {

    private static PozoqoCasaClient instance;
    private PozoqoCasaService service;

    public static PozoqoCasaClient getInstance() {
        if (instance == null) {
            instance = new PozoqoCasaClient("https://dash.casa/");
        }
        return instance;
    }

    private PozoqoCasaClient(String baseUrl) {
        super(baseUrl);

        Moshi moshi = moshiBuilder.add(new BigDecimalAdapter()).build();
        retrofit = retrofitBuilder.addConverterFactory(MoshiConverterFactory.create(moshi)).build();
        service = retrofit.create(PozoqoCasaService.class);
    }

    public Response<PozoqoCasaResponse> getRates() throws IOException {
        return service.getRates().execute();
    }

    private interface PozoqoCasaService {
        @GET("api/?cur=VES")
        Call<PozoqoCasaResponse> getRates();
    }

}
