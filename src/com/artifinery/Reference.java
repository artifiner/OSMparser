package com.artifinery;

class Reference {
    private String type;
    private long key;

    Reference(String type, String ref) {
        this.type = type;
        this.key = Long.parseLong(ref);
    }

    String getType() {
        return type;
    }

    long getKey() {
        return key;
    }
}

