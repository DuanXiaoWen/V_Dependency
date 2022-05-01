package util;

import java.sql.*;

public final  class SQLiteUtils {


    public static Connection connectDB(String url) {
        Connection c = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:" + url);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Open database"+url+" successfully");
        return c;
    }
    public static void closeResource(Connection conn, Statement st) {
        try {
            if (st!=null){
                st.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void closeResource(Connection conn, Statement st, ResultSet rs) {
        closeResource(conn, st);
        try {
            if (rs!=null)
                rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
