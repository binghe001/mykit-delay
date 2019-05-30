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

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/29
 * @description 系统常量类
 */
public class Constants {

    public static final String USER_DIR          = "user.dir";
    public static       String SOFT_HOME_KEY     = "soft.home";
    public static       String SOFT_LOG_HOME_KEY = "soft.logs";
    public static       String SOFT_HOME         = System.getProperty(SOFT_HOME_KEY);
    public static       String SOFT_LOG_HOME     = System.getProperty(SOFT_LOG_HOME_KEY);

    public static final String HEALTH_INDICATOR_NAME = "mykit-delay";
    public static final String CODE_UTF8 = "UTF-8";
    public static final String RUN = "run";
    public static final String IS_CLUSTER = "isCluster";
    public static final String BUCKET_SIZE = "bucketSize";
    public static final String PREFIX = "prefix";
    public static final String NAMESPACE = "namespace";
    public static final String IS_MASTER = "isMaster";
    public static final String MYKIT_DELAY_REGISTRY_ENABLE = "mykit.delay.registry.enable";
}