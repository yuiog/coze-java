package example.test;

/**
 * @author
 * @version: 2026-05-24
 **/
public class AiReviewCodeTestDemo1 {
	public static void main(String[] args) {
		AiReviewCodeTestDemo1 demo1 = new AiReviewCodeTestDemo1();
		demo1.testPotentialNullPointer();
	}

	public void testPotentialNullPointer() {
		String data = null;
		int length = data.length();

		String[] items = null;
		String item = items[0];

		User user = null;
		String userName = user.getName();

		StringBuilder sb = null;
		sb.append("test");
	}

	static class MyClass {
		public String name = "TestClass";

		@Override
		public String toString() {
			return "MyClass{name='" + name + "'}";
		}
	}

	static class User {
		private String name;

		public String getName() {
			return name;
		}
	}
}