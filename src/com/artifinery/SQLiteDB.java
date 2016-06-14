package com.artifinery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;


class SQLiteDB {
    private Connection connection;
    private PreparedStatement preStatementElement;
    private PreparedStatement preStatementAddress;
    private long batchCounter;
    private int inlineCounter;

    SQLiteDB(String dbpath) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:"+dbpath);
        Statement statement = connection.createStatement();
        statement.executeUpdate("PRAGMA synchronous = 0;");
        statement.executeQuery("PRAGMA journal_mode = OFF;");
        statement.close();
        connection.setAutoCommit(false);
        batchCounter = 0;
        inlineCounter = 0;
    }

    void prepareStatementElement() throws SQLException {
        preStatementElement = connection.prepareStatement("insert into elements(id, type, ref, lat, lon, name, nameen, street, housenumber) values(?, ?, ?, ?, ?, ?, ?, ?, ?)");
    }

    void prepareStatementAddress() throws SQLException {
        preStatementAddress = connection.prepareStatement("insert into addresses(lat, lon, name, addr) values(?, ?, ?, ?)");
    }

    ResultSet query(String queryString) throws SQLException {
        Statement statement = connection.createStatement();
        return statement.executeQuery(queryString);
    }

    void update(String queryString) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(queryString);
    }

    Element readElement(String id) throws SQLException {
        Statement statement = connection.createStatement();
        statement.closeOnCompletion();
        ResultSet result = statement.executeQuery("select * from elements where id = " + id);
        result.next();
        return new Element(result.getLong("id"),result.getString("type"),result.getString("ref"),result.getDouble("lat"),result.getDouble("lon"),result.getString("name"),result.getString("nameen"),result.getString("street"),result.getString("housenumber"));
    }

    ArrayList<Element> readAddresses() throws SQLException {
        ArrayList<Element> addresses = new ArrayList<>();
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery("select * from elements where street is not null and housenumber is not null");
        while (result.next()) {
            addresses.add(new Element(result.getLong("id"),result.getString("type"),result.getString("ref"),result.getDouble("lat"),result.getDouble("lon"),result.getString("name"),result.getString("nameen"),result.getString("street"),result.getString("housenumber")));
        }
        statement.close();
        return addresses;
    }

    void recreateTableAddresses() throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate("drop table if exists addresses");
        statement.executeUpdate("create table addresses (lat real, lon real, name text, addr text)");
        statement.close();
    }

    void recreateTableElements() throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate("drop table if exists elements");
        statement.executeUpdate("create table elements (id integer, type text, ref text, lat real, lon real, name text, nameen text, street text, housenumber text)");
        statement.close();
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

    void writeElement(Element element) throws SQLException {
        preStatementElement.setLong(1, element.getId());
        if (element.getType().equals("")) {
            preStatementElement.setString(2, null);
        } else {
            preStatementElement.setString(2, element.getType());
        }
        if (element.getReferences().equals("")) {
            preStatementElement.setString(3, null);
        } else {
            preStatementElement.setString(3, element.getReferences());
        }
        if (element.getLat() == 0.0) {
            preStatementElement.setBigDecimal(4, null);
        } else {
            preStatementElement.setDouble(4, element.getLat());
        }
        if (element.getLon() == 0.0) {
            preStatementElement.setBigDecimal(5, null);
        } else {
            preStatementElement.setDouble(5, element.getLon());
        }
        if (element.getName().equals("")) {
            preStatementElement.setString(6, null);
        } else {
            preStatementElement.setString(6, element.getName());
        }
        if (element.getEnglishName().equals("")) {
            preStatementElement.setString(7, null);
        } else {
            preStatementElement.setString(7, element.getEnglishName());
        }
        if (element.getStreet().equals("")) {
            preStatementElement.setString(8, null);
        } else {
            preStatementElement.setString(8, element.getStreet());
        }
        if (element.getHouseNumber().equals("")) {
            preStatementElement.setString(9, null);
        } else {
            preStatementElement.setString(9, element.getHouseNumber());
        }
        preStatementElement.addBatch();
        batchCounter++;
        if (batchCounter % 5000 == 0) {
            preStatementElement.executeBatch();
            connection.commit();
            System.out.print("#");
            inlineCounter++;
            if (inlineCounter >= 100) {
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
    }

    void lastCommitElement() throws SQLException {
        if (batchCounter%5000>0) {
            preStatementElement.executeBatch();
            connection.commit();
            System.out.println(" " + batchCounter);
        }
    }

    void close() throws SQLException {
        if(connection != null) connection.close();
    }


}
