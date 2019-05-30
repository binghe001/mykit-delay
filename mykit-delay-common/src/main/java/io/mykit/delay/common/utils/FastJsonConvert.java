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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/29
 * @description Json转化类
 */
public class FastJsonConvert {
    private static final SerializerFeature[] featuresWithNullValue = {SerializerFeature.WriteMapNullValue,
            SerializerFeature.WriteNullBooleanAsFalse,
            SerializerFeature.WriteNullListAsEmpty, SerializerFeature.WriteNullNumberAsZero, SerializerFeature
            .WriteNullStringAsEmpty};

    /**
     * <B>方法名称：</B>将JSON字符串转换为实体对象<BR>
     * <B>概要说明：</B>将JSON字符串转换为实体对象<BR>
     *
     * @param data  JSON字符串
     * @param clzss 转换对象
     * @return T
     */
    public static <T> T convertJSONToObject(String data, Class<T> clzss) {
        try {
            T t = JSON.parseObject(data, clzss);
            return t;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * <B>方法名称：</B>将JSONObject对象转换为实体对象<BR>
     * <B>概要说明：</B>将JSONObject对象转换为实体对象<BR>
     *
     * @param data  JSONObject对象
     * @param clzss 转换对象
     * @return T
     */
    public static <T> T convertJSONToObject(JSONObject data, Class<T> clzss) {
        try {
            T t = JSONObject.toJavaObject(data, clzss);
            return t;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * <B>方法名称：</B>将JSON字符串数组转为List集合对象<BR>
     * <B>概要说明：</B>将JSON字符串数组转为List集合对象<BR>
     *
     * @param data  JSON字符串数组
     * @param clzss 转换对象
     * @return List<T>集合对象
     */
    public static <T> List<T> convertJSONToArray(String data, Class<T> clzss) {
        try {
            List<T> t = JSON.parseArray(data, clzss);
            return t;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * <B>方法名称：</B>将List<JSONObject>转为List集合对象<BR>
     * <B>概要说明：</B>将List<JSONObject>转为List集合对象<BR>
     *
     * @param data  List<JSONObject>
     * @param clzss 转换对象
     * @return List<T>集合对象
     */
    public static <T> List<T> convertJSONToArray(List<JSONObject> data, Class<T> clzss) {
        try {
            List<T> t = new ArrayList<T>();
            for (JSONObject jsonObject : data) {
                t.add(convertJSONToObject(jsonObject, clzss));
            }
            return t;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * <B>方法名称：</B>将对象转为JSON字符串<BR>
     * <B>概要说明：</B>将对象转为JSON字符串<BR>
     *
     * @param obj 任意对象
     * @return JSON字符串
     */
    public static String convertObjectToJSON(Object obj) {
        try {
            String text = JSON.toJSONString(obj);
            return text;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * <B>方法名称：</B>将对象转为(JSON字符串)<BR>
     * <B>概要说明：</B>将对象转为(JSON字符串)<BR>
     *
     * @param obj 任意对象
     * @return JSON字符串
     */
    public static String convertObjectToJSONBracket(Object obj) {
        try {
            String text = JSON.toJSONString(obj);
            return "(" + text + ")";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * <B>方法名称：</B>将对象转为JSONObject对象<BR>
     * <B>概要说明：</B>将对象转为JSONObject对象<BR>
     *
     * @param obj 任意对象
     * @return JSONObject对象
     */
    public static JSONObject convertObjectToJSONObject(Object obj) {
        try {
            JSONObject jsonObject = (JSONObject) JSONObject.toJSON(obj);
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * <B>方法名称：</B><BR>
     * <B>概要说明：</B><BR>
     */
    public static String convertObjectToJSONWithNullValue(Object obj) {
        try {
            String text = JSON.toJSONString(obj, featuresWithNullValue);
            return text;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
