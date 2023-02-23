package de.schildbach.wallet.rates;

import androidx.annotation.Nullable;

import com.squareup.moshi.Moshi;

import org.dash.wallet.common.data.BigDecimalAdapter;
import org.dash.wallet.common.data.ExchangeRate;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.converter.moshi.MoshiConverterFactory;
import retrofit2.http.GET;

public class PozoqoRetailClient extends RetrofitClient implements ExchangeRatesClient {

    private static final String PZQ_CURRENCY_SYMBOL = "PZQ";

    private static PozoqoRetailClient instance;

    public static PozoqoRetailClient getInstance() {
        if (instance == null) {
            instance = new PozoqoRetailClient("https://rates2.dashretail.org/");
        }
        return instance;
    }

    private PozoqoRetailService service;

    private PozoqoRetailClient(String baseUrl) {
        super(baseUrl);

        Moshi moshi = moshiBuilder.add(new BigDecimalAdapter()).build();
        retrofit = retrofitBuilder.addConverterFactory(MoshiConverterFactory.create(moshi)).build();
        service = retrofit.create(PozoqoRetailService.class);
    }

    @Nullable
    @Override
    public List<ExchangeRate> getRates() throws Exception {
        Response<List<PozoqoRetailRate>> response = service.getRates().execute();
        List<PozoqoRetailRate> rates = response.body();

        if (rates == null || rates.isEmpty()) {
            throw new IllegalStateException("Failed to fetch prices from PozoqoRetail");
        }

        List<ExchangeRate> exchangeRates = new ArrayList<>();
        for (PozoqoRetailRate rate : rates) {
            if (PZQ_CURRENCY_SYMBOL.equals(rate.getBaseCurrency())) {
                exchangeRates.add(new ExchangeRate(rate.getQuoteCurrency(), rate.getPrice().toPlainString()));
            }
        }

        return exchangeRates;
    }

    private interface PozoqoRetailService {
        @GET("rates?source=dashretail")
        Call<List<PozoqoRetailRate>> getRates();
    }

}
