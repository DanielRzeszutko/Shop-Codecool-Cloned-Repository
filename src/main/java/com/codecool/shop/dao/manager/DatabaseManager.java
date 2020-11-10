package com.codecool.shop.dao.manager;

import com.codecool.shop.dao.dao.CartDao;
import com.codecool.shop.dao.dao.ProductCategoryDao;
import com.codecool.shop.dao.dao.ProductDao;
import com.codecool.shop.dao.dao.SupplierDao;
import com.codecool.shop.dao.jdbc.CartDaoJdbc;
import com.codecool.shop.dao.jdbc.ProductCategoryDaoJdbc;
import com.codecool.shop.dao.jdbc.ProductDaoJdbc;
import com.codecool.shop.dao.jdbc.SupplierDaoJdbc;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

//TODO NOT WORKING YET

public class DatabaseManager {
	public ProductDao productDao;
	public SupplierDao supplierDao;
	public ProductCategoryDao categoryDao;

	public CartDao cartDao;

	public void run() {
		try {
			setup();
		} catch (SQLException | IOException throwables) {
			System.err.println("Could not connect to the database.");
		}
	}

	public void setup() throws SQLException, IOException {
		DataSource dataSource = connect();
		productDao = new ProductDaoJdbc(dataSource);
		supplierDao = new SupplierDaoJdbc(dataSource);
		categoryDao = new ProductCategoryDaoJdbc(dataSource);
		cartDao = new CartDaoJdbc(dataSource);

	}

	private DataSource connect() throws SQLException, IOException {
		PGSimpleDataSource dataSource = new PGSimpleDataSource();
		Properties db_config = new Properties();
		db_config.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("db_config.properties"));
		String dbName = db_config.getProperty("dbName");
		String user = db_config.getProperty("user");
		String password = db_config.getProperty("password");

//		String dbName = System.getenv("PSQL_DB_NAME");
//		String user = System.getenv("PSQL_USER_NAME");
//		String password = System.getenv("PSQL_PASSWORD");

		dataSource.setDatabaseName(dbName);
		dataSource.setUser(user);
		dataSource.setPassword(password);

		System.out.println("Trying to connect");
		dataSource.getConnection().close();
		System.out.println("Connection ok.");

		return dataSource;
	}
}
