package com.artifinery;

import java.sql.*;


public class SQLiteDB {
    private Connection connection;
    private Statement statement;

    public SQLiteDB(String dbpath) {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:"+dbpath);
            statement = connection.createStatement();
            statement.setQueryTimeout(1);
        } catch (SQLException e) {
            System.out.println("Cannot connect SQLite database. " + e);
            System.exit(1);
        }
    }

    public boolean writeNode(long id, double lat, double lon) {
        try {
            statement.executeUpdate("insert into nodes(id, lat, lon) values(" + id + ", " + lat + ", " + lon + ")");
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean addTag(String tag) {
        try {
            ResultSet rs = statement.executeQuery("select count(tag) from taglist where tag='" + tag + "'");

            int count = rs.next() ? rs.getInt(1) : 0;
            if (count > 0) {
                statement.executeUpdate("update taglist set cnt=cnt+1 where tag='" + tag + "'");
            } else {
                statement.executeUpdate("insert into taglist(tag, cnt) values('" + tag + "', 1)");
            }
            return true;
        }catch (SQLException e) {
            return false;
        }
    }

    public void close() {
        try {
            if(connection != null)
                connection.close();
        } catch (Exception e) {
            System.out.println("Cannot close SQLite database. "+e);
            System.exit(1);
        }
    }


}
