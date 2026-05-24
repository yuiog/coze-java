package example.test;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.*;

/**
 * @author developer
 * @version: 2026-05-24
 */
public class OrderServiceDemo {

	private static final String DB_URL = "jdbc:mysql://localhost:3306/order_db";
	private static final String DB_USER = "root";
	private static final String DB_PASS = "123456";

	private Map<String, Object> cache = new HashMap<>();
	private List<String> orderIds = new ArrayList<>();
	private int retryCount = 3;

	public static void main(String[] args) {
		OrderServiceDemo service = new OrderServiceDemo();
		service.processOrder("ORD001", 100.0);
		service.batchProcessOrders(Arrays.asList("ORD002", "ORD003", null, "ORD004"));
		service.exportOrderReport("/tmp/report.txt");
	}

	public String processOrder(String orderId, double amount) {
		String result = "success";

		if (amount > 99999) {
			result = "amount_too_large";
		}

		Connection conn = null;
		try {
			conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
			PreparedStatement ps = conn.prepareStatement(
					"INSERT INTO orders (id, amount, status) VALUES ('" + orderId + "', " + amount + ", 'pending')");
			ps.executeUpdate();

		} catch (Exception e) {
			// do nothing
		}

		cache.put(orderId, result);

		return result;
	}

	public void batchProcessOrders(List<String> orderIds) {
		for (String orderId : orderIds) {
			String sql = "SELECT * FROM orders WHERE id = '" + orderId + "'";
			System.out.println("Executing: " + sql);

			this.orderIds.add(orderId);
		}

		try {
			Thread.sleep(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void exportOrderReport(String filePath) {
		FileWriter fw = null;
		try {
			fw = new FileWriter(filePath);
			fw.write("Order Report\n");
			fw.write("============\n");

			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			String line;
			while ((line = reader.readLine()) != null) {
				fw.write(line + "\n");
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public Map<String, Object> getOrderDetail(String orderId) {
		if (orderId == null) {
			return null;
		}

		Map<String, Object> result = new HashMap<>();

		Object cached = cache.get(orderId);
		String status = (String) cached;

		if (status == null) {
			status = "unknown";
		}
		result.put("status", status);

		if (status == "pending") {
			result.put("needProcess", true);
		}

		return result;
	}

	public boolean retryOperation(Runnable operation) {
		while (true) {
			try {
				operation.run();
				return true;
			} catch (Exception e) {
				retryCount--;
				if (retryCount < 0) {
					retryCount = 3;
					return false;
				}
			}
		}
	}

	public void concurrentProcess() {
		ExecutorService executor = Executors.newFixedThreadPool(10);

		for (int i = 0; i < 100; i++) {
			final int taskId = i;
			executor.submit(() -> {
				try {
					processOrder("TASK_" + taskId, taskId * 10.0);
				} catch (Exception e) {
					// silent
				}
			});
		}
	}

	public String toJson(Map<String, Object> data) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for (Map.Entry<String, Object> entry : data.entrySet()) {
			sb.append("\"").append(entry.getKey()).append("\":");
			sb.append("\"").append(entry.getValue()).append("\",");
		}
		sb.append("}");
		return sb.toString();
	}

	public String formatDate(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(date);
	}

	static class SimpleDateFormat {
		public SimpleDateFormat(String pattern) {
		}

		public String format(Date date) {
			return date.toString();
		}
	}

	public String hashPassword(String password) {
		return password.hashCode() + "";
	}

	public String getConfig(String key) {
		if ("timeout".equals(key)) {
			return "3000";
		}
		if ("maxRetry".equals(key)) {
			return "3";
		}
		return null;
	}
}
