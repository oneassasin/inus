package com.java.onea.inus.processor;

import com.java.onea.inus.annotation.Event;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

@SupportedAnnotationTypes("com.java.onea.inus.annotation.Event")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public final class EventAnnotationProcessor extends AbstractProcessor {

    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        messager.printMessage(Diagnostic.Kind.NOTE, "Processing start!");
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Event.class);
        messager.printMessage(Diagnostic.Kind.NOTE, "Annotated elements found: " + elements.size());
        if (elements.size() == 0)
            return false;
        for (Element element : elements) {
            if (element.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Annotated element not class: " + element);
                continue;
            }
            String eventName = element.getSimpleName().toString();
            String packageEventName = element.toString();
            String packageName = packageEventName.substring(0, packageEventName.lastIndexOf('.'));
            messager.printMessage(Diagnostic.Kind.NOTE, "Founded class: " + element);
            messager.printMessage(Diagnostic.Kind.NOTE, String.format("EventName: %s;\n" +
                    "PackageEventName: %s;\n" +
                    "PackageName: %s", eventName, packageEventName, packageName));
            JavaFileObject file;
            Writer fileWriter = null;
            try {
                try {
                    file = processingEnv.getFiler().createSourceFile(eventName + "Listener", element);
                    fileWriter = file.openWriter();
                    fileWriter.write(renderListenerString(packageName, packageEventName, eventName));
                } finally {
                    if (fileWriter != null) {
                        fileWriter.flush();
                        fileWriter.close();
                    }
                    messager.printMessage(Diagnostic.Kind.NOTE, String.format("Create %s is complete!", eventName + "Listener"));
                }
            } catch (IOException e) {
                e.printStackTrace();
                messager.printMessage(Diagnostic.Kind.ERROR, e.toString(), element);
            }
            try {
                try {
                    file = processingEnv.getFiler().createSourceFile(eventName + "Producer", element);
                    fileWriter = file.openWriter();
                    fileWriter.write(renderProducerString(packageName, packageEventName, eventName));
                } finally {
                    if (fileWriter != null) {
                        fileWriter.flush();
                        fileWriter.close();
                    }
                    messager.printMessage(Diagnostic.Kind.NOTE, String.format("Create %s is complete!", eventName + "Producer"));
                }
            } catch (IOException e) {
                e.printStackTrace();
                messager.printMessage(Diagnostic.Kind.ERROR, e.toString(), element);
            }
        }
        return true;
    }

    private String renderListenerString(String packageName, String packageEventName, String eventName) {
        return "package " + packageName + ";\n" +
                "\n" +
                "import " + packageEventName +";\n" +
                "import com.java.onea.inus.annotation.Subscribe;\n" +
                "\n" +
                "public interface " + eventName + "Listener {\n" +
                "\n" +
                "    @Subscribe\n" +
                "    public void on" + eventName + "(" + eventName + " event);\n" +
                "\n" +
                "}\n" +
                "\n";
    }

    private String renderProducerString(String packageName, String packageEventName, String eventName) {
        return "package " + packageName + ";\n" +
                "\n" +
                "import " + packageEventName + ";\n" +
                "import com.java.onea.inus.annotation.Produce;\n" +
                "\n" +
                "public interface " + eventName + "Producer {\n" +
                "\n" +
                "    @Produce\n" +
                "    public " + eventName + " produce" + eventName + "();\n" +
                "\n" +
                "}\n" +
                "\n";
    }
}
