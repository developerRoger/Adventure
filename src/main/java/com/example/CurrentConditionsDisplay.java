package com.example;

public class CurrentConditionsDisplay  implements Observer, DisplayElement {
    private float temperature;    //温度
    private Subject weatherData;  //主题

    public CurrentConditionsDisplay(Subject weatherData){
        this.weatherData = weatherData;
        //订阅主题
        this.weatherData.registerObserver(this);
    }

    /***
     * 外观显示方法
     */
    @Override
    public void display() {
        if (temperature <= 18) {
            System.out.println("要冻死了，还是窝在家里好");
        }else if(temperature > 18 && temperature < 30){
            System.out.println("风和日丽，适合出去浪");
        }else{
            System.out.println("太晒了，都给我晒糊了");
        }
        this.weatherData.removeObserver(this);
    }

    /***
     * 用以更新数据
     * @param temp 温度
     */
    @Override
    public void update(float temp) {
        this.temperature = temp;
        display();
    }
}
