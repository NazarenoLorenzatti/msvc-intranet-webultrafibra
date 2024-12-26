package ar.com.ultrafibra.intranet.dao.impl;

import ar.com.ultrafibra.intranet.dao.iUserPortalWebDao;
import ar.com.ultrafibra.intranet.entities.UserPortalWeb;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.stereotype.Service;

@Service
@Data
public class UserPortalWebDaoImpl implements iUserPortalWebDao {

    private static final String URL = "jdbc:mysql://localhost:3306/msvc_web_ultrafibra?zeroDateTimeBehavior=CONVERT_TO_NULL&useSSL=false&useTimezone=true&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Ser15$#Ul@";

    public boolean addUser(UserPortalWeb userWeb) {
        String query = "INSERT INTO USERS (identity_number, password, verified, email) VALUES (?, ?, ?, ?)";
        try ( Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);  PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, userWeb.getIdentityNumber());
            preparedStatement.setString(2, userWeb.getPassword());
            preparedStatement.setBoolean(3, userWeb.isVerified());
            preparedStatement.setString(4, userWeb.getEmail());
            return preparedStatement.executeUpdate() > 0; // Retorna true si se insertó al menos una fila
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<UserPortalWeb> getAllUsers() {
        List<UserPortalWeb> users = new ArrayList<>();
        String query = "SELECT id, identity_number, password, verified, email FROM USERS";

        try ( Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);  PreparedStatement preparedStatement = connection.prepareStatement(query);  ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                UserPortalWeb userWeb = new UserPortalWeb();
                userWeb.setId(resultSet.getLong("id"));
                userWeb.setIdentityNumber(resultSet.getString("identity_number"));
                userWeb.setPassword(resultSet.getString("password"));
                userWeb.setVerified(resultSet.getBoolean("verified"));
                userWeb.setEmail(resultSet.getString("email"));
                users.add(userWeb);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public UserPortalWeb getUserById(Long id) {
        String query = "SELECT id, identity_number, password, verified, email FROM USERS WHERE id = ?";

        try ( Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);  PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                UserPortalWeb userWeb = new UserPortalWeb();
                userWeb.setId(resultSet.getLong("id"));
                userWeb.setIdentityNumber(resultSet.getString("identity_number"));
                userWeb.setPassword(resultSet.getString("password"));
                userWeb.setVerified(resultSet.getBoolean("verified"));
                userWeb.setEmail(resultSet.getString("email"));
                return userWeb;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public UserPortalWeb getUserByIdentityNumber(String identityNumber) {
        String query = "SELECT id, identity_number, password, verified, email FROM USERS WHERE identity_number = ?";

        try ( Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);  PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, identityNumber);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                UserPortalWeb userWeb = new UserPortalWeb();
                userWeb.setId(resultSet.getLong("id"));
                userWeb.setIdentityNumber(resultSet.getString("identity_number"));
                userWeb.setPassword(resultSet.getString("password"));
                userWeb.setVerified(resultSet.getBoolean("verified"));
                userWeb.setEmail(resultSet.getString("email"));
                return userWeb;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateUser(UserPortalWeb userWeb) {
        String query = "UPDATE USERS SET identity_number = ?, password = ?, verified = ?, email = ? WHERE id = ?";

        try ( Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);  PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, userWeb.getIdentityNumber());
            preparedStatement.setString(2, userWeb.getPassword());
            preparedStatement.setBoolean(3, userWeb.isVerified());
            preparedStatement.setString(4, userWeb.getEmail());
            preparedStatement.setLong(5, userWeb.getId());
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteUserById(Long id) {
        String query = "DELETE FROM USERS WHERE id = ?";

        try ( Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);  PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setLong(1, id);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
