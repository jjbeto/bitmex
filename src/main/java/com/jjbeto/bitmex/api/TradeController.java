package com.jjbeto.bitmex.api;

import com.jjbeto.bitmex.client.model.TradeBin;
import com.jjbeto.bitmex.service.TradeService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static java.util.Comparator.comparing;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

@RestController
@RequestMapping("/trades")
public class TradeController {

    private final LocalDateTime defaultStartTime = LocalDateTime.of(2020, 1, 1, 0, 0, 0);
    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @GetMapping
    public List<TradeBin> getTradeBins(
            @RequestParam(name = "symbol", defaultValue = "XBT") String symbol,
            @RequestParam(name = "interval", defaultValue = "1h") String interval,
            // yyyy-MM-dd'T'HH:mm:ss.SSSXXX, e.g. "2000-10-31T01:30:00.000-05:00"
            @DateTimeFormat(iso = DATE_TIME) @RequestParam("from") Optional<LocalDateTime> from,
            // yyyy-MM-dd'T'HH:mm:ss.SSSXXX, e.g. "2000-10-31T01:30:00.000-05:00".
            @DateTimeFormat(iso = DATE_TIME) @RequestParam("to") Optional<LocalDateTime> to) {
        final Instant timeFrom = from.orElse(defaultStartTime)
                .toInstant(UTC);
        final Instant timeTo = to.isPresent()
                ? to.get().toInstant(UTC)
                : Instant.now();
        List<TradeBin> history = tradeService.getHistory(symbol, interval, timeFrom, timeTo);
        history.sort(comparing(TradeBin::getTimestamp));
        return history;
    }

}
