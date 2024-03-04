package com.netease.arctic.ams.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation is used to mark fields in a Persistent Object that will be kept consistency by Amoro framework.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface StateField {}
