app.service('uploadService',function ($http) {

    this.upload = function () {
        //创建表单对象
        var formData = new FormData();
        //追加表单中file文件类型的框
        formData.append('file',file.files[0]);
        return $http({
            method:'POST',
            url:"../upload.do",
            data: formData,
            headers: {'Content-Type':undefined},
            transformRequest: angular.identity
        })
    }

})