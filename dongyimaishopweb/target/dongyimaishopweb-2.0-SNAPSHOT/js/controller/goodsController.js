 //控制层 
app.controller('goodsController' ,function($scope,$controller,itemCatService,$location,typeTemplateService,uploadService,goodsService){
	
	$controller('baseController',{$scope:$scope});//继承

	$scope.status=['未审核','审核通过','审核驳回','关闭'];//商品状态

	$scope.entity = {
		tbGoods:{},
		tbGoodsDesc:{
			itemImages:[],
			specificationItems:[]
		}
	}

	$scope.itemCatList=[];//商品分类列表

	$scope.findItemCatList = function(){
		itemCatService.findAll().success(
			function (response) {
				for(var i=0;i<response.length;i++){
					$scope.itemCatList[response[i].id]=response[i].name;
				}
			}
		);
	}

	//创建矩阵对象
	$scope.createItemList = function () {
		//创建sku 的模版
		$scope.entity.itemList = [{spec:{},price:0,num:99999,status:'0',isDefault:'0'}];
		//遍历用户选择规格选项列表
		//[{"attributeName":"网络","attributeValue":["移动3G","移动4G"]},{"attributeName":"机身内存","attributeValue":["16G","64G"]}]
		var items = $scope.entity.tbGoodsDesc.specificationItems;
		for(var i=0;i<items.length;i++){
			//制作矩阵对象数组
			//{"attributeName":"网络","attributeValue":["移动3G","移动4G"]}
			// {spec:{'网络':'移动3G'},price:0,num:99999,status:'0',isDefault:'0'}，{spec:{'网络':'移动4G'},price:0,num:99999,status:'0',isDefault:'0'}
			$scope.entity.itemList = addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue);
		}

	}

	var addColumn = function(itemList,attributeName,attributeValues){
		//定义新的数组 作为返回值
		var newList = [];
		//循环遍历 itemList
		//{spec:{},price:0,num:99999,status:'0',isDefault:'0'}
		// {spec:{'网络':'移动3G'},price:0,num:99999,status:'0',isDefault:'0'}，
		// {spec:{'网络':'移动4G'},price:0,num:99999,status:'0',isDefault:'0'}
		for(var i=0;i<itemList.length;i++){
			//{spec:{},price:0,num:99999,status:'0',isDefault:'0'}
			var oldRow = itemList[i];
			//{"attributeName":"网络","attributeValue":["移动3G","移动4G"]}
			//["移动3G","移动4G"]
			// {"attributeName":"机身内存","attributeValue":["16G","64G"]}]
			for(var j=0;j<attributeValues.length;j++){
				//深克隆 {spec:{},price:0,num:99999,status:'0',isDefault:'0'}
				var newRow = JSON.parse(JSON.stringify(oldRow));
				// {spec:{'网络':'移动3G'},price:0,num:99999,status:'0',isDefault:'0'}
				// {spec:{'网络':'移动3G'，'机身内存':'16G'},price:0,num:99999,status:'0',isDefault:'0'}
				newRow.spec[attributeName] = attributeValues[j];
				//将拼接好的临时变量存入 返回值 数组中
				// {spec:{'网络':'移动3G'},price:0,num:99999,status:'0',isDefault:'0'}，{spec:{'网络':'移动4G'},price:0,num:99999,status:'0',isDefault:'0'}
				// [{spec:{'网络':'移动3G'，'机身内存':'16G'},price:0,num:99999,status:'0',isDefault:'0'},
				// {spec:{'网络':'移动3G'，'机身内存':'64G'},price:0,num:99999,status:'0',isDefault:'0'},
				// {spec:{'网络':'移动4G'，'机身内存':'16G'},price:0,num:99999,status:'0',isDefault:'0'},
				// {spec:{'网络':'移动4G','机身内存':'64G'},price:0,num:99999,status:'0',isDefault:'0'}]
				newList.push(newRow);
			}
		}
		return newList;
	}

	//								   网络		移动3G
	$scope.checkAttributeValue = function (specName,optionName) {
		//获得用户已经选择过的规格
		var specItems = $scope.entity.tbGoodsDesc.specificationItems;
		//调用searchObjectByKey 筛选出用户选择过的该规格名称对应的数组
		//[{"attributeValue":["移动3G","移动4G","联通3G","联通4G"],"attributeName":"网络"},{"attributeValue":["128G","64G"],"attributeName":"机身内存"}]
		var selectedSepcItems = searchObjectByKey(specItems,'attributeName',specName)
		if(selectedSepcItems != null){
			// ["移动3G","移动4G","联通3G","联通4G"]
			if(selectedSepcItems.attributeValue.indexOf(optionName)>=0){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}

	}

	//获取目前所勾选的规格选项 的数组对象
	var searchObjectByKey = function(specArray,key,name){
		// {"attributeValue":["移动3G","移动4G","联通3G","联通4G"],"attributeName":"网络"}
		for(var i=0;i<specArray.length;i++){
			//网络 == 网络
			if(specArray[i][key] == name){
				return specArray[i];
			}
		}
		return null;
	}

	//修改规格选项 勾选内容
	$scope.updateSpecAttribute = function($event,name,value){
		//查询当前所勾选的选项是否存在于 目前的规格选项中
		//人为设定 specificationItems [] name为规格名称
		var obj = searchObjectByKey($scope.entity.tbGoodsDesc.specificationItems,'attributeName',name);
		if(obj != null){
			//判断 该规格选项是否勾选 如果勾选则 推送数组 否则 删除数组中元素
			if($event.target.checked){
				obj.attributeValue.push(value);
			}else{
				obj.attributeValue.splice(obj.attributeValue.indexOf(value),1);
				if( obj.attributeValue.length == 0){
					//如果 规格选项数组中 的 选项对象中 一个值都没有 则需要将该对象从 规格选项数组中移除
					$scope.entity.tbGoodsDesc.specificationItems.splice($scope.entity.tbGoodsDesc.specificationItems.indexOf(obj),1);
				}

			}


		}else{
			//如果没有勾选过该类型的规格 那么就push一个具有 人为设定格式的对象
			$scope.entity.tbGoodsDesc.specificationItems.push({'attributeName':name,'attributeValue':[value]});
		}
	}

	//读取一级分类
	$scope.selectItemCat1List = function(){
		itemCatService.findItemCatListByParentId(0).success(
			function (response) {
				$scope.itemCat1List=response;
			}
		);
	}

	//读取二级分类
	$scope.$watch('entity.tbGoods.category1Id',function (newValue,oldValue) {
		itemCatService.findItemCatListByParentId(newValue).success(
			function (response) {
				$scope.itemCat2List = response;
			}
		);

	})

	//读取三级分离
	$scope.$watch('entity.tbGoods.category2Id',function (newValue) {
		itemCatService.findItemCatListByParentId(newValue).success(
			function (response) {
				$scope.itemCat3List = response;
			}
		);
	})


	//获取模板
	$scope.$watch('entity.tbGoods.category3Id',function (newValue,oldValue) {
		itemCatService.findOne(newValue).success(
			function (response) {
				$scope.entity.tbGoods.typeTemplateId = response.typeId;
			}
		);
	})

	//监控模板生成品牌
	$scope.$watch('entity.tbGoods.typeTemplateId',function (newValue) {
		//对于模板基本信息方法查询
		typeTemplateService.findOne(newValue).success(
			function (response) {
				$scope.typeTemplate = response;
				$scope.typeTemplate.brandIds= JSON.parse( $scope.typeTemplate.brandIds);//品牌列表
				//如果没有ID，则加载模板中的扩展数据
				if($location.search()['id']==null) {
					$scope.entity.tbGoodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);//扩展属性
				}
			}
		);

		//根据模板id对于规格进行查询
		typeTemplateService.findSpecList(newValue).success(
			function (response) {
				$scope.specList = response;
			}
		);
	})



	$scope.saveImgs = function(){
		$scope.entity.tbGoodsDesc.itemImages.push($scope.entity_img);
	}

	$scope.removeImg = function(entity_img){
        $scope.entity.tbGoodsDesc.itemImages.splice($scope.entity.tbGoodsDesc.itemImages.indexOf(entity_img),1);
	}

	$scope.upload = function(){
        uploadService.upload().success(
        	function (response) {
				if(response.success){
					$scope.entity_img.url=response.message;
				}
            }
		);
	}

    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){

		var id= $location.search()['id'];//获取参数值

		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;
				//向富文本编辑器添加商品介绍
				editor.html($scope.entity.tbGoodsDesc.introduction);
				//显示图片列表
				$scope.entity.tbGoodsDesc.itemImages= JSON.parse($scope.entity.tbGoodsDesc.itemImages);
				//显示扩展属性
				$scope.entity.tbGoodsDesc.customAttributeItems=  JSON.parse($scope.entity.tbGoodsDesc.customAttributeItems);
				//规格
				$scope.entity.tbGoodsDesc.specificationItems=  JSON.parse($scope.entity.tbGoodsDesc.specificationItems);
				//SKU列表规格列转换
				for( var i=0;i<$scope.entity.itemList.length;i++ ){
					$scope.entity.itemList[i].spec = JSON.parse( $scope.entity.itemList[i].spec);
				}
			}
		);				
	}
	
	//保存 
	$scope.save=function(){
		//提取文本编辑器的值
		$scope.entity.tbGoodsDesc.introduction = editor.html();

		var serviceObject;//服务层对象  				
		if($scope.entity.tbGoods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
					location.href="goods.html";//跳转到商品列表页
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
    
});	