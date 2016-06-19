package com.artifinery;

import java.sql.*;

class SQLiteDB {
    private Connection connection;
    private PreparedStatement preStatementAddress;
    private long batchCounter;
    private int inlineCounter;

    SQLiteDB(String dbpath) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:"+dbpath);
        Statement statement = connection.createStatement();
        statement.executeUpdate("PRAGMA synchronous = 0;");
        statement.executeQuery("PRAGMA journal_mode = OFF;");
        statement.executeUpdate("drop table if exists addresses");
        statement.executeUpdate("create table addresses (lat real, lon real, name text, addr text)");
        statement.close();
        connection.setAutoCommit(false);
        preStatementAddress = connection.prepareStatement("insert into addresses(lat, lon, name, addr) values(?, ?, ?, ?)");
        batchCounter = 0;
        inlineCounter = 0;
    }

    void writeAddress(Element element) throws SQLException {
        preStatementAddress.setDouble(1, element.getLat());
        preStatementAddress.setDouble(2, element.getLon());
        preStatementAddress.setString(3, element.getName());
        preStatementAddress.setString(4, element.getAddress());
        preStatementAddress.addBatch();
        batchCounter++;
        if (batchCounter%1000==0) {
            preStatementAddress.executeBatch();
            connection.commit();
            System.out.print("#");
            inlineCounter++;
            if(inlineCounter>=10) {
                System.out.println(" " + batchCounter);
                inlineCounter = 0;
            }
        }
    }

    void lastCommitAddress() throws SQLException {
        if (batchCounter%1000>0) {
            preStatementAddress.executeBatch();
            connection.commit();
            System.out.println(" " + batchCounter);
        }
        if(connection != null) connection.close();
    }

}
