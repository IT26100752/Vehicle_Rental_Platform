package user.repository;

import com.vehiclerental.admin.entity.AdminUser;
import com.vehiclerental.common.repository.TableMapping;
import com.vehiclerental.user.entity.CustomerUser;
import com.vehiclerental.user.entity.User;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Table layout of users.json's database twin:
 *
 *   users(id, type, full_name, username, password_hash, email, phone,
 *         nic, address, premium,            -- customer columns
 *         department, permission_level)     -- admin columns
 *
 * The "type" column plays the same role as the JSON type-id: it tells the
 * mapper which subclass to instantiate (INHERITANCE survives the round trip).
 */
public class UserMapping implements TableMapping<User> {

    @Override
    public String table() {
        return "users";
    }

    @Override
    public String columns() {
        return "id,type,full_name,username,password_hash,email,phone,"
                + "nic,address,premium,department,permission_level";
    }

    @Override
    public User map(ResultSet rs) throws SQLException {
        String type = rs.getString("type");
        if ("ADMIN".equals(type)) {
            return new AdminUser(
                    rs.getInt("id"),
                    rs.getString("full_name"),
                    rs.getString("username"),
                    rs.getString("password_hash"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("department"),
                    rs.getInt("permission_level"));
        }
        return new CustomerUser(
                rs.getInt("id"),
                rs.getString("full_name"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("nic"),
                rs.getString("address"),
                rs.getBoolean("premium"));
    }

    @Override
    public Object[] args(User u) {
        String type = u instanceof AdminUser ? "ADMIN" : "CUSTOMER";
        String nic = null, address = null;
        Boolean premium = null;
        String department = null;
        Integer permissionLevel = null;
        if (u instanceof CustomerUser c) {
            nic = c.getNic();
            address = c.getAddress();
            premium = c.isPremium();
        } else if (u instanceof AdminUser a) {
            department = a.getDepartment();
            permissionLevel = a.getPermissionLevel();
        }
        return new Object[]{u.getId(), type, u.getFullName(), u.getUsername(),
                u.getPasswordHash(), u.getEmail(), u.getPhone(),
                nic, address, premium, department, permissionLevel};
    }
}
