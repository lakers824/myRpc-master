package com.xlfc.common.annotion;



import org.springframework.context.annotation.Import;

import java.lang.annotation.*;


@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
//@Import通过快速导入的方式实现把实例加入spring的IOC容器中
@Import(CustomScannerRegistrar.class)
@Documented
public @interface RpcComponentScan {
    String[] basePackages();
}
