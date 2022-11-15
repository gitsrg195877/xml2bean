package com.srg.util;

/**
 * @author: EQ-SRG
 * @create: 2022/7/11
 * @description:
 **/
public class ReflectUtils {


    public static String getClassName(Class clz){
        if(clz != null){
            String fullName = clz.getName();
            String[] names = fullName.split("\\.");
            return names[names.length-1];
        }
        return null;
    }

}
