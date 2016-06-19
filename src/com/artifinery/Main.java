package com.artifinery;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;

public class Main {

    public static void main(String[] args) {
        if(args.length>0) {
            switch (args[0]) {
                case "-analyse":
                    if(args.length>1) {
                        analyse(args[1]);
                    } else {
                        System.out.println("Filename required. See information below.");
                        info();
                    }
                    break;
                case "-parse":
                    if(args.length>2) {
                        try {
                            saveAddresses(args[2], parseXML(args[1]));
                        } catch (javax.xml.stream.XMLStreamException e) {
                            System.out.println("XML error. "+e);
                        } catch (FileNotFoundException e) {
                            System.out.println("File not found. "+e);
                        } catch (SQLException e) {
                            System.out.println("Database error. "+e);
                        }
                    } else {
                        System.out.println("Filename and database name required. See information below.");
                        info();
                    }
                    break;
                default:
                    info();
                    break;
            }
        }
    }

    private static Coordinate calculateCoordinates(Element element, ElementMap map) throws SQLException {
        Coordinate coord = new Coordinate();
        if ((element.getType().equals(OSM.NODE))||(element.getType().equals(""))) {
            coord.lat = element.getLat();
            coord.lon = element.getLon();
        } else {
            int i = 0;
            for(Reference reference : element.getReferences()) {
                Coordinate currentCoord = calculateCoordinates(map.get(reference),map);
                if((currentCoord.lat>0)&&(currentCoord.lon>0)) {
                    coord.lat += currentCoord.lat;
                    coord.lon += currentCoord.lon;
                    i++;
                } else System.out.println("Lost element.");
            }
            coord.lat = coord.lat/i;
            coord.lon = coord.lon/i;
        }
        return coord;
    }

    private static void saveAddresses(String database, ElementMap map) throws SQLException {
        System.out.println("Opening database: " + database);
        SQLiteDB dbout = new SQLiteDB(database);
        for(Element element : map.addresses) {
            if(!element.getType().equals(OSM.NODE)) {
                Coordinate coord = calculateCoordinates(element, map);
                element.setCoordinates(coord.lat,coord.lon);
            }
            dbout.writeAddress(element);
        }
        dbout.lastCommitAddress();
    }

