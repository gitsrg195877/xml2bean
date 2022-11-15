package com.srg.util;

/**
 * @author: EQ-SRG
 * @create: 2022/7/11
 * @description:
 **/
public class StringUtils {

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
