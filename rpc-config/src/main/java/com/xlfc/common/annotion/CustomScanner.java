package com.xlfc.common.annotion;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
        import org.springframework.core.type.filter.AnnotationTypeFilter;

        import java.lang.annotation.Annotation;


public class CustomScanner extends ClassPathBeanDefinitionScanner {

    public CustomScanner(BeanDefinitionRegistry registry, Class<? extends Annotation> annoType) {
        //registry：注册器，实际上就是spring容器
        super(registry);
        super.addIncludeFilter(new AnnotationTypeFilter(annoType));
    }

    @Override
    public int scan(String... basePackages) {
        //扫描包下的类，符合条件的类作为BeanDefinition，注册到registry中。
        return super.scan(basePackages);
    }
}
