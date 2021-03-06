package com.srg;

import com.alibaba.fastjson.JSONObject;
import com.srg.xml2bean.ParseXmlToBean;

import java.io.File;

/**
 * @author : SRG
 * @create : 2022/3/9
 * @describe :
 **/
public class XmlToBeanApplicationTestMain {


    public static void main(String[] args) throws Exception {


        File templateFile = new File("C:\\srgWorkspace\\MyGitHub\\xml2bean\\XmlToBean\\src\\main\\resources\\TemplateTest.xml");

        File sourceFile = new File("C:\\srgWorkspace\\MyGitHub\\xml2bean\\XmlToBean\\src\\main\\resources\\sourceTest.xml");


        String className = "com.srg.bean.TradeBean";

        Object objectBean = ParseXmlToBean.getObjectBean(templateFile, sourceFile, className);

        String s = JSONObject.toJSONString(objectBean);

        System.out.println(s);

    }
}
