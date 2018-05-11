package com.parser;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;

import java.util.*;

public class Parser {
    private static final String CATALOG_VERSION = "globalProductCatalog:Staged";
    private static final String SEMICOLON = ";";
    private static final String SALES_ORG = "0000000332";
    private static final String REFERENCE_TYPE = "CROSSELLING";

    public List<SapProduct> sapProducts = new ArrayList<>();
    public Map<Integer, ReferenceProduct> referenceProducts = new HashMap<>();
    public Map<String, String> vendorNameToCode = new HashMap<>();
    public Map<String, String> categoryNameToCode = new HashMap<>();

    public void prepareData(){
        createVendors();
        createCategories();
    }

    public void createVendors(){
        for (ReferenceProduct product: referenceProducts.values()){
            vendorNameToCode.put(product.getDeveloper(), ParserUtils.trimString(product.getDeveloper()));
        }
    }

    public void writeVendors(){
        String header = "INSERT_UPDATE Vendor;code[unique=true,forceWrite=true,allownull=true];name[lang=en];description[lang=en]\n";
        String vendorTemplate = ";{code};{name};{name}\n";

        String vendorBody = "";
        for (String vendor : vendorNameToCode.keySet()){
            vendorBody += vendorTemplate.replaceFirst("\\{code\\}", vendorNameToCode.get(vendor))
                                        .replaceAll("\\{name\\}", vendor);
        }

        String venderImpex = header + vendorBody;
        String impexName = "initialVendors";
        ParserUtils.writeToFile(venderImpex, impexName);
    }

    public void createCategories(){
        initCategoryNameToCodeBinding();
        customizeCategoriesCodes();
    }

    public void writeCategories() {
        String headerValues = "$productCatalog=globalProductCatalog\n$catalogVersion=catalogversion(catalog(id[default=$productCatalog]),version[default='Staged'])[unique=true,default=$productCatalog:Staged]\n$lang=en\n$allowedPrincipals=allowedPrincipals(uid)[default='customergroup']\n\n";

        String header = "INSERT_UPDATE Category;code[unique=true];name[lang=$lang];description[lang=$lang];$catalogVersion;$allowedPrincipals;\n";
        String categoryTemplate = ";{code};{name};{description}\n";

        String categoryBody = "";
        for (String category : categoryNameToCode.keySet()) {
            if (StringUtils.isNotEmpty(category)) {
                categoryBody += categoryTemplate.replaceFirst("\\{code\\}", categoryNameToCode.get(category))
                        .replaceFirst("\\{name\\}", category)
                        .replaceFirst("\\{description\\}", category);
            }
        }

        String categoryImpex = headerValues + header + categoryBody;
        String impexName = "categories_en";
        ParserUtils.writeToFile(categoryImpex, impexName);
    }

    public void writeProductSupportedCountries() {
        String header = "INSERT_UPDATE SapProductSupportedCountry;catalogVersion(catalog(id),version)[allownull=true];location(Country.isocode|SapArea.isocode)[unique=true];paymentConfigurations(code);product(catalogVersion(catalog(id),version),code)[unique=true];salesOrganization(uid)\n";
        String supCountriesBody = "";

        for (SapProduct product : sapProducts) {
            String row = SEMICOLON + CATALOG_VERSION + SEMICOLON + "US" + SEMICOLON
                    + "" + SEMICOLON + CATALOG_VERSION + ":" + "SAP-" + product.getId() + SEMICOLON + SALES_ORG + "\n";

            supCountriesBody += row;
        }

        for (ReferenceProduct referenceProduct : referenceProducts.values()) {
            String row = SEMICOLON + CATALOG_VERSION + SEMICOLON + "US" + SEMICOLON
                    + "" + SEMICOLON + CATALOG_VERSION + ":" + "APPS-" + referenceProduct.getId() + SEMICOLON + SALES_ORG + "\n";

            supCountriesBody += row;
        }

        String supportedCountriesImpex = header + supCountriesBody;
        String impexName = "supported_countries";
        ParserUtils.writeToFile(supportedCountriesImpex, impexName);
    }

