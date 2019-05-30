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

import com.google.common.base.Joiner;

import java.util.List;
import com.google.common.collect.Lists;


/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/29
 * @description 名称工具类
 */
public class NamedUtil {
    public static final String SPLITE_CHAR = ":";
    public static final String LOCK_CHAR   = "LOCK";

    public static String buildBucketName(String prefix, String name, int index) {
        List<Object> lst = Lists.newArrayList();
        lst.add(prefix);
        lst.add(name);
        lst.add(index);
        return Joiner.on(SPLITE_CHAR).join(lst);
    }

    public static String buildPoolName(String prefix, String name, String pool) {
        return Joiner.on(SPLITE_CHAR).join(Lists.newArrayList(prefix, name, pool));
    }

    public static String buildRealTimeName(String prefix, String name, String readTimeName) {
        return Joiner.on(SPLITE_CHAR).join(Lists.newArrayList(prefix, name, readTimeName));
    }

    public static String buildLockName(String prefix) {
        return Joiner.on(SPLITE_CHAR).join(Lists.newArrayList(prefix.concat(":" + LOCK_CHAR)));
    }
}
