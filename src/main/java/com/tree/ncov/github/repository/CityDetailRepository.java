package com.tree.ncov.github.repository;

import com.tree.ncov.github.entity.NcovCityDetail;
import com.tree.ncov.github.entity.NcovCountry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @ClassName com.tree.ncov.github.repository
 * Description: <类功能描述>. <br>
 * <p>
 * <使用说明>
 * </p>
 * @Author tree
 * @Date 2020-02-18 14:06
 * @Version 1.0
 */
public interface CityDetailRepository extends JpaRepository<NcovCityDetail, Long> {

    /**
     * 查找当天的所有的市的数据
     * @return
     */
    @Query(nativeQuery = true, value = "select * from ncov_detail where TO_DAYS(updateTime) = TO_DAYS(now())")
    List<NcovCityDetail> findByToday();


    /**
     * 查找某个省份最新的一条数据
     * @return
     */
    @Query(nativeQuery = true, value = "select * from ncov_detail where province_name = :province limit 1")
    NcovCityDetail findLatestByProvince(@Param("province") String province);

}