    public void writeClassificationsForReferenceProducts() {
        String variables = "$productCatalog=globalProductCatalog\n" +
                "$catalogVersion=catalogversion(catalog(id[default=$productCatalog]),version[default='Staged'])[unique=true,default=$productCatalog:Staged]\n\n";

        String headerClModifiers = "$clAttrModifiers=system='GlobalClassification',version='1.0',translator=de.hybris.platform.catalog.jalo.classification.impex.ClassificationAttributeTranslator,lang=en\n";
        String headerClModifier1 = "$feature1=@purchasableOnline[$clAttrModifiers];\n";
        String headerClModifier2 = "$feature2=@freeTrialOptionAvailable[$clAttrModifiers];\n";
        String headerClModifier3 = "$feature3=@ecosystemProductType[$clAttrModifiers];\n\n";

        String header = "INSERT_UPDATE Product;code[unique=true];$feature1;$feature2;$feature3;$catalogVersion\n";

        String classificationsBody = "";

        for (Integer refProductKey : referenceProducts.keySet()) {
            ReferenceProduct refProduct = referenceProducts.get(refProductKey);
            String row = SEMICOLON + "APPS-" + refProduct.getId()
                    + SEMICOLON + refProduct.getClassificationAttributes().getBuyable()
                    + SEMICOLON + refProduct.getClassificationAttributes().getFreeTrial()
                    + SEMICOLON + refProduct.getClassificationAttributes().getType() + SEMICOLON + "\n";
            classificationsBody += row;
        }

        String classificationsImpex = variables + headerClModifiers + headerClModifier1 + headerClModifier2 + headerClModifier3 + header + classificationsBody;
        String impexName = "APIsProductClassification";
        ParserUtils.writeToFile(classificationsImpex, impexName);
    }

    public void writeSapAllProducts(){
        for (SapProduct sapProduct : sapProducts){
            writeSapProduct(sapProduct);
        }
    }

    public void writeSapProduct(SapProduct sapProduct){
        String variables = "$productCatalog=globalProductCatalog\n" +
                "$catalogVersion=catalogversion(catalog(id[default=$productCatalog]),version[default='Staged'])[unique=true,default=$productCatalog:Staged]\n" +
                "$vendors=vendors(code)[default=sap]\n";
        String productHeader = "INSERT_UPDATE Product; code[unique=true]; $catalogVersion; name[lang=en]; description[lang=en]; unit(code); approvalStatus(code); startLineNumber; primaryAction(code, itemtype(code))[allownull = true]; primaryActionLink; $vendors; mainCategory(catalogVersion(catalog(id),version),code);\n";
        /*String sapProductTemplate = "; SAP-{id}; ; {name}; {name}; pieces; approved; 0; Buy:PrimaryActionEnum; {link}; sap; globalProductCatalog:Staged:analytics\n";

        String sapProductRow = sapProductTemplate.replaceFirst("\\{id\\}", sapProduct.getId())
                                                .replaceAll("\\{name\\}", sapProduct.getName())
                                                .replaceFirst("\\{link\\}", sapProduct.getLink());*/

        String productReferences = addProductReferences(sapProduct);

        String sapProductImpex = variables + productHeader + "\n" + productReferences;
        String impexName = sapProduct.getName() + "-" + sapProduct.getId();
        impexName = "sapProducts/" + ParserUtils.trimString(impexName);
        ParserUtils.writeToFile(sapProductImpex, impexName);
    }

