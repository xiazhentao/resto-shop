<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>
<div id="control">
	<div class="row form-div" v-if="showform">
		<div class="col-md-offset-3 col-md-6" >
			<div class="portlet light bordered">
	            <div class="portlet-title">
	                <div class="caption">
	                    <span class="caption-subject bold font-blue-hoki"> 表单</span>
	                </div>
	            </div>
	            <div class="portlet-body">
	            	<form role="form" action="{{m.id?'accountchargeorder/modify':'accountchargeorder/create'}}" @submit.prevent="save">
						<div class="form-body">
							<div class="form-group">
                            <label>brandId</label>
                            <input type="text" class="form-control" name="brandId" v-model="m.brandId">
                        </div>
                        <div class="form-group">
                            <label>orderStatus</label>
                            <input type="text" class="form-control" name="orderStatus" v-model="m.orderStatus">
                        </div>
                        <div class="form-group">
                            <label>pushOrderTime</label>
                            <input type="text" class="form-control" name="pushOrderTime" v-model="m.pushOrderTime">
                        </div>
                        <div class="form-group">
                            <label>chargeMoney</label>
                            <input type="text" class="form-control" name="chargeMoney" v-model="m.chargeMoney">
                        </div>
                        <div class="form-group">
                            <label>tradeNo</label>
                            <input type="text" class="form-control" name="tradeNo" v-model="m.tradeNo">
                        </div>
                        <div class="form-group">
                            <label>payType</label>
                            <input type="text" class="form-control" name="payType" v-model="m.payType">
                        </div>
                        <div class="form-group">
                            <label>remark</label>
                            <input type="text" class="form-control" name="remark" v-model="m.remark">
                        </div>
                        <div class="form-group">
                            <label>status</label>
                            <input type="text" class="form-control" name="status" v-model="m.status">
                        </div>

						</div>
						<input type="hidden" name="id" v-model="m.id" />
						<input class="btn green"  type="submit"  value="保存"/>
						<a class="btn default" @click="cancel" >取消</a>
					</form>
	            </div>
	        </div>
		</div>
	</div>

	<div>
        <div>
            <label>账户余额：</label>
            <label>199.8元</label>
        </div>
        <div>
            <label>充值金额</label>
            <input type="text" name = "chargeMoney" placeHolder="测试可以用0.01" v-model="order.chargeMoney"/>
        </div>

          <div>
            <input type="text" name = "payType" placeHolder="测试微信支付用1,支付宝支付2" v-model="order.payType" />
        </div>

        <button class="btn btn-default" type="submit" @click="chargeMoney" >确认充值</button>

    </div>

	
	<div class="table-div">
		<div class="table-operator">
			<s:hasPermission name="accountchargeorder/add">
			<button class="btn green pull-right" @click="create">新建</button>
			</s:hasPermission>
		</div>
		<div class="clearfix"></div>
		<div class="table-filter">&nbsp;</div>
		<div class="table-body">
			<table class="table table-striped table-hover table-bordered "></table>
		</div>
	</div>
</div>


<script>
	(function(){
		var cid="#control";
		var $table = $(".table-body>table");
		var tb = $table.DataTable({
			ajax : {
				url : "accountchargeorder/list_all",
				dataSrc : ""
			},
			columns : [
				{
                    title : "创建时间",
                    data : "createTime",
                },

                {
                    title : "充值金额",
                    data : "chargeMoney",
                },
                {
                    title : "交易号",
                    data : "tradeNo",
                },
                {
                    title : "支付方式",
                    data : "payType",
                },

				{
					title : "操作",
					data : "id",
					createdCell:function(td,tdData,rowData,row){
						var operator=[
							<s:hasPermission name="accountchargeorder/delete">
							C.createDelBtn(tdData,"accountchargeorder/delete"),
							</s:hasPermission>
							<s:hasPermission name="accountchargeorder/modify">
							C.createEditBtn(rowData),
							</s:hasPermission>
						];
						$(td).html(operator);
					}
				}],
		});
		
		var C = new Controller(null,tb);
		var vueObj = new Vue({
			el:"#control",
			data:{
			    order:{
			        chargeMoney:0,
			        payType:''
			    }
			},
			mixins:[C.formVueMix],
			methods:{
			    chargeMoney:function(){
	                var that = this;
			        $.ajax({
			            url:"accountchargeorder/charge",
                        type:"post",
                        data:that.order,
                        success:function(){
                            console.log("-----------");
                        }
			        })

			    }

			}
		});
		C.vue=vueObj;
	}());
	
	

	
</script>
