package me.chaopeng.chaosrx;

import java.lang.annotation.*;

/**
 * After
 * <p>
 * to mark a method need after() invoke
 *
 * @author chao
 * @version 1.0 - 11/27/14
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface After {
}
