package com.javalaabs.patterns.creational.builder;

import lombok.Getter;
import lombok.ToString;

/**
 * 产品类 - 计算机
 * 
 * @author JavaLabs
 */
@Getter
@ToString
public class Computer {
    
    // 必需参数
    private final String cpu;
    private final String memory;
    
    // 可选参数
    private final String storage;
    private final String graphics;
    private final String motherboard;
    private final String powerSupply;
    private final boolean hasWifi;
    private final boolean hasBluetooth;
    
    /**
     * 私有构造函数，只能通过Builder创建
     * 
     * @param builder 建造者
     */
    private Computer(Builder builder) {
        this.cpu = builder.cpu;
        this.memory = builder.memory;
        this.storage = builder.storage;
        this.graphics = builder.graphics;
        this.motherboard = builder.motherboard;
        this.powerSupply = builder.powerSupply;
        this.hasWifi = builder.hasWifi;
        this.hasBluetooth = builder.hasBluetooth;
    }
    
    /**
     * 静态内部建造者类
     */
    public static class Builder {
        // 必需参数
        private final String cpu;
        private final String memory;
        
        // 可选参数 - 使用默认值初始化
        private String storage = "500GB SSD";
        private String graphics = "集成显卡";
        private String motherboard = "标准主板";
        private String powerSupply = "500W";
        private boolean hasWifi = true;
        private boolean hasBluetooth = false;
        
        /**
         * 构造函数，接收必需参数
         * 
         * @param cpu    CPU
         * @param memory 内存
         */
        public Builder(String cpu, String memory) {
            this.cpu = cpu;
            this.memory = memory;
        }
        
        /**
         * 设置存储设备
         * 
         * @param storage 存储设备
         * @return Builder实例
         */
        public Builder storage(String storage) {
            this.storage = storage;
            return this;
        }
        
        /**
         * 设置显卡
         * 
         * @param graphics 显卡
         * @return Builder实例
         */
        public Builder graphics(String graphics) {
            this.graphics = graphics;
            return this;
        }
        
        /**
         * 设置主板
         * 
         * @param motherboard 主板
         * @return Builder实例
         */
        public Builder motherboard(String motherboard) {
            this.motherboard = motherboard;
            return this;
        }
        
        /**
         * 设置电源
         * 
         * @param powerSupply 电源
         * @return Builder实例
         */
        public Builder powerSupply(String powerSupply) {
            this.powerSupply = powerSupply;
            return this;
        }
        
        /**
         * 设置WiFi
         * 
         * @param hasWifi 是否有WiFi
         * @return Builder实例
         */
        public Builder wifi(boolean hasWifi) {
            this.hasWifi = hasWifi;
            return this;
        }
        
        /**
         * 设置蓝牙
         * 
         * @param hasBluetooth 是否有蓝牙
         * @return Builder实例
         */
        public Builder bluetooth(boolean hasBluetooth) {
            this.hasBluetooth = hasBluetooth;
            return this;
        }
        
        /**
         * 构建Computer实例
         * 
         * @return Computer实例
         */
        public Computer build() {
            return new Computer(this);
        }
    }
}
