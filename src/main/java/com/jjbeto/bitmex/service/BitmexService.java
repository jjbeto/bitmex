package com.jjbeto.bitmex.service;

import com.jjbeto.bitmex.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class BitmexService {

    private static final Logger logger = LoggerFactory.getLogger(BitmexService.class);
    private static final String MAC_ALGO = "HmacSHA256";

    private final AppProperties appProperties;

    public BitmexService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public String createSignature(final String text) {
        try {
            final Mac mac = Mac.getInstance(MAC_ALGO);
            final SecretKeySpec secretKey = new SecretKeySpec(appProperties.getSecret().getBytes(UTF_8), MAC_ALGO);
            mac.init(secretKey);
            final String hash = DatatypeConverter.printHexBinary(mac.doFinal(text.getBytes()));
            return hash.toLowerCase();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

}
