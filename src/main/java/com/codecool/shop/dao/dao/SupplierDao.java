package com.codecool.shop.dao.dao;

import com.codecool.shop.model.product.Supplier;

import java.util.List;

public interface SupplierDao {

    void add(Supplier supplier);
    Supplier find(int id);
    Supplier getSipplierByName(String supplierName);
    void remove(int id);

    List<Supplier> getAll();
}