    private String addProductReferences(SapProduct product) {
        String header = "INSERT_UPDATE ProductReference;source(code,$catalogVersion)[unique=true];target(code,$catalogVersion)[unique=true];referenceType(code)[default=CROSSELLING,unique=true];active;preselected;\n";
        String productRefsBody = "";

        String sapProductToSapProductRef = SEMICOLON + "SAP-" + product.getId()
                + SEMICOLON + "SAP-" + product.getId()
                + SEMICOLON + REFERENCE_TYPE + SEMICOLON + "true" + SEMICOLON + "false" + SEMICOLON + "\n";
        productRefsBody += sapProductToSapProductRef;

        for (Integer refProductKey : product.getReferenceProducts()) {
            ReferenceProduct refProduct = referenceProducts.get(refProductKey);
            String row = SEMICOLON + "SAP-" + product.getId()
                    + SEMICOLON + "APPS-" + refProduct.getId()
                    + SEMICOLON + REFERENCE_TYPE + SEMICOLON + "true" + SEMICOLON + "false" + SEMICOLON + "\n";
            productRefsBody += row;
        }

        return header + productRefsBody;
    }

    public void writeReferenceProducts(){
        StringBuilder builder = new StringBuilder();
        builder.append("$productCatalog=globalProductCatalog\n" +
                "$catalogVersion=catalogversion(catalog(id[default=$productCatalog]),version[default='Staged'])[unique=true,default=$productCatalog:Staged]\n" +
                "$vendors=vendors(code)[default=sap]\n\n");
        builder.append("INSERT_UPDATE Product; code[unique=true]; $catalogVersion; name[lang=en]; description[lang=en]; unit(code); approvalStatus(code); startLineNumber; primaryAction(code, itemtype(code))[allownull = true]; primaryActionLink; $vendors; mainCategory(catalogVersion(catalog(id),version),code);\n");

        String refProductTemplate = "; APPS-{id}; ; {productName}; {caption}; pieces; approved; 0; Buy:PrimaryActionEnum; {url}; {vendor}; globalProductCatalog:Staged:{category}\n";

        for (Integer refProductKey : referenceProducts.keySet()){
            ReferenceProduct refProduct = referenceProducts.get(refProductKey);
            String refProductRow = refProductTemplate.replaceFirst("\\{id\\}", refProduct.getId())
                    .replaceFirst("\\{productName\\}", refProduct.getProductName())
                    .replaceFirst("\\{caption\\}", refProduct.getCaption())
                    .replaceFirst("\\{url\\}", refProduct.getUrl())
                    .replaceFirst("\\{vendor\\}", vendorNameToCode.get(refProduct.getDeveloper()));
            if (StringUtils.isEmpty(refProduct.getCategory())){
                refProductRow = refProductRow.replaceFirst("; globalProductCatalog:Staged:\\{category\\}", "");
            } else {
                refProductRow = refProductRow.replaceFirst("\\{category\\}", categoryNameToCode.get(refProduct.getCategory()));
            }

            builder.append(refProductRow);
        }

        String impexName = "APIsProducts";
        ParserUtils.writeToFile(builder.toString(), impexName);
    }

    public void writeCategoryProductRelation() {
        String variables = "$productCatalog=globalProductCatalog\n" +
                "$catalogVersion=catalogversion(catalog(id[default=$productCatalog]),version[default='Staged'])[unique=true,default=$productCatalog:Staged]\n\n";

        String header1 = "$categories=source(code, $catalogVersion)[unique=true]\n";
        String header2 = "$products=target(code, $catalogVersion)[unique=true]\n\n";

        String header = "INSERT_UPDATE CategoryProductRelation;$categories;$products\n";
        String categoryRelationBody = "";

        //Note: don't know where to get category for SAPProduct(is it needed?)
        for (Integer refProductKey : referenceProducts.keySet()) {
            ReferenceProduct refProduct = referenceProducts.get(refProductKey);
            if (StringUtils.isNotEmpty(refProduct.getCategory())) {
                String row = SEMICOLON + categoryNameToCode.get(refProduct.getCategory()) + SEMICOLON + "APPS-" + refProduct.getId() + SEMICOLON + "\n";
                categoryRelationBody += row;
            }
        }

        String categoryRelationImpex = variables + header1 + header2 + header + categoryRelationBody;
        String impexName = "CategoryProductRelation";
        ParserUtils.writeToFile(categoryRelationImpex, impexName);
    }

