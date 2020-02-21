package com.tree.ncov.statdata;

import com.tree.ncov.statdata.entity.*;
import com.tree.ncov.statdata.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @ClassName com.tree.ncov.statdata
 * Description: <类功能描述>. <br>
 * <p>
 * <使用说明>
 * </p>
 * @Author tree
 * @Date 2020-02-18 16:11
 * @Version 1.0
 */
@Service
public class ProvinceDetailService {

    @Autowired
    private ProvDetailRepository provDetailRepository;

    @Autowired
    private ProvLatestDetailRepository provLatestDetailRepository;

    @Autowired
    private CityDetailRepository cityDetailRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private CountryLatestRepository countryLatestRepository;

    /**
     * 更新所有相关的数据
     * 注意前提是城市已经被更新， 即ncov_detail， 否则都是以前数据
     */
    public void updateProvinceTodayData() {
        //TODO 必须要32个省的数据， 有时候更新没有

        NcovCountry country = getTodayCountryDetailFromDB();
        if(country != null && country.getResults()!= null && country.getResults().size() > 0) {
            /*
            处理latest
            删除当天数据所有省数据, 再添加
            */
            provDetailRepository.deleteToday();
            provDetailRepository.saveAll(country.getResults());

            /*
             处理latest
            */
            provLatestDetailRepository.deleteAll();
            provLatestDetailRepository.saveAll(NcovProvDetailLatest.convert(country.getResults()));
        }
    }

    /**
     * 获取今天国家的详细数据
     *
     * @return
     */
    public NcovCountry getTodayCountryDetailFromDB() {
        NcovCountry todayCountry = new NcovCountry();
        List<NcovCityDetail> cityDetails = cityDetailRepository.findByToday();

        if (cityDetails != null && cityDetails.size() > 0) {
            Map<String, List<NcovCityDetail>> provinceCitiesMap = new HashMap<>();
            List<NcovProvDetail> ncovProvDetails = new ArrayList<>();

            long curConfirmCount = 0;
            long confirmedCount = 0;
            long suspectedCount = 0;
            long curedCount = 0;
            long deadCount = 0;
            Date date = cityDetails.get(0).getUpdateTime();
            for (NcovCityDetail cityDetail : cityDetails) {
                String province = cityDetail.getProvinceName();
                //
                //组装所有的省ncovProvDetails， 并累计计算每个省的数据， 用于全国数据
                if (!provinceCitiesMap.containsKey(province)) {
                    NcovProvDetail provDetail = new NcovProvDetail();
                    provDetail.setCountryName(Optional.ofNullable(cityDetail.getCountryName()).orElse("中国"));
                    provDetail.setProvinceName(cityDetail.getProvinceName());
                    provDetail.setProvinceShortName(cityDetail.getProvinceShortName());
                    provDetail.setCurrentConfirmedCount(cityDetail.getProvCurConfirmCount());
                    provDetail.setConfirmedCount(cityDetail.getProvinceConfirmedCount());
                    provDetail.setSuspectedCount(cityDetail.getProvinceSuspectedCount());
                    provDetail.setCuredCount(cityDetail.getProvinceCuredCount());
                    provDetail.setDeadCount(cityDetail.getProvinceDeadCount());
                    provDetail.setUpdateTime(cityDetail.getUpdateTime());
                    ncovProvDetails.add(provDetail);
                    //累加每个省份的数据
                    curConfirmCount += cityDetail.getProvCurConfirmCount();
                    confirmedCount += cityDetail.getProvinceConfirmedCount();
                    suspectedCount += cityDetail.getProvinceSuspectedCount();
                    curedCount += cityDetail.getProvinceCuredCount();
                    deadCount += cityDetail.getProvinceDeadCount();
                }

                //组装每个省的所有市
                List<NcovCityDetail> tmp = provinceCitiesMap.get(province);
                tmp = Optional.ofNullable(tmp).orElse(new ArrayList<>());
                tmp.add(cityDetail);
                provinceCitiesMap.put(province, tmp);
            }
            ncovProvDetails.forEach(provDetail -> {
                provDetail.setCities(provinceCitiesMap.get(provDetail.getProvinceName()));
            });

             /*
            处理当天中国数据
             */
            todayCountry.setCountryName("中国");
            todayCountry.setUpdateTime(date);
            todayCountry.setCurrentConfirmedCount(curConfirmCount);
            todayCountry.setConfirmedCount(confirmedCount);
            todayCountry.setSuspectedCount(suspectedCount);
            todayCountry.setCuredCount(curedCount);
            todayCountry.setDeadCount(deadCount);
            todayCountry.setResults(ncovProvDetails);
        }
        return todayCountry;
    }


}
