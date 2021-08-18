package com.isport.tracker.util;

import android.graphics.Typeface;


import com.isport.tracker.MyApp;
import com.isport.tracker.entity.ClientInfo;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by Administrator on 2016/6/30.
 */
public class XmlParserUtil {


    public String getClient (String path) {
        try{
            InputStream instream = MyApp.getInstance().getResources().getAssets().open(path);
            SAXParserFactory factory = SAXParserFactory.newInstance();//创建SAX解析工厂
            SAXParser paser = factory.newSAXParser();//创建SAX解析器
            PersonPaser personPaser=new PersonPaser();//创建事件处理程序
            paser.parse(instream,personPaser);//开始解析
            instream.close();//关闭输入流
            List<ClientInfo> infos = personPaser.getClients();//返回解析后的内容
            for(int i=0;i<infos.size();i++ ){
                ClientInfo info = infos.get(i);
                if(info.isSelected()){
                    return info.getClientName();
                }
            }
        }catch (Exception e){
           e.printStackTrace();
        }
        return Constants.CLIENT_ISPORT;

    }

    public static Typeface initTypeFace(String path){

         Typeface typeface = Typeface.createFromAsset(MyApp.getInstance().getResources().getAssets(), path);
        return typeface;
    }

    public final class PersonPaser extends DefaultHandler {//创建事件处理程序，也就是编写ContentHandler的实现类，一般继承自DefaultHandler类

        private List<ClientInfo> listClient = null;
        ClientInfo info = null;
        String tagName = null;

        public List<ClientInfo> getClients(){
            return this.listClient;
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            listClient = new ArrayList<ClientInfo>();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            if(localName.equals("item")){
                info = new ClientInfo();
            }
            tagName = localName;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            if(tagName == null) return;
            String data = new String(ch,start,length);
            if(tagName.equals("name")){
                info.setClientName(data);
            }else if(tagName.equals("selected")){
                info.setSelected(data.equals("true"));
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if(localName.equals("item")){
                listClient.add(info);
                info = null;
            }
            tagName = null;
        }

        @Override
        public void endDocument() throws SAXException {
            super.endDocument();
        }
    }
}