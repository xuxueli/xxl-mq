<!DOCTYPE html>
<html>
<head>
	<#-- import macro -->
	<#import "../common/common.macro.ftl" as netCommon>

	<!-- 1-style start -->
	<@netCommon.commonStyle />
	<link rel="stylesheet" href="${request.contextPath}/static/plugins/bootstrap-table/bootstrap-table.min.css">
	<!-- 1-style end -->

</head>
<body class="hold-transition" style="background-color: #ecf0f5;">
<div class="wrapper">
	<section class="content">

		<!-- 2-content start -->

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
					<div class="col-xs-1">
						<button class="btn btn-block btn-default resetBtn" >${I18n.system_reset}</button>
					</div>
				</div>
			</div>
		</div>

		<#-- 数据表格区域 -->
		<div class="row">
			<div class="col-xs-12">
				<div class="box">
					<div class="box-header pull-left" id="data_operation" >
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

		<!-- 2-content end -->

	</section>
</div>

<!-- 3-script start -->
<@netCommon.commonScript />
<script src="${request.contextPath}/static/plugins/bootstrap-table/bootstrap-table.min.js"></script>
<script src="${request.contextPath}/static/plugins/bootstrap-table/locale/bootstrap-table-zh-CN.min.js"></script>
<script src="${request.contextPath}/static/adminlte/plugins/iCheck/icheck.min.js"></script>
<#-- admin table -->
<script src="${request.contextPath}/static/biz/common/admin.table.js"></script>
<script src="${request.contextPath}/static/biz/common/admin.util.js"></script>
<script>
	$(function() {

		/**
		 * init table
		 */
		$.adminTable.initTable({
			table: '#data_list',
			url: base_url + "/topic/pageList",
			queryParams: function (params) {
				var obj = {};
				obj.appname = $('#data_filter .appname').val();
				obj.topic = $('#data_filter .topic').val();
				obj.offset = params.offset;
				obj.pagesize = params.limit;
				return obj;
			},
			columns:[
				{
					checkbox: true,
					field: 'state',
					width: '5',
					widthUnit: '%',
					align: 'center',
					valign: 'middle'
				}, {
					title: 'Topic',
					field: 'topic',
					width: '20',
					widthUnit: '%',
					align: 'left',
					formatter: function(value, row, index) {
						let result = value.length<20
								?value
								:value.substring(0, 20) + '...';
						return '<span title="'+ value +'">'+ result +'</span>';
					}
				}, {
					title: '主题名称',
					field: 'desc',
					width: '20',
					widthUnit: '%',
					align: 'left',
					formatter: function (value, row, index) {
						let result = value.length<20
								?value
								:value.substring(0, 20) + '...';
						return '<span title="'+ value +'">'+ result +'</span>';
					}
				},{
					title: '分区策略',
					field: 'partitionStrategy',
					width: '10',
					widthUnit: '%',
					align: 'left',
					formatter: function (value, row, index) {
						let ret = value;
						$("#addModal [name='partitionStrategy']").children("option").each(function() {
							if ($(this).val() === row.partitionStrategy+"") {
								ret = $(this).html();
							}
						});
						return ret;
					}
				}, {
					title: '重试策略',
					field: 'retryStrategy',
					width: '10',
					widthUnit: '%',
					align: 'left',
					formatter: function(value, row, index) {
						var ret = value
						$("#addModal [name='retryStrategy']").children("option").each(function() {
							if ($(this).val() === row.retryStrategy+"") {
								ret = $(this).html();
							}
						});
						return ret;
					}
				}, {
					title: '负责人',
					field: 'owner',
					width: '10',
					widthUnit: '%',
					align: 'left'
				}, {
					title: '状态',
					field: 'status',
					width: '10',
					widthUnit: '%',
					align: 'left',
					formatter: function(value, row, index) {
						// status
						if (0 === value) {
							return '<small class="label label-success" >RUNNING</small>';
						} else {
							return '<small class="label label-default" >STOP</small>';
						}
					}
				}
			]
		});


		/**
		 * init delete
		 */
		$.adminTable.initDelete({
			url: base_url + "/topic/delete"
		});

		/**
		 * init add
		 */
		// add validator method
		jQuery.validator.addMethod("topicValid", function(value, element) {
			var valid = /^[a-z][a-z0-9_]*$/;
			return this.optional(element) || valid.test(value);
		}, '限制小写字母开头，由小写字母、数字和下划线组成' );
		$.adminTable.initAdd( {
			url: base_url + "/topic/insert",
			rules : {
				topic : {
					required : true,
					rangelength:[4, 100],
					topicValid: true
				},
				desc : {
					required : true,
					rangelength:[4, 50]
				},
				retryCount : {
					required : true,
					min: 0,
					max: 1000
				},
				retryInterval : {
					required : true,
					min: 3,
					max: 1000000
				},
				executionTimeout : {
					required : false,
					min: 0,
					max: 1000000
				}
			},
			messages : {
				topic : {
					required : I18n.system_please_input,
					rangelength: I18n.system_lengh_limit + "[{0}-{1}]"
				},
				desc : {
					required : I18n.system_please_input,
					rangelength: I18n.system_lengh_limit + "[{0}-{1}]"
				},
				retryCount : {
					required : I18n.system_please_input,
					min: '限制不能小于 {0}',
					max: '限制不能大于 {0}'
				},
				retryInterval : {
					required : I18n.system_please_input,
					min: '限制不能小于 {0}',
					max: '限制不能大于 {0}'
				},
				executionTimeout : {
					required : I18n.system_please_input,
					min: '限制不能小于 {0}',
					max: '限制不能大于 {0}'
				}
			},
			readFormData: function() {
				// valid
				var executionTimeout = $("#addModal [name='executionTimeout']").val();
				if(!/^\d+$/.test(executionTimeout)) {
					executionTimeout = 0;
				}
				$("#addModal [name='executionTimeout']").val(executionTimeout);

				// request
				return $("#addModal .form").serializeArray();
			}
		});

		/**
		 * init update
		 */
		$.adminTable.initUpdate( {
			url: base_url + "/topic/update",
			writeFormData: function(row) {

				// base data
				$("#updateModal [name='id']").val( row.id );
				$("#updateModal [name='appname']").val( row.appname );
				$("#updateModal [name='topic']").val( row.topic );
				$("#updateModal [name='desc']").val( row.desc );
				$("#updateModal [name='owner']").val( row.owner );
				$("#updateModal [name='alarmEmail']").val( row.alarmEmail );
				$("#updateModal [name='storeStrategy']").val( row.storeStrategy );
				$("#updateModal [name='partitionStrategy']").val( row.partitionStrategy );
				$("#updateModal [name='archiveStrategy']").val( row.archiveStrategy );
				$("#updateModal [name='retryStrategy']").val( row.retryStrategy );
				$("#updateModal [name='retryCount']").val( row.retryCount );
				$("#updateModal [name='retryInterval']").val( row.retryInterval );
				if (row.executionTimeout && row.executionTimeout>0) {
					$("#updateModal [name='executionTimeout']").val( row.executionTimeout );
				}
				$("#updateModal [name='level']").val( row.level );
			},
			rules : {
				desc : {
					required : true,
					rangelength:[4, 50]
				},
				retryCount : {
					required : true,
					min: 0,
					max: 1000
				},
				retryInterval : {
					required : true,
					min: 3,
					max: 1000000
				},
				executionTimeout : {
					required : false,
					min: 0,
					max: 1000000
				}
			},
			messages : {
				desc : {
					required : I18n.system_please_input,
					rangelength: I18n.system_lengh_limit + "[{0}-{1}]"
				},
				retryCount : {
					required : I18n.system_please_input,
					min: '限制不能小于 {0}',
					max: '限制不能大于 {0}'
				},
				retryInterval : {
					required : I18n.system_please_input,
					min: '限制不能小于 {0}',
					max: '限制不能大于 {0}'
				},
				executionTimeout : {
					required : I18n.system_please_input,
					min: '限制不能小于 {0}',
					max: '限制不能大于 {0}'
				}
			},
			readFormData: function() {
				// valid
				var executionTimeout = $("#updateModal [name='executionTimeout']").val();
				if(!/^\d+$/.test(executionTimeout)) {
					executionTimeout = 0;
				}
				$("#updateModal [name='executionTimeout']").val(executionTimeout);

				// request
				return $("#updateModal .form").serializeArray();
			}
		});

		// ---------- ---------- ---------- update status ---------- ---------- ----------

		$("#data_operation").on('click', '.updateStatus',function() {

			// get selectIds
			let selectIds = $.adminTable.selectIds();
			if (selectIds.length <= 0) {
				layer.msg(I18n.system_please_choose + I18n.system_data);
				return;
			}

			// confirm
			layer.confirm( '确认修改状态 ?', {
				icon: 3,
				title: I18n.system_tips ,
				btn: [ '启动','禁用' ]
			}, function(index, layero){
				layer.close(index);
				// 启动
				updateStatus(selectIds, 0);
			}, function(index, layero){
				layer.close(index);
				// 禁用
				updateStatus(selectIds, 1);
			});
		});
		function updateStatus(selectIds, status){
			$.ajax({
				type : 'POST',
				url : base_url + "/topic/updateStatus",
				data : {
					"ids" : selectIds,
					"status": status
				},
				dataType : "json",
				success : function(data){
					if (data.code == 200) {
						layer.msg( I18n.system_opt + I18n.system_success );

						// refresh
						$('#data_filter .searchBtn').click();
					} else {
						layer.msg( data.msg || I18n.system_opt + I18n.system_fail );
					}
				}
			});
		}

		// ---------- ---------- ---------- query Message ---------- ---------- ----------

		$("#data_operation .queryMessage").click(function(){
			// get selectRows
			var rows = $.adminTable.selectRows();
			if (rows.length !== 1) {
				layer.msg(I18n.system_please_choose + I18n.system_one + I18n.system_data);
				return;
			}
			var row = rows[0];

			// open tab
			let url = base_url + "/message?topic=" + row.topic;
			openTab(url, '消息管理', false);
		});

		$("#data_operation .queryMessageArchive").click(function(){
			// get selectRows
			var rows = $.adminTable.selectRows();
			if (rows.length !== 1) {
				layer.msg(I18n.system_please_choose + I18n.system_one + I18n.system_data);
				return;
			}
			var row = rows[0];

			// open tab
			let url = base_url + "/messageArchive?topic=" + row.topic;
			openTab(url, '归档消息管理', false);
		});

	});

</script>
<!-- 3-script end -->

</body>
</html>