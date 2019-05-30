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
package io.mykit.delay.common.conf;

import org.springframework.beans.BeansException;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/29
 * @description 程序运行环境上下文
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE - 50)
public class AppEnvContext implements EnvironmentAware, ApplicationContextAware {

    private static Environment        env;
    private static ApplicationContext ctx;

    public static String getProperty(String key) {

        return env.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {

        String v = getProperty(key);
        return StringUtils.isEmpty(v) ? defaultValue : v;
    }

    /**
     * <code>
     * getRelaxedPropertyResolver( "spring.datasource.").getProperty("url");
     * <p>
     * </code>
     */
    public static RelaxedPropertyResolver getRelaxedPropertyResolver(String prefix) {

        RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(env, prefix);
        return propertyResolver;
    }

    public static Environment getEnv() {
        return env;
    }

    public static ApplicationContext getCtx() {
        return ctx;
    }

    protected void finalize() throws Throwable {
        super.finalize();
        env = null;
    }

    @Override
    public void setEnvironment(Environment environment) {
        env = environment;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ctx = applicationContext;
    }
}
