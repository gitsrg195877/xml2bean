package com.srg.xml;



import com.srg.ConvertData;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author : SRG
 * @create : 2022/3/9
 * @description :
 **/
public class XmlToBean {

    //数据标识符"${}",ex: ${t.parties#1.partyId}
    private static final String DATA_LOGO_SIGN = "^\\$\\{+.*\\}$";

    //集合规则标识符,带"#"号标识，ex: parties#1
    private static final String LIST_LOGO_SIGN = "^.*_\\d+$";

    /**
     * @description: 根据解析templeteFile获取属性名，以及在sourceFile中相应的数据的Xpath，获取HashMap<属性名,String>的的map
     * 然后根据className通过反射生成相应的JavaEntity.
     * @return: Object
     **/
    public static Object getObjectBean(File templateFile, File sourceFile, String className) throws Exception{

        //读取template获取所有的数据的路径
        Document templateDocument = new SAXReader().read(templateFile);
        Element rootElement = templateDocument.getRootElement();
        HashMap<String, String> hashMap = new HashMap<>();
        getAllXpath(rootElement,rootElement, hashMap);

        //根据路径到source取出所有的数据
        Document sourceDocument = new SAXReader().read(sourceFile);
        Set<String> keySet = hashMap.keySet();
        for (String key : keySet) {
            //如果带有"@"则代表是attribute，否则是element
            if (hashMap.get(key).contains("@")){
                int i = hashMap.get(key).indexOf("@");
                String attributeName = hashMap.get(key).substring(i+1,hashMap.get(key).length());
                String Xpath = hashMap.get(key).substring(0,i-1);
                Element node = (Element) sourceDocument.selectSingleNode(Xpath);
                if(node == null){
                    hashMap.put(key,null);
                }else {
                    Attribute attribute = node.attribute(attributeName);
                    if(attribute == null){
                        hashMap.put(key,null);
                    }else {
                        hashMap.put(key, attribute.getText());
                    }
                }
            }else {
                Element node = (Element) sourceDocument.selectSingleNode(hashMap.get(key));
                if(node == null){
                    hashMap.put(key,null);
                }else {
                    hashMap.put(key, node.getText());
                }
            }

        }

        //通过反射获取javaBean对象
        Class<Object> clz = (Class<Object>) Class.forName(className);
        Object objectBean = getObjectBeanByReflect(hashMap, clz);

        return objectBean;
    }


    /**
     * @description: 解析templete获取所有data的路径，并获取bean属性名,属性名为key，路径为value
     * @return: HashMap
     **/
    public static HashMap<String, String> getAllXpath(Element rootElement,Element element, HashMap<String, String> map) {

        //判断此节点是否有"href"名的attribute，进行相应的处理
        Attribute href = element.attribute("href");
        if(href != null){
            String text = href.getText();
            Element idElement = getHrefToIdElement(text, rootElement);
            HashMap<String, String> hashMap = getHrefToIdPath(element, idElement,map);
            return hashMap;
        }

        //获取element中的attribute数据含有${}特殊标识符的路径
        List<Attribute> attributeList = element.attributes();
        for (Attribute attribute : attributeList) {
            if (attribute.getText().matches(DATA_LOGO_SIGN)) {
                String key = attribute.getText().substring(2, attribute.getText().length() - 1);
                map.put(key, attribute.getUniquePath());
            }
        }

        //如果此节点的elements()方法返回的是空列表，说明下属没子节点，则获取他的text值，并截取${}里面的字符作为key
        //如果不是空列表，则循环此列表里的Element对象,并递归调用
        if (element.elements().toString().equals("[]")) {
            if (element.getText().matches(DATA_LOGO_SIGN)) {
                String key = element.getText().substring(2, element.getText().length() - 1);
                map.put(key, element.getUniquePath());
                return map;
            }
            return map;
        } else {
            List<Element> elements = element.elements();
            for (Element element1 : elements) {
                getAllXpath(rootElement,element1, map);
            }
            return map;
        }
    }

    /**
     *  @return: 返回有id(attribute) = "${hrefText}"的element
     **/
    private static Element getHrefToIdElement(String hrefText,Element element){

        if(element.attribute("id") == null || (!element.attribute("id").getText().equals(hrefText))){
            if(element.elements().toString().equals("[]")){
                return null;
            }else {
                List<Element> elements = element.elements();
                for (Element element_1 : elements) {
                    Element hrefToIdElement = getHrefToIdElement(hrefText, element_1);
                    if (hrefToIdElement != null){
                        return hrefToIdElement;
                    }
                }
            }
        }else {
            return element;
        }
        return null;
    }

    /**
     * 根据hrefElement引用的idElement，根据相同的子节点，获取idElement的路
     **/
    private static HashMap<String,String> getHrefToIdPath(Element hrefElement,Element idElement,HashMap<String,String> map){

        List<Attribute> attributeList = hrefElement.attributes();
        for (Attribute attribute : attributeList) {
            if (attribute.getText().matches(DATA_LOGO_SIGN)) {
                String key = attribute.getText().substring(2, attribute.getText().length() - 1);
                Attribute attribute1 = idElement.attribute(attribute.getName());
                String valuePath = attribute1.getUniquePath();
                map.put(key, valuePath);
            }
        }
        if (hrefElement.elements().toString().equals("[]")) {
            if (hrefElement.getText().matches(DATA_LOGO_SIGN)) {
                String key = hrefElement.getText().substring(2, hrefElement.getText().length() - 1);
                String valuePath = idElement.getUniquePath();
                map.put(key, valuePath);
                return map;
            }
            return map;
        } else {
            List<Element> elements = hrefElement.elements();
            for (Element hrefElement_1 : elements) {
                Element idElement_1 = idElement.element(hrefElement_1.getName());
                getHrefToIdPath(hrefElement_1,idElement_1, map);
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
        //防止遍历到下一个key时，type通过反射初始化生成的对象不是同一个，即针对下面for循环定义一个全局的objMap
        HashMap<String, Object> objMap = new HashMap<>();
        //获取hashMap里所有的key
        Set<String> keySet = hashMap.keySet();
        for (String key : keySet) {
            //每遍历一个key，把根字节码对象、根对象赋给临时变量
            Class tempClass = clz;
            Object tempObj = obj;
            String[] split = key.split("\\.");
            for (int i = 0; i < split.length; i++) {
                //第一个不进行判断，因为根字节码对象跟根对象已经拥有
                if (i > 0) {
                    // 处理集合类型的属性
                    // 设定规则，如果格式为abc_1格式，则表示为集合类型，需要获取泛型(针对没有指定泛型类型的集合是否需要进行特殊处理？)
                    if (split[i].matches(LIST_LOGO_SIGN)) {

                        int strIndex = split[i].indexOf("_");
                        String fieldName = split[i].substring(0, strIndex);
                        //获取属性的Field对象,如果此类中没有，则从父类中去查找，一直查到Object,
                        //@Exception: 需要解决运行时异常：没有父类的异常
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

                        String methodName = "set" + firstToBig(fieldName);
                        Method method = tempClass.getMethod(methodName, List.class);
                        Type genericType = field.getGenericType();
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
                    Class<?> type = field.getType();
                    String methodName = "set" + firstToBig(split[i]);
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
                        method.invoke(tempObj, o);
                        //把tempClass,temObj换掉刚刚的此属性的Class对象，Object对象，以进入下一层反射
                        tempClass = type;
                        tempObj = o;

                    } else {
                        Object o = ConvertData.convertData(type, hashMap.get(key));
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
