package com.artifinery;

public class Main {

    private final static String PATH = "/home/artifiner/data/RU-MOW.osm";
    private final static String DB = "/home/artifiner/data/moscow.db";

    public static void main(String[] args) {
        SQLiteDB db = new SQLiteDB(DB);
        db.writeNode(23133423, 50.1, 30.2);
        db.addTag("name");
        db.addTag("name");
        db.close();
    }
}
