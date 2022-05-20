package com.lsj.compiler;

import com.google.auto.service.AutoService;
import com.lsj.arouter_annotations.Parameter;
import com.lsj.compiler.utils.ProcessorConfig;
import com.lsj.compiler.utils.ProcessorUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * 参数注解处理器
 *
 * @date: 2022/5/19
 * @author: linshujie
 */
@AutoService(Processor.class)// 启用服务
@SupportedAnnotationTypes({ProcessorConfig.PARAMETER_PACKAGE})// 注解
@SupportedSourceVersion(SourceVersion.RELEASE_7)// 环境的版本
public class ParameterProcessor extends AbstractProcessor {
    // 操作Element的工具类（类，函数，属性，其实都是Element）
    private Elements elementUtils;
    // Message用来打印 日志相关信息
    private Messager messager;
    // 文件生成器， 类 资源 等，就是最终要生成的文件 是需要Filer来完成的
    private Filer filer;
    // type(类信息)的工具类，包含用于操作TypeMirror的工具方法
    private Types typeTool;

    // key:类节点, value:被@Parameter注解的属性集合
    private Map<TypeElement, List<Element>> tempParameterMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        initData(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        messager.printMessage(Diagnostic.Kind.NOTE, "ParameterProcessor");

        if (!ProcessorUtils.isEmpty(annotations)) {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Parameter.class);

            if (!ProcessorUtils.isEmpty(elements)) {
                for (Element element : elements) {
                    // 获取字段节点的上一个节点。（例如：属性节点父节点是类节点）
                    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

                    //缓存机制
                    if (tempParameterMap.containsKey(enclosingElement)) {
                        tempParameterMap.get(enclosingElement).add(element);
                    } else {
                        List<Element> fields = new ArrayList<>();
                        fields.add(element);
                        tempParameterMap.put(enclosingElement, fields); // 加入缓存
                    }
                }

                // 判断是否有需要生成的类文件
                if (ProcessorUtils.isEmpty(tempParameterMap)) return true;

                TypeElement activityType = elementUtils.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE);
                TypeElement parameterType = elementUtils.getTypeElement(ProcessorConfig.AROUTER_AIP_PARAMETER_GET);

                // 生成方法
                // Object targetParameter
                ParameterSpec parameterSpec = ParameterSpec
                        .builder(TypeName.OBJECT, ProcessorConfig.PARAMETER_NAME)
                        .build();

                for (Map.Entry<TypeElement, List<Element>> entry : tempParameterMap.entrySet()) {
                    TypeElement typeElement = entry.getKey();

                    // 判断注解是否在类上面
                    if (!typeTool.isSubtype(typeElement.asType(), activityType.asType())) {
                        throw new RuntimeException("@Parameter注解目前仅限用于Activity类之上");
                    }

                    // 是Activity
                    // 获取类名 == Personal_MainActivity
                    ClassName className = ClassName.get(typeElement);

                    // 方法生成成功
                    ParameterFactory factory = new ParameterFactory.Builder(parameterSpec)
                            .setMessager(messager)
                            .setClassName(className)
                            .build();

                    factory.addFirstStatement();

                    for (Element element : entry.getValue()) {
                        factory.buildStatement(element);
                    }

                    // 最终生成的类文件名（类名$$Parameter） 例如：Personal_MainActivity$$Parameter
                    String finalClassName = typeElement.getSimpleName() + ProcessorConfig.PARAMETER_FILE_NAME;
                    messager.printMessage(Diagnostic.Kind.NOTE,
                            "APT生成获取参数类文件："
                                    + className.packageName()
                                    + "."
                                    + finalClassName);

                    // 开始生成文件，例如：PersonalMainActivity$$Parameter
                    try {
                        JavaFile.builder(className.packageName(), // 包名
                                TypeSpec.classBuilder(finalClassName) // 类名
                                        .addSuperinterface(ClassName.get(parameterType)) //  implements ParameterGet 实现ParameterLoad接口
                                        .addModifiers(Modifier.PUBLIC) // public修饰符
                                        .addMethod(factory.build()) // 方法的构建（方法参数 + 方法体）
                                        .build()) // 类构建完成
                                .build() // JavaFile构建完成
                                .writeTo(filer); // 文件生成器开始生成类文件
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }


        return true;
    }

    /**
     * 初始化必要的参数
     * @param processingEnv
     */
    private void initData(ProcessingEnvironment processingEnv) {
        elementUtils = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
        typeTool = processingEnv.getTypeUtils();
    }



}
