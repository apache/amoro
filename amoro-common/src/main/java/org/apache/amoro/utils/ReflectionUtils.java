package org.apache.amoro.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ReflectionUtils {

    private final static Map<String,Class> CLASS_MAP = new HashMap<>();

    public static Object invoke(String className, String methodName) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Class<?> classRef =  CLASS_MAP.computeIfAbsent(className, key-> {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        Method method = classRef.getDeclaredMethod(methodName);
        return method.invoke(null);
    }
}
