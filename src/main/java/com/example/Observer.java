package com.example;

public interface Observer {
    /***
     * 用以更新数据
     * @param temp 温度
     */
    public void update(float temp);
}
