package com.lsj.compiler;

import com.lsj.arouter_annotations.Parameter;
import com.lsj.compiler.utils.ProcessorConfig;
import com.lsj.compiler.utils.ProcessorUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.naming.NameClassPair;
import javax.tools.Diagnostic;

/**
 * @date: 2022/5/19
 * @author: linshujie
 */
public class ParameterFactory {
    private MethodSpec.Builder method;
    private ClassName className;
    private Messager messager;

    public ParameterFactory(Builder builder) {
        this.messager = builder.messager;
        this.className = builder.className;

        // 生成此方法
        // 通过方法参数体构建方法体：public void getParameter(Object target) {
        method = MethodSpec.methodBuilder(ProcessorConfig.PARAMETER_METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(builder.parameterSpec);
    }

    public static class Builder {
        private Messager messager;
        private ClassName className;
        // 方法参数体
        private ParameterSpec parameterSpec;

        public Builder(ParameterSpec parameterSpec) {
            this.parameterSpec = parameterSpec;
        }

        public Builder setMessager(Messager messager) {
            this.messager = messager;
            return this;
        }

        public Builder setClassName(ClassName className) {
            this.className = className;
            return this;
        }

        public ParameterFactory build() {
            if (parameterSpec == null) {
                throw new IllegalArgumentException("parameterSpec方法参数体为空");
            }

            if (className == null) {
                throw new IllegalArgumentException("方法内容中的className为空");
            }

            if (messager == null) {
                throw new IllegalArgumentException("messager为空");
            }

            return new ParameterFactory(this);
        }
    }

    /**
     * 生成代码：
     * className t = (className) targetParameter;
     */
    public void addFirstStatement() {
        method.addStatement("$T t = ($T) " + ProcessorConfig.PARAMETER_NAME, className, className);
    }


    /**
     * 构建方体内容，如：t.s = t.getIntent.getStringExtra("s");
     * @param element 被注解的属性元素
     */
    public void buildStatement(Element element) {
        // 遍历注解的属性节点 生成函数体
        TypeMirror typeMirror = element.asType();

        // 获取 TypeKind 枚举类型的序列号
        int type = typeMirror.getKind().ordinal();

        // 获取属性名 例如： name  age  sex
        String fieldName = element.getSimpleName().toString();

        // 获取注解的值
        String annotationValue = element.getAnnotation(Parameter.class).name();

        // 判断注解的值为空的情况下的处理（注解中有name值就用注解值）
        annotationValue = ProcessorUtils.isEmpty(annotationValue) ? fieldName : annotationValue;

        //生成代码，例如：t.s = t.getIntent.
        String finalValue = "t." + fieldName;
        String methodContent = finalValue + " = t.getIntent().";

        // TypeKind 枚举类型不包含String
        if (type == TypeKind.INT.ordinal()) {
            methodContent += "getIntExtra($S, " + finalValue + ")";  // 有默认值
        } else if (type == TypeKind.BOOLEAN.ordinal()) {
            methodContent += "getBooleanExtra($S, " + finalValue + ")";  // 有默认值
        } else  { // String 类型，没有序列号的提供 需要我们自己完成
            if (typeMirror.toString().equalsIgnoreCase(ProcessorConfig.STRING)) {
                methodContent += "getStringExtra($S)"; // 没有默认值
            }
        }

        // TODO: 2022/5/19
        if (methodContent.endsWith(")")) {
            // t.age = t.getIntent().getBooleanExtra("age", t.age ==  9);
            method.addStatement(methodContent, annotationValue);
        } else {
            messager.printMessage(Diagnostic.Kind.ERROR, "目前暂支持String、int、boolean传参");
        }
    }

    public MethodSpec build() {
        return method.build();
    }
}
