# 2019新型冠状病毒后端数据处理

## 数据来源：
本项目为2019新型冠状病毒（COVID-19/2019-nCoV）疫情状况的时间序列数据仓库

1. 每个城市、省份、国家的来源来自[BlankerL](https://github.com/BlankerL)的项目[[DXY-COVID-19-Data](https://github.com/BlankerL/DXY-COVID-19-Data)]，其作者的数据来源于[丁香园](https://3g.dxy.cn/newh5/view/pneumonia)。非常感谢该作者既提供数据， 还提供API接口给到各开发者使用。

   数据由[2019新型冠状病毒疫情实时爬虫](https://github.com/BlankerL/DXY-COVID-19-Crawler)获得，每小时检测一次更新，若有更新则推送至数据仓库中。

2. 确诊患者分布在各个省区市及具体地址、经纬度等数据来自cbndata， 这里可用作全国地图分布图。 

3. 数据更新频率为半小时。

感谢各个数据提供方。

## 表结构

该程序可以采用mysql或者postgresql， 详见[scheme.sql](file/scheme.sql)

1. 表`ncov_addr_detail` : 全国确认疫情患者地区分布数据

![Image text](https://gitee.com/tree3170/ncov-2019/raw/master/image/ncov_addr_detail_schema.png)

2. 表`ncov_detail` : 全国确认疫情患者地区分布数据

![Image text](https://gitee.com/tree3170/ncov-2019/raw/master/image/ncov_detail_schema.png)

3. 表`ncov_country_stat`: 按每天统计全国确认数、感染数、确诊数、死亡数、治愈数等

![Image text](https://gitee.com/tree3170/ncov-2019/raw/master/image/ncov_country_stat_schema.png)

4. 表`ncov_country_stat_latest`: 今天最新统计全国确认数、感染数、确诊数、死亡数、治愈数、现存确诊人数（较昨日增加数量）、累计确诊人数（较昨日增加数量）、疑似感染人数（较昨日增加数量）、治愈人数（较昨日增加数量）、死亡人数（较昨日增加数量）、重症病例人数（较昨日增加数量）等

![Image text](https://gitee.com/tree3170/ncov-2019/raw/master/image/ncov_country_stat_latest_schema.png)

5. 表`ncov_province_stat`: 按天统计全国省确认数、感染数、确诊数、死亡数、治愈数等

![Image text](https://gitee.com/tree3170/ncov-2019/raw/master/image/ncov_province_stat_schema.png)

6. 表`ncov_province_stat_latest`: 今天最新统计省份确认数、感染数、确诊数、死亡数、治愈数、现存确诊人数（较昨日增加数量）、累计确诊人数（较昨日增加数量）、疑似感染人数（较昨日增加数量）、治愈人数（较昨日增加数量）、死亡人数（较昨日增加数量）、重症病例人数（较昨日增加数量）等

![Image text](https://gitee.com/tree3170/ncov-2019/raw/master/image/ncov_province_stat_latest.png)

  

## 具体展现为大屏

[由于隐私，暂时不方便放出大屏截图， 有兴趣的朋友可以和我单独详聊]

## 快速入门

- [ ] 执行初始化程序

  - [ ] 1. 执行确认疫情患者地址初始化，详见NcovAddrApplicationTests.java

  ```java
  @Test
      public void localInitData() throws Exception{
          addrService.initDataFromLocal();
      }
  ```

  

  - [ ] 2. 执行省市区详细数据初始化，详见NcovDetailApplicationTests.java

  ```java
   @Test
      public void initData() throws Exception{
  			ncovService.initDataFromLocal();
      }
  ```
  
- [ ] 启动程序，自动半小时更新
  
- [ ] 运行日志截图

  ![Image text](https://gitee.com/tree3170/ncov-2019/raw/master/image/运行日志截图.png)

详情设计流程参见[ncov](doc/ncov.md)
## 数据来源

1. [BlankerL](https://github.com/BlankerL)的项目[[DXY-COVID-19-Data](https://github.com/BlankerL/DXY-COVID-19-Data)]

2. [canghailan](https://github.com/canghailan)的项目[[Wuhan-2019-nCoV](https://github.com/canghailan/Wuhan-2019-nCoV)]

3. [cbndata](https://assets.cbndata.org/2019-nCoV/data.json)

  
