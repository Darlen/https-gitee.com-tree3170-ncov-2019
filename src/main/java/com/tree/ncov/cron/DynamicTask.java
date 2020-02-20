package com.tree.ncov.cron;

import com.tree.ncov.service.NcovAddrService;
import com.tree.ncov.service.NcovDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @ClassName com.tree.ncov.cron
 * Description: <类功能描述>. <br>
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
//        @Select("select cron from cron limit 1")
//        public String getCron();
//    }

    /**
     *  每半小时执行一次
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void githubDataSchedule() throws Exception {
        addrService.compareAndUpdate();
        detailService.compareAndUpdate();

    }

    @Scheduled(cron = "*/10 * * * * ?")
    public void test() throws Exception {
        log.info("==>每10秒执行schedual ");
    }

    //每天8:00， 23:50 detail数据全量跑

    //8:30 //9:00 //9：30 , 跑一边 update detail

    //address， 每半个小时跑一遍

    //缺少台湾、西藏、香港数据   --

    // 全国的dead、cure整反了

    //查询当天数据， 要从mysql 改为pg

    //增加 province_short_name, create_time,country, 修改province

    //引入jpa jar包，hutool

}
