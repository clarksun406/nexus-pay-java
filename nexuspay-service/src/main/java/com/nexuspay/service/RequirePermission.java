package com.nexuspay.service;

import java.lang.annotation.*;

/**
 * Requires the caller to hold the specified permission(s).
 * Applied to controller methods or service methods.
 * Checked by {@link PermissionAspect}.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /** Permission code(s) required. All must be held. */
    String[] value();

    /** If true, having ANY of the specified permissions is sufficient. */
    boolean any() default false;
}