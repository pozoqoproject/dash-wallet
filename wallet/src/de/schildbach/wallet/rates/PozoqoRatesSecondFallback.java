package de.schildbach.wallet.rates;

import androidx.annotation.Nullable;

import org.dash.wallet.common.data.ExchangeRate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Samuel Barbosa
 */
public class PozoqoRatesSecondFallback implements ExchangeRatesClient {

    private static PozoqoRatesSecondFallback instance;
    private static final String VES_CURRENCY_CODE = "VES";
    private List<String> excludedRates = Arrays.asList("BTC", "BCH", "XAG", "XAU", "VEF");

    public static PozoqoRatesSecondFallback getInstance() {
        if (instance == null) {
            instance = new PozoqoRatesSecondFallback();
        }
        return instance;
    }

    private PozoqoRatesSecondFallback() {

    }

    @Nullable
    @Override
    public List<ExchangeRate> getRates() throws Exception {
        List<BitPayRate> rates = BitPayClient.getInstance().getRates().body().getRates();
        BigDecimal dashCentralPrice = PozoqoCentralClient.getInstance().getPozoqoBtcPrice().body().getRate();
        BigDecimal poloniexPrice = PoloniexClient.getInstance().getRates().body().getRate();
        BigDecimal dashVesPrice = LocalBitcoinsClient.getInstance().getRates().body().getPozoqoVesPrice();

        if (rates == null || rates.isEmpty() || (dashCentralPrice == null && poloniexPrice == null)) {
            throw new IllegalStateException("Failed to fetch prices from Fallback2");
        }

        BigDecimal dashBtcRate = null;
        if (poloniexPrice.compareTo(BigDecimal.ZERO) > 0) {
            if (dashCentralPrice.compareTo(BigDecimal.ZERO) > 0) {
                dashBtcRate = dashCentralPrice.add(poloniexPrice).divide(BigDecimal.valueOf(2));
            } else {
                dashBtcRate = poloniexPrice;
            }
        } else if (dashCentralPrice.compareTo(BigDecimal.ZERO) > 0) {
            dashBtcRate = dashCentralPrice;
        }

        List<ExchangeRate> exchangeRates = new ArrayList<>();
        for(BitPayRate rate : rates) {
            if (!excludedRates.contains(rate.getCode())) {
                if (VES_CURRENCY_CODE.equalsIgnoreCase(rate.getCode()) && dashVesPrice != null
                        && dashVesPrice.compareTo(BigDecimal.ZERO) > 0) {
                    dashVesPrice = dashBtcRate.multiply(dashVesPrice);
                    exchangeRates.add(new ExchangeRate(rate.getCode(), dashVesPrice.toPlainString()));
                    continue;
                }
                exchangeRates.add(new ExchangeRate(rate.getCode(),
                        dashBtcRate.multiply(rate.getRate()).toPlainString()));
            }
        }

        return exchangeRates;
    }

}