    private static ElementMap parseXML(String path) throws FileNotFoundException,XMLStreamException {
        System.out.println("Opening file: " + path);
        InputStream input = new FileInputStream(path);
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLEventReader reader = inputFactory.createXMLEventReader(input);
        System.out.println("Parsing...");
        ElementMap map = new ElementMap();
        Element element = new Element();
        while (reader.hasNext()) {
            XMLEvent e = reader.nextEvent();
            switch (e.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    StartElement se = e.asStartElement();
                    switch (se.getName().toString()) {
                        case OSM.NODE:
                        case OSM.WAY:
                        case OSM.RELATION:
                            element = new Element(se);
                            break;
                        case OSM.ND:
                        case OSM.MEMBER:
                            element.addReferences(se);
                            break;
                        case OSM.TAG:
                            element.setTag(se);
                            break;
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    switch (e.asEndElement().getName().toString()) {
                        case OSM.NODE:
                        case OSM.WAY:
                        case OSM.RELATION:
                            map.add(element);
                            break;
                    }
                    break;
            }
        }
        reader.close();
        return map;
    }

    private static void info() {
        System.out.println("OSMparser usage information:");
        System.out.println("-analyse FILENAME.osm");
        System.out.println("    will analyse the Open Street Map XML to give some information about it.");
        System.out.println("-parse FILENAME.osm OUTPUT_DATABASE");
        System.out.println("    will parse the Open Street Map XML and save all results into SQLite database. Database should exist.");
    }

    private static void analyse(String path) {
        try {
            ArrayList<String> trace = new ArrayList<>();
            TextWithCountersList tracesList = new TextWithCountersList();
            TextWithCountersList tracesWithCoordinatesList = new TextWithCountersList();
            TextWithCountersList tracesWithNamesList = new TextWithCountersList();
            TextWithCountersList tracesWithStreetsList = new TextWithCountersList();
            TextWithCountersList tracesWithHousesList = new TextWithCountersList();
            TextWithCountersList kValues = new TextWithCountersList();
            TextWithCountersList attrNode = new TextWithCountersList();
            TextWithCountersList attrWay = new TextWithCountersList();
            TextWithCountersList attrNd = new TextWithCountersList();
            TextWithCountersList attrRelation = new TextWithCountersList();
            TextWithCountersList attrTag = new TextWithCountersList();
            TextWithCountersList attrMember = new TextWithCountersList();
            TextWithCountersList memberTypes = new TextWithCountersList();
            TextWithCountersList memberRoles = new TextWithCountersList();
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            System.out.println("Opening file: " + path);
            InputStream input = new FileInputStream(path);
            XMLEventReader reader = inputFactory.createXMLEventReader(input);
            System.out.println("Processing...");
            long start = System.nanoTime();
            while (reader.hasNext()) {
                XMLEvent e = reader.nextEvent();
                switch (e.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        StartElement se = e.asStartElement();
                        String s = se.getName().toString();
                        trace.add(s);
                        String traceString = String.join("/",trace);
                        tracesList.add(traceString);
                        Iterator iter = se.getAttributes();
                        switch (s) {
                            case OSM.NODE:
                                while(iter.hasNext()) attrNode.add(((Attribute)iter.next()).getName().toString());
                                break;
                            case OSM.WAY:
                                while(iter.hasNext()) attrWay.add(((Attribute)iter.next()).getName().toString());
                                break;
                            case OSM.ND:
                                while(iter.hasNext()) attrNd.add(((Attribute)iter.next()).getName().toString());
                                break;
                            case OSM.RELATION:
                                while(iter.hasNext()) attrRelation.add(((Attribute)iter.next()).getName().toString());
                                break;
                            case OSM.TAG:
                                while(iter.hasNext()) attrTag.add(((Attribute)iter.next()).getName().toString());
                                break;
                            case OSM.MEMBER:
                                while(iter.hasNext()) {
                                    Attribute attribute = (Attribute)iter.next();
                                    String aName = attribute.getName().toString();
                                    attrMember.add(aName);
                                    if(aName.equals(OSM.MEMBER_TYPE)) {
                                        memberTypes.add(attribute.getValue());
                                    }
                                    if(aName.equals(OSM.MEMBER_ROLE)) {
                                        memberRoles.add(attribute.getValue());
                                    }
                                }
                                break;
                        }
                        if (se.getAttributeByName(QName.valueOf(OSM.NODE_LATITUDE))!=null) tracesWithCoordinatesList.add(traceString);
                        if (se.getAttributeByName(QName.valueOf(OSM.TAG_KEY))!=null) {
                            kValues.add(se.getAttributeByName(QName.valueOf(OSM.TAG_KEY)).getValue());
                            if (se.getAttributeByName(QName.valueOf(OSM.TAG_KEY)).getValue().equals(OSM.TAG_NAME)) tracesWithNamesList.add(traceString);
                            if (se.getAttributeByName(QName.valueOf(OSM.TAG_KEY)).getValue().equals(OSM.TAG_STREET)) tracesWithStreetsList.add(traceString);
                            if (se.getAttributeByName(QName.valueOf(OSM.TAG_KEY)).getValue().equals(OSM.TAG_HOUSENUMBER)) tracesWithHousesList.add(traceString);
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        trace.remove(trace.size()-1);
                        break;
                }

            }
            long finish = System.nanoTime();
            double time = (double)(finish-start)/1000000000;
            DecimalFormat formatter = new DecimalFormat("0.##");
            tracesList.print(" elements processed in " + formatter.format(time) + " seconds (" + formatter.format(time*1000000/tracesList.getTotal()) + " microseconds/element). Elements structure:", true);
            tracesWithCoordinatesList.print(" with coordinates:", true);
            tracesWithNamesList.print(" with name:", true);
            tracesWithStreetsList.print(" with addr:street:", true);
            tracesWithHousesList.print(" with addr:housenumber:", true);
            attrNode.print("<node> attributes:", false);
            attrNd.print("<nd> attributes:", false);
            attrWay.print("<way> attributes:", false);
            attrRelation.print("<relation> attributes:", false);
            attrTag.print("<tag> attributes:", false);
            attrMember.print("<member> attributes:", false);
            memberTypes.print(" <member> types:", true);
            memberRoles.sortDescending();
            memberRoles.print(" <member> roles:", true);
            kValues.sortDescending();
            kValues.print(" <tag> keys found:", true);
            reader.close();
        } catch (javax.xml.stream.XMLStreamException e) {
            System.out.println("XML error. "+e);
        } catch (FileNotFoundException e) {
            System.out.println("File not found. "+e);
        }
    }
}
