package com.lsj.compiler;

import com.google.auto.service.AutoService;
import com.lsj.arouter_annotations.ARouter;
import com.lsj.arouter_annotations.bean.RouterBean;
import com.lsj.compiler.utils.ProcessorConfig;
import com.lsj.compiler.utils.ProcessorUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
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
 * 注解处理器
 *
 * @date: 2022/4/8
 * @author: linshujie
 */
@AutoService(Processor.class)// 启用服务
@SupportedAnnotationTypes({ProcessorConfig.AROUTER_PACKAGE})// 注解
@SupportedSourceVersion(SourceVersion.RELEASE_7)// 环境的版本
@SupportedOptions({ProcessorConfig.OPTIONS,ProcessorConfig.APT_PACKAGE})// 接收 安卓工程传递过来的参数
public class ARouterProcessor extends AbstractProcessor {

    // 操作Element的工具类（类，函数，属性，其实都是Element）
    private Elements elementUtils;
    // Message用来打印 日志相关信息
    private Messager messager;
    // 文件生成器， 类 资源 等，就是最终要生成的文件 是需要Filer来完成的
    private Filer filer;
    // type(类信息)的工具类，包含用于操作TypeMirror的工具方法
    private Types typeTool;

    //模块名
    private String options;
    //包名
    private String aptPackage;

    //Path缓存
    private Map<String, List<RouterBean>> allPathMap = new HashMap<>();
    //Group缓存
    private Map<String, String> allGroupMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        initData(processingEnv);
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

        options = processingEnv.getOptions().get(ProcessorConfig.OPTIONS);
        aptPackage = processingEnv.getOptions().get(ProcessorConfig.APT_PACKAGE);
        messager.printMessage(Diagnostic.Kind.NOTE,ProcessorConfig.OPTIONS + " : " + options);
        messager.printMessage(Diagnostic.Kind.NOTE,ProcessorConfig.APT_PACKAGE + " : " + aptPackage);

        if (options != null && aptPackage != null) {
            messager.printMessage(Diagnostic.Kind.NOTE, "APT 环境搭建完成....");
        } else {
            messager.printMessage(Diagnostic.Kind.NOTE, "APT 环境有问题，请检查 options 与 aptPackage 为null...");
        }

