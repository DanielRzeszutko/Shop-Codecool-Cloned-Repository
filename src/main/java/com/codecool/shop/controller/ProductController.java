package com.codecool.shop.controller;

import com.codecool.shop.config.TemplateEngineUtil;
import com.codecool.shop.dao.dao.OrderDao;
import com.codecool.shop.dao.dao.ProductCategoryDao;
import com.codecool.shop.dao.dao.ProductDao;
import com.codecool.shop.dao.jdbc.OrderDaoMem;
import com.codecool.shop.dao.jdbc.ProductCategoryDaoMem;
import com.codecool.shop.dao.jdbc.ProductDaoMem;
import com.codecool.shop.dao.jdbc.SupplierDaoMem;
import com.codecool.shop.model.order.Order;
import com.codecool.shop.model.product.Product;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

@WebServlet(urlPatterns = {"/"}, loadOnStartup = 1)
public class ProductController extends HttpServlet {
    List<Product> products;
    private final Util util = new Util();
//    DatabaseManager dbManager = new DatabaseManager();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        dbManager.run();
//        ProductDao productDataStore = dbManager.productDao; //todo change data source to db instead od Mem objects
        ProductDao productDataStore = ProductDaoMem.getInstance();
        ProductCategoryDao productCategoryDataStore = ProductCategoryDaoMem.getInstance();
        SupplierDaoMem supplierDataStore = SupplierDaoMem.getInstance();
        TemplateEngine engine = TemplateEngineUtil.getTemplateEngine(req.getServletContext());
        WebContext context = new WebContext(req, resp, req.getServletContext());

        String category = (req.getParameter("category"));
        String supplier = (req.getParameter("supplier"));

        if (category != null && supplier == null) {
            products = productDataStore.getBy(productCategoryDataStore.getCategoryByName(category));
        }
        else if (category == null && supplier != null) {
            products = productDataStore.getBy(supplierDataStore.getSipplierByName(supplier));
        }
        else {
            products = productDataStore.getAll();
        }

        context.setVariable("itemsNumber", 0);
        if (util.isExistingOrder(req)) {
            addItemsNumberToContext(req, context);
        }

        setContextParameters(productCategoryDataStore, supplierDataStore, context);
        engine.process("product/index.html", context, resp.getWriter());
    }

    private void addItemsNumberToContext(HttpServletRequest req, WebContext context) {
        OrderDao orderDataStore = OrderDaoMem.getInstance();
        Order order = orderDataStore.getActual(Integer.parseInt(util.getCookieValueBy("userId", req)));
        int itemsNumber = order.getCart().getCartSize();
        context.setVariable("itemsNumber", itemsNumber);
    }

    private void setContextParameters(ProductCategoryDao productCategoryDataStore, SupplierDaoMem supplierDataStore, WebContext context) {
        context.setVariable("products", products);
        context.setVariable("categories", productCategoryDataStore.getAll());
        context.setVariable("suppliers", supplierDataStore.getAll());
    }
}


// // Alternative setting of the template context
// Map<String, Object> params = new HashMap<>();
// params.put("category", productCategoryDataStore.find(1));
// params.put("products", productDataStore.getBy(productCategoryDataStore.find(1)));
// context.setVariables(params);