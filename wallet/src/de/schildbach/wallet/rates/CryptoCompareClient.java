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
public class CryptoCompareClient extends RetrofitClient {

    private static CryptoCompareClient instance;

    public static CryptoCompareClient getInstance() {
        if (instance == null) {
            instance = new CryptoCompareClient("https://min-api.cryptocompare.com/");
        }
        return instance;
    }

    private CryptoCompareService service;

    private CryptoCompareClient(String baseUrl) {
        super(baseUrl);

        Moshi moshi = moshiBuilder.add(new BigDecimalAdapter())
                .add(new CryptoComparePozoqoBtcRateAdapter()).build();
        retrofit = retrofitBuilder.addConverterFactory(MoshiConverterFactory.create(moshi)).build();
        service = retrofit.create(CryptoCompareService.class);
    }

    public Response<Rate> getPozoqoCustomAverage() throws IOException {
        return service.getPozoqoCustomAverage().execute();
    }

    public Response<CryptoCompareVesBtcRate> getVESBTCRate() throws IOException {
        return service.getVESBTCRate().execute();
    }

    private interface CryptoCompareService {
        @GET("data/generateAvg?fsym=PZQ&tsym=BTC&e=Binance,Kraken,Poloniex,Bitfinex")
        Call<Rate> getPozoqoCustomAverage();

        @GET("data/price?fsym=BTC&tsyms=VES")
        Call<CryptoCompareVesBtcRate> getVESBTCRate();
    }

}
