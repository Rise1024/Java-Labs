package com.javalaabs.patterns.behavioral.strategy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 策略模式测试
 * 
 * @author JavaLabs
 */
@DisplayName("策略模式测试")
class StrategyTest {
    
    private Context context;
    
    @BeforeEach
    void setUp() {
        context = new Context(new AddStrategy());
    }
    
    @Test
    @DisplayName("加法策略应该正确计算")
    void testAddStrategy() {
        // Given
        context.setStrategy(new AddStrategy());
        
        // When
        int result = context.executeStrategy(10, 5);
        
        // Then
        assertThat(result).isEqualTo(15);
    }
    
    @Test
    @DisplayName("减法策略应该正确计算")
    void testSubtractStrategy() {
        // Given
        context.setStrategy(new SubtractStrategy());
        
        // When
        int result = context.executeStrategy(10, 5);
        
        // Then
        assertThat(result).isEqualTo(5);
    }
    
    @Test
    @DisplayName("乘法策略应该正确计算")
    void testMultiplyStrategy() {
        // Given
        context.setStrategy(new MultiplyStrategy());
        
        // When
        int result = context.executeStrategy(10, 5);
        
        // Then
        assertThat(result).isEqualTo(50);
    }
    
    @Test
    @DisplayName("动态切换策略应该正确工作")
    void testStrategySwitch() {
        // Given
        int a = 8, b = 4;
        
        // When & Then - 加法
        context.setStrategy(new AddStrategy());
        assertThat(context.executeStrategy(a, b)).isEqualTo(12);
        
        // When & Then - 减法
        context.setStrategy(new SubtractStrategy());
        assertThat(context.executeStrategy(a, b)).isEqualTo(4);
        
        // When & Then - 乘法
        context.setStrategy(new MultiplyStrategy());
        assertThat(context.executeStrategy(a, b)).isEqualTo(32);
    }
    
    @Test
    @DisplayName("未设置策略时应该抛出异常")
    void testNoStrategyException() {
        // Given
        Context emptyContext = new Context(null);
        
        // When & Then
        assertThatThrownBy(() -> emptyContext.executeStrategy(1, 2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("策略未设置");
    }
    
    @Test
    @DisplayName("策略名称应该正确返回")
    void testStrategyNames() {
        // Given
        AddStrategy addStrategy = new AddStrategy();
        SubtractStrategy subtractStrategy = new SubtractStrategy();
        MultiplyStrategy multiplyStrategy = new MultiplyStrategy();
        
        // When & Then
        assertThat(addStrategy.getName()).isEqualTo("加法策略");
        assertThat(subtractStrategy.getName()).isEqualTo("减法策略");
        assertThat(multiplyStrategy.getName()).isEqualTo("乘法策略");
    }
}
