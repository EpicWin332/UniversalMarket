package com.magomed.gamzatov.universalmarket.entity;


import java.util.List;

/**
 * Created by MacBookAir on 05.03.16.
 */
public class Items {

    private int id;

    private String brand;
    private String description;
    private int price;
    private Type type;
    private Shop shop;

    private List<String> imageUrls;

    public int getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public String getDescription() {
        return description;
    }

    public int getPrice() {
        return price;
    }

    public Type getType() {
        return type;
    }

    public Shop getShop() {
        return shop;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}
