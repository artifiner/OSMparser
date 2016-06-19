package com.artifinery;

import java.util.ArrayList;
import java.util.HashMap;

class ElementMap {
    private HashMap<Long,Element> main;
    private HashMap<Long,Element> duplicates;
    ArrayList<Element> addresses;

    ElementMap() {
        main = new HashMap<>();
        duplicates = new HashMap<>();
        addresses = new ArrayList<>();
    }

    void add(Element element) {
        if(main.containsKey(element.getId())) {
            duplicates.put(element.getId(),element);
        } else {
            main.put(element.getId(),element);
        }
        if(element.hasAddress()) addresses.add(element);
    }

    Element get(Reference reference) {
        if(duplicates.containsKey(reference.getKey()) && duplicates.get(reference.getKey()).getType().equals(reference.getType())) return duplicates.get(reference.getKey());
        if(main.containsKey(reference.getKey())) return main.get(reference.getKey());
        return new Element();
    }
}
