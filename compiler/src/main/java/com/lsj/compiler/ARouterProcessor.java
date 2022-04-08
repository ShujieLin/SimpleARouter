package com.lsj.compiler;

import com.google.auto.service.AutoService;
import com.lsj.arouter_annotations.ARouter;
import com.lsj.arouter_annotations.bean.RouterBean;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * @date: 2022/4/8
 * @author: linshujie
 */
@AutoService(Processor.class)// 启用服务
@SupportedAnnotationTypes({"com.lsj.arouter_annotations.ARouter"})// 注解
@SupportedSourceVersion(SourceVersion.RELEASE_7)// 环境的版本
@SupportedOptions("argument")// 接收 安卓工程传递过来的参数
public class ARouterProcessor extends AbstractProcessor {

    // 操作Element的工具类（类，函数，属性，其实都是Element）
    private Elements elementUtils;
    // Message用来打印 日志相关信息
    private Messager messager;
    // 文件生成器， 类 资源 等，就是最终要生成的文件 是需要Filer来完成的
    private Filer filer;
    // type(类信息)的工具类，包含用于操作TypeMirror的工具方法
    private Types typeTool;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        elementUtils = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();

        String value = processingEnv.getOptions().get("argument");
        messager.printMessage(Diagnostic.Kind.NOTE," >>>>>>> " + value);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        messager.printMessage(Diagnostic.Kind.NOTE,"process");
        if (annotations.isEmpty()) return false;
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(ARouter.class);

//        elementUtils.getTypeElement(ProcessorConfig)

        for (Element element:
             elements) {
//            JavaFileUtils.writeJavaFileHelloWold(filer,messager);
//            JavaFileUtils.writeFindTargetClass(element, elementUtils,messager,filer);

            String className = element.getSimpleName().toString();
            messager.printMessage(Diagnostic.Kind.NOTE,"使用了ARouter注解类的类有 ：" + className);
            ARouter aRouter = element.getAnnotation(ARouter.class);
            new RouterBean.Builder()
                    .addGroup(aRouter.group())
                    .addPath(aRouter.path())
                    .addElement(element)
                    .build();

            TypeMirror elementMirror = element.asType();
//            if (typeTool.isSubtype(elementMirror,a))

        }

        return false;
    }

}
