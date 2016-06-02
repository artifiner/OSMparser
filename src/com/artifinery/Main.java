package com.artifinery;


import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;


public class Main {

    private final static String PATH = "/home/artifiner/data/RU-MOW.osm";
    private final static String DB = "/home/artifiner/data/moscow.db";

    public static void main(String[] args) {
        ArrayList<String> elements = new ArrayList<>();
        ArrayList<String> elementsWithCoordinates = new ArrayList<>();
        ArrayList<String> elementsWithStreet = new ArrayList<>();
        ArrayList<String> elementsWithHouse = new ArrayList<>();
        XMLEventReader reader = null;
        InputStream input;
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            input = new FileInputStream(PATH);
            reader = inputFactory.createXMLEventReader(input);
            int i = 0;
            int j = 0;
            int k = 0;
            int l = 0;
            System.out.println("Processing...");
            long start = System.nanoTime();
            while (reader.hasNext()) {
                XMLEvent e = reader.nextEvent();
                switch (e.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        i++;
                        String s = e.asStartElement().getName().toString();
                        if (!elements.contains(s)) elements.add(s);
                        if (e.asStartElement().getAttributeByName(QName.valueOf("lat"))!=null) {
                            j++;
                            if (!elementsWithCoordinates.contains(s)) elementsWithCoordinates.add(s);
                        }
                        if (e.asStartElement().getAttributeByName(QName.valueOf("k"))!=null) {
                            if (e.asStartElement().getAttributeByName(QName.valueOf("k")).getValue().equals("addr:street")) {
                                k++;
                                if (!elementsWithStreet.contains(s)) elementsWithStreet.add(s);
                            }
                            if (e.asStartElement().getAttributeByName(QName.valueOf("k")).getValue().equals("addr:housenumber")) {
                                l++;
                                if (!elementsWithHouse.contains(s)) elementsWithHouse.add(s);
                            }
                        }
                }

            }
            long finish = System.nanoTime();
            double time = (double)(finish-start)/1000000000;
            DecimalFormat formatter = new DecimalFormat("0.##");
            System.out.println(i + " elements processed in " + formatter.format(time) + " seconds (" + formatter.format(time*1000000/i) + " microseconds/element). Elements names:");
            for (String s:elements) {
                System.out.println(s);
            }
            System.out.println(j + " with coordinates:");
            for (String s:elementsWithCoordinates) {
                System.out.println(s);
            }
            System.out.println(k + " with street:");
            for (String s:elementsWithStreet) {
                System.out.println(s);
            }
            System.out.println(l + " with house:");
            for (String s:elementsWithHouse) {
                System.out.println(s);
            }
        } catch (javax.xml.stream.XMLStreamException e) {
            System.out.println("Cannot parse XML. "+e);
        } catch (FileNotFoundException e) {
            System.out.println("File not found. "+e);
        }
        try {
            if (reader!=null)
                reader.close();
        } catch (javax.xml.stream.XMLStreamException e) {
            System.out.println("Cannot close XML reader. "+e);
        }
//        SQLiteDB db = new SQLiteDB(DB);
//        db.writeNode(23133423, 50.1, 30.2);
//        db.addTag("name");
//        db.addTag("name");
//        db.close();
    }
}
