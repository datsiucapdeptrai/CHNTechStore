package com.example.calculatorapp;

public class CartItem {
    private final String documentId;
    private final String productName;
    private final String price;

    public CartItem(String documentId, String productName, String price) {
        this.documentId = documentId;
        this.productName = productName;
        this.price = price;
    }

    public String getDocumentId() { return documentId; }
    public String getProductName() { return productName; }
    public String getPrice() { return price; }
}