    public void writeProductsToClassificationClass() {
        StringBuilder impex = new StringBuilder("$productCatalog=globalProductCatalog\n");
        impex.append("$catalogVersion=catalogversion(catalog(id[default=$productCatalog]),version[default='Staged'])[unique=true,default=$productCatalog:Staged]\n");
        impex.append(";globalclassification;\n");
        for (ReferenceProduct product : referenceProducts.values()){
            impex.append("," + product.getId());
        }

        String impexName = "addProductsToClassificationClass";
        ParserUtils.writeToFile(impex.toString(), impexName);
    }

    public void writeReferenceProductsMedia() {
        StringBuilder builder = new StringBuilder();
        builder.append("$productCatalog=globalProductCatalog\n" +
                "$catalogVersion=catalogversion(catalog(id[default=$productCatalog]),version[default='Staged'])[unique=true,default=$productCatalog:Staged]\n");
        builder.append(System.lineSeparator());

        builder.append("$siteResource=jar:com.sap.marketplace.initialdata.setup.InitialDataSystemSetup&/marketplaceinitialdata/import/project/$productCatalog\n");
        builder.append("$medias=medias(code, $catalogVersion)\n");
        builder.append("$thumbnail=thumbnail(code, $catalogVersion)\n");
        builder.append("$picture=picture(code, $catalogVersion)\n");
        builder.append("$thumbnails=thumbnails(code, $catalogVersion)\n");
        builder.append("$detail=detail(code, $catalogVersion)\n");
        builder.append("$normal=normal(code, $catalogVersion)\n");
        builder.append("$others=others(code, $catalogVersion)\n");
        builder.append("$galleryImages=galleryImages(qualifier, $catalogVersion)\n");
        builder.append(System.lineSeparator());

        builder.append("INSERT_UPDATE MediaFormat;qualifier[unique=true];name\n");
        builder.append(";450Wx450H\n");
        builder.append(System.lineSeparator());

        builder.append("INSERT_UPDATE Media; mediaFormat(qualifier); code[unique=true]; @media[translator=de.hybris.platform.impex.jalo.media.MediaDataTranslator]; mime[default='image/jpeg']; $catalogVersion; folder(qualifier)\n");

        //Note: don't know where to get picture for SAP product
        for (Integer refProductKey : referenceProducts.keySet()) {
            ReferenceProduct refProduct = referenceProducts.get(refProductKey);
            builder.append(SEMICOLON);
            builder.append("450Wx450H");
            builder.append(SEMICOLON);
            builder.append("/450Wx450H/" + refProduct.getImage());
            builder.append(SEMICOLON);
            builder.append("$siteResource/images/450Wx450H/" + refProduct.getImage());
            builder.append(SEMICOLON);
            builder.append("png");
            builder.append(SEMICOLON);
            builder.append("");
            builder.append(SEMICOLON);
            builder.append("images");
            builder.append(System.lineSeparator());
        }

        //Do we really need this media continers?
        builder.append(System.lineSeparator());
        builder.append("INSERT_UPDATE MediaContainer; qualifier[unique=true]; $medias; $catalogVersion\n");
        for (Integer refProductKey : referenceProducts.keySet()) {
            ReferenceProduct refProduct = referenceProducts.get(refProductKey);
            builder.append(SEMICOLON);
            builder.append("APPS-" + refProduct.getId());
            builder.append(SEMICOLON);
            builder.append("/450Wx450H/" + refProduct.getImage());
            builder.append(SEMICOLON);
            builder.append(System.lineSeparator());
        }

        builder.append(System.lineSeparator());
        builder.append("UPDATE Product;code[unique=true];$picture;$thumbnail;$detail;$others;$normal;$thumbnails;$catalogVersion\n");
        for (Integer refProductKey : referenceProducts.keySet()) {
            ReferenceProduct refProduct = referenceProducts.get(refProductKey);
            builder.append(SEMICOLON);
            builder.append("APPS-" + refProduct.getId());
            builder.append(SEMICOLON);
            for (int i = 0; i < 6; i++) {
                builder.append("/450Wx450H/" + refProduct.getImage());
                builder.append(SEMICOLON);
            }
            builder.append(System.lineSeparator());
        }

        String impexMedias = builder.toString();
        String impexName = "APIsProductsMedia";
        ParserUtils.writeToFile(impexMedias, impexName);
    }

