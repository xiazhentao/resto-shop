<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="s" uri="http://shiro.apache.org/tags" %>

<div id="control">
    <h2 class="text-center"><strong>评论报表</strong></h2><br/>
    <div class="row" id="searchTools">
        <div class="col-md-12">
            <form class="form-inline">
                <div class="form-group" style="margin-right: 50px;">
                    <label for="beginDate">开始时间：</label>
                    <input type="text" class="form-control form_datetime" id="beginDate" v-model="searchDate.beginDate"   readonly="readonly">
                </div>
                <div class="form-group" style="margin-right: 50px;">
                    <label for="endDate">结束时间：</label>
                    <input type="text" class="form-control form_datetime" id="endDate" v-model="searchDate.endDate"   readonly="readonly">
                </div>

                <button type="button" class="btn btn-primary" @click="today"> 今日</button>

                <button type="button" class="btn btn-primary" @click="yesterDay">昨日</button>

                <!--              <button type="button" class="btn btn-primary" @click="benxun">本询</button> -->

                <button type="button" class="btn btn-primary" @click="week">本周</button>
                <button type="button" class="btn btn-primary" @click="month">本月</button>

                <button type="button" class="btn btn-primary" @click="searchInfo">查询报表</button>&nbsp;
                <button type="button" class="btn btn-primary" @click="brandreportExcel">下载报表</button><br/>
            </form>

        </div>
    </div>
    <br/>
    <br/>

    <!-- 品牌订单列表 -->
    <div role="tabpanel" class="tab-pane" id="orderReport">
        <div class="panel panel-primary" style="border-color:write;">
            <!-- 品牌订单 -->
            <div class="panel panel-info">
                <div class="panel-heading text-center">
                    <strong style="margin-right:100px;font-size:22px">品牌评论报表</strong>
                    <button type="button" style="float: right;" @click="openModal(0)" class="btn btn-primary">月报表</button>
                </div>
                <div class="panel-body">
                    <table id="brandOrderTable" class="table table-striped table-bordered table-hover" width="100%">
                        <thead>
                        <tr>
                            <th>品牌</th>
                            <th>评价单数(份)</th>
                            <th>评价率</th>
                            <th>评论红包总额(元)</th>
                            <th>订单总额(元)</th>
                            <th>五星评价数</th>
                            <th>四星评价数</th>
                            <th>三星评价数</th>
                            <th>二星评价数</th>
                            <th>一星评价数</th>
                        </tr>
                        </thead>
                        <tbody>
                        <template v-if="brandAppraise.name != null">
                            <tr>
                                <td><strong>{{brandAppraise.name}}</strong></td>
                                <td>{{brandAppraise.appraiseNum}}</td>
                                <td>{{brandAppraise.appraiseRatio}}</td>
                                <td>{{brandAppraise.redMoney}}</td>
                                <td>{{brandAppraise.totalMoney}}</td>
                                <td>{{brandAppraise.fivestar}}</td>
                                <td>{{brandAppraise.fourstar}}</td>
                                <td>{{brandAppraise.threestar}}</td>
                                <td>{{brandAppraise.twostar}}</td>
                                <td>{{brandAppraise.onestar}}</td>
                            </tr>
                        </template>
                        <template v-else>
                            <tr>
                                <td colspan="10" align="center">暂时没有数据...</td>
                            </tr>
                        </template>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>

    <div role="tabpanel" class="tab-pane">
        <div class="panel panel-primary" style="border-color:write;">
            <!-- 品牌订单 -->
            <div class="panel panel-info">
                <div class="panel-heading text-center">
                    <strong style="margin-right:100px;font-size:22px">店铺评论报表</strong>
                    <button type="button" style="float: right;" @click="openModal(1)" class="btn btn-primary">月报表</button>
                </div>
                <div class="panel-body">
                    <table id="appraiseTable" class="table table-striped table-bordered table-hover" width="100%">
                    </table>
                </div>
            </div>
        </div>
    </div>

    <div class="modal fade" id="reportModal" tabindex="-1" role="dialog" aria-hidden="true">
        <div class="modal-dialog modal-full">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true" @click="closeModal"></button>
                </div>
                <div class="modal-body"> </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-info btn-block" data-dismiss="modal" aria-hidden="true" @click="closeModal" style="position:absolute;bottom:32px;">关闭</button>
                </div>
            </div>
        </div>
    </div>
    <!-- 月报表弹窗 -->
    <div class="modal fade" id="queryCriteriaModal" tabindex="-1" role="dialog" data-backdrop="static">
        <div class="modal-dialog modal-full">
            <div class="modal-content" style="width: 30em;margin: 15% auto;">
                <div class="modal-header" style="border-bottom:initial;">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true"></button>
                    <h4 align="center"><b>下载月报表</b></h4>
                </div>
                <div class="modal-body" align="center">
                    <select style="padding: 5px 12px;" v-model="selectYear">
                        <option :value="year" v-for="year in years">{{year}}</option>
                    </select>
                    <span style="font-size: 16px;margin-left: 15px;font-weight: bold;">年</span>
                    <select style="padding: 5px 12px;" v-model="selectMonth">
                        <option :value="month" v-for="month in months">{{month}}</option>
                    </select>
                    <span style="font-size: 16px;margin-left: 15px;font-weight: bold;">月</span>
                </div>
                <div class="modal-footer" style="border-top:initial;">
                    <button type="button" class="btn btn-default" data-dismiss="modal" style="float: left;margin-left: 5em;">取消</button>
                    <button type="button" class="btn btn-primary" v-if="state == 1" @click="createMonthDto" style="float: right;margin-right: 5em;">生成并下载</button>
                    <button type="button" class="btn btn-success" v-if="state == 2" disabled="disabled" style="float: right;margin-right: 5em;">生成中...</button>
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


    //创建vue对象
    var vueObj =  new Vue({
        el:"#control",
        data:{
            brandAppraise:{},
            shopAppraises:[],
            searchDate : {
                beginDate : "",
                endDate : "",
            },
            appraiseTable : {},
            years : [],
            months : ["01","02","03","04","05","06","07","08","09","10","11","12"],
            selectYear : new Date().format("yyyy"),
            selectMonth : new Date().format("MM"),
            type : 0,
            state : 1
        },
        created : function() {
            var date = new Date().format("yyyy-MM-dd");
            this.searchDate.beginDate = date;
            this.searchDate.endDate = date;
            this.createAppriseTable();
            this.searchInfo();
            this.getYears();
        },
        methods:{
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
            createAppriseTable : function () {
                var that = this;
                this.appraiseTable=$("#appraiseTable").DataTable({
                    lengthMenu: [ [50, 75, 100, -1], [50, 75, 100, "All"] ],
                    order: [[ 0, "asc" ]],
                    columns : [
                        {
                            title : "店铺",
                            data : "shopName"
                        },
                        {
                            title : "评价单数",
                            data : "appraiseNum"
                        },
                        {
                            title : "评价率",
                            data : "appraiseRatio"
                        },
                        {
                            title : "评论红包总额(元)",
                            data : "redMoney"
                        },
                        {
                            title : "订单总额",
                            data : "totalMoney"
                        },
                        {
                            title : "五星评价数",
                            data : "fivestar"
                        },
                        {
                            title : "四星评价数",
                            data : "fourstar"
                        },
                        {
                            title : "三星评价数",
                            data : "threestar"
                        },
                        {
                            title : "二星评价数",
                            data : "twostar"
                        },
                        {
                            title : "一星评价数",
                            data : "onestar"
                        },
                        {
                            title: "查看详情",
                            data: "shopId",
                            orderable : false,
                            createdCell: function (td, tdData, rowData) {
                                var button = $("<a href='appraiseReport/shopReport?beginDate="+that.searchDate.beginDate+"&&endDate="+that.searchDate.endDate+"&&shopId="+tdData+"' class='btn green ajaxify '>查看详情</a>");
                                $(td).html(button);
                            }
                        }
                    ]
                });
            },
            searchInfo : function() {
                var that = this;
                var timeCha = new Date(that.searchDate.endDate).getTime() - new Date(that.searchDate.beginDate).getTime();
                if(timeCha < 0){
                    toastr.clear();
                    toastr.error("开始时间应该少于结束时间！");
                    return false;
                }else if(timeCha > 2678400000){
                    toastr.clear();
                    toastr.error("暂时未开放大于一月以内的查询！");
                    return false;
                }
                toastr.clear();
                toastr.success("查询中...");
                try {
                    $.post("appraiseReport/brand_data", this.getDate(null), function (result) {
                        if (result.success) {
                            that.brandAppraise = result.data.brandAppraise;
                            that.shopAppraises = result.data.shopAppraise;
                            that.appraiseTable.clear();
                            that.appraiseTable.rows.add(result.data.shopAppraise).draw();
                            toastr.clear();
                            toastr.success("查询成功");
                        } else {
                            toastr.clear();
                            toastr.error("查询出错");
                        }
                    });
                }catch (e){
                    toastr.clear();
                    toastr.error("系统异常，请刷新重试");
                }
            },
            getDate : function(shopId){
                var data = {
                    beginDate : this.searchDate.beginDate,
                    endDate : this.searchDate.endDate,
                    shopId : shopId
                };
                return data;
            },
            today : function(){
                date = new Date().format("yyyy-MM-dd");
                this.searchDate.beginDate = date
                this.searchDate.endDate = date
                this.searchInfo();
            },
            yesterDay : function(){
                this.searchDate.beginDate = GetDateStr(-1);
                this.searchDate.endDate  = GetDateStr(-1);
                this.searchInfo();
            },
            week : function(){
                this.searchDate.beginDate  = getWeekStartDate();
                this.searchDate.endDate  = new Date().format("yyyy-MM-dd")
                this.searchInfo();
            },
            month : function(){
                this.searchDate.beginDate  = getMonthStartDate();
                this.searchDate.endDate  = new Date().format("yyyy-MM-dd")
                this.searchInfo();
            },
            //下载
            brandreportExcel : function (){
                var that  = this;
                try{
                    var object = that.getDate(null);
                    object.brandAppraise = that.brandAppraise;
                    object.shopAppraises = that.shopAppraises;
                    $.post("appraiseReport/create_brand_excel",object,function (result) {
                        if (result.success){
                            location.href="appraiseReport/downloadBrandExcel?path="+result.data+"";
                        }else{
                            toastr.clear();
                            toastr.error("下载报表出错！");
                        }
                    });
                }catch (e){
                    toastr.clear();
                    toastr.error("系统异常，请刷新重试");
                }
            },
            openModal : function (type) {
                $("#queryCriteriaModal").modal();
                this.type = type;
            },
            createMonthDto : function () {
                toastr.clear();
                toastr.success("生成中...");
                var that = this;
                that.state = 2;
                try {
                    $.post("appraiseReport/createMonthDto",{year : that.selectYear, month : that.selectMonth, type : that.type},function (result) {
                        if (result.success){
                            that.state = 1;
                            window.location.href="appraiseReport/downloadBrandExcel?path="+result.data+"";
                        }else{
                            that.state = 1;
                            toastr.clear();
                            toastr.error("生成月报表出错");
                        }
                    });
                }catch (e){
                    that.state = 1;
                    toastr.clear();
                    toastr.error("生成月报表出错");
                    return;
                }
            }
        }
    })
</script>

