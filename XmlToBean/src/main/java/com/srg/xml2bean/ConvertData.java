package com.srg.xml2bean;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

/**
 * @author : SRG
 * @create : 2022/3/11
 * @describe :
 **/


public class ConvertData {


    /**
     * 根据常用数据类型的字节码对象，通过判断，对字符串初始化相应类型的属性值
     **/
    public static Object convertData(Class<?> cla, String str) throws Exception {
        if(str == null){
            return null;
        }
        //基本数据类型的封装类
        else if (Integer.class == cla) {
            Integer data = Integer.valueOf(str);
            return data;
        } else if (Long.class == cla) {
            Long data = Long.valueOf(str);
            return data;
        } else if (Byte.class == cla) {
            Byte data = Byte.valueOf(str);
            return data;
        }
        else if (Short.class == cla) {
            Short data = Short.valueOf(str);
            return data;
        }else if (Boolean.class == cla) {
            Boolean data = Boolean.valueOf(str);
            return data;
        }else if (Double.class == cla) {
            Double data = Double.valueOf(str);
            return data;
        }else if (Float.class == cla) {
            Float data = Float.valueOf(str);
            return data;
        }

        //基本数据类型
        else if(int.class == cla){
            int i = Integer.parseInt(str);
            return i;
        }else if(long.class == cla){
            long i = Long.parseLong(str);
            return i;
        }else if(byte.class == cla){
            byte i = Byte.parseByte(str);
            return i;
        }else if(short.class == cla){
            short i = Short.parseShort(str);
            return i;
        }else if(boolean.class == cla){
            boolean i = Boolean.parseBoolean(str);
            return i;
        }else if(double.class == cla){
            double i = Double.parseDouble(str);
            return i;
        }else if(float.class == cla){
            float i = Float.parseFloat(str);
            return i;
        }

        //字符串
        else if (String.class == cla) {
            return str;
        }

        //Date类型
        else if (Date.class == cla) {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            Date datetime = null;
            try {
                datetime = format.parse(str);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return datetime;
        }

        //LocalDate
        else if(LocalDate.class == cla){
            String[] split = str.split("\\-");
            int year = Integer.parseInt(split[0]);
            int month = Integer.parseInt(split[1]);
            int day = Integer.parseInt(split[2]);
            LocalDate localDate = LocalDate.of(year, month, day);
            return localDate;
        }


        //下面需要根据实际情况拓展相应条件的枚举类

        //如果没有相应的数据类型，则抛出异常
        else {
            Exception exception = new Exception("未定义相应的类型转换");
            throw exception;
        }

    }
}
