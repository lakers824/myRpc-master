package com.xlfc.common.annotion;

import java.lang.annotation.*;


@Documented
//Documented表示可以用来修饰其他注解
//relention配置注解的生命周期
@Retention(RetentionPolicy.RUNTIME)
//Target作用范围表示在接口、类
@Target({ElementType.TYPE})
//Inherited表示子类可以继承该注解
@Inherited
public @interface Service {
    String interfaceName() default "";

    String version() default "";

    String group() default "";
}
