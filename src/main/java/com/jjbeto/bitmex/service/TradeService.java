package com.jjbeto.bitmex.service;

import com.jjbeto.bitmex.client.api.TradeApi;
import com.jjbeto.bitmex.client.model.TradeBin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.valueOf;
import static java.util.Comparator.comparing;

@Service
public class TradeService {

    private static final Logger logger = LoggerFactory.getLogger(TradeService.class);

    private final TradeApi tradeApi;
    private BigDecimal limit = valueOf(1000);

    public TradeService(TradeApi tradeApi) {
        this.tradeApi = tradeApi;
    }

    /**
     * Get complete history from start to end date. Recursively request data using the limit until data is complete.
     */
    public List<TradeBin> getHistory(String symbol, String interval, Instant timeFrom, Instant timeTo) {
        logger.info(String.format("[%s/%s] Start loading data for [%s/%s]", symbol, interval, timeFrom, timeTo));
        final List<TradeBin> history = new ArrayList<>();
        historyIterator(history, symbol, interval, timeFrom, timeTo, timeTo);
        logger.info(String.format("[%s/%s] Data from [%s] to [%s] is up-to-date.", symbol, interval, timeFrom, timeTo));
        return history;
    }

    private Instant historyIterator(List<TradeBin> history, String asset, String interval, Instant timeFrom, Instant timeTo, Instant targetTime) {
        final Instant lastTime = loadHistory(history, asset, interval, timeFrom, timeTo);
        if (lastTime == null) {
            return null;
        }

        final Instant nextTime = getNextTime(lastTime, targetTime);
        return historyIterator(history, asset, interval, lastTime, nextTime, targetTime);
    }

    private Instant loadHistory(List<TradeBin> history, String symbol, String interval, Instant startTime, Instant endTime) {
        try {
            final List<TradeBin> bucket = tradeApi.tradeGetBucketed(interval, false, symbol, null, null, limit, null, false, startTime, endTime);
            logger.info("Count: " + bucket.size());

            final Optional<TradeBin> oldest = bucket.stream().max(comparing(TradeBin::getTimestamp));
            if (oldest.isPresent() && bucket.size() > 1) {
                // the search is probably incomplete: only a part of the result is retrieved in each call!
                // in this case, return the oldest date to be used in a new search!
                history.addAll(bucket); // if bucket.size() == 1 its redundant result
                return oldest.get().getTimestamp().plusSeconds(1); // hint to start next call
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error(String.format("[%s/%s] - %s/%s)", symbol, interval, startTime, endTime), e);
        }
        return null;
    }

    private Instant getNextTime(Instant lastTime, Instant targetTime) {
        if (lastTime == null && targetTime != null) {
            return targetTime;
        } else if (lastTime == null) {
            return Instant.now(); // both times is null
        } else if (targetTime == null) {
            return lastTime;
        }

        final Instant possibleNextTime = lastTime.plus(7, ChronoUnit.DAYS);
        return possibleNextTime.isAfter(targetTime) ? targetTime : possibleNextTime;
    }

    public void setLimit(BigDecimal limit) {
        if (limit != null && limit.intValue() >= 1) {
            this.limit = limit;
        }
    }

}
