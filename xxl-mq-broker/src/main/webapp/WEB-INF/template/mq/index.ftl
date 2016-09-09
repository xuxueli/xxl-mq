<!DOCTYPE html>
<html>
<head>
  	<title>分布式消息队列</title>
  	<#import "/common/common.macro.ftl" as netCommon>
	<@netCommon.commonStyle />
	<!-- DataTables -->
  	<link rel="stylesheet" href="${request.contextPath}/static/adminlte/plugins/datatables/dataTables.bootstrap.css">

</head>
<body class="hold-transition skin-blue sidebar-mini <#if cookieMap?exists && "off" == cookieMap["adminlte_settings"].value >sidebar-collapse</#if>">
<div class="wrapper">
	<!-- header -->
	<@netCommon.commonHeader />
	<!-- left -->
	<@netCommon.commonLeft />
	
	<!-- Content Wrapper. Contains page content -->
	<div class="content-wrapper">
		<!-- Content Header (Page header) -->
		<section class="content-header">
			<h1>消息管理<small></small></h1>
			<#--<ol class="breadcrumb">
				<li><a><i class="fa fa-dashboard"></i>服务管理</a></li>
				<li class="active">服务中心</li>
			</ol>-->
		</section>
		
		<!-- Main content -->
	    <section class="content">
	    
	    	<div class="row">
	            <div class="col-xs-8">
	              	<div class="input-group">
	                	<span class="input-group-addon">主题</span>
	                	<input type="text" class="form-control" id="name" autocomplete="on" >
	              	</div>
	            </div>
	            <div class="col-xs-2">
	            	<button class="btn btn-block btn-info" id="searchBtn">搜索</button>
	            </div>
          	</div>
	    	
			<div class="row">
				<div class="col-xs-12">
					<div class="box">
			            <div class="box-header">
			            	<h3 class="box-title">消息列表</h3>
			            </div>
			            <div class="box-body">
			              	<table id="data_list" class="table table-bordered table-striped">
				                <thead>
					            	<tr>
					                	<th name="id" >ID</th>
					                  	<th name="name" >主题</th>
                                        <th name="data" >消息数据</th>
                                        <th name="delayTime" title="延迟执行的时间, new Date()立即执行, 否则在延迟时间点之后开始执行;" >Delay执行时间</th>
                                        <th name="addTime/updateTime" >创建时间/更新时间</th>
                                        <th name="status" >消息状态</th>
                                        <th name="msg" >历史流转日志</th>
					                  	<th>操作</th>
					                </tr>
				                </thead>
				                <tbody></tbody>
				                <tfoot></tfoot>
							</table>
						</div>
					</div>
				</div>
			</div>
			
	    </section>
	</div>

    <!-- 新增.模态框 -->
    <div class="modal fade" id="addModal" tabindex="-1" role="dialog"  aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h4 class="modal-title" >新增配置</h4>
                </div>
                <div class="modal-body">
                    <form class="form-horizontal form" role="form" >
                        <div class="form-group">
                            <label for="firstname" class="col-sm-2 control-label">KEY</label>
                            <div class="col-sm-10"><input type="text" class="form-control" name="nodeKey" placeholder="请输入KEY" maxlength="100" ></div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-2 control-label">描述</label>
                            <div class="col-sm-10"><input type="text" class="form-control" name="nodeDesc" placeholder="请输入描述" maxlength="100" ></div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-2 control-label">VALUE</label>
                            <div class="col-sm-10">
                                <textarea class="textarea" name="nodeValue" maxlength="512" placeholder="请输入VALUE" style="width: 100%; height: 100px; font-size: 14px; line-height: 18px; border: 1px solid #dddddd; padding: 10px;"></textarea>
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="col-sm-offset-2 col-sm-10">
                                <button type="submit" class="btn btn-primary"  >保存</button>
                                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <!-- 更新.模态框 -->
    <div class="modal fade" id="updateModal" tabindex="-1" role="dialog"  aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h4 class="modal-title" >更新消息</h4>
                </div>
                <div class="modal-body">
                    <form class="form-horizontal form" role="form" >
                        <div class="form-group">
                            <label for="lastname" class="col-sm-3 control-label">数据</label>
                            <div class="col-sm-9">
                                <textarea class="textarea" name="data" maxlength="1024" style="width: 100%; height: 100px; font-size: 14px; line-height: 18px; border: 1px solid #dddddd; padding: 10px;"></textarea>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-3 control-label">Delay</label>
                            <div class="col-sm-9"><input type="text" class="form-control" name="delayTime" data-inputmask="'alias': 'dd/mm/yyyy hh:mm xm'" data-mask ></div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-3 control-label">状态</label>
                            <div class="col-sm-9">
                                <select class="form-control" name="status">
									<#list status as item>
                                    <option value="${item}">${item}</option>
									</#list>
                                </select>
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="col-sm-offset-3 col-sm-9">
                                <button type="submit" class="btn btn-primary"  >更新</button>
                                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
								<input type="hidden" name="id">
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>

	<!-- footer -->
	<@netCommon.commonFooter />
</div>

<@netCommon.commonScript />
<!-- DataTables -->
<script src="${request.contextPath}/static/adminlte/plugins/datatables/jquery.dataTables.min.js"></script>
<script src="${request.contextPath}/static/adminlte/plugins/datatables/dataTables.bootstrap.min.js"></script>
<script src="${request.contextPath}/static/plugins/jquery/jquery.validate.min.js"></script>
<!-- datepicker -->
<script src="${request.contextPath}/static/adminlte/plugins/daterangepicker/moment.min.js"></script>
<script src="${request.contextPath}/static/adminlte/plugins/input-mask/jquery.inputmask.js"></script>
<script src="${request.contextPath}/static/adminlte/plugins/input-mask/jquery.inputmask.date.extensions.js"></script>

<script>var base_url = '${request.contextPath}';</script>
<script src="${request.contextPath}/static/js/mq.index.1.js"></script>
</body>
</html>
