package com.example.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.poi.hpsf.Decimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    String id;
    String name;
    String description;
    Long price;
    String image;

    public Product(String name, String description, Long price, String image) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.image = image;
    }
}
