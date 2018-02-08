package com.anxin.inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by anxin on 2018/2/8.
 * <p>
 */

public class Inject {

    private Inject() {
    }

    @SuppressWarnings("unchecked")
    public static Unbinder bind(Object target) {
        if (target != null) {
            Class<?> tClass = target.getClass();
            String tClassName = tClass.getName();
            if (tClassName.startsWith("android.") || tClassName.startsWith("java.")) return null;

            try {
                Class<?> cls = tClass.getClassLoader().loadClass(tClassName + "_BindView");
                Constructor<? extends Unbinder> tConstructor = (Constructor<? extends Unbinder>) cls.getConstructor(tClass);
                return tConstructor.newInstance(target);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
