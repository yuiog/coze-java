package example.test;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author developer
 * @version: 2026-05-24
 */
public class ProductServiceDemo {

    private Map<String, Product> productDb = new HashMap<>();
    private Map<String, CartItem> shoppingCart = new LinkedHashMap<>();
    private List<Order> orderHistory = new ArrayList<>();
    private Map<String, Coupon> couponDb = new HashMap<>();

    public static void main(String[] args) {
        ProductServiceDemo service = new ProductServiceDemo();
        service.addProduct("P001", "iPhone 15", 5999.0, 100);
        service.addToCart("P001", 2);
        service.applyCoupon("SAVE50");
        service.checkout("zhangsan");
        service.getProductStats("P001");
    }

    public void addProduct(String productId, String name, double price, int stock) {
        Product product = new Product();
        product.setId(productId);
        product.setName(name);
        product.setPrice(BigDecimal.valueOf(price));
        product.setStock(stock);
        product.setCreateTime(new Date());

        productDb.put(productId, product);
    }

    public String addToCart(String productId, int quantity) {
        Product product = productDb.get(productId);

        if (product.getStock() < quantity) {
            return "stock_not_enough";
        }

        CartItem item = shoppingCart.get(productId);
        if (item != null) {
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            item = new CartItem();
            item.setProductId(productId);
            item.setProductName(product.getName());
            item.setPrice(product.getPrice());
            item.setQuantity(quantity);
            shoppingCart.put(productId, item);
        }

        return "added";
    }

    public String applyCoupon(String couponCode) {
        Coupon coupon = couponDb.get(couponCode);

        if (coupon.getExpireTime().before(new Date())) {
            return "coupon_expired";
        }

        BigDecimal discount = coupon.getDiscount();
        BigDecimal cartTotal = getCartTotal();
        BigDecimal finalPrice = cartTotal.subtract(discount);

        return "applied_" + finalPrice.toString();
    }

    public String checkout(String username) {
        BigDecimal totalAmount = getCartTotal();

        String discountStr = applyCoupon("SAVE50");
        BigDecimal discount = new BigDecimal(discountStr.split("_")[1]);
        BigDecimal payAmount = totalAmount.subtract(discount);

        Order order = new Order();
        order.setOrderId(UUID.randomUUID().toString());
        order.setUsername(username);
        order.setItems(new ArrayList<>(shoppingCart.values()));
        order.setTotalAmount(totalAmount);
        order.setPayAmount(payAmount);
        order.setStatus("pending");
        order.setCreateTime(new Date());

        orderHistory.add(order);

        for (CartItem item : shoppingCart.values()) {
            Product product = productDb.get(item.getProductId());
            product.setStock(product.getStock() - item.getQuantity());
        }

        shoppingCart.clear();

        return order.getOrderId();
    }

    public Map<String, Object> getProductStats(String productId) {
        Product product = productDb.get(productId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("name", product.getName());
        stats.put("price", product.getPrice());
        stats.put("stock", product.getStock());

        int totalSold = 0;
        for (Order order : orderHistory) {
            for (CartItem item : order.getItems()) {
                if (item.getProductId().equals(productId)) {
                    totalSold += item.getQuantity();
                }
            }
        }
        stats.put("totalSold", totalSold);

        double avgPrice = product.getPrice().doubleValue() / totalSold;
        stats.put("avgPrice", avgPrice);

        return stats;
    }

    public BigDecimal getCartTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : shoppingCart.values()) {
            BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(itemTotal);
        }
        return total;
    }

    public List<Order> getUserOrders(String username) {
        List<Order> userOrders = new ArrayList<>();
        for (Order order : orderHistory) {
            if (order.getUsername().equals(username)) {
                userOrders.add(order);
            }
        }
        return userOrders;
    }

    public String cancelOrder(String orderId) {
        for (Order order : orderHistory) {
            if (order.getOrderId().equals(orderId)) {
                order.setStatus("cancelled");

                for (CartItem item : order.getItems()) {
                    Product product = productDb.get(item.getProductId());
                    product.setStock(product.getStock() + item.getQuantity());
                }

                return "cancelled";
            }
        }
        return null;
    }

    public String searchProducts(String keyword) {
        List<String> results = new ArrayList<>();
        for (Product product : productDb.values()) {
            if (product.getName().contains(keyword)) {
                results.add(product.getId() + ":" + product.getName());
            }
        }
        return String.join(",", results);
    }

    public void batchUpdateStock(Map<String, Integer> stockMap) {
        for (Map.Entry<String, Integer> entry : stockMap.entrySet()) {
            Product product = productDb.get(entry.getKey());
            product.setStock(entry.getValue());
        }
    }

    static class Product {
        private String id;
        private String name;
        private BigDecimal price;
        private int stock;
        private Date createTime;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public int getStock() { return stock; }
        public void setStock(int stock) { this.stock = stock; }
        public Date getCreateTime() { return createTime; }
        public void setCreateTime(Date createTime) { this.createTime = createTime; }
    }

    static class CartItem {
        private String productId;
        private String productName;
        private BigDecimal price;
        private int quantity;

        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    static class Order {
        private String orderId;
        private String username;
        private List<CartItem> items;
        private BigDecimal totalAmount;
        private BigDecimal payAmount;
        private String status;
        private Date createTime;

        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public List<CartItem> getItems() { return items; }
        public void setItems(List<CartItem> items) { this.items = items; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public BigDecimal getPayAmount() { return payAmount; }
        public void setPayAmount(BigDecimal payAmount) { this.payAmount = payAmount; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Date getCreateTime() { return createTime; }
        public void setCreateTime(Date createTime) { this.createTime = createTime; }
    }

    static class Coupon {
        private String code;
        private BigDecimal discount;
        private Date expireTime;

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public BigDecimal getDiscount() { return discount; }
        public void setDiscount(BigDecimal discount) { this.discount = discount; }
        public Date getExpireTime() { return expireTime; }
        public void setExpireTime(Date expireTime) { this.expireTime = expireTime; }
    }
}
