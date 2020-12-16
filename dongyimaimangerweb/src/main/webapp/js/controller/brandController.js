app.controller('brandController',function ($controller,$scope,$http,brandService) {

    //控制器的继承
    $controller('baseController',{$scope:$scope});

    $scope.findAll = function(){
        //3、访问后台
        brandService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }


//分页
    $scope.findPage=function(page,rows){
        brandService.findPage(page,rows).success(
            function(response){
                $scope.list=response.rows;
                $scope.paginationConf.totalItems=response.total;//更新总记录数
            }
        );
    }

    $scope.save = function () {

        var methodType = "save";
        if($scope.entity.id != null){
            methodType = "update";
        }

        brandService.save(methodType,$scope.entity).success(
            function (response) {
                if(response.success){
                    $scope.reloadList();
                }else{
                    alert(response.message);
                }
            }
        );
    }

    $scope.findOne = function (id) {
        brandService.findOne(id).success(
            function (response) {
                $scope.entity = response;
            }
        );
    }


    //删除方法
    $scope.dele = function () {

        brandService.dele($scope.selectIds).success(
            function (response) {
                if(response.success){
                    $scope.reloadList();
                }else{
                    alert(response.message);
                }
            }
        );
    }

    //1、条件拼接
    $scope.searchEntity = {};

    $scope.search = function (page,rows) {
        brandService.search(page,rows,$scope.searchEntity).success(
            function (response) {
                $scope.list=response.rows;
                $scope.paginationConf.totalItems=response.total;//更新总记录数
            }
        );
    }

});