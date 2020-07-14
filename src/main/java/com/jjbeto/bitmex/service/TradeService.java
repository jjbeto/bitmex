package com.jjbeto.bitmex.service;

import com.jjbeto.bitmex.client.api.TradeApi;
import com.jjbeto.bitmex.client.model.TradeBin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.valueOf;
import static java.util.Comparator.comparing;

@Service
public class TradeService {

    private static final Logger logger = LoggerFactory.getLogger(TradeService.class);

    private final TradeApi tradeApi;
    private final FileService fileService;

    public TradeService(TradeApi tradeApi, FileService fileService) {
        this.tradeApi = tradeApi;
        this.fileService = fileService;
    }

    @PostConstruct
    public void init() {
        Instant startTime = LocalDate.of(2016, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant endTime = Instant.now();
        getQuotes("XBT", "1h", startTime, endTime);
    }

    public void write(String symbol, String interval, List<TradeBin> quotes) {
        quotes.stream()
                .map(TradeBin::toString)
                .forEach(logger::info);
        fileService.saveToJson(symbol, interval, quotes);
        fileService.saveToCsv(symbol, interval, quotes);
    }

    public void getQuotes(String symbol, String interval, Instant timeFrom, Instant timeTo) {
        logger.info(String.format("[%s/%s] Start loading data for [%s/%s]", symbol, interval, timeFrom, timeTo));
        loadQuotes(symbol, interval, timeFrom, timeTo, timeTo);
        logger.info(String.format("[%s/%s] Data from [%s] to [%s] is up-to-date.", symbol, interval, timeFrom, timeTo));
    }

    private Instant loadQuotes(String asset, String interval, Instant timeFrom, Instant timeTo, Instant timeFinal) {
        final Instant lastTime = loadQuotes(asset, interval, timeFrom, timeTo);
        if (lastTime == null) {
            return null;
        }

        final Instant nextTime = getNextTime(lastTime, timeFinal);
        return loadQuotes(asset, interval, lastTime, nextTime, timeFinal);
    }

    private Instant loadQuotes(String asset, String interval, Instant startTime, Instant endTime) {
        try {
            int limit = 1000;
            final List<TradeBin> quotes = tradeApi.tradeGetBucketed(interval, false, asset, null, null, valueOf(limit), null, false, startTime, endTime);
            logger.info("Count: " + quotes.size());
            write(asset, interval, quotes);

            final Optional<TradeBin> oldest = quotes.stream().max(comparing(TradeBin::getTimestamp));
            if (oldest.isPresent() && quotes.size() > 999) {
                // the search is probably incomplete: only the top 1000 results was returned!
                // in this case, return the oldest date to be used in a new search!
                return oldest.get().getTimestamp();
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error(String.format("[%s/%s] - %s/%s)", asset, interval, startTime, endTime), e);
        }
        return null;
    }

    private Instant getNextTime(Instant from, Instant to) {
        if (from == null && to != null) {
            return to;
        } else if (from == null) {
            return Instant.now();
        } else if (to == null) {
            return from;
        }

        final Instant possibleNextTime = from.plus(7, ChronoUnit.DAYS);
        return possibleNextTime.isAfter(to) ? to : possibleNextTime;
    }

}
