package mate.academy.lib;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Injector {
    private static final Injector injector = new Injector();

    // mapowanie interfejs -> implementacja
    private Map<Class<?>, Class<?>> interfaceImplementationMap = new HashMap<>();

    private Injector() {
        interfaceImplementationMap.put(
                mate.academy.service.FileReaderService.class,
                mate.academy.service.impl.FileReaderServiceImpl.class
        );
        interfaceImplementationMap.put(
                mate.academy.service.ProductParser.class,
                mate.academy.service.impl.ProductParserImpl.class
        );
        interfaceImplementationMap.put(
                mate.academy.service.ProductService.class,
                mate.academy.service.impl.ProductServiceImpl.class
        );
    }

    public static Injector getInjector() {
        return injector;
    }

    public <T> T getInstance(Class<T> interfaceClazz) {
        Class<?> implementationClass = interfaceImplementationMap.get(interfaceClazz);

        if (implementationClass == null) {
            throw new RuntimeException("No implementation for " + interfaceClazz.getName());
        }

        if (!implementationClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + implementationClass.getName()
                    + " is not annotated with @Component");
        }

        try {
            Object instance = implementationClass.getDeclaredConstructor().newInstance();

            injectDependencies(instance);

            return (T) instance;
        } catch (Exception e) {
            throw new RuntimeException("Can't create instance of "
                    + implementationClass.getName(), e);
        }
    }

    private void injectDependencies(Object instance) throws IllegalAccessException {
        Field[] fields = instance.getClass().getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object dependency = getInstance(field.getType());
                field.setAccessible(true);
                field.set(instance, dependency);
            }
        }
    }
}
