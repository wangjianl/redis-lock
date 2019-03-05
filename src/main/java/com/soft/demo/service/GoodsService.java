package com.soft.demo.service;

import com.soft.demo.dao.GoodsDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 数据操作服务
 */

@Service
public class GoodsService {

    @Autowired(required = false)
    private GoodsDao goodsDao;


    /**
     * 购买商品
     * @param goodsCode
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean buy(String goodsCode){
        try {
            long count = goodsDao.buy(goodsCode);
            if(count!=1){
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    /**
     *  获取库存数
     * @param goodsCode
     * @return
     */
    public int getCount(String goodsCode){
        int count = goodsDao.getCount(goodsCode);
        return count;
    }

    /**
     * 添加商品数据
     * @param goodsCode
     * @param count
     */
    public void insertGoods(String goodsCode,int count){
        goodsDao.insertGoods(goodsCode,count);
    }


}
