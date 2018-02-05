package com.anxin.lib_annotation.test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by anxin on 2018/2/5.
 * <p>
 */

public class TestAnnotation {

    public static void main(String[] args) {
        testClass();
//        testRuntime();
    }

    private static void testClass(){
        Class<TestClassAnnotation> tClass = TestClassAnnotation.class;
        Class<TestClass> tAClass = TestClass.class;
        TestClass tAnnotation = tClass.getAnnotation(tAClass);
        String tValue = tAnnotation.value();
        System.out.print("testClass() ->  default value = " + tValue);
    }

    private static void testRuntime(){
        Class<TestRuntimeAnnotation> tClass = TestRuntimeAnnotation.class;
        Class<TestRuntime> tAClass = TestRuntime.class;
        TestRuntime tAnnotation = tClass.getAnnotation(tAClass);
        String tValue = tAnnotation.value();
        System.out.print("testRuntime() ->  default value = " + tValue);
    }

    @TestClass
    private class TestClassAnnotation{

    }

    @TestRuntime
    private class TestRuntimeAnnotation{

    }

    @Retention(RetentionPolicy.CLASS)
    @interface TestClass{
        String value() default "test class annotation";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface TestRuntime{
        String value() default "test runtime annotation";
    }
}
