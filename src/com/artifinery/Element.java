package com.artifinery;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import java.util.ArrayList;

class Element {
    private long id;
    private String type;
    private ArrayList<Reference> references;
    private Coordinate coord;
    private String name;
    private String nameen;
    private String street;
    private String housenumber;

    Element(StartElement se) {
        setId(Long.parseLong(se.getAttributeByName(QName.valueOf(OSM.ID)).getValue()));
        setType(se.getName().toString());
        references = new ArrayList<>();
        coord = new Coordinate();
        if(getType().equals(OSM.NODE)) setCoordinates(se.getAttributeByName(QName.valueOf(OSM.NODE_LATITUDE)).getValue(),se.getAttributeByName(QName.valueOf(OSM.NODE_LONGITUDE)).getValue());
        setName("");
        setEnglishName("");
        setStreet("");
        setHouseNumber("");
    }

    Element() {
        setId(0);
        setType("");
        references = new ArrayList<>();
        coord = new Coordinate();
        setName("");
        setEnglishName("");
        setStreet("");
        setHouseNumber("");
    }

    void setCoordinates(double lat, double lon) {
        coord.lat = lat;
        coord.lon = lon;
    }

    void setCoordinates(String lat, String lon) {
        setCoordinates(Double.parseDouble(lat), Double.parseDouble(lon));
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

    ArrayList<Reference> getReferences() {
        return references;
    }

    void addReferences(StartElement se) {
            this.references.add(new Reference(se.getName().toString().equals(OSM.ND) ? OSM.NODE : se.getAttributeByName(QName.valueOf(OSM.MEMBER_TYPE)).getValue(), se.getAttributeByName(QName.valueOf(OSM.REFERENCE)).getValue()));
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

    void setTag(StartElement se) {
        String value = se.getAttributeByName(QName.valueOf(OSM.TAG_VALUE)).getValue();
        switch (se.getAttributeByName(QName.valueOf(OSM.TAG_KEY)).getValue()) {
            case OSM.TAG_NAME:
                setName(value);
                break;
            case OSM.TAG_ENGLISH_NAME:
                setEnglishName(value);
                break;
            case OSM.TAG_STREET:
                setStreet(value);
                break;
            case OSM.TAG_HOUSENUMBER:
                setHouseNumber(value);
                break;
        }
    }

    boolean hasAddress() {
        return (!getStreet().equals(""))&&(!getHouseNumber().equals(""));
    }

    String getAddress() {
        return getStreet() + ", " + getHouseNumber();
    }

}
