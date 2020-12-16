app.service('cartService',function ($http) {

    //保存订单
    this.submit=function(entity){
        return $http.post('../order/add.do',entity);
    }


    this.findByAddrUserId = function () {
        return $http.get('../address/findByAddrUserId.do');
    }


    this.findCartList = function () {
        return $http.get('../cart/findCartList.do');
    }

    this.addGoodsToCart = function (itemId,num) {
        return $http.get('../cart/addGoodsToCart.do?itemId='+itemId +"&num=" + num);
    }

    //求合计
    this.sum = function (cartList) {
        var totalValue = {totalNum:0,totalMoney:0.00};

        for(var i=0;i<cartList.length;i++){
            var cart = cartList[i];
            for(var j=0;j<cart.orderItemList.length;j++){
                var item = cart.orderItemList[j];
                totalValue.totalNum += item.num;
                totalValue.totalMoney += item.totalFee;
            }
        }

        return totalValue;
    }
})