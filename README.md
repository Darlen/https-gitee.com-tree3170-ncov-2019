# 2019新型冠状病毒后端数据处理

## 数据来源：
本项目为2019新型冠状病毒（COVID-19/2019-nCoV）疫情状况的时间序列数据仓库

1. 每个城市、省份、国家的来源来自[BlankerL](https://github.com/BlankerL)的项目[[DXY-COVID-19-Data](https://github.com/BlankerL/DXY-COVID-19-Data)]，其作者的数据来源于[丁香园](https://3g.dxy.cn/newh5/view/pneumonia)。非常感谢该作者既提供数据， 还提供API接口给到各开发者使用。

   数据由[2019新型冠状病毒疫情实时爬虫](https://github.com/BlankerL/DXY-COVID-19-Crawler)获得，每小时检测一次更新，若有更新则推送至数据仓库中。

2. 确诊患者分布在各个省区市及具体地址、经纬度等数据来自cbndata， 这里可用作全国地图分布图。 

3. 数据更新频率为半小时。

感谢各个数据提供方。

## 表结构

该程序可以采用mysql或者postgresql， 详见doc/scheme.sql

## 具体展现图表



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
  

## 数据来源

1. [BlankerL](https://github.com/BlankerL)的项目[[DXY-COVID-19-Data](https://github.com/BlankerL/DXY-COVID-19-Data)]

2. [canghailan](https://github.com/canghailan)的项目[[Wuhan-2019-nCoV](https://github.com/canghailan/Wuhan-2019-nCoV)]

3. [cbndata](https://assets.cbndata.org/2019-nCoV/data.json)

  
