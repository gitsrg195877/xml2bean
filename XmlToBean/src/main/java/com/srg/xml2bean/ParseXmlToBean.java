package com.srg.xml2bean;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author : SRG
 * @create : 2022/3/9
 * @description :
 **/
public class ParseXmlToBean {

    /**
     * @description: 根据解析templeteFile获取属性名，以及在sourceFile中相应的数据的Xpath，获取HashMap<属性名,String>的的map
     * 然后根据className通过反射生成相应的JavaEntity.(目前JavaBean的属性只支持String类型)
     * @return: Object
     **/
    public static Object getObjectBean(File templateFile, File sourceFile, String className) throws Exception{

        //读取template获取所有的数据的路径
        Document templateDocument = new SAXReader().read(templateFile);
        Element rootElement = templateDocument.getRootElement();
        HashMap<String, String> hashMap = new HashMap<>();
        getAllXpath(rootElement, hashMap);

        //根据路径到source取出所有的数据
        Document sourceDocument = new SAXReader().read(sourceFile);
        Set<String> keySet = hashMap.keySet();
        for (String key : keySet) {
            if (hashMap.get(key).contains("@")){
                int i = hashMap.get(key).indexOf("@");
                String attributeName = hashMap.get(key).substring(i+1,hashMap.get(key).length());
                String Xpath = hashMap.get(key).substring(0,i-1);
                Element node = (Element) sourceDocument.selectSingleNode(Xpath);
                Attribute attribute = node.attribute(attributeName);
                hashMap.put(key, attribute.getText());
            }else {
                Element node = (Element) sourceDocument.selectSingleNode(hashMap.get(key));
                hashMap.put(key, node.getText());
            }

        }

        //通过反射获取javaBean对象
        Class<Object> cls = (Class<Object>) Class.forName(className);
        Object objectBean = getObjectBeanByReflect(hashMap, cls);

        return objectBean;
    }


    /**
     * @description: 解析templete获取所有data的路径，并获取bean属性名,属性名为key，路径为value
     * @return: HashMap
     **/
    public static HashMap<String, String> getAllXpath(Element element, HashMap<String, String> map) {

        List<Attribute> attributeList = element.attributes();
        for (Attribute attribute : attributeList) {
            if (attribute.getText().matches("^\\$\\{+.*\\}$")) {
                String key = attribute.getText().substring(2, attribute.getText().length() - 1);
                map.put(key, attribute.getUniquePath());
            }
        }

        //如果此节点的elements()方法返回的是空列表，说明下属没子节点，则获取他的text值
        //如果不是空列表，则循环此列表里的Element对象,并递归调用
        if (element.elements().toString().equals("[]")) {
            //匹配${}
            if (element.getText().matches("^\\$\\{+.*\\}$")) {
                //截取${}里面的字符作为key
                String key = element.getText().substring(2, element.getText().length() - 1);
                //把此element的唯一路径put进Map集合里
                map.put(key, element.getUniquePath());
                return map;
            }
            return map;
        } else {
            List<Element> elements = element.elements();
            for (Element element1 : elements) {
                //递归调用
                getAllXpath(element1, map);
            }
            return map;
        }
    }


