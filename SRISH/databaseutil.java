package com.srmart.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseUtil {
    private static HikariDataSource dataSource;

    public static synchronized void initialize() {
        if (dataSource == null) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:h2:mem:srmartdb;DB_CLOSE_DELAY=-1;INIT=RUNSCRIPT FROM 'classpath:db/schema.sql'\\;RUNSCRIPT FROM 'classpath:db/seed.sql'");
            config.setUsername("sa");
            config.setPassword("");
            config.setDriverClassName("org.h2.Driver");
            config.setMaximumPoolSize(10);
            dataSource = new HikariDataSource(config);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            initialize();
        }
        return dataSource.getConnection();
    }

    public static synchronized void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseUtil {
    private static HikariDataSource dataSource;

    public static synchronized void initialize() {
        if (dataSource == null) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:h2:mem:srmartdb;DB_CLOSE_DELAY=-1;INIT=RUNSCRIPT FROM 'classpath:db/schema.sql'\\;RUNSCRIPT FROM 'classpath:db/seed.sql'");
            config.setUsername("sa");
            config.setPassword("");
            config.setDriverClassName("org.h2.Driver");
            config.setMaximumPoolSize(10);
            dataSource = new HikariDataSource(config);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            initialize();
        }
        return dataSource.getConnection();
    }

    public static synchronized void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
package com.srmart.listener;

import com.srmart.util.DatabaseUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        DatabaseUtil.initialize();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        DatabaseUtil.shutdown();
    }
}
package com.srmart.model;

public class User {
    private int id;
    private String name;
    private String email;
    private String username;
    private String passwordHash;
    private String role;

    public User() {}