        printAnnotationsInfos();
    }

    /**
     * 打印组件传输过来的参数
     */
    private void printAnnotationsInfos() {

    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) return false;

        //获取所有被ARouter注解的元素集合
        putAnnotationsInfo2Cache(roundEnv,allPathMap);

        //获取接口com.lsj.arouter_api.ARouterPath
        TypeElement pathType = elementUtils.getTypeElement(ProcessorConfig.AROUTER_API_PATH);

        //获取接口com.lsj.arouter_api.ARouterGroup
        TypeElement groupType = elementUtils.getTypeElement(ProcessorConfig.AROUTER_API_GROUP);
        messager.printMessage(Diagnostic.Kind.NOTE, "pathType = " + pathType.toString());
        messager.printMessage(Diagnostic.Kind.NOTE, "groupType = " + groupType.toString());

        try {
            createPathFileGenerater(pathType);
        } catch (IOException e) {
            e.printStackTrace();
            messager.printMessage(Diagnostic.Kind.NOTE, "在生成PATH代码模板时，异常了 e:" + e.getMessage());
        }

        try {
            createGoupFileGenerater(groupType,pathType);
        } catch (IOException e) {
            e.printStackTrace();
            messager.printMessage(Diagnostic.Kind.NOTE, "在生成Group代码模板时，异常了 e:" + e.getMessage());
        }
        return false;
    }

    /**
     * 生成的代码效果：
     *
     * public class ARouter$$Group$$app implements ARouterGroup {
     *   @Override
     *   public Map<String, Class<? extends ARouterPath>> getGroupMap() {
     *     Map<String, Class<? extends ARouterPath>> groupMap = new HashMap<>();
     *     groupMap.put("app", ARouter$$Path$$app.class);
     *     return groupMap;
     *   }
     * }
     * @param groupType
     * @param pathType
     */
    private void createGoupFileGenerater(TypeElement groupType, TypeElement pathType) throws IOException {
        if (ProcessorUtils.isEmpty(allGroupMap) || ProcessorUtils.isEmpty(allPathMap)) return;

        //  Map<String, Class<? extends ARouterPath>>
        TypeName methodReturns = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(pathType)))
        );

        //public Map<String, Class<? extends ARouterPath>> getGroupMap() {
        MethodSpec.Builder methodBuidler = MethodSpec.methodBuilder(ProcessorConfig.GROUP_METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(methodReturns);

        // Map<String, Class<? extends ARouterPath>> groupMap = new HashMap<>();
        methodBuidler.addStatement("$T<$T, $T> $N = new $T<>()",
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(pathType))), // ? extends ARouterPath
                ProcessorConfig.GROUP_VAR1,
                ClassName.get(HashMap.class));

        //  groupMap.put("personal", ARouter$$Path$$personal.class);
        //	groupMap.put("order", ARouter$$Path$$order.class);
        for (Map.Entry<String, String> entry : allGroupMap.entrySet()) {
            methodBuidler.addStatement("$N.put($S, $T.class)",
                    ProcessorConfig.GROUP_VAR1, // groupMap.put
                    entry.getKey(), // order, personal ,app
                    ClassName.get(aptPackage, entry.getValue()));
        }

        // return groupMap;
        methodBuidler.addStatement("return $N", ProcessorConfig.GROUP_VAR1);

        // 最终生成的类文件名 ARouter$$Group$$ + personal
        String finalClassName = ProcessorConfig.GROUP_FILE_NAME + options;

        messager.printMessage(Diagnostic.Kind.NOTE, "APT生成路由组Group类文件：" +
                aptPackage + "." + finalClassName);

        // 生成类文件：ARouter$$Group$$app
        JavaFile.builder(aptPackage, // 包名
                TypeSpec.classBuilder(finalClassName) // 类名
                        .addSuperinterface(ClassName.get(groupType)) // 实现ARouterLoadGroup接口 implements ARouterGroup
                        .addModifiers(Modifier.PUBLIC) // public修饰符
                        .addMethod(methodBuidler.build()) // 方法的构建（方法参数 + 方法体）
                        .build()) // 类构建完成
                .build() // JavaFile构建完成
                .writeTo(filer); // 文件生成器开始生成类文件
    }

    /**
     * 拿到注解的group、path和类信息
     * @param roundEnv
     * @param allPathMap
     * @return
     */
    private void putAnnotationsInfo2Cache(RoundEnvironment roundEnv, Map<String, List<RouterBean>> allPathMap) {
        //获取添加了注解的类信息和注解信息
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(ARouter.class);

        // 通过Element工具类，获取Activity，Callback类型
        TypeElement activityType = elementUtils.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE);
        //获取类信息
        TypeMirror typeMirror = activityType.asType();

        for (Element element: elements) {
            String className = element.getSimpleName().toString();
            messager.printMessage(Diagnostic.Kind.NOTE,"使用了ARouter注解类的类有 ：" + className);
            ARouter aRouter = element.getAnnotation(ARouter.class);
            RouterBean routerBean = new RouterBean.Builder()
                    .addGroup(aRouter.group())
                    .addPath(aRouter.path())
                    .addElement(element)//存起来，后面能通过他取到其他可能用到的信息
                    .build();

            checkAnnotationsType(typeMirror, element, routerBean);

            if (checkRouterPath(routerBean)){
                putRouterBean2PathCache(routerBean,allPathMap);
            }else {
                messager.printMessage(Diagnostic.Kind.ERROR,"@ARouter注解未按规范配置，如：/app/MainActivity");
            }
        }
    }

    /**
     * 把遍历到的元素routerBean添加到path缓存中
     * @param routerBean
     * @param allPathMap
     */
    private void putRouterBean2PathCache(RouterBean routerBean, Map<String, List<RouterBean>> allPathMap) {
        messager.printMessage(Diagnostic.Kind.NOTE,"ARouter校验成功:" + routerBean.toString());
        List<RouterBean> routerBeans = allPathMap.get(routerBean.getGroup());
        if (ProcessorUtils.isEmpty(routerBeans)){
            routerBeans = new ArrayList<>();
            routerBeans.add(routerBean);
            allPathMap.put(routerBean.getGroup(),routerBeans);
        }else {
            routerBeans.add(routerBean);
        }
    }

    /**
     * 检查类型是否符合规则
     * @param typeMirror
     * @param element
     * @param routerBean
     */
    private void checkAnnotationsType(TypeMirror typeMirror, Element element, RouterBean routerBean) {
        //ARouter注解的类
        TypeMirror elementMirror = element.asType();

        //判断注解是否是注解在activity上
        if (typeTool.isSubtype(elementMirror, typeMirror)){
            routerBean.setTypeEnum(RouterBean.TypeEnum.ACTIVITY);
        }else {
            throw new RuntimeException("SimpleARouter框架目前仅支持activity");
        }
    }



    /**
     * 存储所有的path到缓存中
     * @param elements
     */
    private void putPathsToCache(Set<? extends Element> elements) {

    }


    /**
     * 创建java类生成器：用于存放java路径的类
     * 样例：
     * public class ARouter$$Path$$app implements ARouterPath {
     *   @Override
     *   public Map<String, RouterBean> getPathMap() {
     *     Map<String, RouterBean> pathMap = new HashMap<>();
     *     pathMap.put("/app/MainActivity", RouterBean.create(RouterBean.TypeEnum.ACTIVITY, MainActivity.class, "/app/MainActivity", "app"));
     *     pathMap.put("/app/Main2Activity", RouterBean.create(RouterBean.TypeEnum.ACTIVITY, Main2Activity.class, "/app/Main2Activity", "app"));
     *     return pathMap;
     *   }
     * }
     * @param pathType
     */
    private void createPathFileGenerater(TypeElement pathType) throws IOException {
        //判断map仓库是否有需要生成的文件
        if (ProcessorUtils.isEmpty(allPathMap)) return;

        //生成代码
        //构建Map<String, RouterBean>
        TypeName methodReturn = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(RouterBean.class));

        //遍历所有path仓库
        for (Map.Entry<String, List<RouterBean>> entry: allPathMap.entrySet()){
            //生成方法体
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(ProcessorConfig.PATH_METHOD_NAME)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(methodReturn);

            // Map<String, RouterBean> pathMap = new HashMap<>(); 应用类型使用$N
            methodBuilder.addStatement("$T<$T,$T> $N = new $T<>()",
                    ClassName.get(Map.class),
                    ClassName.get(String.class),
                    ClassName.get(RouterBean.class),
                    ProcessorConfig.PATH_VAR1,
                    ClassName.get(HashMap.class)
            );

            List<RouterBean> pathList = entry.getValue();
            for (RouterBean bean : pathList) {
                methodBuilder.addStatement("$N.put($S,$T.create($T.$L,$T.class,$S,$S))",
                        ProcessorConfig.PATH_VAR1,// pathMap.put
                        bean.getPath(),// "/personal/Personal_Main2Activity"
                        ClassName.get(RouterBean.class),// RouterBean
                        ClassName.get(RouterBean.TypeEnum.class),// RouterBean.Type
                        bean.getTypeEnum(),//枚举类型：ACTIVITY
                        ClassName.get((TypeElement) bean.getElement()),//Personal_Main2Activity.class
                        bean.getPath(),//"/personal/Personal_Main2Activity"
                        bean.getGroup()// "personal"
                );
            }

            //return pathMap;
            methodBuilder.addStatement("return $N",ProcessorConfig.PATH_VAR1);

            //生成的java文件的文件名
            String finalClassName = ProcessorConfig.PATH_FILE_NAME + entry.getKey();
            messager.printMessage(Diagnostic.Kind.NOTE, "APT生成路由Path类文件：" + aptPackage + "." + finalClassName);

            // 生成类文件：ARouter$$Path$$personal
            JavaFile.builder(aptPackage, // 包名  APT 存放的路径
                    TypeSpec.classBuilder(finalClassName) // 类名
                            .addSuperinterface(ClassName.get(pathType)) //实现接口：com.lsj.arouter_api.ARouterPath
                            .addModifiers(Modifier.PUBLIC) // public修饰符
                            .addMethod(methodBuilder.build()) // 方法的构建（方法参数 + 方法体）
                            .build()) // 类构建完成
                    .build() // JavaFile构建完成
                    .writeTo(filer); // 文件生成器开始生成类文件

            //存放到path缓存中
            allGroupMap.put(entry.getKey(), finalClassName);
        }
    }

    /**
     * 检查用户编写的路径是否存在问题
     * @param routerBean
     * @return
     */
    private boolean checkRouterPath(RouterBean routerBean) {
        String group = routerBean.getGroup();
        String path = routerBean.getPath();

        //校验path
        //开头必须为"/"
        if (ProcessorUtils.isEmpty(path) || !path.startsWith("/")){
            messager.printMessage(Diagnostic.Kind.ERROR,"@ARouter的开头必须以 / 开头");
            return false;
        }
        //最后一个"/"不能就是第一个"/"
        if (path.lastIndexOf("/") == 0){
            messager.printMessage(Diagnostic.Kind.ERROR,"@ARouter注解未按规范配置，如：/app/MainActivity");
            return false;
        }

        //校验group
        // 从第一个 / 到第二个 / 中间截取，如：/app/MainActivity 截取出group
        String finalGroup = path.substring(1, path.indexOf("/", 1));
        // @ARouter注解中的group有赋值情况
        if (!ProcessorUtils.isEmpty(group) && !group.equals(options)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解中的group值必须和子模块名一致！");
            return false;
        } else {
            routerBean.setGroup(finalGroup);
        }

        return true;
    }

}
