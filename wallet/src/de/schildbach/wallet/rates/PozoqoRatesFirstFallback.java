package de.schildbach.wallet.rates;

import androidx.annotation.Nullable;

import org.dash.wallet.common.data.ExchangeRate;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Samuel Barbosa
 */
public class PozoqoRatesFirstFallback implements ExchangeRatesClient {

    private static PozoqoRatesFirstFallback instance;
    private static final String VES_CURRENCY_CODE = "VES";

    public static PozoqoRatesFirstFallback getInstance() {
        if (instance == null) {
            instance = new PozoqoRatesFirstFallback();
        }
        return instance;
    }

    private PozoqoRatesFirstFallback() {

    }

    @Nullable
    @Override
    public List<ExchangeRate> getRates() throws Exception {
        BitcoinAverageClient btcAvgClient = BitcoinAverageClient.getInstance();
        CryptoCompareClient cryptoCompareClient = CryptoCompareClient.getInstance();

        List<ExchangeRate> rates = btcAvgClient.getGlobalIndices().body();
        Rate dashBtcRate = cryptoCompareClient.getPozoqoCustomAverage().body();

        BigDecimal dashVesPrice = PozoqoCasaClient.getInstance().getRates().body().getPozoqoVesPrice();

        if (rates == null || rates.isEmpty() || dashBtcRate == null || dashVesPrice == null) {
            throw new IllegalStateException("Failed to fetch prices from Fallback1");
        }

        boolean vesRateExists = false;
        for (ExchangeRate rate : rates) {
            if (VES_CURRENCY_CODE.equalsIgnoreCase(rate.getCurrencyCode())) {
                vesRateExists = true;
                if (dashVesPrice.compareTo(BigDecimal.ZERO) > 0) {
                    rate.setRate(dashVesPrice.toPlainString());
                }
            } else {
                BigDecimal currencyBtcRate = new BigDecimal(rate.getRate());
                rate.setRate(currencyBtcRate.multiply(dashBtcRate.getRate()).toPlainString());
            }
        }
        if (!vesRateExists) {
            rates.add(new ExchangeRate(VES_CURRENCY_CODE, dashVesPrice.toPlainString()));
        }

        return rates;
    }

}
