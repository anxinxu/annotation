package com.anxin.lib_annotation_compiler;

import android.support.annotation.NonNull;

import com.anxin.lib_annotation.BindView;
import com.anxin.lib_annotation.BindViews;
import com.anxin.lib_annotation.test.Test;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;

/**
 * Created by anxin on 2018/2/5.
 * <p>
 */

@AutoService(Processor.class)
public class BindViewProcessor extends AbstractProcessor {

    private static final ClassName INJECT_CLASS_NAME = ClassName.get("com.anxin.inject", "Unbinder");

    private Elements typeUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        note(" -> init ()");
        typeUtils = processingEnv.getElementUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        for (Class<? extends Annotation> annotation : getSupportedAnnotations()) {
            types.add(annotation.getCanonicalName());
        }
        note(" -> getSupportedAnnotationTypes ()  types = " + types);
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        note(" -> getSupportedSourceVersion ()");
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        note(" -> process () set : " + set + " , roundEnvironment : " + roundEnvironment);

        Map<Element, JavaFile> bindMap = findAndParseTarget(roundEnvironment);
        for (Map.Entry<Element, JavaFile> tFileEntry : bindMap.entrySet()) {
            Element tKey = tFileEntry.getKey();
            JavaFile tValue = tFileEntry.getValue();
            note("key : %s ", tKey.toString());
            try {
                tValue.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
                StringWriter tWriter = new StringWriter();
                e.printStackTrace(new PrintWriter(tWriter));
                printMessage(Kind.ERROR, tKey, "process() -> e : %s", tWriter);
            }
        }

        return false;
    }

    private Map<Element, JavaFile> findAndParseTarget(RoundEnvironment roundEnvironment) {
        Map<Element, JavaFile> bindingMap = new LinkedHashMap<>();
        // Process each @Test element.
        for (Element tElement : roundEnvironment.getElementsAnnotatedWith(Test.class)) {
            JavaFile tJavaFile = parseTest(tElement);
            bindingMap.put(tElement, tJavaFile);
        }
        // Process each @BindView element.
        for (Element tElement : roundEnvironment.getElementsAnnotatedWith(BindView.class)) {
            JavaFile tJavaFile = parseBindView(tElement);
            if (tJavaFile != null) {

                bindingMap.put(tElement, tJavaFile);
            } else {
                error("parse bind view error", tElement, null);
            }
        }

        return bindingMap;
    }

    private JavaFile parseBindView(Element typeElement) {
        BindView tAnnotation = typeElement.getAnnotation(BindView.class);
        try {
            int tValue = tAnnotation.value();
            TypeElement tTypeElement = (TypeElement) typeElement.getEnclosingElement();
            ClassName targetClass = ClassName.get(tTypeElement);
            TypeSpec.Builder tBuilder = TypeSpec.classBuilder(String.format("%s_BindView", tTypeElement.getSimpleName()))
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addJavadoc("don't edit this code!\n")
                    .addSuperinterface(INJECT_CLASS_NAME);
            MethodSpec unbind = MethodSpec.methodBuilder("unbind")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .returns(void.class)
                    .build();
            MethodSpec constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(targetClass, "target", Modifier.FINAL)
                    .addStatement("$L.$L = $L.findViewById($L)", "target", typeElement, "target", tValue)
                    .build();
            tBuilder.addMethod(unbind);
            tBuilder.addMethod(constructor);
            return JavaFile.builder(typeUtils.getPackageOf(typeElement).getQualifiedName().toString(), tBuilder.build()).build();

        } catch (Exception e) {
            StringWriter tWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(tWriter));
            error("parseBindView() -> annotation : %s ", typeElement, e, tAnnotation);
        }

        return null;
    }

    private JavaFile parseTest(Element typeElement) {
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addJavadoc(String.format("constructor() ->  tElement = %s , tTypeElement = %s", typeElement.toString(), typeElement.toString()))
                .addParameter(TypeName.get(typeElement.asType()), "target")
                .addParameter(String.class, "msg")
                .addStatement(String.format("android.util.Log.d(%s,\"%s\")", "TAG", "init<>"))
                .addStatement("name = \"test annotation\"")
                .addStatement("int count = 0;")
                .beginControlFlow("for(int i = 0;i < 10;i++)")
                .addCode("//add code ========>\n")
                .addStatement("count++")
                .endControlFlow()
                .addStatement("target.count = count")
                .addStatement(String.format("android.util.Log.d(%s,\"%s -> count = \" + %s)", "TAG", "init<>", "count"))
                .build();
        MethodSpec main = MethodSpec.methodBuilder("main")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .returns(void.class)
                .addParameter(String[].class, "args")
                .build();
        FieldSpec TAG = FieldSpec.builder(String.class, "TAG", Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                .initializer("\"Test_" + typeElement.getSimpleName() + "\"")
                .build();
        TypeSpec tTypeSpec = TypeSpec.classBuilder("Test_" + typeElement.getSimpleName())
                .addMethod(constructor)
                .addMethod(main)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(TAG)
                .addField(String.class, "name", Modifier.PRIVATE)
                .build();
        return JavaFile.builder(typeUtils.getPackageOf(typeElement).getQualifiedName().toString(), tTypeSpec).build();
    }

    @NonNull
    private Set<Class<? extends Annotation>> getSupportedAnnotations() {
        Set<Class<? extends Annotation>> annotations = new LinkedHashSet<>();

        annotations.add(Test.class);
        annotations.add(BindView.class);
        annotations.add(BindViews.class);
        return annotations;
    }

    private void printMessage(Kind kind, String message, Object... args) {
        printMessage(kind, null, message, args);
    }

    private void printMessage(Kind kind, Element element, String message, Object... args) {
        if (args != null && args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(kind, message, element);
    }

    private void note(String message, Object... args) {
        printMessage(Kind.NOTE, message, args);
    }

    private void error(String message, Element typeElement, Exception e, Object... args) {
        if (e != null) {
            StringWriter tWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(tWriter));
            message = "\n" + message;
            message += " , \n exception message : %s , \n exception : %s";
            Object[] tStrings = new String[args != null ? (args.length + 2) : 2];
            for (int i = 0; i < tStrings.length; i++) {
                if (args != null && i < args.length) {
                    tStrings[i] = args[i].toString();
                } else if (i == tStrings.length - 2) {
                    tStrings[i] = e.getMessage();
                } else {
                    tStrings[i] = tWriter.toString();
                }
            }
            printMessage(Kind.ERROR, typeElement, message, tStrings);
        } else {
            printMessage(Kind.ERROR, typeElement, message, args);
        }
    }

}
