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

        parser.createAndWriteCategories();
        parser.createAndWriteProductSupportedCountries();
        //Methods with single product passed as a parameter, can be edited to append data to
        //main impex. For ex (SAPAnalyticsCloud-8004107.impex)
        parser.createAndWriteProductReferences(parser.sapProducts.get(0));
        parser.createClassificationForProduct(parser.sapProducts.get(0));
        parser.createCategoryProductRelation(parser.sapProducts.get(0));
        parser.createMediasForProduct(parser.sapProducts.get(0));

        parser.createAndWriteVendors();
        parser.createSapProductImpexes(parser.sapProducts.get(0));

        System.out.println("Hello Artem");
    }
}
