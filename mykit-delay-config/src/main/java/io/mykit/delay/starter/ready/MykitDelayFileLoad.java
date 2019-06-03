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
package io.mykit.delay.starter.ready;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/31
 * @description 加载Properties文件
 */
public class MykitDelayFileLoad {

    public static final String LOG_PATH = "log.path";
    public static final String CLASS_PATH = "class.path";
    public static final String DEFAULT_CQ = "default.cq";

    private volatile static Properties mProperties;

    static{
        mProperties = new Properties();
        InputStream in = MykitDelayFileLoad.class.getClassLoader().getResourceAsStream("properties/starter.properties");
        try {
            mProperties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getStringValue(String key){
        return mProperties.getProperty(key, "");
    }
}
