package com.lsj.compiler;

import com.google.auto.service.AutoService;

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
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

/**
 * @date: 2022/4/8
 * @author: linshujie
 */
@AutoService(Processor.class)// 启用服务
@SupportedAnnotationTypes({"com.lsj.arouter_annotations.ARouter"})// 注解
@SupportedSourceVersion(SourceVersion.RELEASE_7)// 环境的版本
@SupportedOptions("student")// 接收 安卓工程传递过来的参数
public class ARouterProcessor extends AbstractProcessor {

    // 操作Element的工具类（类，函数，属性，其实都是Element）
    private Elements elementUtils;
    // Message用来打印 日志相关信息
    private Messager messager;
    // 文件生成器， 类 资源 等，就是最终要生成的文件 是需要Filer来完成的
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        elementUtils = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();

        String value = processingEnv.getOptions().get("student");
        messager.printMessage(Diagnostic.Kind.NOTE," >>>>>>> " + value);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return false;
    }
}
