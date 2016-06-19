package com.artifinery;

import java.util.ArrayList;

class TextWithCountersList {

    private ArrayList<String> texts;
    private ArrayList<Integer> counters;
    private long total;

    TextWithCountersList() {
        texts = new ArrayList<>();
        counters = new ArrayList<>();
        total = 0;
    }

    void add(String text) {
        total++;
        int i = texts.indexOf(text);
        if(i>-1) {
            counters.set(i,counters.get(i)+1);
        } else {
            texts.add(text);
            counters.add(1);
        }
    }

    void print(String description, boolean total) {
        if (total) description = "Total: " + getTotal() + description;
        System.out.println(description);
        for (int i = 0; i < texts.size(); i++) {
            System.out.println(counters.get(i) + " - " + texts.get(i));
        }
        System.out.println();
    }

    long getTotal() {
        return total;
    }

    private void swap(int i,int j) {
        String s = texts.get(i);
        texts.set(i,texts.get(j));
        texts.set(j,s);
        int x = counters.get(i);
        counters.set(i,counters.get(j));
        counters.set(j,x);
    }

    void sortDescending() {
        if (counters.size()==0) return;
        for (int i = 0; i < counters.size()-1; i++) {
            for (int j = i+1; j < counters.size(); j++) {
                if(counters.get(i)<counters.get(j)) swap(i,j);
            }
        }
    }
}
