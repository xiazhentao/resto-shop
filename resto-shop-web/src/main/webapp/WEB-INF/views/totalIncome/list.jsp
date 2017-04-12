<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>

<h2 class="text-center"><strong>营业总额报表</strong></h2>
<div id="control">
    <div class="row" id="searchTools">
        <div class="col-md-12">
            <form class="form-inline">
                <div class="form-group" style="margin-right: 50px;">
                    <label for="beginDate">开始时间：</label>
                    <input type="text" class="form-control form_datetime" id="beginDate" readonly="readonly">
                </div>
                <div class="form-group" style="margin-right: 50px;">
                    <label for="endDate">结束时间：</label>
                    <input type="text" class="form-control form_datetime" id="endDate" readonly="readonly">
                    <br></div>
                <button type="button" class="btn btn-primary" id="today"> 今日</button>
                <button type="button" class="btn btn-primary" id="yesterDay">昨日</button>
                <!--              <button type="button" class="btn yellow" id="benxun">本询</button> -->
                <button type="button" class="btn btn-primary" id="week">本周</button>
                <button type="button" class="btn btn-primary" id="month">本月</button>

                <button type="button" class="btn btn-primary" id="searchReport">查询报表</button>&nbsp;
                <button type="button" class="btn btn-primary" id="brandreportExcel">下载报表</button><br/>
                <form>
                    <input type="hidden" id="brandDataTable">
                    <input type="hidden" id="shopDataTable">
                </form>&nbsp;&nbsp;&nbsp;
            </form>
        </div>
    </div>
    <br/>
    <div>
        <!-- 每日报表 -->
        <div id="report-editor">
            <div class="panel panel-success">
                <div class="panel-heading text-center">
                    <strong style="margin-right:100px;font-size:22px">品牌收入条目</strong>
                    <button type="button" style="float: right;" @click="openModal(0)" class="btn btn-primary">月报表</button>
                </div>
                <div class="panel-body">
                    <table id="brandReportTable" class="table table-striped table-bordered table-hover" width="100%"></table>
                </div>
                <div class="panel-heading text-center">
                    <strong style="margin-right:100px;font-size:22px">店铺收入条目</strong>
                    <button type="button" style="float: right;" @click="openModal(1)" class="btn btn-primary">月报表</button>
                </div>
                <div class="panel-body">
                    <table id="shopReportTable" class="table table-striped table-bordered table-hover" width="100%"></table>
                </div>
            </div>
        </div>

        <div class="modal fade" id="queryCriteriaModal" tabindex="-1" role="dialog" data-backdrop="static">
            <div class="modal-dialog modal-full">
                <div class="modal-content" style="width: 30em;margin: 15% auto;">
                    <div class="modal-header" style="border-bottom:initial;">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                        <h4 align="center"><b>下载月报表</b></h4>
                    </div>
                    <div class="modal-body" align="center">
                        <select style="padding: 5px 12px;" :value="selectYear" v-model="selectYear">
                            <option :value="year" v-for="year in years">{{year}}</option>
                        </select>
                        <span style="font-size: 16px;margin-left: 15px;font-weight: bold;">年</span>
                        <select style="padding: 5px 12px;" :value="selectMonth" v-model="selectMonth">
                            <option :value="month" v-for="month in months">{{month}}</option>
                        </select>
                        <span style="font-size: 16px;margin-left: 15px;font-weight: bold;">月</span>
                    </div>
                    <div class="modal-footer" style="border-top:initial;">
                        <button type="button" class="btn btn-default" data-dismiss="modal" style="float: left;margin-left: 5em;">取消</button>
                        <button type="button" class="btn btn-primary" @click="createMonthDto" style="float: right;margin-right: 5em;">生成并下载</button>
                    </div>
                </div>
            </div>
        </div>

    </div>
