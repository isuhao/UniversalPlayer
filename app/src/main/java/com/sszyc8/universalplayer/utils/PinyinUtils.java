package com.sszyc8.universalplayer.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;


public class PinyinUtils {

    /**
     * 根据传入的字符串(包含汉字),得到拼音
     *
     * @param str 字符串
     * @return
     */
    public static String getPinyin(String str) {

        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        //	设置大小写
        format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        //  设置不显示音标
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

        StringBuilder sb = new StringBuilder();

        char[] charArray = str.toCharArray();
        //  循环数组
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            // 如果是空格, 跳过
            if (Character.isWhitespace(c)) {
                continue;
            }
            if (c >= -127 && c < 128) {
                // 肯定不是汉字
                sb.append(c);
            } else {
                String s = "";
                try {
                    s = PinyinHelper.toHanyuPinyinStringArray(c, format)[0];
                    sb.append(s);
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                    sb.append(s);
                }
            }
        }
        return sb.toString();
    }


    /**
     * 小写字母转换成大写字母
     *
     * @param str
     * @return
     */
    public static String exChange(String str) {
        StringBuffer sb = new StringBuffer();
        if (str != null) {
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                //  大写不转换
//                if (Character.isUpperCase(c)) {
//                    sb.append(Character.toLowerCase(c));
//                } else
                if (Character.isLowerCase(c)) {
                    sb.append(Character.toUpperCase(c));
                } else
                    return str;
            }
        }

        return sb.toString();
    }
}