    public void readReferenceProducts(Sheet referenceProductSheet){
        Iterator<Row> rowIterator = referenceProductSheet.rowIterator();
        if (!rowIterator.hasNext()){
            return;
        }
        rowIterator.next(); // skip first header row

        while (rowIterator.hasNext()){
            Row row = rowIterator.next();
            Iterator<Cell> cellIterator = row.cellIterator();
            Cell firstCell = cellIterator.next();

            if (StringUtils.isEmpty(firstCell.toString())){
                return;//to not read empty rows
            }

            ReferenceProduct referenceProduct = new ReferenceProduct();
            referenceProduct.setKey(Integer.valueOf(firstCell.toString()));
            referenceProduct.setStore(cellIterator.next().toString());
            referenceProduct.setId(cellIterator.next().toString());
            referenceProduct.setProductName(cellIterator.next().toString());
            referenceProduct.setCaption(cellIterator.next().toString());
            ClassificationAttributes classificationAttributes = new ClassificationAttributes();
            referenceProduct.setClassificationAttributes(classificationAttributes);
            classificationAttributes.setType(cellIterator.next().toString());
            referenceProduct.setCategory(cellIterator.next().toString());
            referenceProduct.setImage(cellIterator.next().toString());
            referenceProduct.setDeveloper(cellIterator.next().toString());
            classificationAttributes.setBuyable(BooleanUtils.toBoolean(cellIterator.next().toString()));
            classificationAttributes.setFreeTrial(BooleanUtils.toBoolean(cellIterator.next().toString()));
            referenceProduct.setFeatured(BooleanUtils.toBoolean(cellIterator.next().toString()));
            referenceProduct.setUrl(cellIterator.next().toString());

            referenceProducts.put(referenceProduct.getKey(), referenceProduct);
        }
    }

    public void readSapProducts(Sheet sapProductSheet){
        Iterator<Row> rowIterator = sapProductSheet.rowIterator();
        while (rowIterator.hasNext()){
            Row row = rowIterator.next();
            Iterator<Cell> cellIterator = row.cellIterator();
            Cell firstCell = cellIterator.next();

            if (StringUtils.isEmpty(firstCell.toString())){
                return;//to not read empty rows
            }

            SapProduct sapProduct = new SapProduct();
            sapProduct.setName(firstCell.toString());
            sapProduct.setId(cellIterator.next().toString());
            sapProduct.setLink(cellIterator.next().toString());
            while (cellIterator.hasNext()) {
                Cell refProductCell = cellIterator.next();
                if (StringUtils.isEmpty(refProductCell.toString())){
                    break;
                }
                Integer referenceKey = Integer.valueOf(refProductCell.toString());
                sapProduct.getReferenceProducts().add(referenceKey);
            }

            sapProducts.add(sapProduct);
        }
    }

    private void initCategoryNameToCodeBinding() {
        for (ReferenceProduct product: referenceProducts.values()){
            if (StringUtils.isNotEmpty(product.getCategory())) {
                String categoryWithUpperLetters = ParserUtils.firstLetterToUpper(product.getCategory());
                categoryNameToCode.put(product.getCategory(), ParserUtils.firstLetterLowerCase(ParserUtils.trimString(categoryWithUpperLetters)));
            }
        }
    }

    private void customizeCategoriesCodes() {
        for (String categoryKey : categoryNameToCode.keySet()) {
            if (categoryKey.contains("(HR)")) {
                categoryNameToCode.replace(categoryKey, "hr");
            } else if (categoryKey.contains("ERP")) {
                categoryNameToCode.replace(categoryKey, "erpAndDigitalCore");
            }
        }
    }

}
