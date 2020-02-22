package com.tree.ncov.schedule;

import com.tree.ncov.service.NcovAddrService;
import com.tree.ncov.service.NcovDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;

/**
 * @ClassName com.tree.ncov.schedule
 * Description: 定时执行器
 * <p>
 * <使用说明>
 * </p>
 * @Author tree
 * @Date 2020-02-16 12:30
 * @Version 1.0
 */
@Slf4j
@EnableScheduling
@Configuration
public class DynamicTask {
    @Autowired
    private NcovDetailService detailService;
    @Autowired
    private NcovAddrService addrService;

//    @Mapper
//    public interface CronMapper {
//        @Select("select schedule from schedule limit 1")
//        public String getCron();
//    }
    /**
     *重试次数
     */
    @Value("${ncov.retry.count:10}")
    private int retryCount;
    /**
     * 重试睡眠时间
     */
    @Value("${ncov.retry.sleep:5000}")
    private int sleep;

    /**
     *  每半小时执行一次
     */
    @PostConstruct
    @Scheduled(cron = "0 */30 * * * ?")
    public void dataSchedule() throws Exception {
        for(int i = 0; i < retryCount; i++) {
            try {
                addrService.compareAndUpdate();
                detailService.compareAndUpdate();
                break;
            }catch (Exception e){
                log.error("执行[dataSchedule] 失败， 当前重试次数为【{}】, 睡眠【{}】毫秒之后再执行" ,i,sleep,e);
                Thread.sleep(sleep);
            }
        }
    }

}
