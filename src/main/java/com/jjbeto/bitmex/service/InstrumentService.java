package com.jjbeto.bitmex.service;

import com.jjbeto.bitmex.client.api.InstrumentApi;
import com.jjbeto.bitmex.client.model.Instrument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class InstrumentService {

    private static final Logger logger = LoggerFactory.getLogger(InstrumentService.class);

    private InstrumentApi instrumentApi;

    public InstrumentService(InstrumentApi instrumentApi) {
        this.instrumentApi = instrumentApi;
    }

    @PostConstruct
    public void init() {
        instrumentApi.instrumentGet("XBTUSD", null, null, null, null, null, null, null)
                .stream()
                .map(Instrument::toString)
                .forEach(logger::info);
    }

}
