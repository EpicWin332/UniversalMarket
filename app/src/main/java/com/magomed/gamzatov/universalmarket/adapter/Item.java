package com.magomed.gamzatov.universalmarket.adapter;

public class Item {
    String name;
    String description;
    String photoUrl;
    int id;
    String type;
    public Item(String name, String description, String photoUrl, int id, String type) {
        this.name = name;
        this.description = description;
        this.photoUrl = photoUrl;
        this.id = id;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}

