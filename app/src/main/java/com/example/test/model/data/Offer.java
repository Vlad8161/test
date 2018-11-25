package com.example.test.model.data;

import com.example.test.model.room.entity.OfferRoom;

/**
 * Created by vlad on 24.11.18.
 */

public class Offer {
    public String id;
    public String title;
    public String desc;
    public String groupName;
    public String type;
    public String image;
    public Float price;
    public Float discount;

    public Offer() {
    }

    public Offer(OfferRoom offerRoom) {
        this.id = offerRoom.getId();
        this.title = offerRoom.getTitle();
        this.desc = offerRoom.getDescription();
        this.groupName = offerRoom.getGroup();
        this.type = offerRoom.getType();
        this.image = offerRoom.getImage();
        this.price = offerRoom.getPrice();
        this.discount = offerRoom.getDiscount();
    }
}
