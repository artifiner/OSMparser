package com.artifinery;

class Element {
    private long id;
    private String type;
    private String references;
    private Coordinate coord;
    private String name;
    private String nameen;
    private String street;
    private String housenumber;

    void newElement(long id, String type) {
        this.setId(id);
        this.setType(type);
        references="";
        coord = new Coordinate();
        name="";
        nameen="";
        street="";
        housenumber="";

    }

    Element() {
        coord = new Coordinate();
    }

    void setCoordinates(double lat, double lon) {
        coord.lat = lat;
        coord.lon = lon;
    }

    long getId() {
        return id;
    }

    void setId(long id) {
        this.id = id;
    }

    String getType() {
        return type;
    }

    void setType(String type) {
        this.type = type;
    }

    String getReferences() {
        return references;
    }

    void addReferences(String reference) {
        this.references = this.references.equals("") ? reference : this.references + "," + reference;
    }

    double getLat() {
        return coord.lat;
    }

    double getLon() {
        return coord.lon;
    }

    Coordinate getCoordinate() {
        return coord;
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    String getEnglishName() {
        return nameen;
    }

    void setEnglishName(String nameen) {
        this.nameen = nameen;
    }

    String getStreet() {
        return street;
    }

    void setStreet(String street) {
        this.street = street;
    }

    String getHouseNumber() {
        return housenumber;
    }

    void setHouseNumber(String housenumber) {
        this.housenumber = housenumber;
    }

    String getAddress() {
        return getStreet() + ", " + getHouseNumber();
    }
}
