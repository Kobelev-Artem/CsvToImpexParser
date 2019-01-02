package com.parser;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import static com.parser.CsvParser.parseCsvFile;

public class Main {

    private static final String PRODUCTS_EXCEL_FILE = "products.xlsx";

    public static void main(String[] args) {
        Map<String, String> hapiHotelUpdated = parseCsvFile("/Users/akobeliev/Desktop/testProjects/CsvToImpexParser/hapiHotelMappings.csv", 0, 2);
        Map<String, String> hapiPropertiesExisting = parseCsvFile("/Users/akobeliev/Desktop/testProjects/CsvToImpexParser/ST.csv", 1, 32);

        compareTimeZonesAndPrintDifferences(hapiHotelUpdated, hapiPropertiesExisting);
    }

    private static void compareTimeZonesAndPrintDifferences(Map<String, String> hapiHotelUpdated, Map<String, String> hapiPropertiesExisting) {
        hapiHotelUpdated.forEach((id, timezone) -> {
            String existingTZ = hapiPropertiesExisting.get(id);
            if (StringUtils.isEmpty(existingTZ)) {
                System.out.println("*** Property with id " + id + "not found!");
            } else {
                int currentOffset = DateTimeZone.forID(timezone).toTimeZone().getRawOffset();
                Optional<String> existingZoneId = ZoneId.getAvailableZoneIds().stream().filter(zone -> zone.contains(existingTZ)).findFirst();
                if (!existingZoneId.isPresent()) {
                    System.out.println("=== Time Zone " + existingTZ + " not found!");
                } else {
                    int newOffset = TimeZone.getTimeZone(existingZoneId.get()).getRawOffset();
                    if (!(currentOffset == newOffset)) {
                        System.out.println("TZ different for " + id);
                        System.out.println("- new one: " + timezone + ", existing: " + existingZoneId.get());
                    }
                }
            }
        });
    }

    public static void parseCsvFilesAndGenerateImpexFiles(String[] args) throws IOException, InvalidFormatException {
        Workbook workbook = WorkbookFactory.create(new File(PRODUCTS_EXCEL_FILE));
        Sheet referenceProductSheet =  workbook.getSheetAt(0);
        Sheet sapProductSheet = workbook.getSheetAt(1);

        Parser parser = new Parser();
        parser.readReferenceProducts(referenceProductSheet);
        parser.readSapProducts(sapProductSheet);
        parser.prepareData();

        parser.writeVendors();
        parser.writeCategories();
        parser.writeReferenceProducts();
        parser.writeReferenceProductsMedia();
        parser.writeClassificationsForReferenceProducts();
        parser.writeCategoryProductRelation();

        parser.writeSapAllProducts();

        parser.writeProductSupportedCountries();

        parser.writeProductsToClassificationClass();

        System.out.println("Program finished");
    }
}
