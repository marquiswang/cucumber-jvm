package cucumber.runtime.java;

import cucumber.classpath.Classpath;
import cucumber.runtime.Backend;
import cucumber.runtime.StepDefinition;
import gherkin.formatter.model.Step;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class JavaBackend implements Backend {
    private final ObjectFactory objectFactory;
    private List<StepDefinition> stepDefinitions = new ArrayList<StepDefinition>();

    public JavaBackend(String packagePrefix) {
        this.objectFactory = Classpath.instantiateExactlyOneSubclass(ObjectFactory.class, "cucumber.runtime");
        new ClasspathMethodScanner().scan(this, packagePrefix);
    }

    public List<StepDefinition> getStepDefinitions() {
        return stepDefinitions;
    }

    public void newWorld() {
        objectFactory.createInstances();
    }

    public void disposeWorld() {
        objectFactory.disposeInstances();
    }

    public String getSnippet(Step step) {
        return new JavaSnippetGenerator(step).getSnippet();
    }

    void addStepDefinition(Pattern pattern, Method method, Locale locale) {
        Class<?> clazz = method.getDeclaringClass();
        objectFactory.addClass(clazz);
        stepDefinitions.add(new JavaStepDefinition(pattern, method, objectFactory, locale));
    }
}