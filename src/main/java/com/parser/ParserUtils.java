package com.parser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ParserUtils {

    static String trimString(String something) {
        return something.replaceAll("\\s+","");
    }

    private static String lcFirst(String input) {
        return input.substring(0,  1).toLowerCase() + input.substring(1);
    }

    private static String ucFirst(String input) {
        return input.substring(0,  1).toUpperCase() + input.substring(1);
    }

    /**
     * To make words like 'and' upper in trimmed code
     * @param input
     * @return
     */
    public static String firstLetterToUpper(String input) {
        final Pattern pattern = Pattern.compile("\\b(?=\\w)");

        return pattern.splitAsStream(input)
                .map(ParserUtils::ucFirst)
                .collect(Collectors.joining());
    }

    /**
     * To start code from lowerCase
     * @param input
     * @return
     */
    public static String firstLetterLowerCase(String input) {
        final Pattern pattern = Pattern.compile("\\b(?=\\w)");

        return pattern.splitAsStream(input)
                .map(ParserUtils::lcFirst)
                .collect(Collectors.joining());
    }

    public static void writeToFile(String text, String fileName){
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter("output/" + fileName + ".impex"));
            writer.write(text);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
