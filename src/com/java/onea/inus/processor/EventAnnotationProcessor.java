package com.java.onea.inus.processor;

import com.java.onea.inus.annotation.Event;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.Set;

@SupportedAnnotationTypes({"*"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public final class EventAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(Event.class)) {
            try {
                String eventPackageName = element.getClass().getPackage().getName();
                String eventFullName = element.getClass().getName();
                String eventName = element.getClass().getSimpleName();
                JavaFileObject file = processingEnv.getFiler().createSourceFile(eventName + "Listener");
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // 0 - package name
    // 1 - full event name
    // 2 - only class event name
    private final String LISTENER_TEMPLATE = "package {0}.subscriber;\n" +
            "\n" +
            "import {1};\n" +
            "import com.android.onea.inus.annotation.Subscribe;\n" +
            "\n" +
            "    public interface {2}Listener {\n" +
            "\n" +
            "        @Subscribe\n" +
            "        public void on{2}({2} event);\n" +
            "\n" +
            "   }\n" +
            "\n" +
            "}";

    private final String PRODUCER_TEMPLATE = "package {0}.producer;\n" +
            "\n" +
            "import {1};\n" +
            "import com.android.onea.inus.annotation.Produce;\n" +
            "\n" +
            "    public interface {2}Producer {\n" +
            "\n" +
            "        @Produce\n" +
            "        public {2} produce{2}();\n" +
            "\n" +
            "   }\n" +
            "\n" +
            "}";

}
