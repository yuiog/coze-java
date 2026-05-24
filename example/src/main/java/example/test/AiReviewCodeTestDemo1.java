package example.test;

/**
 * @author
 * @version: 2026-05-24
 **/
public class AiReviewCodeTestDemo1 {
	public static void main(String[] args) {
        // 示例1：访问空对象的方法
        String str = null;
        try {
            System.out.println(str.length());
        } catch (NullPointerException e) {
            System.out.println("捕获到空指针异常: " + e.getMessage());
        }
        
        // 示例2：访问空对象的属性
        MyClass obj = null;
        try {
            System.out.println(obj.toString());
        } catch (NullPointerException e) {
            System.out.println("捕获到空指针异常: " + e.getMessage());
        }
        
        // 示例3：数组为null时访问元素
        int[] arr = null;
        try {
            System.out.println(arr[0]);
        } catch (NullPointerException e) {
            System.out.println("捕获到空指针异常: " + e.getMessage());
        }
        
        // 示例4：调用null字符串的方法
        String nullStr = null;
        try {
            int len = nullStr.length();
        } catch (NullPointerException e) {
            System.out.println("捕获到空指针异常: " + e.getMessage());
        }
        
        System.out.println("程序执行完毕");
    }
    
    static class MyClass {
        public String name = "TestClass";
        
        @Override
        public String toString() {
            return "MyClass{name='" + name + "'";
        }
    }
}