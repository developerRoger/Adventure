package com.firstTry.Adventure.tasks;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.stereotype.Component;
/**
 * 定时任务
 * @author Roger
 * 注解@Component为spring中间件
 */
@Component
public class ScheduledTasks {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

//    @Scheduled(fixedRate = 5000)
    public void reportCurrentTime() {
        System.out.println("现在时间：" + dateFormat.format(new Date()));
    }

}