    public User(int id, String name, String email, String username, String passwordHash, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
package com.srmart.model;

import java.math.BigDecimal;

public class Product {
    private int id;
    private int sellerId;
    private String name;
    private String description;
    private BigDecimal price;
    private int stockQty;
    private String category;
    private String sizeOptions;
    private String color;
    private String imageUrl;
    private String status;

    public Product() {}

    public Product(int id, int sellerId, String name, String description, BigDecimal price, int stockQty, String category, String sizeOptions, String color, String imageUrl, String status) {
        this.id = id;
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQty = stockQty;
        this.category = category;
        this.sizeOptions = sizeOptions;
        this.color = color;
        this.imageUrl = imageUrl;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getSellerId() { return sellerId; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public int getStockQty() { return stockQty; }
    public void setStockQty(int stockQty) { this.stockQty = stockQty; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSizeOptions() { return sizeOptions; }
    public void setSizeOptions(String sizeOptions) { this.sizeOptions = sizeOptions; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
package com.srmart.dao;

import com.srmart.model.Product;
import com.srmart.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public List<Product> getAllProducts(String category, String query) throws SQLException {
        List<Product> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE status = 'Active'");

        if (category != null && !category.trim().isEmpty() && !"All".equalsIgnoreCase(category)) {
            sql.append(" AND category = ?");
        }
        if (query != null && !query.trim().isEmpty()) {
            sql.append(" AND (LOWER(name) LIKE ? OR LOWER(description) LIKE ?)");
        }

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            int idx = 1;
            if (category != null && !category.trim().isEmpty() && !"All".equalsIgnoreCase(category)) {
                ps.setString(idx++, category);
            }
            if (query != null && !query.trim().isEmpty()) {
                String term = "%" + query.toLowerCase() + "%";
                ps.setString(idx++, term);
                ps.setString(idx++, term);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToProduct(rs));
                }
            }
        }
        return list;
    }

    public boolean addProduct(Product p) throws SQLException {
        String sql = "INSERT INTO products (seller_id, name, description, price, stock_qty, category, size_options, color, image_url, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'Active')";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getSellerId());
            ps.setString(2, p.getName());
            ps.setString(3, p.getDescription());
            ps.setBigDecimal(4, p.getPrice());
            ps.setInt(5, p.getStockQty());
            ps.setString(6, p.getCategory());
            ps.setString(7, p.getSizeOptions());
            ps.setString(8, p.getColor());
            ps.setString(9, p.getImageUrl());
            return ps.executeUpdate() > 0;
        }
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        return new Product(
            rs.getInt("id"),
            rs.getInt("seller_id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getBigDecimal("price"),
            rs.getInt("stock_qty"),
            rs.getString("category"),
            rs.getString("size_options"),
            rs.getString("color"),
            rs.getString("image_url"),
            rs.getString("status")
        );
    }
}
package com.srmart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.srmart.dao.ProductDAO;
import com.srmart.model.Product;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/v1/products")
public class ProductServlet extends HttpServlet {
    private final ProductDAO productDAO = new ProductDAO();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String category = req.getParameter("category");
        String query = req.getParameter("q");

        try {
            List<Product> products = productDAO.getAllProducts(category, query);
            mapper.writeValue(resp.getWriter(), products);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Unable to fetch apparel items.\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        try {
            Product product = mapper.readValue(req.getInputStream(), Product.class);
            boolean success = productDAO.addProduct(product);
            if (success) {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.getWriter().write("{\"message\": \"Apparel item created successfully.\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Failed to add item.\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Server error while adding product.\"}");
        }
    }
}
package com.srmart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/chat")
public class ChatServlet extends HttpServlet {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        Map<String, String> body = mapper.readValue(req.getInputStream(), Map.class);
        String userMsg = body.getOrDefault("message", "").toLowerCase();

        String reply;
        if (userMsg.contains("size") || userMsg.contains("fit")) {
            reply = "SR Mart sizing follows international standards: S (36\"), M (38\"), L (40\"), XL (42\"). Check individual listing details for stretch & tailored measurements!";
        } else if (userMsg.contains("material") || userMsg.contains("fabric") || userMsg.contains("cotton")) {
            reply = "Our apparel catalog prioritizes 100% organic cotton, silk blends, and premium washed denim.";
        } else if (userMsg.contains("return") || userMsg.contains("exchange")) {
            reply = "You can return or exchange any unworn apparel within 30 days of delivery with original tags attached.";
        } else if (userMsg.contains("shipping") || userMsg.contains("delivery")) {
            reply = "Standard express delivery takes 2 to 4 business days across India.";
        } else {
            reply = "Welcome to SR Mart Fashion AI! Ask me about sizes, clothing fabrics, delivery, or return policies.";
        }

        Map<String, String> responseData = new HashMap<>();
        responseData.put("reply", reply);
        mapper.writeValue(resp.getWriter(), responseData);
    }
}
package com.srmart.model;

public class User {
    private Long id;
    private String name;
    private String email;
    private String username;
    private String password;
    private String role;

    public User() {}

    public User(Long id, String name, String email, String username, String password, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
package com.srmart.model;

public class Product {
    private Long id;
    private Long seller;
    private String name;
    private String desc;
    private double price;
    private int stock;
    private String cat;
    private String img;
    private String status;

    public Product() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSeller() { return seller; }
    public void setSeller(Long seller) { this.seller = seller; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getCat() { return cat; }
    public void setCat(String cat) { this.cat = cat; }
    public String getImg() { return img; }
    public void setImg(String img) { this.img = img; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
package com.srmart.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.srmart.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {
    private final ObjectMapper mapper = new ObjectMapper();
    private final List<User> users = new ArrayList<>();

    @Override
    public void init() {
        // Initial Dummy Users from your JS file
        users.add(new User(1L, "Demo Buyer", "buyer@srmart.com", "buyer", "1234", "Buyer"));
        users.add(new User(2L, "Vogue Fashion Seller", "seller@srmart.com", "seller", "1234", "Seller"));
        users.add(new User(3L, "SR Mart Admin", "admin@srmart.com", "admin", "1234", "Admin"));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        String path = req.getPathInfo();

        if ("/login".equals(path)) {
            Map<String, String> credentials = mapper.readValue(req.getInputStream(), Map.class);
            String inputUser = credentials.get("username").toLowerCase();
            String password = credentials.get("password");

            User found = users.stream()
                    .filter(u -> (u.getUsername().equalsIgnoreCase(inputUser) || u.getEmail().equalsIgnoreCase(inputUser)) 
                            && u.getPassword().equals(password))
                    .findFirst()
                    .orElse(null);

            if (found != null) {
                mapper.writeValue(resp.getWriter(), Map.of("success", true, "user", found));
            } else {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                mapper.writeValue(resp.getWriter(), Map.of("error", "Invalid username or password"));
            }
        } else if ("/register".equals(path)) {
            User newUser = mapper.readValue(req.getInputStream(), User.class);
            
            boolean exists = users.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(newUser.getUsername()));
            if (exists) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                mapper.writeValue(resp.getWriter(), Map.of("error", "Username already taken"));
                return;
            }

            newUser.setId(System.currentTimeMillis());
            users.add(newUser);
            mapper.writeValue(resp.getWriter(), Map.of("success", true, "user", newUser));
        }
    }
}
package com.srmart.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet("/api/chat")
public class ChatServlet extends HttpServlet {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        
        Map<String, String> body = mapper.readValue(req.getInputStream(), Map.class);
        String userMsg = body.getOrDefault("message", "").toLowerCase();
        
        String reply;
        if (userMsg.contains("size") || userMsg.contains("fit")) {
            reply = "SR Mart sizing: S (36\"), M (38\"), L (40\"), XL (42\"). Check individual descriptions for tailored fit tips!";
        } else if (userMsg.contains("material") || userMsg.contains("fabric")) {
            reply = "Our clothing uses 100% organic breathable cotton, silk blends, and premium washed denim.";
        } else {
            reply = "SR Mart offers true-to-size apparel. Standard delivery takes 2 to 4 business days with a 30-day return policy.";
        }

        mapper.writeValue(resp.getWriter(), Map.of("reply", reply));
    }
}
