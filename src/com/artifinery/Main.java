package com.artifinery;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


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
                        parse(args[1],args[2]);
                    } else {
                        System.out.println("Filename and database name required. See information below.");
                        info();
                    }
                    break;
                case "-select":
                    if(args.length>2) {
                        select(args[1],args[2]);
                    } else {
                        System.out.println("Database names for input and output required. See information below.");
                        info();
                    }
                    break;
                default:
                    info();
                    break;
            }
        }
    }

    private static Coordinate calculateCoordinates(Element element, SQLiteDB db) throws SQLException {
        Coordinate coord = new Coordinate();
        if (element.getType().equals("node")) {
            coord.lat = element.getLat();
            coord.lon = element.getLon();
        } else {
            int i = 0;
            String[] references = element.getReferences().split(",");
            for(String reference : references) {
                Coordinate currentCoord = calculateCoordinates(db.readElement(reference),db);
                coord.lat += currentCoord.lat;
                coord.lon += currentCoord.lon;
                i++;
            }
            coord.lat = coord.lat/i;
            coord.lon = coord.lon/i;
        }
        return coord;
    }

    private static void select(String inputDB, String outputDB) {
        try {
            System.out.println("Opening database: " + inputDB);
            SQLiteDB dbin = new SQLiteDB(inputDB);
            System.out.println("Opening database: " + outputDB);
            SQLiteDB dbout = new SQLiteDB(outputDB);
            long start = System.nanoTime();
            long elementsCount = 0;
            dbout.recreateTableAddresses();
            dbout.prepareStatementAddress();
            System.out.println("Reading...");
            ArrayList<Element> elements = dbin.readAddresses();
            System.out.println("Processing...");
            for(Element element : elements) {
                if(!element.getType().equals("node")) {
                    Coordinate coord = calculateCoordinates(element, dbin);
                    element.setCoordinates(coord.lat,coord.lon);
                }
                dbout.writeAddress(element);
                elementsCount++;
            }
            dbout.lastCommitAddress();
            long finish = System.nanoTime();
            double time = (double)(finish-start)/1000000000;
            DecimalFormat formatter = new DecimalFormat("0.##");
            System.out.println("Created " + elementsCount + " database items in " + formatter.format(time) + " seconds (" + formatter.format(time*1000000/elementsCount) + " microseconds/element).");
            dbin.close();
            dbout.close();
        } catch (SQLException e) {
            System.out.println("Database error. "+e);
        }
    }

    private static void parse(String path, String database) {
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            System.out.println("Opening file: " + path);
            InputStream input = new FileInputStream(path);
            XMLEventReader reader = inputFactory.createXMLEventReader(input);
            System.out.println("Opening database: " + database);
            SQLiteDB db = new SQLiteDB(database);
            db.recreateTableElements();
            db.prepareStatementElement();
            System.out.println("Processing...");
            long start = System.nanoTime();
            Element element = new Element();
            HashMap<Integer,Element> map = new HashMap<>(4300000);
            HashMap<Integer,Element> duplicatesMap = new HashMap<>();
            long elementsCount = 0;
            long duplicatesCount = 0;
            XMLEvent e;
            StartElement se;
            String s;
            String key;
            String value;
            while (reader.hasNext()) {
                e = reader.nextEvent();
                switch (e.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT: // Start of new object. Collect the data.
                        se = e.asStartElement();
                        s = se.getName().toString();
                        switch (s) {
                            case "node":  // Create new node
                                element.newElement(se.getAttributeByName(QName.valueOf("id")).getValue(),s);
                                element.setCoordinates(se.getAttributeByName(QName.valueOf("lat")).getValue(),se.getAttributeByName(QName.valueOf("lon")).getValue());
                                break;
                            case "way":  // Create new way
                            case "relation":  // Create new relation
                                element.newElement(se.getAttributeByName(QName.valueOf("id")).getValue(),s);
                                break;
                            case "nd":  // Add node references to the current way
                                element.addReferences(se.getAttributeByName(QName.valueOf("ref")).getValue());
                                break;
                            case "member":  // Add members to the current relation
                                if (!se.getAttributeByName(QName.valueOf("role")).getValue().equals("inner")) element.addReferences(se.getAttributeByName(QName.valueOf("ref")).getValue());
                                break;
                            case "tag":  // Add information to the current element
                                key = se.getAttributeByName(QName.valueOf("k")).getValue();
                                value = se.getAttributeByName(QName.valueOf("v")).getValue();
                                switch (key) {
                                    case "name":
                                        element.setName(value);
                                        break;
                                    case "name:en":
                                        element.setEnglishName(value);
                                        break;
                                    case "addr:street":
                                        element.setStreet(value);
                                        break;
                                    case "addr:housenumber":
                                        element.setHouseNumber(value);
                                        break;
                                }
                                break;
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT: // End of the object. Push to the database.
                        switch (e.asEndElement().getName().toString()) {
                            case "node":
                            case "way":
                            case "relation":
                                if(map.containsKey(element.getId())) {
                                    duplicatesMap.put(element.getId(),element);
                                    duplicatesCount++;
                                } else {
                                    map.put(element.getId(),element);
                                }
                                break;
                        }
                        break;
                }
            }
//            int i = 0;
//            for(Map.Entry<Integer,Element> entry : map.entrySet()) {
//                i++;
//                System.out.println(""+entry.getValue());
//                if (i>100) break;
//            }
            for(Map.Entry<Integer,Element> entry : duplicatesMap.entrySet()) {
                System.out.println(duplicatesCount);
                duplicatesCount--;
                System.out.println("1 Existing: "+map.get(entry.getKey()));
                System.out.println("2 New     : "+entry.getValue());
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                String userChoice = "";
                long startUser = System.nanoTime();
                try {
                    userChoice = br.readLine();
                } catch (IOException io) {
                    System.out.println("Input error: " + io);
                }
                long finishUser = System.nanoTime();
                start += finishUser - startUser;
                switch (userChoice) {
                    case "1":
                        break;
                    case "2":
                        map.put(entry.getKey(),entry.getValue());
                        break;
                    default:
                        System.exit(111);
                }
            }
            for(Map.Entry<Integer,Element> entry : map.entrySet()) {
                db.writeElement(entry.getValue());
                elementsCount++;
            }
            db.lastCommitElement();
            long finish = System.nanoTime();
            double time = (double)(finish-start)/1000000000;
            DecimalFormat formatter = new DecimalFormat("0.##");
            System.out.println("Created " + elementsCount + " database items in " + formatter.format(time) + " seconds (" + formatter.format(time*1000000/elementsCount) + " microseconds/element).");
            db.close();
            reader.close();
        } catch (javax.xml.stream.XMLStreamException e) {
            System.out.println("XML error. "+e);
        } catch (FileNotFoundException e) {
            System.out.println("File not found. "+e);
        } catch (SQLException e) {
            System.out.println("Database error. "+e);
        }
    }

    private static void info() {
        System.out.println("OSMparser usage information:");
        System.out.println("-analyse FILENAME.osm");
        System.out.println("    will analyse the Open Street Map XML to give some information about it.");
        System.out.println("-parse FILENAME.osm OUTPUT_DATABASE");
        System.out.println("    will parse the Open Street Map XML and save all results into SQLite database. Database should exist.");
        System.out.println("-select INPUT_DATABASE OUTPUT_DATABASE");
        System.out.println("    will select buildings with addresses from SQLite database, calculate their coordinates and save all results into another database. Database should exist.");
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
                            case "node":
                                while(iter.hasNext()) attrNode.add(((Attribute)iter.next()).getName().toString());
                                break;
                            case "way":
                                while(iter.hasNext()) attrWay.add(((Attribute)iter.next()).getName().toString());
                                break;
                            case "nd":
                                while(iter.hasNext()) attrNd.add(((Attribute)iter.next()).getName().toString());
                                break;
                            case "relation":
                                while(iter.hasNext()) attrRelation.add(((Attribute)iter.next()).getName().toString());
                                break;
                            case "tag":
                                while(iter.hasNext()) attrTag.add(((Attribute)iter.next()).getName().toString());
                                break;
                            case "member":
                                while(iter.hasNext()) {
                                    Attribute attribute = (Attribute)iter.next();
                                    String aName = attribute.getName().toString();
                                    attrMember.add(aName);
                                    if(aName.equals("type")) {
                                        memberTypes.add(attribute.getValue());
                                    }
                                    if(aName.equals("role")) {
                                        memberRoles.add(attribute.getValue());
                                    }
                                }
                                break;
                        }
                        if (se.getAttributeByName(QName.valueOf("lat"))!=null) tracesWithCoordinatesList.add(traceString);
                        if (se.getAttributeByName(QName.valueOf("k"))!=null) {
                            kValues.add(se.getAttributeByName(QName.valueOf("k")).getValue());
                            if (se.getAttributeByName(QName.valueOf("k")).getValue().equals("name")) tracesWithNamesList.add(traceString);
                            if (se.getAttributeByName(QName.valueOf("k")).getValue().equals("addr:street")) tracesWithStreetsList.add(traceString);
                            if (se.getAttributeByName(QName.valueOf("k")).getValue().equals("addr:housenumber")) tracesWithHousesList.add(traceString);
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
