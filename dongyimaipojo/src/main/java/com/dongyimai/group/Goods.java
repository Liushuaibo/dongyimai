package com.dongyimai.group;

import com.dongyimai.pojo.TbGoods;
import com.dongyimai.pojo.TbGoodsDesc;
import com.dongyimai.pojo.TbItem;

import java.io.Serializable;
import java.util.List;

public class Goods implements Serializable {

    private TbGoods tbGoods;
    private TbGoodsDesc tbGoodsDesc;

    private List<TbItem> itemList;//商品的不同规格

    public Goods() {
    }

    public Goods(TbGoods tbGoods, TbGoodsDesc tbGoodsDesc, List<TbItem> itemList) {
        this.tbGoods = tbGoods;
        this.tbGoodsDesc = tbGoodsDesc;
        this.itemList = itemList;
    }

    public TbGoods getTbGoods() {
        return tbGoods;
    }

    public void setTbGoods(TbGoods tbGoods) {
        this.tbGoods = tbGoods;
    }

    public TbGoodsDesc getTbGoodsDesc() {
        return tbGoodsDesc;
    }

    public void setTbGoodsDesc(TbGoodsDesc tbGoodsDesc) {
        this.tbGoodsDesc = tbGoodsDesc;
    }

    public List<TbItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<TbItem> itemList) {
        this.itemList = itemList;
    }
}
