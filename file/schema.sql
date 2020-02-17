CREATE TABLE `ncov_detail` (
                               `id` int(11) NOT NULL AUTO_INCREMENT,
                               `province_name` varchar(255) NOT NULL COMMENT '省',
                               `city_name` varchar(255) NOT NULL COMMENT '市',
                               `province_cur_confirmed_count` int(11) unsigned zerofill DEFAULT '00000000000' COMMENT '当前省确诊数',
                               `province_confirmed_count` int(11) DEFAULT NULL COMMENT '累计省确诊数',
                               `province_suspected_count` int(11) DEFAULT NULL COMMENT '省疑似感染数',
                               `province_cured_count` int(11) DEFAULT NULL COMMENT '省治愈数',
                               `province_dead_count` int(11) DEFAULT NULL COMMENT '省死亡数',
                               `city_cur_confirmed_count` int(11) unsigned zerofill DEFAULT '00000000000' COMMENT '市当前确诊数',
                               `city_confirmed_count` int(11) DEFAULT NULL COMMENT '市累计确诊数',
                               `city_suspected_count` int(11) DEFAULT NULL COMMENT '市疑似数',
                               `city_cured_count` int(11) DEFAULT NULL COMMENT '市治愈数',
                               `city_dead_count` int(11) DEFAULT NULL COMMENT '市死亡数',
                               `updateTime` datetime NOT NULL COMMENT '更新时间',
                               `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `ncov_addr_detail` (
                                    `id` int(11) NOT NULL AUTO_INCREMENT,
                                    `address` varchar(255) NOT NULL COMMENT '地址',
                                    `province` varchar(255) NOT NULL COMMENT '省',
                                    `city` varchar(255) NOT NULL COMMENT '市',
                                    `district` varchar(255) NOT NULL COMMENT '区/县',
                                    `latitude` varchar(255) NOT NULL COMMENT '纬度',
                                    `count` int(11) DEFAULT NULL COMMENT '数量',
                                    `longitude` varchar(255) NOT NULL COMMENT '经度',
                                    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6292 DEFAULT CHARSET=utf8;