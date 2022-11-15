package com.srg;

import com.alibaba.fastjson.JSONObject;
import com.srg.bean.Component;
import com.srg.bean.Party;
import com.srg.xml.BeanToXml;
import com.srg.xml.XmlToBean;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

/**
 * @author : SRG
 * @create : 2022/3/9
 * @describe :
 **/
public class XmlToBeanApplicationTestMain {




    public static void main(String[] args) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Party party = new Party();
        party.setPartyId("123");
        party.setPartyName("srg");
        BeanToXml beanToXml = new BeanToXml();
        String s = beanToXml.beanToXml(party);
        System.out.println(s);
    }

    public static void baseBeanTest(){
        Party party = new Party();
//        String[] header = party.getHeader();
//        System.out.println(header);
//        for (String s : header) {
//            System.out.println(s);
//        }
        String[] header1 = party.getChildNodes();
        System.out.println(header1);

        for (String s : header1) {
            System.out.println(s);
        }

        Component component = new Component();
        String[] header = component.getChildNodes();
        System.out.println(header);
        for (String s : header) {
            System.out.println(s);
        }

    }


    public static void xml2BeanTest() throws Exception {
        File templateFile = new File("C:\\srgWorkspace\\MyGitHub\\xml2bean\\XmlToBean\\src\\main\\resources\\TemplateTest.xml");

        File sourceFile = new File("C:\\srgWorkspace\\MyGitHub\\xml2bean\\XmlToBean\\src\\main\\resources\\sourceTest.xml");


        String className = "com.srg.bean.TradeBean";

        Object objectBean = XmlToBean.getObjectBean(templateFile, sourceFile, className);

        String s = JSONObject.toJSONString(objectBean);

        System.out.println(s);
    }



}
