package store;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import messages.LoginMessage;
import messages.RegisterMessage;

public class SQLiteJDBC {
    private final String databaseName;
    private Connection conn;
    
    public SQLiteJDBC (String databaseName) {
        this.databaseName = databaseName;
        this.conn = null;
    }
    
    public synchronized boolean insertUser(RegisterMessage msg) throws SQLException {
        if (findUser(msg.getUsername())) {
            return false;
        } else {            
            Statement statement = conn.createStatement();
            String sql = "INSERT INTO users (id, password) VALUES ('" + msg.getUsername() + "','" + msg.getPassword() + "');";
            System.out.println("Executing: " + sql);
            
            statement.executeUpdate(sql);           
           
            conn.commit();
            statement.close();
            return true;
        }
    }
    
    public synchronized boolean checkPassword(LoginMessage msg) {
        try {
            Statement statement = conn.createStatement();
            String sql = "SELECT * FROM users WHERE id = '" + msg.getUsername() + "';";                
            
            System.out.println("Executing: " + sql);
            
            ResultSet rSet = statement.executeQuery(sql);
            
            if (rSet.getFetchSize() == 1) {
                String password = rSet.getString("password");
                if (password.equals(msg.getPassword())) {
                    return true;
                } else {
                    return false;
                }
            }
            
            rSet.close();
            statement.close();
             
             return true;
        } catch (SQLException ex) {
            System.err.println(ex);
            return false;
        }
    }
    
    public synchronized boolean findUser(String username) {
        boolean retValue = false;
        try {
            Statement statement = conn.createStatement();
            String sql = "SELECT * FROM users WHERE id = '" + username + "';";
            
            System.out.println("Executing: " + sql);
            
            ResultSet rSet = statement.executeQuery(sql);
            if (rSet.next()) {
                retValue = true;
            } else {
                retValue = false;
            }
            rSet.close();
            statement.close();        
        } catch (SQLException ex) {
            System.err.println(ex);
        }
        
        return retValue;
    }
    
    public synchronized boolean updateUser(LoginMessage msg) {
        try {
            String ip = msg.getIP();
            Integer port = msg.getPort();
            Statement statement = conn.createStatement();
            String sql = "UPDATE users set ip = '" + ip + "', port = " + port +" WHERE id = '" + msg.getUsername() + "';";
            
            System.out.println("Executing: " + sql);
            
            statement.executeUpdate(sql);       
            
            conn.commit();
            statement.close();
             
             return true;
        } catch (SQLException ex) {
            System.err.println(ex);
            return false;
        }
    } 
    
    public void connect() throws ClassNotFoundException, SQLException {
        if (this.conn == null) {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + databaseName);
            conn.setAutoCommit(false);
            System.out.println("Connected to database successfully");          
        }
    }
    
    public synchronized void close() throws SQLException {
        conn.close();
    }
    
}
