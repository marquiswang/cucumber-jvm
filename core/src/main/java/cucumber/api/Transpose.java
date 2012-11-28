package cucumber.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotation to specify how a Step Definition argument is transformed.
 *
 * @see cucumber.api.Transformer
 */
@java.lang.annotation.Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({ElementType.PARAMETER})
@java.lang.annotation.Documented
public @interface Transpose {
}
