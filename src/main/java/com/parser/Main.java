package com.parser;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.IOException;

public class Main {

    private static final String PRODUCTS_EXCEL_FILE = "products.xlsx";

    public static void main(String[] args) throws IOException, InvalidFormatException {
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
