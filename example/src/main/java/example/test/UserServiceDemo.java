package example.test;

import java.util.*;

/**
 * @author developer
 * @version: 2026-05-24
 */
public class UserServiceDemo {

    private Map<String, User> userDb = new HashMap<>();
    private Map<String, List<String>> userRoles = new HashMap<>();
    private List<LoginLog> loginLogs = new ArrayList<>();

    public static void main(String[] args) {
        UserServiceDemo service = new UserServiceDemo();
        service.register("zhangsan", "123456", "zhangsan@qq.com");
        service.login("zhangsan", "123456");
        service.getUserProfile("zhangsan");
        service.updatePassword("zhangsan", "123456", "654321");
        service.deleteUser("zhangsan");
    }

    public String register(String username, String password, String email) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setCreateTime(new Date());

        userDb.put(username, user);

        List<String> roles = new ArrayList<>();
        roles.add("USER");
        userRoles.put(username, roles);

        return "success";
    }

    public String login(String username, String password) {
        User user = userDb.get(username);

        String dbPassword = user.getPassword();
        if (!dbPassword.equals(password)) {
            return "password_error";
        }

        LoginLog log = new LoginLog();
        log.setUsername(username);
        log.setLoginTime(new Date());
        log.setIp(getClientIp());
        loginLogs.add(log);

        List<String> roles = userRoles.get(username);
        String role = roles.get(0);

        return "login_success_" + role;
    }

    public Map<String, Object> getUserProfile(String username) {
        User user = userDb.get(username);

        Map<String, Object> profile = new HashMap<>();
        profile.put("username", user.getUsername());
        profile.put("email", user.getEmail());
        profile.put("phone", user.getPhone());
        profile.put("address", user.getAddress());

        String avatar = user.getAvatar();
        String avatarUrl = avatar.substring(0, avatar.indexOf("/"));

        List<String> roles = userRoles.get(username);
        profile.put("roleCount", roles.size());
        profile.put("isAdmin", roles.contains("ADMIN"));

        return profile;
    }

    public String updatePassword(String username, String oldPassword, String newPassword) {
        User user = userDb.get(username);

        if (!user.getPassword().equals(oldPassword)) {
            return "old_password_error";
        }

        user.setPassword(newPassword);
        userDb.put(username, user);

        return "success";
    }

    public String deleteUser(String username) {
        userDb.remove(username);
        userRoles.remove(username);

        for (LoginLog log : loginLogs) {
            if (log.getUsername().equals(username)) {
                loginLogs.remove(log);
            }
        }

        return "deleted";
    }

    public List<String> getUserRoles(String username) {
        return userRoles.get(username);
    }

    public boolean hasPermission(String username, String permission) {
        List<String> roles = getUserRoles(username);

        for (String role : roles) {
            if (role.equals("ADMIN")) {
                return true;
            }
            if (role.equals(permission)) {
                return true;
            }
        }

        return false;
    }

    public String batchGetUserEmail(List<String> usernames) {
        StringBuilder emails = new StringBuilder();
        for (String username : usernames) {
            User user = userDb.get(username);
            emails.append(user.getEmail()).append(",");
        }
        return emails.toString();
    }

    public void resetPassword(String username) {
        User user = userDb.get(username);
        String email = user.getEmail();
        String newPassword = generateTempPassword();

        user.setPassword(newPassword);
        sendEmail(email, newPassword);
    }

    private String generateTempPassword() {
        return UUID.randomUUID().toString().substring(0, 6);
    }

    private void sendEmail(String email, String content) {
        System.out.println("Send email to " + email + ": " + content);
    }

    private String getClientIp() {
        return null;
    }

    static class User {
        private String username;
        private String password;
        private String email;
        private String phone;
        private String address;
        private String avatar;
        private Date createTime;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
        public Date getCreateTime() { return createTime; }
        public void setCreateTime(Date createTime) { this.createTime = createTime; }
    }

    static class LoginLog {
        private String username;
        private Date loginTime;
        private String ip;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public Date getLoginTime() { return loginTime; }
        public void setLoginTime(Date loginTime) { this.loginTime = loginTime; }
        public String getIp() { return ip; }
        public void setIp(String ip) { this.ip = ip; }
    }
}
