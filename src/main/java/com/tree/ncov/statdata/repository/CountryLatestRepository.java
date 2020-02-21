package com.tree.ncov.statdata.repository;

import com.tree.ncov.statdata.entity.NcovCountryLatest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * @ClassName com.tree.ncov.statdata.repository
 * Description: <类功能描述>. <br>
 * <p>
 * <使用说明>
 * </p>
 * @Author tree
 * @Date 2020-02-18 14:06
 * @Version 1.0
 */
public interface CountryLatestRepository extends JpaRepository<NcovCountryLatest, Long> {

    /**
     * 删除当天数据
     */
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "delete from ncov_country_stat_latest ")
    void deleteToday();

}
