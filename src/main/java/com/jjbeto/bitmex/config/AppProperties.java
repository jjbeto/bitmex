package com.jjbeto.bitmex.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private boolean useTestnet = true;

    private Map<String, String> testnet;
    private Map<String, String> prod;

    public String getUrl() {
        return useTestnet ? testnet.get("url") : prod.get("url");
    }

    public String getKey() {
        return useTestnet ? testnet.get("key") : prod.get("key");
    }

    public String getSecret() {
        return useTestnet ? testnet.get("secret") : prod.get("secret");
    }

    public boolean isUseTestnet() {
        return useTestnet;
    }

    public void setUseTestnet(boolean useTestnet) {
        this.useTestnet = useTestnet;
    }

    public Map<String, String> getTestnet() {
        return testnet;
    }

    public void setTestnet(Map<String, String> testnet) {
        this.testnet = testnet;
    }

    public Map<String, String> getProd() {
        return prod;
    }

    public void setProd(Map<String, String> prod) {
        this.prod = prod;
    }

}
