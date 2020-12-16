app.controller('cartController',function ($scope,cartService) {

    $scope.submitOrder = function(){

        //将js选择的地址信息赋予 传值对象
        $scope.order.receiverAreaName = $scope.address.address;
        $scope.order.receiverMobile = $scope.address.mobile;
        $scope.order.receiver = $scope.address.contact;

        cartService.submit($scope.order).success(
            function (response) {
                if(response.success){
                    if($scope.order.paymentType == "1"){
                        location.href = "pay.html";
                    }else{
                        location.href = "paysuccess.html";
                    }

                }else{
                    alert(response.message);
                }
            }
        );
    }


    $scope.order = {paymentType:'1'};

    $scope.selectPayType = function(type){
        $scope.order.paymentType = type;
    }


    //选择地址
    $scope.selectAddress=function(address){
        $scope.address=address;
    }

    //判断是否是当前选中的地址
    $scope.isSelectedAddress=function(address){
        if(address==$scope.address){
            return true;
        }else{
            return false;
        }
    }


    $scope.findByAddrUserId = function(){
        cartService.findByAddrUserId().success(
            function (response) {
                $scope.addressList = response;

                //设置默认地址
                for(var i=0;i< $scope.addressList.length;i++){
                    if($scope.addressList[i].isDefault=='1'){
                        $scope.address=$scope.addressList[i];
                        break;
                    }
                }
            }
        );
    }


    $scope.findCartList = function () {
        cartService.findCartList().success(
            function (response) {
                $scope.cartList = response;
                $scope.totalValue=cartService.sum($scope.cartList);//求合计数
            }
        );
    }

    $scope.addGoodsToCart = function (itemId, num) {
        cartService.addGoodsToCart(itemId, num).success(
          function (response) {
            if (response.success){
                $scope.findCartList();//刷新列表
            }else{
                alert(response.message);//弹出错误提示
            }
          }
        );
    }




})