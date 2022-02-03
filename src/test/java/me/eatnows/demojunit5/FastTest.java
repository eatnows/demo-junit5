package me.eatnows.demojunit5;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test           // 메타 애너테이션
@Tag("fast")    // 메타 애너테이션
public @interface FastTest { // 컴포지션 애터네이션 
    
}