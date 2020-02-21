package com.tree.ncov.addrdata.repository;

import com.tree.ncov.addrdata.entity.NcovAddrDetail;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @ClassName com.tree.ncov.addrdata.repository
 * Description: <类功能描述>. <br>
 * <p>
 * <使用说明>
 * </p>
 * @Author tree
 * @Date 2020-02-20 17:08
 * @Version 1.0
 */
public interface AddrRepository extends JpaRepository<NcovAddrDetail, Long> {


}
