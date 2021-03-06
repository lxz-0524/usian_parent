package com.usian.config;

import com.usian.factory.MyAdaptableJobFactory;
import com.usian.quartz.OrderQuartz;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

//Quartz配置类
@Configuration
public class QuartzConfig  {

    //创建job对象
    @Bean
    public JobDetailFactoryBean jobDetailFactoryBean(){
        JobDetailFactoryBean factory = new JobDetailFactoryBean();
        //关联我们创建的job类
        factory.setJobClass(OrderQuartz.class);
        return factory ;
    }

    //cron trigger
    @Bean
    public CronTriggerFactoryBean cronTriggerFactoryBean(JobDetailFactoryBean jobDetailFactoryBean){
        CronTriggerFactoryBean triggerFactoryBean = new CronTriggerFactoryBean();
        triggerFactoryBean.setJobDetail(jobDetailFactoryBean.getObject());
        //设置触发时间  每分钟执行一次
        triggerFactoryBean.setCronExpression("0 */1 * * * ?");//设置cron语句
        return triggerFactoryBean ;
    }

    //创建Scheduler对象
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(CronTriggerFactoryBean cronTriggerFactoryBean,
                                                     MyAdaptableJobFactory myAdaptableJobFactory){
        SchedulerFactoryBean factoryBean = new SchedulerFactoryBean();
        //关联trigger
        factoryBean.setTriggers(cronTriggerFactoryBean.getObject());
        //将OrderQuartz实例化，并放入到spring容器中
        factoryBean.setJobFactory(myAdaptableJobFactory);
        return factoryBean ;
    }
}
