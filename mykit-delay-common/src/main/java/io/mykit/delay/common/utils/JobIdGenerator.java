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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/29
 * @description  Job ID 生成器
 *  <pre>
 *      -DmachineId=num 机器标识
 *  </pre>
 */
public class JobIdGenerator {
    public static final Logger LOGGER           = LoggerFactory.getLogger(JobIdGenerator.class);
    public static final int       DATACENTER       = 2;
    public static final int       DEFAULT_MACHINED = 1;
    public static final String    MACHINE_ID       = "machineId";
    private static      SnowFlake snowFlake        = null;

    static {
        int m = machinedId();
        LOGGER.info(" machined {}", m);
        snowFlake = new SnowFlake(DATACENTER, m);
    }

    private static int machinedId() {
        //通过获取IP地址最后一位来获取
        String MACHINED = System.getProperty(MACHINE_ID);
        if (!StringUtils.isEmpty(MACHINED)) {
            try {
                return Integer.parseInt(MACHINED);
            } catch (Exception e) {
                return DEFAULT_MACHINED;
            }
        }
        return DEFAULT_MACHINED;
    }

    public static long getLongId() {
        return snowFlake.nextId();
    }

    public static String getStringId() {
        return String.valueOf(snowFlake.nextId());
    }

//    public static void main(String[] args) {
//       final Map<String,Object> c= Maps.newHashMap();
//       final Map<String,Object> c1=Maps.newHashMap();
//       final AtomicLong atomicLong=new AtomicLong(0);
//        for(int i=0;i<20;i++){
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    for(int i=0;i<5000;i++){
//                        String k=getStringId();
//                        atomicLong.incrementAndGet();
//                        System.out.println(k+"=="+c.containsKey(k)+"--"+atomicLong.get());
//                        if(!c.containsKey(k)){
//                            c.put(k,i);
//                        }else{
//                            throw new RuntimeException(String.format("id %s重复了",k));
//                        }
//
//
//                    }
//                    c1.put(Thread.currentThread().getName(),"");
//                }
//            }).start();
//        }
//        while(c1.size()>=20){
//            System.out.println(c.size());
//        }
//        try {
//            Thread.sleep(5000000L);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
}
