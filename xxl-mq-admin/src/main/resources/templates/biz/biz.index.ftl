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
	<@netCommon.commonLeft "biz" />
	
	<!-- Content Wrapper. Contains page content -->
	<div class="content-wrapper">
		<!-- Content Header (Page header) -->
		<section class="content-header">
			<h1>业务线管理</h1>
		</section>

		<!-- Main content -->
	    <section class="content">
			
			<div class="row">
				<div class="col-xs-12">
					<div class="box">
			            <div class="box-header">
							<h3 class="box-title">业务线列表</h3>&nbsp;&nbsp;
                            <button class="btn btn-info btn-xs pull-left2 add" >新增业务线</button>
						</div>
			            <div class="box-body">
			              	<table id="joblog_list" class="table table-bordered table-striped display" width="100%" >
				                <thead>
					            	<tr>
                                        <#--<th name="id" >ID</th>-->
                                        <th name="bizName" >业务线名称</th>
                                        <th name="order" >顺序</th>
                                        <th name="operate" >操作</th>
					                </tr>
				                </thead>
                                <tbody>
								<#if bizList?exists && bizList?size gt 0>
								<#list bizList as bizItem>
									<tr>
                                        <#--<td>${bizItem.id}</td>-->
                                        <td>${bizItem.bizName}</td>
                                        <td>${bizItem.order}</td>
										<td>
                                            <button class="btn btn-warning btn-xs update"
                                                    data-id="${bizItem.id}"
                                                    data-bizName="${bizItem.bizName}"
                                                    data-order="${bizItem.order}" >更新</button>
                                            <button class="btn btn-danger btn-xs remove" data-id="${bizItem.id}" >删除</button>
										</td>
									</tr>
								</#list>
								</#if>
								</tbody>
							</table>
						</div>
					</div>
				</div>
			</div>
	    </section>
	</div>

    <!-- 新增.模态框 -->
    <div class="modal fade" id="addModal" tabindex="-1" role="dialog"  aria-hidden="true">
        <div class="modal-dialog ">
            <div class="modal-content">
                <div class="modal-header">
                    <h4 class="modal-title" >新增业务线</h4>
                </div>
                <div class="modal-body">
                    <form class="form-horizontal form" role="form" >
                        <div class="form-group">
                            <label for="lastname" class="col-sm-2 control-label">名称<font color="red">*</font></label>
                            <div class="col-sm-10"><input type="text" class="form-control" name="bizName" placeholder="请输入业务线名称" maxlength="12" ></div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-2 control-label">顺序<font color="red">*</font></label>
                            <div class="col-sm-10"><input type="text" class="form-control" name="order" placeholder="请输入顺序" maxlength="4" ></div>
                        </div>
                        <hr>
                        <div class="form-group">
                            <div class="col-sm-offset-3 col-sm-6">
                                <button type="submit" class="btn btn-primary"  >确认</button>
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
        <div class="modal-dialog ">
            <div class="modal-content">
                <div class="modal-header">
                    <h4 class="modal-title" >更新业务线</h4>
                </div>
                <div class="modal-body">
                    <form class="form-horizontal form" role="form" >
                        <div class="form-group">
                            <label for="lastname" class="col-sm-2 control-label">名称<font color="red">*</font></label>
                            <div class="col-sm-10"><input type="text" class="form-control" name="bizName" placeholder="请输入业务线名称" maxlength="12" ></div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-2 control-label">顺序<font color="red">*</font></label>
                            <div class="col-sm-10"><input type="text" class="form-control" name="order" placeholder="请输入顺序" maxlength="4" ></div>
                        </div>
                        <hr>
                        <div class="form-group">
                            <div class="col-sm-offset-3 col-sm-6">
                                <button type="submit" class="btn btn-primary"  >确认</button>
                                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                                <input type="hidden" name="id" >
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
<#-- jquery.validate -->
<script src="${request.contextPath}/static/plugins/jquery/jquery.validate.min.js"></script>
<script src="${request.contextPath}/static/js/biz.index.1.js"></script>
</body>
</html>
