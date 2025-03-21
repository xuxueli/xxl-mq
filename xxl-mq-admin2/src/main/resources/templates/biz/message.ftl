<!DOCTYPE html>
<html>
<head>
	<#-- import macro -->
	<#import "../common/common.macro.ftl" as netCommon>
	<#-- commonStyle -->
	<@netCommon.commonStyle />

	<#-- biz start（1/5 style） -->
	<link rel="stylesheet" href="${request.contextPath}/static/adminlte/bower_components/datatables.net-bs/css/dataTables.bootstrap.min.css">
	<link rel="stylesheet" href="${request.contextPath}/static/adminlte/bower_components/bootstrap-daterangepicker/daterangepicker.css">
	<#-- biz end（1/5 end） -->

</head>
<body class="hold-transition skin-blue sidebar-mini" >
<div class="wrapper">

	<!-- header -->
	<@netCommon.commonHeader />

	<!-- left -->
	<#-- biz start（2/5 left） -->
	<@netCommon.commonLeft "/message" />
	<#-- biz end（2/5 left） -->

	<!-- right start -->
	<div class="content-wrapper">

		<!-- content-header -->
		<section class="content-header">
			<#-- biz start（3/5 name） -->
			<h1>消息管理</h1>
			<#-- biz end（3/5 name） -->
		</section>

		<!-- content-main -->
		<section class="content">

			<#-- biz start（4/5 content） -->

			<#-- 查询区域 -->
			<div class="box" style="margin-bottom:9px;">
				<div class="box-body">
					<div class="row" id="data_filter" >
						<div class="col-xs-5">
							<div class="input-group">
								<span class="input-group-addon">Topic</span>
								<input type="text" class="form-control topic" autocomplete="on" placeholder="请输入消息Topic，精确匹配" <#if topic??>value="${topic}" </#if>  >
							</div>
						</div>
						<div class="col-xs-5">
							<div class="input-group">
								<span class="input-group-addon">生效时间</span>
								<input type="text" class="form-control filterTime" >
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
							<button class="btn btn-sm btn-danger selectAny delete" type="button"><i class="fa fa-remove "></i>${I18n.system_opt_del}</button>
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
				<div class="modal-dialog">
					<div class="modal-content">
						<div class="modal-header">
							<h4 class="modal-title" >${I18n.system_opt_add}消息</h4>
						</div>
						<div class="modal-body">
							<form class="form-horizontal form" role="form" >

								<div class="form-group">
									<label for="lastname" class="col-sm-2 control-label">Topic<font color="red">*</font></label>
									<div class="col-sm-10"><input type="text" class="form-control" name="topic" placeholder="请输入Topic" maxlength="100" ></div>
								</div>
								<div class="form-group">
									<label for="lastname" class="col-sm-2 control-label">Group<font color="red">*</font></label>
									<div class="col-sm-6"><input type="text" class="form-control" name="group" placeholder="请输入消息分组" maxlength="20" value="DEFAULT" ></div>
								</div>
								<div class="form-group">
									<label for="lastname" class="col-sm-2 control-label">PartitionId<font color="red">*</font></label>
									<div class="col-sm-6"><input type="number" class="form-control" name="partitionId" placeholder="请输入分区ID" value="1" ></div>
								</div>
								<div class="form-group">
									<label for="lastname" class="col-sm-2 control-label">生效时间<font color="red">*</font></label>
									<div class="col-sm-10">
										<input type="text" class="form-control" name="effectTime" >
									</div>
								</div>
								<div class="form-group">
									<label for="lastname" class="col-sm-2 control-label">消息数据<font color="red">*</font></label>
									<div class="col-sm-10">
										<textarea type="text" class="form-control" name="data" placeholder="${I18n.system_please_input}" maxlength="1000" style="height: 100px;" ></textarea>
									</div>
								</div>
								<div class="form-group">
									<label for="lastname" class="col-sm-2 control-label">消息状态<font color="red">*</font></label>
									<div class="col-sm-10">
										<select class="form-control" name="status" >
											<#list MessageStatusEnum as item>
												<#if item.value == 0>
													<option value="${item.value}" >${item.desc}</option>
												</#if>
											</#list>
										</select>
									</div>
								</div>

								<hr>
								<div class="form-group">
									<div class="col-sm-offset-3 col-sm-6">
										<button type="submit" class="btn btn-primary"  >保存</button>
										<button type="button" class="btn btn-default" data-dismiss="modal">确认</button>
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
							<h4 class="modal-title" >${I18n.system_opt_edit}消息</h4>
						</div>
						<div class="modal-body">
							<form class="form-horizontal form" role="form" >

								<div class="form-group">
									<label for="lastname" class="col-sm-2 control-label">Topic<font color="red">*</font></label>
									<div class="col-sm-10"><input type="text" class="form-control" name="topic" placeholder="请输入Topic" readonly ></div>
								</div>
								<div class="form-group">
									<label for="lastname" class="col-sm-2 control-label">Group<font color="red">*</font></label>
									<div class="col-sm-6"><input type="text" class="form-control" name="group" placeholder="请输入消息分组" readonly ></div>
								</div>
								<div class="form-group">
									<label for="lastname" class="col-sm-2 control-label">PartitionId<font color="red">*</font></label>
									<div class="col-sm-6"><input type="number" class="form-control" name="partitionId" placeholder="请输入分区ID" readonly ></div>
								</div>
								<div class="form-group">
									<label for="lastname" class="col-sm-2 control-label">生效时间<font color="red">*</font></label>
									<div class="col-sm-10">
										<input type="text" class="form-control" name="effectTime" >
									</div>
								</div>
								<div class="form-group">
									<label for="lastname" class="col-sm-2 control-label">消息数据<font color="red">*</font></label>
									<div class="col-sm-10">
										<textarea type="text" class="form-control" name="data" placeholder="${I18n.system_please_input}" maxlength="1000" style="height: 100px;" ></textarea>
									</div>
								</div>
								<div class="form-group">
									<label for="lastname" class="col-sm-2 control-label">消息状态<font color="red">*</font></label>
									<div class="col-sm-10">
										<select class="form-control" name="status" >
											<#list MessageStatusEnum as item>
												<option value="${item.value}" >${item.desc}</option>
											</#list>
										</select>
									</div>
								</div>

								<hr>
								<div class="form-group">
									<div class="col-sm-offset-3 col-sm-6">
										<button type="submit" class="btn btn-primary"  >保存</button>
										<button type="button" class="btn btn-default" data-dismiss="modal">确认</button>

										<#-- id -->
										<input type="hidden" class="form-control" name="id" >
									</div>
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

<script src="${request.contextPath}/static/adminlte/bower_components/moment/moment.min.js"></script>
<script src="${request.contextPath}/static/adminlte/bower_components/bootstrap-daterangepicker/daterangepicker.js"></script>

<script src="${request.contextPath}/static/js/common/datatables.select.js"></script>
<script src="${request.contextPath}/static/js/biz/message.js"></script>
<#-- biz end（5/5 script） -->

</body>
</html>