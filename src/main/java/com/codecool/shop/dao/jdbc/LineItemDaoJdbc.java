package com.codecool.shop.dao.jdbc;

import com.codecool.shop.dao.dao.LineItemDao;
import com.codecool.shop.dao.dao.ProductCategoryDao;
import com.codecool.shop.dao.dao.ProductDao;
import com.codecool.shop.dao.dao.SupplierDao;
import com.codecool.shop.model.order.Cart;
import com.codecool.shop.model.order.LineItem;
import com.codecool.shop.model.product.Product;


import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LineItemDaoJdbc implements LineItemDao {

    private final DataSource dataSource;
    private final ProductDao productDao;


    public LineItemDaoJdbc(DataSource dataSource,  ProductDao productDao) {
        this.dataSource = dataSource;
        this.productDao = productDao;
    }

    @Override
    public void addProduct(Cart cart, int productId, int addedQuantity) {
        int cartId = cart.getId();
        try (Connection conn = dataSource.getConnection()) {
            Product product = productDao.find(productId);
            String sql = "INSERT INTO line_item AS current_line_item (cart_id, product_id, quantity, total_line_price) VALUES (?, ?, ?, ?)" +
                    " ON CONFLICT (cart_id, product_id)" +
                    " DO UPDATE SET quantity = current_line_item.quantity + ?, total_line_price = current_line_item.total_line_price + ?";
            PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, cartId);
            statement.setInt(2, product.getId());
            statement.setInt(3, addedQuantity);
            statement.setInt(4, (int) product.getDefaultPrice() * addedQuantity);
            statement.setInt(5, addedQuantity);
            statement.setInt(6, (int) product.getDefaultPrice() * addedQuantity);
            statement.executeUpdate();
            ResultSet resultSet = statement.getGeneratedKeys();
            resultSet.next();
            Optional<LineItem> updatedLineItem = cart.getLineItemByProductId(productId);
            if (updatedLineItem.isPresent()){
                updatedLineItem.get().setQty(resultSet.getInt("quantity"));
                updatedLineItem.get().setLinePrice(resultSet.getInt("total_line_price"));
            }
            else {
                cart.addLineItem(product, cartId);
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Error while adding new line item.", exception);
        }
    }

    @Override
    public LineItem find(int productId, int cartId) {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT * FROM line_item WHERE product_id = ? AND cart_id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, productId);
            statement.setInt(2, cartId);
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                return null;
            }
            Product product = productDao.find(productId);
            LineItem lineItem = new LineItem(product, resultSet.getInt("quantity"),
                    resultSet.getInt("cart_id"));
            lineItem.setLineId(resultSet.getInt("id"));
            return lineItem;
        } catch (SQLException exception) {
            throw new RuntimeException("Error while retrieving line item for product id: " +  productId, exception);
        }
    }


    @Override
    public void remove(int productId,  Cart cart) {
        int cartId = cart.getId();
        try (Connection conn = dataSource.getConnection()) {
            String sql = "DELETE FROM line_item WHERE product_id = ? AND cart_id = ? RETURNING *";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, productId);
            statement.setInt(2, cartId);
            ResultSet rs = statement.executeQuery();
            cart.removeLineByProductId(productId);
        } catch (SQLException exception) {
            throw new RuntimeException("Error while deleting line_item with for product_id " + productId , exception);
        }
    }

    @Override
    public List<LineItem> findLineItemsByCartId(int cartId) {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT * FROM line_item WHERE cart_id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, cartId);
            ResultSet rs = statement.executeQuery();
            List<LineItem> lineItems = new ArrayList<>();
            while (rs.next()) {
                LineItem lineItem = new LineItem(productDao.find(rs.getInt("product_id")),
                        rs.getInt("quantity"), rs.getInt("total_line_price"));
                lineItem.setLineId(rs.getInt("id"));
                lineItems.add(lineItem);
            }
            return lineItems;
        } catch (SQLException e) {
            throw new RuntimeException("ERROR while reading line items." + e.getMessage());
        }
    }


    @Override
    public int getTotalValueOfLinesInCart(int cartId){
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT SUM(total_line_price) as total_price FROM line_item WHERE cart_id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, cartId);
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                return 0;
            }
            return resultSet.getInt("total_price");
        } catch (SQLException e) {
            throw new RuntimeException("ERROR while reading line items " + e.getMessage());
        }
    }

    @Override
    public int getTotalNumberOfLinesInCart(int cartId){
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT SUM(quantity) as cart_size FROM line_item WHERE cart_id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, cartId);
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                return 0;
            }
            return resultSet.getInt("cart_size");
        } catch (SQLException e) {
            throw new RuntimeException("ERROR while reading line items " + e.getMessage());
        }
    }

}
