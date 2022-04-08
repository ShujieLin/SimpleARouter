package com.lsj.compiler.utils;

import com.lsj.arouter_annotations.ARouter;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

/**
 * @date: 2022/4/8
 * @author: linshujie
 */
public class JavaFileUtils {
    public static void writeJavaFileHelloWold(Filer filer, Messager messager) {
        /** package com.example.helloworld;

         public final class HelloWorld {

         public static void main(String[] args) {
         System.out.println("Hello, JavaPoet!");
         }
         }**/
        MethodSpec methodSpec = MethodSpec.methodBuilder("main")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(String[].class, "args")
                .addStatement("$T.out.println($S)", System.class, "hello,javapoet!")
                .build();

        TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
                .addMethod(methodSpec)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .build();

        JavaFile javaFile = JavaFile.builder("com.lsj.simplearouter", helloWorld)
                .build();

        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
            messager.printMessage(Diagnostic.Kind.NOTE,"正常java文件失败，异常 : " + e.getMessage());
        }

    }


    public static void writeFindTargetClass(Element element, Elements elementUtils, Messager messager, Filer filer) {
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
}
