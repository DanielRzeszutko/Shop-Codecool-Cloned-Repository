package com.codecool.shop.dao.dao;

import com.codecool.shop.model.product.ProductCategory;

import java.util.List;

public interface ProductCategoryDao {

    void add(ProductCategory category);
    ProductCategory find(int id);
    ProductCategory getCategoryByName(String categoryName);
    void remove(int id);
    List<ProductCategory> getAll();
}
