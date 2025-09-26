package com.javalaabs.patterns.structural.decorator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 装饰器模式测试
 * 
 * @author JavaLabs
 */
@DisplayName("装饰器模式测试")
class DecoratorTest {
    
    private Component baseComponent;
    
    @BeforeEach
    void setUp() {
        baseComponent = new ConcreteComponent();
    }
    
    @Test
    @DisplayName("基础组件应该正确工作")
    void testBaseComponent() {
        // When
        String result = baseComponent.operation();
        double cost = baseComponent.getCost();
        
        // Then
        assertThat(result).isEqualTo("基础咖啡");
        assertThat(cost).isEqualTo(10.0);
    }
    
    @Test
    @DisplayName("牛奶装饰器应该正确添加功能")
    void testMilkDecorator() {
        // Given
        Component milkCoffee = new MilkDecorator(baseComponent);
        
        // When
        String result = milkCoffee.operation();
        double cost = milkCoffee.getCost();
        
        // Then
        assertThat(result).isEqualTo("基础咖啡 + 牛奶");
        assertThat(cost).isEqualTo(12.0); // 10.0 + 2.0
    }
    
    @Test
    @DisplayName("糖装饰器应该正确添加功能")
    void testSugarDecorator() {
        // Given
        Component sugarCoffee = new SugarDecorator(baseComponent);
        
        // When
        String result = sugarCoffee.operation();
        double cost = sugarCoffee.getCost();
        
        // Then
        assertThat(result).isEqualTo("基础咖啡 + 糖");
        assertThat(cost).isEqualTo(11.0); // 10.0 + 1.0
    }
    
    @Test
    @DisplayName("多重装饰器应该能够组合使用")
    void testMultipleDecorators() {
        // Given
        Component decoratedCoffee = new SugarDecorator(
            new MilkDecorator(baseComponent)
        );
        
        // When
        String result = decoratedCoffee.operation();
        double cost = decoratedCoffee.getCost();
        
        // Then
        assertThat(result).isEqualTo("基础咖啡 + 牛奶 + 糖");
        assertThat(cost).isEqualTo(13.0); // 10.0 + 2.0 + 1.0
    }
    
    @Test
    @DisplayName("装饰器顺序应该影响结果")
    void testDecoratorOrder() {
        // Given
        Component milkFirst = new SugarDecorator(new MilkDecorator(baseComponent));
        Component sugarFirst = new MilkDecorator(new SugarDecorator(baseComponent));
        
        // When
        String milkFirstResult = milkFirst.operation();
        String sugarFirstResult = sugarFirst.operation();
        
        // Then
        assertThat(milkFirstResult).isEqualTo("基础咖啡 + 牛奶 + 糖");
        assertThat(sugarFirstResult).isEqualTo("基础咖啡 + 糖 + 牛奶");
        assertThat(milkFirstResult).isNotEqualTo(sugarFirstResult);
        
        // 但成本应该相同
        assertThat(milkFirst.getCost()).isEqualTo(sugarFirst.getCost());
    }
}
