package com.game.contact.model;

/**
 * Created by walter on 14/12/19.
 */
public class PhoneData {

    private String name;

    private String phone;

    public void setName(String val)
    {
        this.name = val;
    }

    public String getName()
    {
        return this.name;
    }

    public void setPhone(String val)
    {
        this.phone = val;
    }

    public String getPhone()
    {
        return this.phone;
    }

    public boolean hasPhone()
    {
        return phone != null && phone != "";
    }

    public void clear()
    {
        name = null;
        phone = null;
    }
}
