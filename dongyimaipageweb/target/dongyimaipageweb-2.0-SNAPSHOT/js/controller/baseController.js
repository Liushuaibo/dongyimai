app.controller('baseController',function ($scope) {

    //定义选中的复选框的id数组对象
    $scope.selectIds = [];
    //修改id数组对象
    $scope.updateSelection = function ($event,id) {
        if($event.target.checked){
            $scope.selectIds.push(id);
        }else{
            //使用js原生语言
            var idx = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(idx,1);
        }
    }

    //重新加载列表 数据
    $scope.reloadList=function(){
        //切换页码
        //$scope.findPage( $scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    }

    //分页控件配置
    $scope.paginationConf = {
        currentPage: 1,
        totalItems: 10,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function(){
            $scope.reloadList();//重新加载
        }
    };


});