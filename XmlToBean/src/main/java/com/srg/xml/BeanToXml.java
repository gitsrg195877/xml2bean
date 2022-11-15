package com.srg.xml;

import com.srg.AbstractBaseBean;
import com.srg.util.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author: EQ-SRG
 * @create: 2022/7/8
 * @description: parse Bean to xml;目前只支持element，好像不太好解决除了节点之外的内容，比如attribute,href等
 **/
public class BeanToXml {

    public String beanToXml(AbstractBaseBean baseBean) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (baseBean == null) {
            return null;
        }
        Document document = DocumentHelper.createDocument();
        Element rootElement = document.addElement(baseBean.getClassName());
        writeElement(rootElement,baseBean);
        return document.asXML().replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>","");
    }




    private void writeElement(Element element, AbstractBaseBean baseBean) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String[] nodes = baseBean.getChildNodes();
        Class<? extends AbstractBaseBean> clz = baseBean.getClass();
        for (String node : nodes) {
            Element nodeElement = element.addElement(node);
            String methodNameGet = "get"+ StringUtils.firstToBig(node);
            Method method = clz.getDeclaredMethod(methodNameGet,null);
            Object invoke = method.invoke(baseBean);
            if (invoke != null) {
                nodeElement.setText(invoke.toString());
            } else {
                nodeElement.setText("");
            }
        }
    }




}
