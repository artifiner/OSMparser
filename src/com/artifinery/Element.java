package com.artifinery;

class Element {
    private int id;
    private String type;
    private String references;
    private Coordinate coord;
    private String name;
    private String nameen;
    private String street;
    private String housenumber;

    void newElement(long id, String type) {
        setId(id);
        setType(type);
        references="";
        coord = new Coordinate();
        setName("");
        setEnglishName("");
        setStreet("");
        setHouseNumber("");
    }

    void newElement(String id, String type) {
        setId(Long.parseLong(id));
        setType(type);
        references="";
        coord = new Coordinate();
        setName("");
        setEnglishName("");
        setStreet("");
        setHouseNumber("");
    }

    Element() {
        coord = new Coordinate();
    }

    Element(long id, String type, String references, double lat, double lon, String name, String nameen, String street, String housenumber) {
        setId(id);
        setType(type);
        this.references=references;
        this.coord = new Coordinate();
        setCoordinates(lat, lon);
        setName(name);
        setEnglishName(nameen);
        setStreet(street);
        setHouseNumber(housenumber);
    }

    void setCoordinates(double lat, double lon) {
        coord.lat = lat;
        coord.lon = lon;
    }

    void setCoordinates(String lat, String lon) {
        coord.lat = Double.parseDouble(lat);
        coord.lon = Double.parseDouble(lon);
    }

    int getId() {
        return id;
    }

    void setId(long id) {
        this.id = (int)(id+Integer.MIN_VALUE);
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
        this.references = this.references.equals("") ? "" + (Long.parseLong(reference)+Integer.MIN_VALUE) : this.references + "," + (Long.parseLong(reference)+Integer.MIN_VALUE);
    }

    double getLat() {
        return coord.lat;
    }

    double getLon() {
        return coord.lon;
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

    boolean compare(Element element) {
        return (getId()==element.getId())&&(getType().equals(element.getType()))&&(getReferences().equals(element.getReferences()))&&(getLat()==element.getLat())&&(getLon()==element.getLon())&&
                (getName().equals(element.getName()))&&(getEnglishName().equals(element.getEnglishName()))&&(getStreet().equals(element.getStreet()))&&(getHouseNumber().equals(element.getHouseNumber()));
    }

    @Override
    public String toString() {
        return "ID:" + getId() + " T:" + getType() + " lat:" + getLat() + " lon:" + getLon() + " N:" + getName() + " S:" + getStreet() + " H:" + getHouseNumber() + " R:" + getReferences();
    }
}
