package com.lsj.arouter_annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description:
 * 参数
 *
 * @date: 2022/5/19
 * @author: linshujie
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface Parameter {
    String name() default "";
}
