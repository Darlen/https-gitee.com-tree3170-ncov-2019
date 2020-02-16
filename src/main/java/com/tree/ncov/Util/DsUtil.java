package com.tree.ncov.Util;

import com.tree.nebula.datasource.dto.DataSourceDTO;
import com.tree.nebula.datasource.dto.ParamDTO;
import com.tree.nebula.datasource.enums.DataSourceTypesEnum;
import com.tree.nebula.datasource.executor.QueryExecutorFactory;
import com.tree.nebula.datasource.executor.RdsQueryExecutor;
import com.tree.nebula.datasource.query.BoundSql;
import com.tree.nebula.datasource.query.QueryContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @ClassName com.tree.ncov.Util
 * Description: <类功能描述>. <br>
 * <p>
 * <使用说明>
 * </p>
 * @Author tree
 * @Date 2020-02-14 16:08
 * @Version 1.0
 */
@Component
public class DsUtil {

    public static void execute(String sql, List<ParamDTO> params){
        DataSourceDTO dataSourceDTO = getDatasourceDto();
        RdsQueryExecutor executor = QueryExecutorFactory.getQueryExecutor(dataSourceDTO.getType());
        QueryContext context = QueryContext.sqlBuilder()
                .boundSql(new BoundSql(sql,null))
                .dataSourceDTO(dataSourceDTO)
                .build();

        executor.updateData(context);
    }


    private static DataSourceDTO getDatasourceDto() {
        DataSourceDTO dataSource = new DataSourceDTO.Builder()
                .name("ncov")
                .type(DataSourceTypesEnum.mysql)
                .domain("127.0.0.1")
                .port("3306")
                .databaseName("user")
                .username("root")
                .password("root")
                //设置datasourcePool
//                .dataSoucePool(DataSourcePool.builder().maxIdle(60).minIdle(1).build())
                .build();
        return dataSource;
    }
}
