package com.srg;


import java.lang.reflect.Field;

/**
 * @author: EQ-SRG
 * @create: 2022/7/8
 * @description:
 **/

public abstract class AbstractBaseBean {

    public String[] childNodes;

    public String[] attributes;


    public String[] getChildNodes() {
        if (this.childNodes == null) {
            setChildNodes();
        }
        return this.childNodes;
    }

    public void setChildNodes() {
        Field[] fields = this.getClass().getDeclaredFields();
        this.childNodes = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            this.childNodes[i] = fields[i].getName();
        }
    }

    public String[] getAttributes(){
        return this.attributes;
    }




    public String getClassName(){
        return getClassName(this.getClass());
    }

    private String getClassName(Class clz) {
        if (clz != null) {
            String fullName = clz.getName();
            String[] names = fullName.split("\\.");
            return names[names.length - 1];
        }
        return null;
    }

}
