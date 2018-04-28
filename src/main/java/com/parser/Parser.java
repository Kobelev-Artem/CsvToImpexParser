package com.parser;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Parser {

    public List<SapProduct> sapProducts = new ArrayList<>();
    public Map<Integer, ReferenceProduct> referenceProducts = new HashMap<>();
    public Map<String, String> vendorNameToCode = new HashMap<>();

    public void createAndWriteVendors(){
        String header = "INSERT_UPDATE Vendor;code[unique=true,forceWrite=true,allownull=true];name[lang=en];description[lang=en]\n";
        String vendorTemplate = ";{code};{name};{name}\n";

        for (ReferenceProduct product: referenceProducts.values()){
            vendorNameToCode.put(product.getDeveloper(), trimString(product.getDeveloper()));
        }

        String vendorBody = "";
        for (String vendor : vendorNameToCode.keySet()){
            vendorBody += vendorTemplate.replaceFirst("\\{code\\}", vendorNameToCode.get(vendor))
                                        .replaceAll("\\{name\\}", vendor);
        }

        String venderImpex = header + vendorBody;
        String impexName = "initialVendors";
        writeToFile(venderImpex, impexName);
    }

    public void createSapProductImpexes(SapProduct sapProduct){
        String variables = "$productCatalog=globalProductCatalog\n" +
                "$catalogVersion=catalogversion(catalog(id[default=$productCatalog]),version[default='Staged'])[unique=true,default=$productCatalog:Staged]\n" +
                "$vendors=vendors(code)[default=sap]\n";
        String productHeader = "INSERT_UPDATE Product; code[unique=true]; $catalogVersion; name[lang=en]; description[lang=en]; unit(code); approvalStatus(code); startLineNumber; primaryAction(code, itemtype(code))[allownull = true]; primaryActionLink; $vendors; mainCategory(catalogVersion(catalog(id),version),code);\n";
        String sapProductTemplate = "; SAP-{id}; ; {name}; {name}; pieces; approved; 0; Buy:PrimaryActionEnum; {link}; sap; globalProductCatalog:Staged:analytics\n";
        String refProductTemplate = "; APPS-{id}; ; {productName}; {caption}; pieces; approved; 0; Buy:PrimaryActionEnum; {url}; {vendor}; globalProductCatalog:Staged:{#category}\n";

        String sapProductRow = sapProductTemplate.replaceFirst("\\{id\\}", sapProduct.getId())
                                                .replaceAll("\\{name\\}", sapProduct.getName())
                                                .replaceFirst("\\{link\\}", sapProduct.getLink());
        String refProductRows = "";
        for (Integer refProductKey : sapProduct.getReferenceProducts()){
            ReferenceProduct refProduct = referenceProducts.get(refProductKey);
            String refProductRow = refProductTemplate.replaceFirst("\\{id\\}", refProduct.getId())
                                                    .replaceFirst("\\{productName\\}", refProduct.getProductName())
                                                    .replaceFirst("\\{caption\\}", refProduct.getCaption())
                                                    .replaceFirst("\\{url\\}", refProduct.getUrl())
                                                    .replaceFirst("\\{vendor\\}", vendorNameToCode.get(refProduct.getDeveloper()));
            refProductRows += refProductRow;
        }

        String sapProductImpex = variables + productHeader + sapProductRow + refProductRows;
        String impexName = sapProduct.getName() + "-" + sapProduct.getId();
        impexName = trimString(impexName);
        writeToFile(sapProductImpex, impexName);
    }

    private String trimString(String something) {
        return something.replaceAll("\\s+","");
    }

    public void writeToFile(String text, String fileName){
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

    public void createVendorsImpex(){}

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

}
