package com.jasgeetest.dynamiccronscheduler.scheduler;


import com.jasgeetest.dynamiccronscheduler.utils.BeanFactoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.TimeZone;


/**
 * Created by Jasgeet on 30/08/18.
 */
@Service
public class DynamicCronScheduler implements  DisposableBean {




    private static final Logger logger = LoggerFactory.getLogger(DynamicCronScheduler.class);

    /**
     * This method is used to set new cron job
     * @param cronExpression
     * @param jobName
     * @param task
     */
    public void setCronScheduling(String cronExpression, String jobName, Runnable task){
//        logger.debug("Initiate job of ----> ", jobName);
//        logger.info("cronExpression ::::::::: "+ cronExpression);
        CronTrigger trigger = new CronTrigger(cronExpression);
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setThreadNamePrefix(jobName);
        threadPoolTaskScheduler.setWaitForTasksToCompleteOnShutdown(true);
        threadPoolTaskScheduler.initialize();
        threadPoolTaskScheduler.setPoolSize(5);
        threadPoolTaskScheduler.schedule(task, trigger);

        BeanFactoryUtil.registerSingleton("scheduler-" + jobName, threadPoolTaskScheduler);

        logger.info("Name of cron job is --> " + "scheduler-" + jobName);



    }

    public Date getNextExecutionTime(CronTrigger trigger, Date lastScheduledExecutionTime, TimeZone timeZone){
        TriggerContext triggerContext = getTriggerContext(lastScheduledExecutionTime);
        triggerContext.lastActualExecutionTime();
        return trigger.nextExecutionTime(triggerContext);
    }

    private static TriggerContext getTriggerContext(Date lastCompletionTime) {
        SimpleTriggerContext context = new SimpleTriggerContext();
        context.update(null, null, lastCompletionTime);
        return context;
    }

    public void stopCronJob(String jobName){
        try {
//            logger.info("Check scheduler --> "+jobName);
            String scheduler = "scheduler-" + jobName;
            ThreadPoolTaskScheduler scheduler2 = BeanFactoryUtil.getDefaultListableBeanFactory()
                    .getBean(scheduler, ThreadPoolTaskScheduler.class);
//            logger.info("scheduler2-----> "+scheduler2);
            if (scheduler2 != null){
                scheduler2.getScheduledExecutor().shutdownNow();
                logger.info("  stopCronJob () scheduler2--=============---> "+scheduler2);
                logger.info("  stopCronJob () scheduler2- Thread name prefix "+            scheduler2.getThreadNamePrefix());
                logger.info("  stopCronJob () scheduler2- get Active account "+            scheduler2.getActiveCount());
            } else {
                logger.info("  stopCronJob () scheduler2 is null");
            }
        }catch (NoSuchBeanDefinitionException exception){
            logger.error("No bean registered for cron job, May be this is your first time to scheduling cron job!!");
        }

    }

    public void startCronJob(String cronExpression, String jobName, Runnable task){
        try {
            String scheduler = "scheduler-" + jobName;
         logger.info("Start scheduler from BootStrap--> "+scheduler);
            CronTrigger trigger = new CronTrigger(cronExpression);

            ThreadPoolTaskScheduler scheduler2 = BeanFactoryUtil.getDefaultListableBeanFactory()
                    .getBean(scheduler, ThreadPoolTaskScheduler.class);

            if (scheduler2 != null){
                logger.info("  startCronJob () scheduler2--=============---> "+scheduler2);
                logger.info("  startCronJob () scheduler2- Thread name prefix "+            scheduler2.getThreadNamePrefix());
                logger.info("  startCronJob () scheduler2- get Active account "+            scheduler2.getActiveCount());
                scheduler2.initialize();
                scheduler2.schedule(task, trigger);
            }  else {
                logger.info("  startCronJob () ELSE PART scheduler2--=============---> "+scheduler2);
                setCronScheduling(cronExpression,jobName,task);
            }
        }catch (NoSuchBeanDefinitionException exception){
            setCronScheduling(cronExpression,jobName,task);
            logger.error("No bean registered for cron job, May be this is your first time to scheduling cron job!!");
        }

    }
    
    public void destroy(){

    }





}
