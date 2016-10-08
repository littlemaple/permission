package com.medzone.permission;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class PermissionProcessor extends AbstractProcessor {

    private Messager messager;
    private Elements elementUtils;
    private Map<String, ProxyInfo> mProxyMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportAnnotationTypes = new HashSet<>();
        supportAnnotationTypes.add(PermissionDenied.class.getCanonicalName());
        supportAnnotationTypes.add(PermissionGant.class.getCanonicalName());
        return supportAnnotationTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        mProxyMap.clear();
        messager.printMessage(Diagnostic.Kind.NOTE, "process...");

        if (!processAnnotations(roundEnv, PermissionGant.class)) return false;
        if (!processAnnotations(roundEnv, PermissionDenied.class)) return false;


        for (String key : mProxyMap.keySet()) {
            ProxyInfo proxyInfo = mProxyMap.get(key);
            try {
                JavaFileObject jfo = processingEnv.getFiler().createSourceFile(
                        proxyInfo.getProxyClassFullName(),
                        proxyInfo.getTypeElement());
                Writer writer = jfo.openWriter();
                writer.write(proxyInfo.generateJavaCode());
                writer.flush();
                writer.close();
            } catch (IOException e) {
                error(proxyInfo.getTypeElement(),
                        "Unable to write injector for type %s: %s",
                        proxyInfo.getTypeElement(), e.getMessage());
            }

        }
        return true;
    }


    private boolean processAnnotations(RoundEnvironment roundEnv, Class<? extends Annotation> clazz) {
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(clazz)) {

            if (!checkMethodValid(annotatedElement, clazz)) return false;

            ExecutableElement annotatedMethod = (ExecutableElement) annotatedElement;
            //class type
            TypeElement classElement = (TypeElement) annotatedMethod.getEnclosingElement();
            //full class name
            String fqClassName = classElement.getQualifiedName().toString();

            ProxyInfo proxyInfo = mProxyMap.get(fqClassName);
            if (proxyInfo == null) {
                proxyInfo = new ProxyInfo(elementUtils, classElement);
                mProxyMap.put(fqClassName, proxyInfo);
                proxyInfo.setTypeElement(classElement);
            }


            Annotation annotation = annotatedMethod.getAnnotation(clazz);
            if (annotation instanceof PermissionGant) {
                int requestCode = ((PermissionGant) annotation).value();
                proxyInfo.grantMethodMap.put(requestCode, annotatedMethod.getSimpleName().toString());
            } else if (annotation instanceof PermissionDenied) {
                int requestCode = ((PermissionDenied) annotation).value();
                proxyInfo.deniedMethodMap.put(requestCode, annotatedMethod.getSimpleName().toString());
            } else {
                error(annotatedElement, "%s not support .", clazz.getSimpleName());
                return false;
            }

        }

        return true;
    }

    private boolean checkMethodValid(Element annotatedElement, Class clazz) {
        if (annotatedElement.getKind() != ElementKind.METHOD) {
            error(annotatedElement, "%s must be declared on method.", clazz.getSimpleName());
            return false;
        }
        if (ClassValidator.isPrivate(annotatedElement) || ClassValidator.isAbstract(annotatedElement)) {
            error(annotatedElement, "%s() must can not be abstract or private.", annotatedElement.getSimpleName());
            return false;
        }

        return true;
    }

    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message, element);
    }

    private void generateProxy() {
        messager.printMessage(Diagnostic.Kind.NOTE, ">>prepared to generate proxy");

        MethodSpec getSuperPowerMethod = MethodSpec.methodBuilder("getSuperPower")
                .returns(void.class)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .build();

        TypeSpec superPowerInterface = TypeSpec.interfaceBuilder("SuperPower")
                .addMethod(getSuperPowerMethod)
                .addModifiers(Modifier.PUBLIC).build();

        String packageName = getClass().getPackage().getName();
        ClassName superInterface = ClassName.get(packageName, superPowerInterface.name);
        MethodSpec suppleMethod = MethodSpec.methodBuilder("getSuperPower").returns(void.class).addAnnotation(Override.class).addModifiers(Modifier.PUBLIC).addStatement("return")
                .build();
        TypeSpec typeSpec = TypeSpec.classBuilder("NetworkManager").addSuperinterface(superInterface).addMethod(suppleMethod).build();
        writeTypeSpec(packageName, superPowerInterface);
        writeTypeSpec(packageName, typeSpec);
        messager.printMessage(Diagnostic.Kind.NOTE, ">>finish generate proxy");
    }

    private void writeTypeSpec(String packageName, TypeSpec typeSpec) {
        try {
            JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
