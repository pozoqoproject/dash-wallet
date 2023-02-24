package de.schildbach.wallet.rates;

import java.math.BigDecimal;

public class PozoqoRetailRate {

    private final String baseCurrency;
    private final String quoteCurrency;
    private final BigDecimal price;

    public PozoqoRetailRate(String baseCurrency, String quoteCurrency, BigDecimal price) {
        this.baseCurrency = baseCurrency;
        this.quoteCurrency = quoteCurrency;
        this.price = price;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public String getQuoteCurrency() {
        return quoteCurrency;
    }

    public BigDecimal getPrice() {
        return price;
    }

}
