package de.schildbach.wallet.rates;

import com.squareup.moshi.Json;

import java.math.BigDecimal;

/**
 * @author Samuel Barbosa
 */
public class PozoqoCasaResponse {

    @Json(name = "dashrate")
    private final BigDecimal dashVesPrice;

    public PozoqoCasaResponse(BigDecimal dashVesPrice) {
        this.dashVesPrice = dashVesPrice;
    }

    public BigDecimal getPozoqoVesPrice() {
        return dashVesPrice;
    }

}
