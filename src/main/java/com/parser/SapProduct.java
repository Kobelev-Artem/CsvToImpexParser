package com.parser;

import java.util.ArrayList;
import java.util.List;

public class SapProduct {

    private String name;
    private String id;
    private String link;
    private List<Integer> referenceProducts = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public List<Integer> getReferenceProducts() {
        return referenceProducts;
    }

    public void setReferenceProducts(List<Integer> referenceProducts) {
        this.referenceProducts = referenceProducts;
    }
}
