package com.artifinery;

import java.sql.*;


 class SQLiteDB {
    private Connection connection;
    private Statement statement;
    private PreparedStatement preparedStatement;
    private long batchCounter;
    private int inlineCounter;

    SQLiteDB(String dbpath) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:"+dbpath);
        statement = connection.createStatement();
        statement.setQueryTimeout(1);
        statement.executeUpdate("PRAGMA synchronous = 0;");
        statement.executeQuery("PRAGMA journal_mode = OFF;");
        connection.setAutoCommit(false);
        preparedStatement = connection.prepareStatement("insert into elements(id, type, ref, lat, lon, name, nameen, street, housenumber) values(?, ?, ?, ?, ?, ?, ?, ?, ?)");
        batchCounter = 0;
        inlineCounter = 0;
    }

    void recreateTableElements() throws SQLException {
        statement.executeUpdate("drop table if exists elements");
        statement.executeUpdate("create table elements (id integer, type text, ref text, lat real, lon real, name text, nameen text, street text, housenumber text)");
    }

    void writeElement(Element element) throws SQLException {
        if (element.getId()==0) {
            preparedStatement.setBigDecimal(1, null);
        } else {
            preparedStatement.setLong(1, element.getId());
        }
        if(element.getType().equals("")) {
            preparedStatement.setString(2, null);
        } else {
            preparedStatement.setString(2, element.getType());
        }
        if(element.getReferences().equals("")) {
            preparedStatement.setString(3, null);
        } else {
            preparedStatement.setString(3, element.getReferences());
        }
        if (element.getLat()==0) {
            preparedStatement.setBigDecimal(4, null);
        } else {
            preparedStatement.setDouble(4, element.getLat());
        }
        if (element.getLon()==0) {
            preparedStatement.setBigDecimal(5, null);
        } else {
            preparedStatement.setDouble(5, element.getLon());
        }
        if(element.getName().equals("")) {
            preparedStatement.setString(6, null);
        } else {
            preparedStatement.setString(6, element.getName());
        }
        if(element.getEnglishName().equals("")) {
            preparedStatement.setString(7, null);
        } else {
            preparedStatement.setString(7, element.getEnglishName());
        }
        if(element.getStreet().equals("")) {
            preparedStatement.setString(8, null);
        } else {
            preparedStatement.setString(8, element.getStreet());
        }
        if(element.getHouseNumber().equals("")) {
            preparedStatement.setString(9, null);
        } else {
            preparedStatement.setString(9, element.getHouseNumber());
        }
        preparedStatement.addBatch();
        batchCounter++;
        if (batchCounter%5000==0) {
            preparedStatement.executeBatch();
            connection.commit();
            System.out.print("#");
            inlineCounter++;
            if(inlineCounter>=100) {
                System.out.println(" " + batchCounter);
                inlineCounter = 0;
            }
        }
    }

    void lastCommit() throws SQLException {
        if (batchCounter%5000>0) {
            preparedStatement.executeBatch();
            connection.commit();
            System.out.println(" " + batchCounter);
        }
    }

    void close() throws SQLException {
        if(connection != null) connection.close();
    }


}
