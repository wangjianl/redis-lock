package com.soft.demo.dao;

import org.apache.ibatis.annotations.*;

/**
 * Created by wangjian on 19/3/4.
 * goods code count
 */

@Mapper
public interface GoodsDao {

    @Update("update goods set count = count -1 where code =#{code} and count>0")
    int buy(@Param("code") String GoodsCode);

    @Select("select count from goods where code = #{code}")
    int getCount(@Param("code") String goodsCode);

    @Insert("insert goods(code,count)values(#{code},#{count})")
    void insertGoods(@Param("code") String goodsCode,@Param("count") int count);

}
