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
	<@netCommon.commonLeft "topic" />
	
	<!-- Content Wrapper. Contains page content -->
	<div class="content-wrapper">
		<!-- Content Header (Page header) -->
		<section class="content-header">
			<h1>消息主题管理<small></small></h1>
		</section>
		
		<!-- Main content -->
	    <section class="content">
	    
	    	<div class="row">
                <div class="col-xs-4">
                    <div class="input-group">
                        <span class="input-group-addon">业务线</span>
                        <select class="form-control" id="bizId" >
                            <option value="-1">全部</option>
                            <option value="0">无</option>
                            <#list bizList as item>
                                <option value="${item.id}">${item.bizName}</option>
                            </#list>
                        </select>
                    </div>
                </div>
                <div class="col-xs-4">
                    <div class="input-group">
                        <span class="input-group-addon">消息主题</span>
                        <input type="text" class="form-control" id="topic" autocomplete="on" placeholder="请输入消息主题，模糊匹配" >
                    </div>
                </div>
	            <div class="col-xs-2">
	            	<button class="btn btn-block btn-info" id="searchBtn">搜索</button>
	            </div>
                <div class="col-xs-2">
                    <button class="btn btn-block btn-default" id="topic_add">+新增消息主题</button>
                </div>
          	</div>
	    	
			<div class="row">
				<div class="col-xs-12">
					<div class="box">
			            <#--<div class="box-header">
			            	<h3 class="box-title">消息主题列表</h3>
			            </div>-->
			            <div class="box-body">
			              	<table id="data_list" class="table table-bordered table-striped" width="100%" >
				                <thead>
					            	<tr>
					                	<th name="bizId" >业务线</th>
					                  	<th name="topic" >消息主题</th>
                                        <th name="author" >负责人</th>
                                        <th name="alarmEmails" >告警邮箱</th>
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
                    <h4 class="modal-title" >新增消息主题</h4>
                </div>
                <div class="modal-body">
                    <form class="form-horizontal form" role="form" >
                        <div class="form-group">
                            <label for="lastname" class="col-sm-3 control-label">消息主题</label>
                            <div class="col-sm-9"><input type="text" class="form-control" name="topic" placeholder="请输入消息主题" maxlength="255" ></div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-3 control-label">业务线</label>
                            <div class="col-sm-9">
                                <select class="form-control" name="bizId">
                                    <option value="0">无</option>
                                    <#list bizList as item>
                                        <option value="${item.id}">${item.bizName}</option>
                                    </#list>
                                </select>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-3 control-label">负责人</label>
                            <div class="col-sm-9"><input type="text" class="form-control" name="author" placeholder="请输入负责人" maxlength="64" ></div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-3 control-label">告警邮箱</label>
                            <div class="col-sm-9"><input type="text" class="form-control" name="alarmEmails" placeholder="请输入告警邮箱，多个逗号分隔" maxlength="255" ></div>
                        </div>
                        <div class="form-group">
                            <div class="col-sm-offset-3 col-sm-9">
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
                    <h4 class="modal-title" >更新消息主题</h4>
                </div>
                <div class="modal-body">
                    <form class="form-horizontal form" role="form" >
                        <div class="form-group">
                            <label for="lastname" class="col-sm-3 control-label">消息主题</label>
                            <div class="col-sm-9"><input type="text" class="form-control" name="topic" placeholder="请输入消息主题" maxlength="255" readonly ></div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-3 control-label">业务线</label>
                            <div class="col-sm-9">
                                <select class="form-control" name="bizId">
                                    <option value="0">无</option>
                                    <#list bizList as item>
                                        <option value="${item.id}">${item.bizName}</option>
                                    </#list>
                                </select>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-3 control-label">负责人</label>
                            <div class="col-sm-9"><input type="text" class="form-control" name="author" placeholder="请输入负责人" maxlength="64" ></div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-3 control-label">告警邮箱</label>
                            <div class="col-sm-9"><input type="text" class="form-control" name="alarmEmails" placeholder="请输入告警邮箱，多个逗号分隔" maxlength="255" ></div>
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

	<!-- footer -->
	<@netCommon.commonFooter />
</div>

<@netCommon.commonScript />
<!-- DataTables -->
<script src="${request.contextPath}/static/adminlte/bower_components/datatables.net/js/jquery.dataTables.min.js"></script>
<script src="${request.contextPath}/static/adminlte/bower_components/datatables.net-bs/js/dataTables.bootstrap.min.js"></script>
<script src="${request.contextPath}/static/plugins/jquery/jquery.validate.min.js"></script>

<script>
    var bizListObj = {};
    bizListObj['0'] = '无';
    <#list bizList as item>
    bizListObj['${item.id}'] = '${item.bizName}';
    </#list>
</script>
<script src="${request.contextPath}/static/js/topic.index.1.js"></script>

</body>
</html>
