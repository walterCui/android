package com.game.contact;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.game.contact.model.PhoneData;

import org.xml.sax.helpers.AttributesImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button importButton = (Button)findViewById(R.id.import_button);
        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(getResources().getString(R.string.import_tip));
                builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.this.importContact();
                    }
                });
                builder.setNegativeButton("Cancel",null);
                builder.show();
            }
        });

        final Button exportButton = (Button)findViewById(R.id.export_button);
        exportButton.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(getResources().getString(R.string.export_tip));
                builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.this.exportContact();
                    }
                });
                builder.setNegativeButton("Cancel",null);
                builder.show();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private  void importContact()
    {
        try {
            List<PhoneData> phoneDatas = deserialize(readFromFile("phone.txt"));
            Uri rawContactUri = ContactsContract.RawContacts.CONTENT_URI;
            Uri dataUri = ContactsContract.Data.CONTENT_URI;
            ContentResolver resolver = this.getContentResolver();

            ContentValues contentValue;
            long contactId = 0;

            contentValue = new ContentValues();

            for(PhoneData phoneData : phoneDatas) {
                contactId = ContentUris.parseId(resolver.insert(rawContactUri, contentValue));
                //add name;
                contentValue.put(ContactsContract.Data.RAW_CONTACT_ID, contactId);
                contentValue.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
                contentValue.put(ContactsContract.Data.DATA1, phoneData.getName());
                resolver.insert(dataUri,contentValue);
                contentValue.clear();

                //add phone;
                contentValue.put(ContactsContract.Data.RAW_CONTACT_ID, contactId);
                contentValue.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                contentValue.put(ContactsContract.Data.DATA1, phoneData.getPhone());
                resolver.insert(dataUri, contentValue);
                contentValue.clear();
            }
            new AlertDialog.Builder(this).setTitle("import ok").show();
        }
        catch (Exception e)
        {
            new AlertDialog.Builder(this).setTitle(e.getMessage()).show();
        }
    }

    private void exportContact()
    {
        Uri uri = Uri.parse("content://com.android.contacts/contacts");

        ContentResolver resolver =  MainActivity.this.getBaseContext().getContentResolver();
        Cursor cursor = resolver.query(uri, new String[]{ContactsContract.Data._ID}, null, null,null);

        List<PhoneData> phones = new ArrayList<PhoneData>(cursor.getCount());

        PhoneData temp = new PhoneData();
        while (cursor.moveToNext())
        {
            int id = cursor.getInt(0);

            uri = Uri.parse("content://com.android.contacts/contacts/"+id+"/data");
            Cursor cursor2 = resolver.query(uri, new String[]{ContactsContract.Data.DATA1, ContactsContract.Data.MIMETYPE},null,null,null);
            while (cursor2.moveToNext())
            {
                String mimeType = cursor2.getString(cursor2.getColumnIndex("mimetype"));
                String data = cursor2.getString(cursor2.getColumnIndex("data1"));
                if(mimeType.equals("vnd.android.cursor.item/name"))
                    temp.setName(data);
                else if(mimeType.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE))
                {
                    temp.setPhone(data);
                }
            }
            if(temp.hasPhone()) {
                phones.add(temp);
                temp = new PhoneData();
            }
            else
                temp.clear();
        }

        try
        {

            serialize(phones);

            new AlertDialog.Builder(this).setTitle("import ok").show();
        }
        catch (Exception e)
        {
            new AlertDialog.Builder(this).setTitle(e.getMessage()).show();
        }
    }

    private void serialize(List<PhoneData> phones)  throws Exception {
        SAXTransformerFactory factory = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler handler = factory.newTransformerHandler();
        Transformer transformer = handler.getTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");


        StringWriter writer = new StringWriter();

        Result result = new StreamResult(writer);
        handler.setResult(result);

        String uri = "";
        String localName = "";

        handler.startDocument();
        handler.startElement(uri, localName, "phoneDatas", null);

        //AttributesImpl attrs = new AttributesImpl();
        char[] ch = null;

        for (PhoneData phone : phones) {
            //  attrs.clear();
            handler.startElement(uri, localName, XMLHandler.PhoneDataElement, null);

            handler.startElement(uri, localName, XMLHandler.NameElement, null);
            ch = phone.getName().toCharArray();
            handler.characters(ch, 0, ch.length);
            handler.endElement(uri, localName, XMLHandler.NameElement);

            handler.startElement(uri, localName, XMLHandler.PhoneElement, null);
            ch = phone.getPhone().toCharArray();
            handler.characters(ch, 0, ch.length);
            handler.endElement(uri, localName, XMLHandler.PhoneElement);

            handler.endElement(uri, localName, XMLHandler.PhoneDataElement);

        }
        handler.endElement(uri, localName, "phoneDatas");
        handler.endDocument();

        writeToFile(writer.toString().getBytes("UTF-8"), "phone.txt");
    }

    private boolean writeToFile(byte[] buffer, String fileName) throws  Exception{
        File sdCardDir = Environment.getExternalStorageDirectory();
        File saveFile = new File(sdCardDir, fileName);
        FileOutputStream outStream = new FileOutputStream(saveFile);
        outStream.write(buffer);
        outStream.close();
//            FileOutputStream fos = openFileOutput("phone.xml", Context.MODE_PRIVATE);
//            fos.write("dfsdf".toString().getBytes("UTF-8"));
//            fos.close();
        return true;
    }

    private List<PhoneData> deserialize(InputStream inputStream) throws Exception
    {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        XMLHandler handler = new XMLHandler();
        parser.parse(inputStream, handler);

        return handler.getPhoneDatas();
    }

    private InputStream readFromFile(String fileName) throws Exception
    {
        File sdCardDir = Environment.getExternalStorageDirectory();
        File saveFile = new File(sdCardDir, fileName);
        FileInputStream inputStream = new FileInputStream(saveFile);

        return inputStream;

//        return getAssets().open(fileName);
    }
}
