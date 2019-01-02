package com.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CsvParser {

    private static final String CSV_SEPARATOR = ",";

    public static Map<String, String> parseCsvFile(String fileName, int keyRowNUmber, int valueRowNumber) {
        String line = "";
        Map<String, String> result = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            while ((line = br.readLine()) != null) {
                String[] row = line.split(CSV_SEPARATOR);

                result.put(row[keyRowNUmber], row[valueRowNumber]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
