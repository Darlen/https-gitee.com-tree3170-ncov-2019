package com.tree.ncov.github.repository;

import com.tree.ncov.github.entity.NcovCountry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

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
public interface CountryRepository extends JpaRepository<NcovCountry, Long> {

    /**
     * 删除当天数据
     */
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "delete from ncov_country_stat where TO_DAYS(update_date) = TO_DAYS(now())")
    void deleteToday();

}
