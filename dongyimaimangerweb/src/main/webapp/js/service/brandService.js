app.service('brandService',function ($http) {

    this.selectOptionList = function () {
        return $http.get('../brand/selectOptionList.do');
    }

    this.findAll = function () {
        return $http.get('../brand/findAll.do');
    }

    this.findPage = function (page,rows) {
        return $http.get('../brand/findPage.do?page='+page+'&rows='+rows);
    }

    this.save = function (methodType,entity) {
        return $http.post('../brand/'+methodType+'.do',entity);
    }

    this.findOne = function (id) {
        return $http.get('../brand/findOne.do?id='+id);
    }

    this.dele = function (ids) {
        return $http.get('../brand/dele.do?ids='+ids);
    }

    this.search = function (page,rows,searchEntity) {
        return $http.post('../brand/search.do?page='+page+'&rows='+rows,searchEntity);
    }

});