</div>
<script src="assets/customer/date.js" type="text/javascript"></script>
<script>
    //时间插件
    $('.form_datetime').datetimepicker({
        endDate:new Date(),
        minView:"month",
        maxView:"month",
        autoclose:true,//选择后自动关闭时间选择器
        todayBtn:true,//在底部显示 当天日期
        todayHighlight:true,//高亮当前日期
        format:"yyyy-mm-dd",
        startView:"month",
        language:"zh-CN"
    });

    var obj = new Vue({
        el : "#control",
        data : {
            years : [],
            months : ["01","02","03","04","05","06","07","08","09","10","11","12"],
            selectYear : new Date().format("yyyy"),
            selectMonth : new Date().format("MM"),
            type : null
        },
        created : function(){
            this.getYears();
        },
        methods : {
            getYears : function() {
                var years = new Array();
                var year = 2016;
                var nowYear = parseInt(new Date().format("yyyy"));
                for (var i = 0;true;i++){
                    years[i] = year;
                    if (year == nowYear){
                        break;
                    }
                    year++;
                }
                this.years = years;
            },
            openModal : function (type) {
                $("#queryCriteriaModal").modal();
                this.type = type;
            },
            createMonthDto : function () {
                var that = this;
                try {
                    location.href = "totalIncome/createMonthDto?year=" + that.selectYear + "&month="+that.selectMonth+"&type="+that.type+"";
                }catch (e){
                    toastr.clear();
                    toastr.error("生成月报表出错");
                    return;
                }
            }
        }
    });

    //文本框默认值
    $('.form_datetime').val(new Date().format("yyyy-MM-dd"));

    var beginDate = $("#beginDate").val();
    var endDate = $("#endDate").val();
    var dataSource;
    toastr.success('查询中...');
    $.ajax( {
        url:'totalIncome/reportIncome',
        async:false,
        data:{
            'beginDate':beginDate,
            'endDate':endDate
        },
        success:function(data) {
            dataSource=data;
            toastr.clear();
            toastr.success('查询成功');
        },
        error : function() {
            toastr.clear();
            toastr.error("系统异常,请刷新重试");
        }
    });

    var tb1 = $("#brandReportTable").DataTable({
        data:dataSource.brandIncome,
        dom:'i',
        columns : [
            {
                title : "品牌",
                data : "shopName",
            },
            {
                title : "原价销售总额(元)",
                data : "originalAmount"
            },
            {
                title : "订单总额(元)",
                data : "totalIncome",
            },
            {
                title : "微信支付(元)",
                data : "wechatIncome",
            },
            {
                title : "充值账户支付(元)",
                data : "chargeAccountIncome",
            },
            {
                title : "红包支付(元)",
                data : "redIncome",
            },
            {
                title : "优惠券支付(元)",
                data : "couponIncome",
            },
            {
                title : "充值赠送支付(元)",
                data : "chargeGifAccountIncome",
            },
            {
                title : "等位红包支付(元)",
                data : "waitNumberIncome",
            },
            {
                title : "支付宝支付(元)",
                data : "aliPayment"
            },
            {
                title : "刷卡支付(元)",
                data : "backCartPay"
            },
            {
                title : "现金支付(元)",
                data : "moneyPay"
            },
            {
                title : "闪惠支付(元)",
                data : "shanhuiPayment"
            },
            {
                title : "积分支付(元)",
                data : "integralPayment"
            },
            {
                title : "退菜返还红包(元)",
                data:"articleBackPay"
            },
            {
                title : "其他方式支付(元)",
                data : "otherPayment",
            }
        ]

    });

    var tb2 = $("#shopReportTable").DataTable({
        data:dataSource.shopIncome,
        columns : [
            {
                title : "店铺名称",
                data : "shopName",
            },
            {
                title : "原价销售总额(元)",
                data : "originalAmount"
            },
            {
                title : "订单总额(元)",
                data : "totalIncome",
            },
            {
                title : "微信支付(元)",
                data : "wechatIncome",
            },
            {
                title : "充值账户支付(元)",
                data : "chargeAccountIncome",
            },
            {
                title : "红包支付(元)",
                data : "redIncome",
            },
            {
                title : "优惠券支付(元)",
                data : "couponIncome",
            },

            {
                title : "充值赠送支付(元)",
                data : "chargeGifAccountIncome",
            },
            {
                title : "等位红包支付(元)",
                data : "waitNumberIncome",
            },
            {
                title : "支付宝支付(元)",
                data : "aliPayment",
            },
            {
                title : "刷卡支付(元)",
                data : "backCartPay"
            },
            {
                title : "现金支付(元)",
                data : "moneyPay"
            },
            {
                title : "闪惠支付(元)",
                data : "shanhuiPayment"
            },
            {
                title : "积分支付(元)",
                data : "integralPayment"
            },
            {
                title : "退菜返还红包(元)",
                data:"articleBackPay"
            },
            {
                title : "其他方式支付(元)",
                data : "otherPayment"
            }
        ]

    });

    //查询
    $("#searchReport").click(function(){
        beginDate = $("#beginDate").val();
        endDate = $("#endDate").val();
        searchInfo(beginDate,endDate);

    })

    //今日

    $("#today").click(function(){
        date = new Date().format("yyyy-MM-dd");
        beginDate = date;
        endDate = date;
        searchInfo(beginDate,endDate);
    });

    //昨日
    $("#yesterDay").click(function(){
        beginDate = GetDateStr(-1);
        endDate = GetDateStr(-1);
        $("#beginDate").val(beginDate);
        $("#endDate").val(endDate);
        searchInfo(beginDate,endDate);

    });

    //本周
    $("#week").click(function(){
        beginDate = getWeekStartDate();
        endDate = new Date().format("yyyy-MM-dd");
        $("#beginDate").val(beginDate);
        $("#endDate").val(endDate);
        searchInfo(beginDate,endDate);

    });

    //本月
    $("#month").click(function(){
        beginDate = getMonthStartDate();
        endDate = new Date().format("yyyy-MM-dd");
        $("#beginDate").val(beginDate);
        $("#endDate").val(endDate);
        searchInfo(beginDate,endDate);

    });

    function searchInfo(beginDate,endDate){
        toastr.clear();
        toastr.success('查询中...');
        //更新数据源
        $.ajax( {
            url:'totalIncome/reportIncome',
            data:{
                'beginDate':beginDate,
                'endDate':endDate
            },
            success:function(result) {
                dataSource=result;
                tb1.clear().draw();
                tb2.clear().draw();
                tb1.rows.add(result.brandIncome).draw();
                tb2.rows.add(result.shopIncome).draw();
                toastr.clear();
                toastr.success('查询成功');
            },
            error : function() {
                toastr.clear();
                toastr.error("系统异常，请刷新重试");
            }
        });
    }

    //导出品牌数据
    $("#brandreportExcel").click(function(){
        beginDate = $("#beginDate").val();
        endDate = $("#endDate").val();
        var object = {
            beginDate : beginDate,
            endDate : endDate,
            brandIncomeDtos : dataSource.brandIncome,
            shopIncomeDtos : dataSource.shopIncome
        }
        try {
            $.post("totalIncome/brandExprotExcel",object,function (result) {
                if (result.success){
                    location.href="totalIncome/downloadExcel?path="+result.data+"";
                }else{
                    toastr.clear();
                    toastr.error("生成报表出错");
                }
            });
        }catch (e){
            toastr.clear();
            toastr.error("系统异常，请刷新重试");
        }

    });
</script>
