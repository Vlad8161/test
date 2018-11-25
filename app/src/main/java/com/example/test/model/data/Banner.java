package com.example.test.model.data;

import com.example.test.model.room.entity.BannerRoom;

/**
 * Created by vlad on 24.11.18.
 */

public class Banner {
    public String id;
    public String title;
    public String desc;
    public String image;

    public Banner() {
    }

    public Banner(BannerRoom bannerRoom) {
        this.id = bannerRoom.getId();
        this.title = bannerRoom.getTitle();
        this.desc = bannerRoom.getDescription();
        this.image = bannerRoom.getImage();
    }
}
