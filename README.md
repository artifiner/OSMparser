# OSMparser
OSMparser parses XML file with OSM and gets all addresses (addr:street + addr:housenumber) from it.
Result stored in SQLite database.
Command line arguments:
-analyse FILENAME.osm
  will analyse the Open Street Map XML to give some information about it.
-parse FILENAME.osm OUTPUT_DATABASE
  will parse the Open Street Map XML and save all results into SQLite database. Database should exist.
