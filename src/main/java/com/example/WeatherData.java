package com.example;

import java.util.ArrayList;

public class WeatherData implements Subject {


    //维护所有注册的观察者
    private ArrayList<Observer> observers;

    private float temperature;//温度

    public WeatherData(){
        //实例化对象
        observers = new ArrayList<>();
    }

    /**
     * 注册观察者
     * @param o 具体的观察者
     */
    @Override
    public void registerObserver(Observer o) {
        observers.add(o);
    }

    /**
     * 删除观察者
     * @param o 具体的观察者
     */
    @Override
    public void removeObserver(Observer o) {
        int i = observers.indexOf(o);
        if (i >= 0){
            observers.remove(i);
        }
    }

    /**
     * 通知所有注册的观察者
     */
    @Override
    public void notifyObservers() {
        for (int i = 0; i < observers.size(); i++){
            Observer observer = observers.get(i);
            observer.update(temperature);
        }
    }

    /**
     * 当数据改变时通知，掌握主动
     */
    public void measurementsChanged(){
        notifyObservers();
    }

    /**
     * 设置温度
     * @param temperature
     */
    public void setMeasurements(float temperature){
        this.temperature = temperature;
        measurementsChanged();
    }
}
