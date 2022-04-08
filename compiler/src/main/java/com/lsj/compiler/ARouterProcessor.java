package com.lsj.compiler;

import com.google.auto.service.AutoService;
import com.lsj.arouter_annotations.ARouter;
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
import javax.lang.model.util.Elements;
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

        if (annotations.isEmpty())
            return false;
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(ARouter.class);
        for (Element element:
             elements) {
//            JavaFileUtils.writeJavaFileHelloWold(filer,messager);
            String packageName = elementUtils.getPackageOf(element).getQualifiedName().toString();
            String className = element.getSimpleName().toString();
            messager.printMessage(Diagnostic.Kind.NOTE,"注释的类有 ：" + className);
            String finalClassName = className + "$ARouter";

            ARouter aRouter = element.getAnnotation(ARouter.class);

            /**
             模板：
             public class MainActivity3$$$$$$$$$ARouter {

                 public static Class findTargetClass(String path) {
                    return path.equals("/app/MainActivity3") ? MainActivity3.class : null;
                 }

             }
             */
            //方法
            MethodSpec findTargetClass = MethodSpec.methodBuilder("findTargetClass")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(String.class,"path")
                    .returns(Class.class)
                    .addStatement("return path.equals($S) ? $T.class : null",
                            aRouter.path(),
                            ClassName.get((TypeElement) element))
                    .build();

            //类
            TypeSpec typeSpec = TypeSpec.classBuilder(finalClassName)
                    .addMethod(findTargetClass)
                    .addModifiers(Modifier.PUBLIC)
                    .build();

            //包
            JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
                    .build();

            //生成java文件
            try {
                javaFile.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
                messager.printMessage(Diagnostic.Kind.NOTE,"生成" + finalClassName + " 异常。异常信息 ： " + e.getMessage());
            }
        }

        return false;
    }
}
