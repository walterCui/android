package com.game.contact;

import com.game.contact.model.PhoneData;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by walter on 14/12/21.
 */
public class XMLHandler extends DefaultHandler {

    public static String NameElement = "name";

    public static String PhoneElement = "phone";

    public static String PhoneDataElement = "phoneData";

    private List<PhoneData> phoneDatas;

    private  PhoneData phoneData;

    private String currentElement;

    public List<PhoneData> getPhoneDatas()
    {
        return phoneDatas;
    }


    @Override
    public void startDocument() throws SAXException
    {
        phoneDatas = new ArrayList<PhoneData>();
    }

    @Override
    public void characters (char ch[], int start, int length)
            throws SAXException
    {
        if(phoneData != null)
        {
            String str = new String(ch, start, length);
            if(NameElement == currentElement)
                phoneData.setName(str);
            else if(PhoneElement == currentElement)
                phoneData.setPhone(str);
        }
    }

    @Override
    public void startElement (String uri, String localName,
                              String qName, Attributes attributes)
            throws SAXException
    {
        if(localName == PhoneDataElement)
            phoneData = new PhoneData();
        currentElement = localName;
    }

    @Override
    public void endElement (String uri, String localName, String qName)
            throws SAXException
    {
        if(localName == PhoneDataElement && phoneData != null)
        {
            phoneDatas.add(phoneData);
            phoneData = null;
        }
        currentElement = null;
    }
}
