<!DOCTYPE html>
<html>
<head>
	<#-- import macro -->
	<#import "../common/common.macro.ftl" as netCommon>
	<#-- commonStyle -->
	<@netCommon.commonStyle />

	<#-- biz start（1/5 style） -->
	<link rel="stylesheet" href="${request.contextPath}/static/adminlte/bower_components/datatables.net-bs/css/dataTables.bootstrap.min.css">
	<#-- biz end（1/5 end） -->

</head>
<body class="hold-transition skin-blue sidebar-mini" >
<div class="wrapper">

	<!-- header -->
	<@netCommon.commonHeader />

	<!-- left -->
	<#-- biz start（2/5 left） -->
	<@netCommon.commonLeft "/topic" />
	<#-- biz end（2/5 left） -->

	<!-- right start -->
	<div class="content-wrapper">

		<!-- content-header -->
		<section class="content-header">
			<#-- biz start（3/5 name） -->
			<h1>主题管理</h1>
			<#-- biz end（3/5 name） -->
		</section>

		<!-- content-main -->
		<section class="content">

			<#-- biz start（4/5 content） -->

			<#-- 查询区域 -->
			<div class="box" style="margin-bottom:9px;">
				<div class="box-body">
					<div class="row" id="data_filter" >
						<div class="col-xs-4">
							<div class="input-group">
								<span class="input-group-addon">AppName</span>
								<select class="form-control appname" >
									<#list applicationList as item>
										<option value="${item.appname}" >${item.appname}</option>
									</#list>
								</select>
							</div>
						</div>

						<div class="col-xs-5">
							<div class="input-group">
								<span class="input-group-addon">Topic</span>
								<input type="text" class="form-control topic" autocomplete="on" >
							</div>
						</div>
						<div class="col-xs-1">
							<button class="btn btn-block btn-primary searchBtn" >${I18n.system_search}</button>
						</div>
					</div>
				</div>
			</div>

			<#-- 数据表格区域 -->
			<div class="row">
				<div class="col-xs-12">
					<div class="box">
						<div class="box-header" style="float: right" id="data_operation" >
							<button class="btn btn-sm btn-info add" type="button"><i class="fa fa-plus" ></i>${I18n.system_opt_add}</button>
							<button class="btn btn-sm btn-warning selectOnlyOne update" type="button"><i class="fa fa-edit"></i>${I18n.system_opt_edit}</button>
							<button class="btn btn-sm btn-warning selectAny updateStatus" type="button"><i class="fa fa-edit"></i>更新状态</button>
							<button class="btn btn-sm btn-danger selectAny delete" type="button"><i class="fa fa-remove "></i>${I18n.system_opt_del}</button>
							<button class="btn btn-sm btn-primary selectOnlyOne queryMessage" type="button">查看实时消息</button>
							<button class="btn btn-sm btn-default selectOnlyOne queryMessageArchive" type="button">查看归档消息</button>
						</div>
						<div class="box-body" >
							<table id="data_list" class="table table-bordered table-striped" width="100%" >
								<thead></thead>
								<tbody></tbody>
								<tfoot></tfoot>
							</table>
						</div>
					</div>
				</div>
			</div>

			<!-- 新增.模态框 -->
			<div class="modal fade" id="addModal" tabindex="-1" role="dialog"  aria-hidden="true">
				<div class="modal-dialog modal-lg">
					<div class="modal-content">
						<div class="modal-header">
							<h4 class="modal-title" >${I18n.system_opt_add}Topic</h4>
						</div>
						<div class="modal-body">
							<form class="form-horizontal form" role="form" >

								<p style="margin: 0 0 10px;text-align: left;border-bottom: 1px solid #e5e5e5;color: gray;">基础配置</p>
								<div class="form-group">
									<label for="lastname" class="col-sm-2 control-label">Topic<font color="red">*</font></label>
									<div class="col-sm-10"><input type="text" class="form-control" name="topic" placeholder="请输入Topic" maxlength="100" ></div>
								</div>
								<div class="form-group">
									<label for="firstname" class="col-sm-2 control-label">AppName<font color="red">*</font></label>
									<div class="col-sm-4">
										<select class="form-control" name="appname" >
											<#list applicationList as item>
												<option value="${item.appname}" >${item.appname}</option>
											</#list>
										</select>
									</div>
									<label for="lastname" class="col-sm-2 control-label">主题描述<font color="red">*</font></label>
									<div class="col-sm-4"><input type="text" class="form-control" name="desc" placeholder="请输入主题描述" maxlength="50" ></div>
								</div>
								<div class="form-group">
									<label for="lastname" class="col-sm-2 control-label">负责人<font color="black">*</font></label>
									<div class="col-sm-4"><input type="text" class="form-control" name="owner" placeholder="请输入负责人" maxlength="50" ></div>
									<label for="lastname" class="col-sm-2 control-label">告警配置<font color="black">*</font></label>
									<div class="col-sm-4"><input type="text" class="form-control" name="alarmEmail" placeholder="请输入告警配置" maxlength="200" ></div>
								</div>

								<br>
								<p style="margin: 0 0 10px;text-align: left;border-bottom: 1px solid #e5e5e5;color: gray;">存储路由</p>
								<div class="form-group">
									<label for="firstname" class="col-sm-2 control-label hide">存储策略<font color="red">*</font></label>
									<div class="col-sm-4 hide">
										<select class="form-control" name="storeStrategy" >
											<#list StoreStrategyEnum as item>
												<option value="${item.value}" >${item.desc}</option>
											</#list>
										</select>
									</div>

									<label for="firstname" class="col-sm-2 control-label">分区路由<font color="red">*</font></label>
									<div class="col-sm-4">
										<select class="form-control" name="partitionStrategy" >
											<#list PartitionRouteStrategyEnum as item>
												<option value="${item.value}" >${item.desc}</option>
											</#list>
										</select>
									</div>
									<label for="firstname" class="col-sm-2 control-label">归档策略<font color="red">*</font></label>
									<div class="col-sm-4">
										<select class="form-control" name="archiveStrategy" >
											<#list ArchiveStrategyEnum as item>
												<option value="${item.value}" >${item.desc}</option>
											</#list>
										</select>
									</div>
								</div>

								<br>
								<p style="margin: 0 0 10px;text-align: left;border-bottom: 1px solid #e5e5e5;color: gray;">消费策略</p>
								<div class="form-group">
									<label for="firstname" class="col-sm-2 control-label">重试策略<font color="red">*</font></label>
									<div class="col-sm-4">
										<select class="form-control" name="retryStrategy" >
											<#list RetryStrategyEnum as item>
											<option value="${item.value}" >${item.desc}</option>
											</#list>
										</select>
									</div>
									<label for="firstname" class="col-sm-2 control-label">重试次数<font color="black">*</font></label>
									<div class="col-sm-4"><input type="number" class="form-control" name="retryCount" placeholder="请出入重试次数" value="0" ></div>
								</div>
								<div class="form-group">
									<label for="firstname" class="col-sm-2 control-label">重试间隔<font color="black">*</font></label>
									<div class="col-sm-4"><input type="number" class="form-control" name="retryInterval" placeholder="请出入重试间隔时间/秒" value="3" ></div>
									<label for="firstname" class="col-sm-2 control-label">超时时间<font color="black">*</font></label>
									<div class="col-sm-4"><input type="number" class="form-control" name="executionTimeout" placeholder="请出入超时时间/秒，大于零时生效" ></div>
								</div>
								<div class="form-group">
									<label for="firstname" class="col-sm-2 control-label">优先级<font color="red">*</font></label>
									<div class="col-sm-4">
										<select class="form-control" name="level" >
											<#list TopicLevelStrategyEnum as item>
												<option value="${item.value}" >${item.desc}</option>
											</#list>
										</select>
									</div>
								</div>

								<hr>
								<div class="form-group">
									<div class="col-sm-offset-5 col-sm-2">
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
				<div class="modal-dialog modal-lg">
					<div class="modal-content">
						<div class="modal-header">
							<h4 class="modal-title" >${I18n.system_opt_edit}Topic</h4>
						</div>
						<div class="modal-body">
							<form class="form-horizontal form" role="form" >

								<p style="margin: 0 0 10px;text-align: left;border-bottom: 1px solid #e5e5e5;color: gray;">基础配置</p>
								<div class="form-group">
									<label for="lastname" class="col-sm-2 control-label">Topic<font color="red">*</font></label>
									<div class="col-sm-10"><input type="text" class="form-control" name="topic" placeholder="请输入Topic" readonly ></div>
								</div>
								<div class="form-group">
									<label for="firstname" class="col-sm-2 control-label">AppName<font color="red">*</font></label>
									<div class="col-sm-4">
										<input type="text" class="form-control" name="appname" readonly >
									</div>
									<label for="lastname" class="col-sm-2 control-label">主题描述<font color="red">*</font></label>
									<div class="col-sm-4"><input type="text" class="form-control" name="desc" placeholder="请输入主题描述" maxlength="50" ></div>
								</div>
								<div class="form-group">
									<label for="lastname" class="col-sm-2 control-label">负责人<font color="black">*</font></label>
									<div class="col-sm-4"><input type="text" class="form-control" name="owner" placeholder="请输入负责人" maxlength="50" ></div>
									<label for="lastname" class="col-sm-2 control-label">告警配置<font color="black">*</font></label>
									<div class="col-sm-4"><input type="text" class="form-control" name="alarmEmail" placeholder="请输入告警配置" maxlength="200" ></div>
								</div>

								<br>
								<p style="margin: 0 0 10px;text-align: left;border-bottom: 1px solid #e5e5e5;color: gray;">存储路由</p>
								<div class="form-group">
									<label for="firstname" class="col-sm-2 control-label hide">存储策略<font color="red">*</font></label>
									<div class="col-sm-4 hide">
										<select class="form-control" name="storeStrategy" >
											<#list StoreStrategyEnum as item>
												<option value="${item.value}" >${item.desc}</option>
											</#list>
										</select>
									</div>


									<label for="firstname" class="col-sm-2 control-label">分区路由<font color="red">*</font></label>
									<div class="col-sm-4">
										<select class="form-control" name="partitionStrategy" >
											<#list PartitionRouteStrategyEnum as item>
												<option value="${item.value}" >${item.desc}</option>
											</#list>
										</select>
									</div>
									<label for="firstname" class="col-sm-2 control-label">归档策略<font color="red">*</font></label>
									<div class="col-sm-4">
										<select class="form-control" name="archiveStrategy" >
											<#list ArchiveStrategyEnum as item>
												<option value="${item.value}" >${item.desc}</option>
											</#list>
										</select>
									</div>
								</div>

								<br>
								<p style="margin: 0 0 10px;text-align: left;border-bottom: 1px solid #e5e5e5;color: gray;">消费策略</p>
								<div class="form-group">
									<label for="firstname" class="col-sm-2 control-label">重试策略<font color="red">*</font></label>
									<div class="col-sm-4">
										<select class="form-control" name="retryStrategy" >
											<#list RetryStrategyEnum as item>
												<option value="${item.value}" >${item.desc}</option>
											</#list>
										</select>
									</div>
									<label for="firstname" class="col-sm-2 control-label">重试次数<font color="black">*</font></label>
									<div class="col-sm-4"><input type="number" class="form-control" name="retryCount" placeholder="请出入重试次数" value="0" ></div>
								</div>
								<div class="form-group">
									<label for="firstname" class="col-sm-2 control-label">重试间隔<font color="black">*</font></label>
									<div class="col-sm-4"><input type="number" class="form-control" name="retryInterval" placeholder="请出入重试间隔时间/秒" value="3" ></div>
									<label for="firstname" class="col-sm-2 control-label">超时时间<font color="black">*</font></label>
									<div class="col-sm-4"><input type="number" class="form-control" name="executionTimeout" placeholder="请出入超时时间/秒，大于零时生效" ></div>
								</div>
								<div class="form-group">
									<label for="firstname" class="col-sm-2 control-label">优先级<font color="red">*</font></label>
									<div class="col-sm-4">
										<select class="form-control" name="level" >
											<#list TopicLevelStrategyEnum as item>
												<option value="${item.value}" >${item.desc}</option>
											</#list>
										</select>
									</div>
								</div>

								<hr>
								<div class="form-group">
									<div class="col-sm-offset-5 col-sm-2">
										<button type="submit" class="btn btn-primary"  >保存</button>
										<button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
									</div>

									<input type="hidden" class="form-control" name="id"  >
								</div>

							</form>
						</div>
					</div>
				</div>
			</div>

			<#-- biz end（4/5 content） -->

		</section>

	</div>
	<!-- right end -->

	<!-- footer -->
	<@netCommon.commonFooter />
</div>
<@netCommon.commonScript />

<#-- biz start（5/5 script） -->
<script src="${request.contextPath}/static/adminlte/bower_components/datatables.net/js/jquery.dataTables.min.js"></script>
<script src="${request.contextPath}/static/adminlte/bower_components/datatables.net-bs/js/dataTables.bootstrap.min.js"></script>

<script src="${request.contextPath}/static/js/common/datatables.select.js"></script>
<script src="${request.contextPath}/static/js/biz/topic.js"></script>
<#-- biz end（5/5 script） -->

</body>
</html>