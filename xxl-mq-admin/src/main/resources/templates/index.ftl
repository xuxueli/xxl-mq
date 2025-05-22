<!DOCTYPE html>
<html>
<head>
    <#-- import macro -->
  	<#import "./common/common.macro.ftl" as netCommon>
    <#-- commonStyle -->
	<@netCommon.commonStyle />

    <#-- biz start（1/5 style） -->
    <link rel="stylesheet" href="${request.contextPath}/static/adminlte/bower_components/bootstrap-daterangepicker/daterangepicker.css">
    <#-- biz end（1/5 end） -->

</head>
<body class="hold-transition skin-blue sidebar-mini" >
<div class="wrapper" >

    <!-- header -->
    <@netCommon.commonHeader />

    <!-- left -->
    <#-- biz start（2/5 left） -->
    <@netCommon.commonLeft "/index" />
    <#-- biz end（2/5 left） -->

    <!-- right start -->
    <div class="content-wrapper">

        <!-- content-header -->
        <section class="content-header">
            <#-- biz start（3/5 name） -->
            <h1>${I18n.index_name}</h1>
            <#-- biz end（3/5 name） -->
        </section>

        <!-- content-main -->
        <section class="content">

            <#-- biz start（4/5 content） -->

            <!-- 报表摘要 start -->
            <div class="row">
                <div class="col-md-4 col-sm-6 col-xs-12">
                    <div class="info-box">
                        <span class="info-box-icon bg-aqua"><i class="fa fa-cloud"></i></span>
                        <div class="info-box-content">
                            <span class="info-box-text">消费者服务数量</span>
                            <span class="info-box-number">${dashboardInfo.applicationCount}</span>
                        </div>
                    </div>
                </div>
                <div class="col-md-4 col-sm-6 col-xs-12">
                    <div class="info-box">
                        <span class="info-box-icon bg-red"><i class="fa fa-cubes"></i></span>
                        <div class="info-box-content">
                            <span class="info-box-text">消息主题数量</span>
                            <span class="info-box-number">${dashboardInfo.topicCount}</span>
                        </div>
                    </div>
                </div>
                <div class="col-md-4 col-sm-6 col-xs-12">
                    <div class="info-box">
                        <span class="info-box-icon bg-green"><i class="fa fa-database"></i></span>
                        <div class="info-box-content">
                            <span class="info-box-text">消息数量(近一年)</span>
                            <span class="info-box-number">${dashboardInfo.messageCount}</span>
                        </div>
                    </div>
                </div>
            </div>
            <!-- 报表摘要 end --->

            <#-- 运行报表 start -->
            <div class="row">
                <div class="col-md-12">
                    <div class="box">
                        <div class="box-header with-border">
                            <h3 class="box-title">运行报表</h3>
                            <#--<input type="text" class="form-control" id="filterTime" readonly >-->

                            <!-- tools box -->
                            <div class="pull-right box-tools">
                                <button type="button" class="btn btn-primary btn-sm daterange pull-right" data-toggle="tooltip" id="filterTime" >
                                    <i class="fa fa-calendar"></i>
                                </button>
                                <#--<button type="button" class="btn btn-primary btn-sm pull-right" data-widget="collapse" data-toggle="tooltip" title="" style="margin-right: 5px;" data-original-title="Collapse">
                                    <i class="fa fa-minus"></i>
                                </button>-->
                            </div>
                            <!-- /. tools -->

                        </div>
                        <div class="box-body">
                            <div class="row">
                                <#-- 左侧折线图 -->
                                <div class="col-md-8">
                                    <div id="lineChart" style="height: 350px;"></div>
                                </div>
                                <#-- 右侧饼图 -->
                                <div class="col-md-4">
                                    <div id="pieChart" style="height: 350px;"></div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <#-- 调度报表 end -->

            <#-- biz end（4/5 content） -->

        </section>

    </div>
    <!-- right end -->

    <!-- footer -->
    <@netCommon.commonFooter />
</div>

<#-- commonScript -->
<@netCommon.commonScript />

<#-- biz start（5/5 script） -->
<!-- daterangepicker -->
<script src="${request.contextPath}/static/adminlte/bower_components/moment/moment.min.js"></script>
<script src="${request.contextPath}/static/adminlte/bower_components/bootstrap-daterangepicker/daterangepicker.js"></script>
<!-- echarts -->
<script src="${request.contextPath}/static/plugins/echarts/echarts.common.min.js"></script>
<!-- js file -->
<script src="${request.contextPath}/static/js/index.js"></script>
<#-- biz end（5/5 script） -->

</body>
</html>
