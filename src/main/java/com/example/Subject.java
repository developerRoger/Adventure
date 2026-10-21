package com.example;

public interface Subject {
    /***
     * 注册观察者
     * @param o 具体的观察者
     */
    public void registerObserver(Observer o);

    /***
     * 删除观察者
     * @param o 具体的观察者
     */
    public void removeObserver(Observer o);

    /***
     * 通知所有注册的观察者
     */
    public void notifyObservers();
}
