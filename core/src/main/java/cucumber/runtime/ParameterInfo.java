package cucumber.runtime;

import cucumber.api.Format;
import cucumber.api.Delimiter;
import cucumber.api.Transform;
import cucumber.api.Transformer;
import cucumber.deps.com.thoughtworks.xstream.annotations.XStreamConverter;
import cucumber.deps.com.thoughtworks.xstream.converters.SingleValueConverter;
import cucumber.runtime.xstream.LocalizedXStreams;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This class composes all interesting parameter information into one object.
 */
public class ParameterInfo {
    private final Type type;
    private final String format;
    private final String delimiter;
    private final Transformer transformer;

    public static List<ParameterInfo> fromMethod(Method method) {
        List<ParameterInfo> result = new ArrayList<ParameterInfo>();
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Annotation[][] annotations = method.getParameterAnnotations();
        for (int i = 0; i < genericParameterTypes.length; i++) {
            Builder parameterInfo = new Builder(genericParameterTypes[i]);
            for (Annotation annotation : annotations[i]) {
                if (annotation instanceof Format) {
                    parameterInfo.setFormat(((Format) annotation).value());
                }
                if (annotation instanceof Delimiter) {
                    parameterInfo.setDelimiter(((Delimiter) annotation).value());
                }
                if (annotation instanceof Transform) {
                    try {
                        parameterInfo.setTransformer(((Transform) annotation).value().newInstance());
                    } catch (InstantiationException e) {
                        throw new CucumberException(e);
                    } catch (IllegalAccessException e) {
                        throw new CucumberException(e);
                    }
                }
            }
            result.add(parameterInfo.build());
        }
        return result;
    }

    public static Builder builder(Type type) {
        return new Builder(type);
    }

    public static class Builder {
        public static final String DEFAULT_DELIMITER = ",\\s?";

        private Type type = null;
        private String format = null;
        private String delimiter = DEFAULT_DELIMITER;
        private Transformer transformer = null;

        private Builder(Type type) {
            this.type = type;
        }

        public ParameterInfo build() {
            return new ParameterInfo(type, format, delimiter, transformer);
        }

        public Builder setFormat(String format) {
            this.format = format;
            return this;
        }

        public Builder setDelimiter(String delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        public Builder setTransformer(Transformer transformer) {
            this.transformer = transformer;
            return this;
        }
    }

    private ParameterInfo(Type type, String format, String delimiter, Transformer transformer) {
        this.type = type;
        this.format = format;
        this.delimiter = delimiter;
        this.transformer = transformer;
    }

    public Class<?> getRawType() {
        return getRawType(type);
    }

    private Class<?> getRawType(Type type) {
        if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        } else {
            return (Class<?>) type;
        }
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return type.toString();
    }

    public Object convert(String value, LocalizedXStreams.LocalizedXStream xStream, Locale locale) {
        try {
            xStream.setParameterType(this);
            SingleValueConverter converter;
            xStream.processAnnotations(getRawType());

            if (transformer != null) {
                transformer.setParameterInfoAndLocale(this, locale);
                converter = transformer;
            } else {
                if (List.class.isAssignableFrom(getRawType())) {
                    converter = getListConverter(type, xStream, locale);
                } else {
                    converter = getConverter(getRawType(), xStream, locale);
                }
                if (converter == null) {
                    throw new CucumberException(String.format(
                            "Don't know how to convert \"%s\" into %s.\n" +
                                    "Try writing your own converter:\n" +
                                    "\n" +
                                    "@%s(%sConverter.class)\n" +
                                    "public class %s {}\n",
                            value,
                            getRawType().getName(),
                            XStreamConverter.class.getName(),
                            getRawType().getSimpleName(),
                            getRawType().getSimpleName()
                    ));
                }
            }
            return converter.fromString(value);
        } finally {
            xStream.unsetParameterInfo();
        }
    }

    private SingleValueConverter getListConverter(Type type, LocalizedXStreams.LocalizedXStream xStream, Locale locale) {
        Class elementType = type instanceof ParameterizedType
                ? getRawType(((ParameterizedType) type).getActualTypeArguments()[0])
                : Object.class;

        SingleValueConverter elementConverter = getConverter(elementType, xStream, locale);
        if (elementConverter == null) {
            return null;
        } else {
            return xStream.createListConverter(delimiter, elementConverter);
        }
    }

    private SingleValueConverter getConverter(Class<?> type, LocalizedXStreams.LocalizedXStream xStream, Locale locale) {
        if (type.isEnum()) {
            return xStream.createEnumConverter(locale, (Class<? extends Enum>) type);
        } else {
            return xStream.getSingleValueConverter(type);
        }
    }

    public String getFormat() {
        return format;
    }
}
