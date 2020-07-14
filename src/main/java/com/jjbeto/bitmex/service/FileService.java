package com.jjbeto.bitmex.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.jjbeto.bitmex.client.model.TradeBin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.List;

@Service
public class FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    private final ObjectMapper objectMapper;
    private final CsvMapper csvMapper;
    private final CsvSchema schemaTradeBin;

    public FileService(ObjectMapper objectMapper, CsvMapper csvMapper) {
        this.objectMapper = objectMapper;
        this.csvMapper = csvMapper;
        schemaTradeBin = csvMapper.schemaFor(TradeBin.class)
                .withColumnSeparator(',')
                .withHeader();
    }

    public void saveToJson(String symbol, String interval, List<TradeBin> quotes) {
        final String path = getResourcePath(symbol, interval, "json");
        try (OutputStream out = new FileOutputStream(path)) {
            objectMapper.writeValue(out, quotes);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void saveToCsv(String symbol, String interval, List<TradeBin> quotes) {
        final ObjectWriter writer = csvMapper.writer(schemaTradeBin);
        String path = getResourcePath(symbol, interval, "csv");
        try (OutputStream out = new FileOutputStream(path)) {
            writer.writeValue(out, quotes);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private String getResourcePath(String symbol, String interval, String ext) {
        final String filename = symbol + "_" + interval + "." + ext;
        String path = Paths.get(this.getClass().getResource("/").getPath()).toString();
        return path + "/" + filename;
    }

}
