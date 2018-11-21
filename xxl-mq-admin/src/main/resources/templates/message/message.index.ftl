<!DOCTYPE html>
<html>
<head>
  	<#import "../common/common.macro.ftl" as netCommon>
    <title>消息队列中心</title>
	<@netCommon.commonStyle />
    <!-- DataTables -->
    <link rel="stylesheet" href="${request.contextPath}/static/adminlte/bower_components/datatables.net-bs/css/dataTables.bootstrap.min.css">
    <!-- daterangepicker -->
    <link rel="stylesheet" href="${request.contextPath}/static/adminlte/bower_components/bootstrap-daterangepicker/daterangepicker.css">
</head>
<body class="hold-transition skin-blue sidebar-mini <#if cookieMap?exists && cookieMap["xxlmq_adminlte_settings"]?exists && "off" == cookieMap["xxlmq_adminlte_settings"].value >sidebar-collapse</#if>">
<div class="wrapper">
	<!-- header -->
	<@netCommon.commonHeader />
	<!-- left -->
	<@netCommon.commonLeft "message" />
	
	<!-- Content Wrapper. Contains page content -->
	<div class="content-wrapper">
		<!-- Content Header (Page header) -->
		<section class="content-header">
			<h1>消息管理<small></small></h1>
		</section>
		
		<!-- Main content -->
	    <section class="content">
	    
	    	<div class="row">
                <div class="col-xs-2">
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
                <div class="col-xs-4">
                    <div class="input-group">
                		<span class="input-group-addon">创建时间</span>
                        <input type="text" class="form-control" id="filterTime" readonly >
                    </div>
                </div>
                <div class="col-xs-3">
                    <input type="text" class="form-control" id="topic" autocomplete="on" value="${topic!''}" placeholder="请输入消息主题，精确匹配" >
                </div>
	            <div class="col-xs-1">
	            	<button class="btn btn-block btn-info" id="searchBtn">搜索</button>
	            </div>
                <div class="col-xs-2 pull-right">
                    <div class="btn-group">
                        <button class="btn btn-default pull-left" id="msg_add">添加</button>
                        <button class="btn btn-default pull-left" id="clearMessage">清理</button>
                    </div>
                </div>
          	</div>
	    	
			<div class="row">
				<div class="col-xs-12">
					<div class="box">
			            <div class="box-body">
			              	<table id="data_list" class="table table-bordered table-striped" width="100%" >
				                <thead>
					            	<tr>
					                	<th name="id" >ID</th>
					                  	<th name="topic" >主题</th>
                                        <th name="group" >分组</th>
                                        <th name="data" >数据</th>
                                        <th name="status" >状态</th>
                                        <th name="retryCount" >重试次数</th>
                                        <th name="shardingId" >分片ID</th>
                                        <th name="timeout" >超时时间</th>
                                        <th name="effectTime" >生效时间</th>
                                        <th name="addTime" >创建时间</th>
                                        <th name="log" >流转日志</th>
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
                            <label for="lastname" class="col-sm-3 control-label">消息主题 <font color="red">*</font></label>
                            <div class="col-sm-9"><input type="text" class="form-control" name="topic" maxlength="255" placeholder="请输入消息主题" ></div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-3 control-label">消息分组 *</label>
                            <div class="col-sm-9"><input type="text" class="form-control" name="group" maxlength="255" placeholder="请输入消息分组，为空则为默认分组" ></div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-3 control-label">消息数据 *</label>
                            <div class="col-sm-9">
                                <textarea class="textarea" name="data" maxlength="60000" placeholder="请输入消息数据" style="width: 100%; height: 100px; font-size: 14px; line-height: 18px; border: 1px solid #dddddd; padding: 10px;"></textarea>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-3 control-label">状态 *</label>
                            <div class="col-sm-9">
                                <select class="form-control" name="status">
                                <#list status as item>
                                    <option value="${item}">${item}</option>
                                </#list>
                                </select>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-3 control-label">重试次数 *</label>
                            <div class="col-sm-9"><input type="text" class="form-control" name="retryCount" maxlength="9" placeholder="请输入重试次数，大于零时生效" ></div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-3 control-label">分片ID *</label>
                            <div class="col-sm-9"><input type="text" class="form-control" name="shardingId" maxlength="9" placeholder="请输入分片ID，大于零时生效" ></div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-3 control-label">超时时间 *</label>
                            <div class="col-sm-9"><input type="text" class="form-control" name="timeout" maxlength="9" placeholder="请输入超时时间，单位秒，大于零时生效" ></div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-3 control-label">生效时间 *</label>
                            <div class="col-sm-9"><input type="text" class="form-control inputmask" name="effectTime" placeholder="请输入生效时间，为空则立即执行" ></div>
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
                    <h4 class="modal-title" >更新消息</h4>
                </div>
                <div class="modal-body">
                    <form class="form-horizontal form" role="form" >
                        <div class="form-group">
                            <label for="lastname" class="col-sm-3 control-label">消息主题 <font color="red">*</font></label>
                            <div class="col-sm-9"><input type="text" class="form-control" name="topic" maxlength="255" placeholder="请输入消息主题" readonly ></div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-3 control-label">消息分组 *</label>
                            <div class="col-sm-9"><input type="text" class="form-control" name="group" maxlength="255" placeholder="请输入消息分组，为空则为默认分组" ></div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-3 control-label">消息数据 *</label>
                            <div class="col-sm-9">
                                <textarea class="textarea" name="data" maxlength="60000" placeholder="请输入消息数据" style="width: 100%; height: 100px; font-size: 14px; line-height: 18px; border: 1px solid #dddddd; padding: 10px;"></textarea>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-3 control-label">状态 *</label>
                            <div class="col-sm-9">
                                <select class="form-control" name="status">
                                <#list status as item>
                                    <option value="${item}">${item}</option>
                                </#list>
                                </select>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-3 control-label">重试次数 *</label>
                            <div class="col-sm-9"><input type="text" class="form-control" name="retryCount" maxlength="9" placeholder="请输入重试次数，大于零时生效" ></div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-3 control-label">分片ID *</label>
                            <div class="col-sm-9"><input type="text" class="form-control" name="shardingId" maxlength="9" placeholder="请输入分片ID，大于零时生效" ></div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-3 control-label">超时时间 *</label>
                            <div class="col-sm-9"><input type="text" class="form-control" name="timeout" maxlength="9" placeholder="请输入超时时间，单位秒，大于零时生效" ></div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-3 control-label">生效时间 *</label>
                            <div class="col-sm-9"><input type="text" class="form-control inputmask" name="effectTime" placeholder="请输入生效时间，为空则立即执行" ></div>
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


    <!-- 清理消息.模态框 -->
    <div class="modal fade" id="clearMessageModal" tabindex="-1" role="dialog"  aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h4 class="modal-title" >清理消息</h4>
                </div>
                <div class="modal-body">
                    <form class="form-horizontal form" role="form" >
                        <div class="form-group">
                            <label for="lastname" class="col-sm-3 control-label">消息主题<font color="red">*</font></label>
                            <div class="col-sm-9"><input type="text" class="form-control" name="topic" maxlength="255" placeholder="请输入消息主题" ></div>
                        </div>
                        <div class="form-group">
                            <label for="lastname" class="col-sm-3 control-label">状态 *</label>
                            <div class="col-sm-9">
                                <select class="form-control" name="status">
                                    <option value="">全部</option>
                                    <#list status as item>
                                        <option value="${item}" <#if item = 'SUCCESS'>selected</#if> >${item}</option>
                                    </#list>
                                </select>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-sm-3 control-label"">清理方式：</label>
                            <div class="col-sm-9">
                                <select class="form-control" name="type" >
                                    <option value="1" >清理一个月之前日志数据</option>
                                    <option value="2" >清理三个月之前日志数据</option>
                                    <option value="3" >清理六个月之前日志数据</option>
                                    <option value="4" >清理一年之前日志数据</option>
                                    <option value="5" >清理一千条以前日志数据</option>
                                    <option value="6" >清理一万条以前日志数据</option>
                                    <option value="7" >清理十万条以前日志数据</option>
                                    <option value="8" >清理所有日志数据</option>
                                </select>
                            </div>
                        </div>

                        <hr>
                        <div class="form-group">
                            <div class="col-sm-offset-3 col-sm-6">
                                <button type="button" class="btn btn-primary ok" >确定</button>
                                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                            </div>
                        </div>

                        <#--<p class="help-block">提示：仅清理 "SUCCESS" 状态消息.</p>-->

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
<#-- inputmask -->
<script src="${request.contextPath}/static/adminlte/plugins/input-mask/jquery.inputmask.js"></script>
<script src="${request.contextPath}/static/adminlte/plugins/input-mask/jquery.inputmask.date.extensions.js"></script>
<!-- daterangepicker -->
<script src="${request.contextPath}/static/adminlte/bower_components/bootstrap-daterangepicker/daterangepicker.js"></script>

<script src="${request.contextPath}/static/js/message.index.1.js"></script>

</body>
</html>
