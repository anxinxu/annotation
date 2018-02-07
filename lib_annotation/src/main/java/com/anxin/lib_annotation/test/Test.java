package com.anxin.lib_annotation.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by anxin on 2018/2/5.
 * <p>
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Test {}
