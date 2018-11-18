<!DOCTYPE html>
<html>
<head>
  	<#import "../common/common.macro.ftl" as netCommon>
    <title>消息队列中心</title>
	<@netCommon.commonStyle />
    <!-- DataTables -->
    <link rel="stylesheet" href="${request.contextPath}/static/adminlte/bower_components/datatables.net-bs/css/dataTables.bootstrap.min.css">
</head>
<body class="hold-transition skin-blue sidebar-mini <#if cookieMap?exists && cookieMap["xxlmq_adminlte_settings"]?exists && "off" == cookieMap["xxlmq_adminlte_settings"].value >sidebar-collapse</#if>">
<div class="wrapper">
	<!-- header -->
	<@netCommon.commonHeader />
	<!-- left -->
	<@netCommon.commonLeft "mq" />
	
	<!-- Content Wrapper. Contains page content -->
	<div class="content-wrapper">
		<!-- Content Header (Page header) -->
		<section class="content-header">
			<h1>消息管理<small></small></h1>
		</section>
		
		<!-- Main content -->
	    <section class="content">
	    
	    	<div class="row">
	            <div class="col-xs-4">
	              	<div class="input-group">
	                	<span class="input-group-addon">主题</span>
	                	<input type="text" class="form-control" id="topic" autocomplete="on" >
	              	</div>
	            </div>
                <div class="col-xs-4">
                    <div class="input-group">
                        <span class="input-group-addon">状态</span>
                        <select class="form-control" id="status" >
                            <option value="">全部</option>
                            <#list status as item>
                                <option value="${item}">${item}</option>
                            </#list>
                        </select>
                    </div>
                </div>
	            <div class="col-xs-2">
	            	<button class="btn btn-block btn-info" id="searchBtn">搜索</button>
	            </div>
                <div class="col-xs-2">
                    <button class="btn btn-block btn-default" id="msg_add">+新增消息</button>
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
                                        <th name="effectTime" >Delay执行时间</th>
                                        <th name="addTime/updateTime" >创建时间/更新时间</th>
                                        <th name="status" >消息状态</th>
                                        <th name="msg" >历史流转日志</th>
                                        <th name="retryCount" >剩余重试次数</th>
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
                    <h4 class="modal-title" >新增消息</h4>
                </div>
                <div class="modal-body">
                    <form class="form-horizontal form" role="form" >
                        <div class="form-group">
                            <label for="lastname" class="col-sm-3 control-label">主题</label>
                            <div class="col-sm-9"><input type="text" class="form-control" name="name"  ></div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-3 control-label">数据</label>
                            <div class="col-sm-9">
                                <textarea class="textarea" name="data" maxlength="1024" style="width: 100%; height: 100px; font-size: 14px; line-height: 18px; border: 1px solid #dddddd; padding: 10px;"></textarea>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-3 control-label">Delay</label>
                            <div class="col-sm-9"><input type="text" class="form-control" name="effectTime" data-inputmask="'alias': 'dd/mm/yyyy hh:mm xm'" data-mask ></div>
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
                            <label for="lastname" class="col-sm-3 control-label">剩余重试次数</label>
                            <div class="col-sm-9"><input type="text" class="form-control" name="retryCount" value="0" ></div>
                        </div>
                        <div class="form-group">
                            <div class="col-sm-offset-3 col-sm-9">
                                <button type="submit" class="btn btn-primary"  >更新</button>
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
                                <textarea class="textarea" name="data" maxlength="2048" style="width: 100%; height: 100px; font-size: 14px; line-height: 18px; border: 1px solid #dddddd; padding: 10px;"></textarea>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-3 control-label">Delay</label>
                            <div class="col-sm-9"><input type="text" class="form-control" name="effectTime" data-inputmask="'alias': 'dd/mm/yyyy hh:mm xm'" data-mask ></div>
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
                            <label for="lastname" class="col-sm-3 control-label">剩余重试次数</label>
                            <div class="col-sm-9"><input type="text" class="form-control" name="retryCount" value="0" ></div>
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
<script src="${request.contextPath}/static/adminlte/bower_components/datatables.net/js/jquery.dataTables.min.js"></script>
<script src="${request.contextPath}/static/adminlte/bower_components/datatables.net-bs/js/dataTables.bootstrap.min.js"></script>
<script src="${request.contextPath}/static/plugins/jquery/jquery.validate.min.js"></script>
<!-- moment -->
<script src="${request.contextPath}/static/adminlte/bower_components/moment/moment.min.js"></script>
<script src="${request.contextPath}/static/adminlte/plugins/input-mask/jquery.inputmask.js"></script>
<script src="${request.contextPath}/static/adminlte/plugins/input-mask/jquery.inputmask.date.extensions.js"></script>

<script src="${request.contextPath}/static/js/mq.index.1.js"></script>

</body>
</html>
