package de.schildbach.wallet.rates;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.ToJson;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * @author Samuel Barbosa
 */
public class CryptoComparePozoqoBtcRateAdapter {


    @ToJson
    String toJson(Rate value) {
        return null;
    }

    @FromJson
    Rate fromJson(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            if (jsonReader.nextName().equalsIgnoreCase("RAW")) {
                return getPrice(jsonReader);
            }
        }
        jsonReader.endObject();
        return null;
    }

    private Rate getPrice(JsonReader jsonReader) throws IOException {
        Rate cryptoComparePozoqoBtcRate = null;
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            if (jsonReader.nextName().equalsIgnoreCase("PRICE")) {
                cryptoComparePozoqoBtcRate = new Rate(
                        new BigDecimal(jsonReader.nextString()));

            } else {
                jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
        return cryptoComparePozoqoBtcRate;
    }

}
