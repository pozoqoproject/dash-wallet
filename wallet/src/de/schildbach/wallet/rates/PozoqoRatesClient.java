package de.schildbach.wallet.rates;

import androidx.annotation.Nullable;

import com.squareup.moshi.Moshi;

import org.dash.wallet.common.data.ExchangeRate;

import java.util.List;

import retrofit2.Call;
import retrofit2.converter.moshi.MoshiConverterFactory;
import retrofit2.http.GET;

/**
 * @author Samuel Barbosa
 */
public class PozoqoRatesClient extends RetrofitClient implements ExchangeRatesClient {

    private static PozoqoRatesClient instance;

    public static PozoqoRatesClient getInstance() {
        if (instance == null) {
            instance = new PozoqoRatesClient();
        }
        return instance;
    }

    private PozoqoRatesService dashRatesService;

    private PozoqoRatesClient() {
        super("https://api.get-spark.com/");
        Moshi moshi = moshiBuilder.add(new ExchangeRateListMoshiAdapter()).build();
        retrofit = retrofitBuilder.addConverterFactory(MoshiConverterFactory.create(moshi)).build();
        dashRatesService = retrofit.create(PozoqoRatesService.class);
    }

    @Override
    @Nullable
    public List<ExchangeRate> getRates() throws Exception {
        List<ExchangeRate> rates = dashRatesService.getRates().execute().body();
        if (rates == null || rates.isEmpty()) {
            throw new IllegalStateException("Failed to fetch prices from PozoqoRates source");
        }
        return rates;
    }

    private interface PozoqoRatesService {
        @GET("list")
        Call<List<ExchangeRate>> getRates();
    }

}
