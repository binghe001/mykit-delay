/**
 * Copyright 2019-2999 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mykit.delay.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/29
 * @description 日期工具类
 */
public class DateUtils {
    public static final String FORMAT_DEFAULT                 = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_YYYY_MM_DD_HH_MM_SS_SSS = "yyyy-MM-dd HH:mm:ss SSS";
    public static final String FORMAT_TIMESTAMP               = "yyyy-MM-dd_HH_mm_ss_SSS";
    public static final String FORMAT_YYYYMMDD                = "yyyyMMdd";

    public static final String FORMAT_DATE = "yyyy-MM-dd";

    public static final String FORMAT_MONTH = "yyyy-MM";

    public static final String FORMAT_TIME = "HH:mm:ss";

    public static final String FORMAT_SHORT_DATE_TIME = "MM-dd HH:mm";

    public static final String FORMAT_DATE_TIME = FORMAT_DEFAULT;

    public static final String FORMAT_NO_SECOND = "yyyy-MM-dd HH:mm";

    public static final String FORMAT_JAPAN = "MM.dd(EEE) HH";

    public static final String FORMAT_CHINESE_NO_SECOND = "yyyy年MM月dd日 HH:mm";

    public static final String FORMAT_CHINESE_NO_SECOND_1 = "yyyy年MM月dd日HH:mm";

    public static final String FORMAT_CHINESE = "yyyy年MM月dd日 HH点mm分";

    public static final String FROMAT_CHAINESE_WEEK_SECOND = "yyyy-MM-dd(E) HH:mm";

    public static final int TYPE_HTML_SPACE = 2;

    public static final int TYPE_DECREASE_SYMBOL = 3;

    public static final int TYPE_SPACE = 4;

    public static final int                           TYPE_NULL = 5;
    private static Map<String, SimpleDateFormat> formaters = new HashMap<String, SimpleDateFormat>();

    static {
        SimpleDateFormat defaultFormater = new SimpleDateFormat(FORMAT_DEFAULT, Locale.CHINA);
        formaters.put(FORMAT_DEFAULT, defaultFormater);
        formaters.put(FORMAT_DATE, new SimpleDateFormat(FORMAT_DATE, Locale.CHINA));
        formaters.put(FORMAT_MONTH, new SimpleDateFormat(FORMAT_MONTH, Locale.CHINA));
        formaters.put(FORMAT_TIME, new SimpleDateFormat(FORMAT_TIME, Locale.CHINA));
        formaters.put(FORMAT_SHORT_DATE_TIME, new SimpleDateFormat(FORMAT_SHORT_DATE_TIME, Locale.CHINA));
        formaters.put(FORMAT_CHINESE_NO_SECOND, new SimpleDateFormat(FORMAT_CHINESE_NO_SECOND, Locale.CHINA));
        formaters.put(FORMAT_CHINESE, new SimpleDateFormat(FORMAT_CHINESE, Locale.CHINA));
        formaters.put(FORMAT_DATE_TIME, defaultFormater);
        formaters.put(FORMAT_NO_SECOND, new SimpleDateFormat(FORMAT_NO_SECOND, Locale.CHINA));
        formaters.put(FORMAT_JAPAN, new SimpleDateFormat(FORMAT_JAPAN, Locale.JAPAN));
        formaters.put(FORMAT_CHINESE_NO_SECOND_1, new SimpleDateFormat(FORMAT_CHINESE_NO_SECOND_1, Locale.CHINA));
        formaters.put(FROMAT_CHAINESE_WEEK_SECOND, new SimpleDateFormat(FROMAT_CHAINESE_WEEK_SECOND, Locale.CHINA));
        formaters.put(FORMAT_YYYY_MM_DD_HH_MM_SS_SSS, new SimpleDateFormat(FORMAT_YYYY_MM_DD_HH_MM_SS_SSS, Locale.CHINA));
        formaters.put(FORMAT_TIMESTAMP, new SimpleDateFormat(FORMAT_TIMESTAMP, Locale.CHINA));
    }

    public static Map<String, SimpleDateFormat> getFormaters() {
        return formaters;
    }

    /**
     * 使用给定的 pattern 对日期进格式化为字符串
     *
     * @param date    待格式化的日期
     * @param pattern 格式字符串
     * @return 格式化后的日期字符串
     */
    public static String format(Date date, String pattern) {
        SimpleDateFormat dateFormat;
        if (formaters.containsKey(pattern)) {
            dateFormat = formaters.get(pattern);
        } else {
            dateFormat = new SimpleDateFormat(pattern, Locale.CHINA);
        }
        return dateFormat.format(date);
    }

    /**
     * 以默认日期格式(yyyy-MM-dd HH:mm:ss)对日期进行格式化
     *
     * @param date 待格式化的日期
     * @return 格式化后的日期字符串
     */
    public static String format(Date date) {
        return formaters.get(FORMAT_DEFAULT).format(date);
    }


    public static String format(Date date,
                                String format,
                                int type) {
        if (date != null) {
            //---------------------------------
            // 日期不为空时才格式
            //---------------------------------
            try {
                //---------------------------------
                // 调用SimpleDateFormat来格式化
                //---------------------------------
                return new SimpleDateFormat(format).format(date);
            } catch (Exception e) {
                //---------------------------------
                // 格式化失败后，返回一个空串
                //---------------------------------
                return "";
            }
        } else {
            //---------------------------------
            // 如果传入日期为空，则根据类型返回结果
            //---------------------------------
            switch (type) {
                case TYPE_HTML_SPACE: // '\002'
                    return "&nbsp;";

                case TYPE_DECREASE_SYMBOL: // '\003'
                    return "-";

                case TYPE_SPACE: // '\004'
                    return " ";

                case TYPE_NULL:
                    return null;

                default:
                    //---------------------------------
                    // 默认为空串
                    //---------------------------------
                    return "";
            }
        }
    }

    /**
     * 将给定字符串解析为对应格式的日期,循环尝试使用预定义的日期格式进行解析
     *
     * @param str 待解析的日期字符串
     * @return 解析成功的日期，解析失败返回null
     */
    public static Date parse(String str) {
        Date date = null;
        for (String _pattern : formaters.keySet()) {
            if (_pattern.getBytes().length == str.getBytes().length) {
                try {
                    date = formaters.get(_pattern).parse(str);
                    //格式化成功则退出
                    break;
                } catch (ParseException e) {
                    //格式化失败，继续尝试下一个
                    //e.printStackTrace();
                }
            } else if (_pattern.equals(FORMAT_JAPAN)) {
                try {
                    date = formaters.get(_pattern).parse(str);
                    //格式化成功则退出
                    break;
                } catch (ParseException e) {
                }
            }
        }
        return date;
    }
}
