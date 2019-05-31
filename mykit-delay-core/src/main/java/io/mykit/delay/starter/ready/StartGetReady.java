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

import io.mykit.delay.common.utils.Constants;
import io.mykit.delay.queue.core.ConsumeQueueProvider;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/29
 * @description 准备启动类
 */
public class StartGetReady {

    private static final String LOG_PATH = MykitDelayFileLoad.getStringValue(MykitDelayFileLoad.LOG_PATH);
    private static final String CLASS_PATH = MykitDelayFileLoad.getStringValue(MykitDelayFileLoad.CLASS_PATH);
    private static final String DEFAULT_CQ = MykitDelayFileLoad.getStringValue(MykitDelayFileLoad.DEFAULT_CQ);

    public static void ready() {
        if (System.getProperty(Constants.SOFT_HOME_KEY) == null) {
            System.setProperty(Constants.SOFT_HOME_KEY, getClazzPathUrl());
        }
        if (System.getProperty(Constants.SOFT_LOG_HOME_KEY) == null) {
            System.setProperty(Constants.SOFT_LOG_HOME_KEY, "".concat(LOG_PATH));
        }
        String defaultCQKey = ConsumeQueueProvider.class.getName();
        if(System.getProperty(defaultCQKey) == null){
            System.setProperty(defaultCQKey, DEFAULT_CQ);
        }
    }

    private static String getClazzPathUrl() {
        File path = null;
        try {
            path = new File(ResourceUtils.getURL(CLASS_PATH).getPath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (!path.exists())
            path = new File("");
        return path.getAbsolutePath();
    }
}
