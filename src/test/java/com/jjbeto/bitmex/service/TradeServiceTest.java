package com.jjbeto.bitmex.service;

import com.jjbeto.bitmex.client.model.TradeBin;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class TradeServiceTest {

    @Autowired
    TradeService tradeService;

    @Test
    public void getTradeBin() {
        Instant start = LocalDateTime.of(2020, 1, 1, 1, 1).toInstant(UTC);
        Instant end = LocalDateTime.of(2020, 1, 1, 1, 15).toInstant(UTC);

        // set max results per call for test, our result must have 15 quotes (15m -> 1 per minute)
        tradeService.setLimit(10);
        List<TradeBin> history = tradeService.getHistory("XBT", "1m", start, end);
        assertThat(history.size()).isEqualTo(15);
    }

    @Test
    public void getTradeBinWithLotsOfResults() {
        Instant start = LocalDateTime.of(2020, 1, 1, 1, 1).toInstant(UTC);
        Instant end = LocalDateTime.of(2020, 1, 2, 1, 1).toInstant(UTC);

        // set max results per call for test, our result must have 15 quotes (15m -> 1 per minute)
        tradeService.setLimit(100);
        List<TradeBin> history = tradeService.getHistory("XBT", "1m", start, end);
        assertThat(history.size()).isEqualTo(1441);
    }

}
