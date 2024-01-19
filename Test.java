
import java.sql.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        String host = "jdbc:mysql://localhost:3306/mydata";
        String username = "root";
        
        try (Connection con = DriverManager.getConnection(host, username, password);
             Statement stmt = con.createStatement()) {

            String insertQuery = "INSERT INTO STUDENT VALUES('anu', 10)";
            stmt.execute(insertQuery);

            System.out.println("Query executed successfully");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}



