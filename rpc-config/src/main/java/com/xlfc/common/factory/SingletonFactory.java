package com.xlfc.common.factory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SingletonFactory {
    private static final Map<String, Object> OBJECT_MAP=new ConcurrentHashMap<>();
    public SingletonFactory() {
    }
    public static <T> T getInstance(Class<T> c) {
        if (c==null){
            throw new IllegalArgumentException();
        }
        String key = c.toString();
        if (OBJECT_MAP.containsKey(key)){
            //cast强制转换
            return c.cast(OBJECT_MAP.get(key));
        }else {
            return c.cast(OBJECT_MAP.computeIfAbsent(key,p->{
                try {
                    //返回构造方法，构造实例
                    return c.getDeclaredConstructor().newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e.getMessage(),e);
                }
            }));
        }
    }
}
