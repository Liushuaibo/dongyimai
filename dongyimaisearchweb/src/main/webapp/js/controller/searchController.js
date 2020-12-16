app.controller('searchController',function ($scope,$location,searchService) {

    $scope.searchMap = {keywords:'',category:'',brand:'',
        price:'',spec:{},pageNo:1,pageSize:10,sortField:'',sort:''};


    $scope.loadkeywords = function(){
        $scope.searchMap.keywords = $location.search()['keywords'];
        $scope.search();
    }

    //排序方法
    $scope.sortSearch = function(sortField,sort){
        $scope.searchMap.sortField = sortField;
        $scope.searchMap.sort = sort;
        $scope.search();
    }

    //隐藏品牌列表   判断关键字是不是品牌
    $scope.keywordsIsBrand=function(){
        for(var i=0;i<$scope.resultMap.brandList.length;i++){
            if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){
                return true;
            }
        }
        return false;
    }

    $scope.queryByPage = function (pageNo) {
        if(pageNo < 1){
            pageNo= 1;
        }else if(pageNo > $scope.resultMap.totalPages){
            pageNo = $scope.resultMap.totalPages;
        }
        $scope.searchMap.pageNo = parseInt(pageNo);
        $scope.search();
    }

    //制作分页
    var buildPagLabel = function(){
        //定义一个页码的数组
        $scope.pageLabel = [];

        var firstPage = 1;
        var lastPage = $scope.resultMap.totalPages;//查询回的最后一页
        var endPage = lastPage;

         $scope.firstDot = false;
         $scope.lastDot = false;

        //每页显示5个页码 封装页码数组
        if (lastPage > 5){

            if($scope.searchMap.pageNo<=3){
                endPage = 5;
                $scope.lastDot = true;
            }else if($scope.searchMap.pageNo >= (lastPage-2)){
                firstPage = lastPage - 4;
                $scope.firstDot = true;
            }else{
                firstPage = $scope.searchMap.pageNo - 2;
                endPage = $scope.searchMap.pageNo + 2;

                $scope.firstDot =true;
                $scope.lastDot =true;
            }

        }

        for(var i=firstPage;i<=endPage;i++){
            $scope.pageLabel.push(i);
        }
    }


    //移除搜索元素
    $scope.removeSearchItem = function(key,value){
        if (key == 'category' || key == 'brand' || key == 'price' ) {
            $scope.searchMap[key] = "";
        }else{
            delete $scope.searchMap.spec[key];
        }
        $scope.search();
    }

    //添加搜索元素
    $scope.addSearchItem = function(key,value){
        if (key == 'category' || key == 'brand' || key == 'price' ) {
            $scope.searchMap[key] = value;
        }else{
            $scope.searchMap.spec[key] = value;
        }
        $scope.search();
    }

    //搜索
    $scope.search = function () {
        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap = response;
                buildPagLabel();
            }
        );
    }

})