    /**
     * @description: 通过反射获取JavaBean对象
     * @return: Object
     **/
    public static Object getObjectBeanByReflect(HashMap<String, String> hashMap, Class<Object> clz) throws Exception{


        //初始化根对象
        Object obj = clz.getConstructor().newInstance();

        //直接获取字段通过反射给bean类赋值，如果字段是private的，则需要解除私有限定，会破坏封装性
//        for (int i = 0 ;i< fields.length;i++) {
//            fields[i].setAccessible(true);//暴力反射，解除私有限定,此处破坏了封装性，应获取公有方法set值
//            fields[i].set(obj,hashMap.get(fields[i].getName()));
//        }

        //防止遍历到下一个key时，type通过反射初始化生成的对象不是同一个，即针对下面for循环定义一个全局的objMap
        HashMap<String, Object> objMap = new HashMap<>();

        //获取hashMap里所有的key
        Set<String> keySet = hashMap.keySet();

        for (String key : keySet) {

            //每遍历一个key，把根字节码对象、根对象赋给临时变量
            Class tempClass = clz;
            Object tempObj = obj;

            // split,以"."作为分割
            String[] split = key.split("\\.");

            //循环遍历分割得到的字符串数组
            for (int i = 0; i < split.length; i++) {

                //第一个不进行判断，因为根字节码对象跟根对象已经拥有
                if (i > 0) {

                    // 处理集合类型的属性
                    // 设定规则，如果格式为abc_1格式，则表示为集合类型，需要获取泛型(针对没有指定泛型类型的集合是否需要进行特殊处理？)
                    if (split[i].matches("^.*_\\d+$")) {

                        //获取下划线的索引位置
                        int strIndex = split[i].indexOf("_");

                        //截取下划线之前的字符+"s"为属性名
                        String fieldName = split[i].substring(0, strIndex);

                        //获取属性的Field对象,如果此类中没有，则从父类中去查找，一直查到Object,???????????????需要解决运行时异常：没有父类的异常
                        Field field = null;
                        Class tempTempClass = tempClass;
                        while (field == null){
                            try {
                                field = tempTempClass.getDeclaredField(fieldName);
                            }catch (NoSuchFieldException e){
                                System.out.println(fieldName+"属性从父类中查找");
                            }
                            tempTempClass = tempTempClass.getSuperclass();
                        }

                        //构建此属性的set()的方法名
                        String methodName = "set" + firstToBig(fieldName);

                        //根据方法名，以及参数类型获取方法的Method对象，因为已经知道为List,所以写死为List.class
                        Method method = tempClass.getMethod(methodName, List.class);

                        //获取泛型
                        Type genericType = field.getGenericType();

                        //截取ClassName
                        //获取指定泛型的字节码对象
                        // (此处是否需要优化，目前是截取字符串做的反射，应该有相应的方法进行处理)
                        String className = genericType.getTypeName().substring(genericType.getTypeName().indexOf("<") + 1, genericType.getTypeName().indexOf(">"));
                        Class<?> type = Class.forName(className);

                        //获取map里的split[i - 1] + "." + fieldName规则设定作为Key的List集合
                        List oList = (List) objMap.get(split[i - 1] + "." + fieldName);

                        //如果oList为null，则初始化oList，并put到objMap里
                        if (oList == null) {
                            oList = new ArrayList<>();
                            objMap.put(split[i - 1] + "." + fieldName, oList);
                        }

                        //调用invoke方法，即通过set()进行初始化
                        method.invoke(tempObj, oList);

                        // split.length - 1 > i 说明有oList的泛型类型不是基本常用类型(包括String)，而是自定义的普通javabean
                        //否则说明oList的泛型类型是常用的数据类型
                        if (split.length - 1 > i){


                            //获取map里的split[i - 1] + "." + split[i]规则设定作为Key的嵌套Bean对象
                            Object o = objMap.get(split[i - 1] + "." + split[i]);

                            //如果o为null，则根据type Class初始化对象，并put到objMap里面，同时添加到集合里
                            if (o == null) {
                                o = type.newInstance();
                                objMap.put(split[i - 1] + "." + split[i], o);
                                oList.add(o);
                            }



                            //把tempClass,temObj换掉刚刚的此属性的Class对象，Object对象，以进入下一层反射
                            //注意：这里不是变为List,而是改变成List集合指定泛型类型生成的对象
                            tempClass = type;
                            tempObj = o;

                        }else {
                            //根据type以及hashmap创建oList里面的泛型对象并添加到oList里面去
                            Object o = ConvertData.convertData(type, hashMap.get(key));
                            oList.add(o);

                        }



                        continue;
                    }

                    //获取属性的Field对象        ???????????????需要解决运行时异常：没有父类的异常

                    Field field = null;
                    Class tempTempClass = tempClass;
                    while (field == null){
                        try {
                            field = tempTempClass.getDeclaredField(split[i]);
                        }catch (NoSuchFieldException e){
                            System.out.println(split[i]+"属性从父类中查找");
                        }
                        tempTempClass = tempTempClass.getSuperclass();
                    }


                    //获取字节码对象
                    Class<?> type = field.getType();

                    //构建此属性的set()的方法名
                    String methodName = "set" + firstToBig(split[i]);

                    //根据方法名，以及参数类型获取方法的Method对象
                    Method method = tempClass.getMethod(methodName, type);

                    //如果split.length - 1 > i,说明有Bean嵌套；
                    //否则说明没有嵌套了，直接set值进去
                    if (split.length - 1 > i) {

                        //首先判断objMap里是否有此对象。
                        //注意规则，在设计JavaBean的时候，多重嵌套时，
                        //需要保证：此属性名+上级属性名 在整个根Bean下来的结构里是唯一的，
                        //或许针对此key的设计需要优化
                        Object o = objMap.get(split[i - 1] + "." + split[i]);
                        //如果没有，则根据type Class初始化对象，并put到objMap里面
                        if (o == null) {
                            o = type.newInstance();
                            objMap.put(split[i - 1] + "." + split[i], o);
                        }

                        //Method对象调用invoke方法，以调用set()方法set值
                        method.invoke(tempObj, o);

                        //把tempClass,temObj换掉刚刚的此属性的Class对象，Object对象，以进入下一层反射
                        tempClass = type;
                        tempObj = o;

                    } else {

                        //根据type对字符创初始化相应类型的属性值
                        Object o = ConvertData.convertData(type, hashMap.get(key));

                        //Method对象调用invoke方法，以调用set()方法set值
                        method.invoke(tempObj, o);

                    }
                }
            }
        }
        return obj;
    }


    /**
     * @description: 把首字母小写的字符串转为首字母大写
     * @return: String
     **/
    public static String firstToBig(String s) {
        char[] chars = s.toCharArray();
        if (chars[0] >= 97 && chars[0] <= 122) {
            chars[0] = (char) (chars[0] - 32);
        }
        return new String(chars);
    }
}
