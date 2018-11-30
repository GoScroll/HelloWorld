package com.example.helloworld;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

/**
 * Created by ‘。；op on 2018/11/30.
 */

public class Person extends BmobObject{
    private String name;
    private String address;
    private BmobFile addimage;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BmobFile getAddimage() {
        return addimage;
    }

    public void setAddimage(BmobFile addimage) {
        this.addimage = addimage;
    }
}
