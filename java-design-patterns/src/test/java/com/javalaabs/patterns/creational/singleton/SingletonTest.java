package com.javalaabs.patterns.creational.singleton;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 单例模式测试
 * 
 * @author JavaLabs
 */
@DisplayName("单例模式测试")
class SingletonTest {
    
    @Test
    @DisplayName("饿汉式单例应该返回相同实例")
    void testEagerSingleton() {
        // Given & When
        Singleton instance1 = Singleton.getInstance();
        Singleton instance2 = Singleton.getInstance();
        
        // Then
        assertThat(instance1).isNotNull();
        assertThat(instance2).isNotNull();
        assertThat(instance1).isSameAs(instance2);
        assertThat(instance1.hashCode()).isEqualTo(instance2.hashCode());
    }
    
    @Test
    @DisplayName("懒汉式单例应该返回相同实例")
    void testLazySingleton() {
        // Given & When
        LazySingleton instance1 = LazySingleton.getInstance();
        LazySingleton instance2 = LazySingleton.getInstance();
        
        // Then
        assertThat(instance1).isNotNull();
        assertThat(instance2).isNotNull();
        assertThat(instance1).isSameAs(instance2);
        assertThat(instance1.hashCode()).isEqualTo(instance2.hashCode());
    }
    
    @Test
    @DisplayName("枚举单例应该返回相同实例")
    void testEnumSingleton() {
        // Given & When
        EnumSingleton instance1 = EnumSingleton.getInstance();
        EnumSingleton instance2 = EnumSingleton.INSTANCE;
        EnumSingleton instance3 = EnumSingleton.valueOf("INSTANCE");
        
        // Then
        assertThat(instance1).isNotNull();
        assertThat(instance2).isNotNull();
        assertThat(instance3).isNotNull();
        assertThat(instance1).isSameAs(instance2);
        assertThat(instance1).isSameAs(instance3);
        assertThat(instance1.hashCode()).isEqualTo(instance2.hashCode());
    }
    
    @Test
    @DisplayName("多线程环境下懒汉式单例应该是线程安全的")
    void testLazySingletonThreadSafety() throws InterruptedException {
        // Given
        final LazySingleton[] instances = new LazySingleton[2];
        
        // When
        Thread thread1 = new Thread(() -> instances[0] = LazySingleton.getInstance());
        Thread thread2 = new Thread(() -> instances[1] = LazySingleton.getInstance());
        
        thread1.start();
        thread2.start();
        
        thread1.join();
        thread2.join();
        
        // Then
        assertThat(instances[0]).isNotNull();
        assertThat(instances[1]).isNotNull();
        assertThat(instances[0]).isSameAs(instances[1]);
    }
}
