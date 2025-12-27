<!DOCTYPE html>
<html>
<head>
	<#-- import macro -->
	<#import "../common/common.macro.ftl" as netCommon>

	<!-- 1-style start -->
	<@netCommon.commonStyle />
	<link rel="stylesheet" href="${request.contextPath}/static/plugins/bootstrap-table/bootstrap-table.min.css">
	<link rel="stylesheet" href="${request.contextPath}/static/adminlte/bower_components/bootstrap-daterangepicker/daterangepicker.css">
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
							<span class="input-group-addon">Topic</span>
							<input type="text" class="form-control topic" autocomplete="on" placeholder="请输入消息Topic，精确匹配" <#if topic??>value="${topic}" </#if>  >
						</div>
					</div>
					<div class="col-xs-2">
						<div class="input-group">
							<span class="input-group-addon">状态</span>
							<select class="form-control status" >
								<option value="-1" >全部</option>
								<#list MessageStatusEnum as item>
									<option value="${item.value}" >${item.desc}</option>
								</#list>
							</select>
						</div>
					</div>
					<div class="col-xs-4">
						<div class="input-group">
							<span class="input-group-addon">生效时间</span>
							<input type="text" class="form-control filterTime" readonly >
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
						<button class="btn btn-sm btn-danger archive" type="button">归档清理</button>
						<button class="btn btn-sm btn-primary selectOnlyOne showConsumeLog" type="button">查看消费轨迹</button>
						<button class="btn btn-sm btn-primary queryMessage" type="button">查看实时消息</button>
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
								<div class="col-sm-offset-5 col-sm-2">
									<button type="submit" class="btn btn-primary"  >保存</button>
									<button type="button" class="btn btn-default" data-dismiss="modal">取消</button>

									<#-- id -->
									<input type="hidden" class="form-control" name="id" >
								</div>
							</div>
						</form>
					</div>
				</div>
			</div>
		</div>

		<!-- 消费日志.模态框 -->
		<div class="modal fade" id="showConsumeLogModel" tabindex="-1" role="dialog"  aria-hidden="true">
			<div class="modal-dialog modal-lg">
				<div class="modal-content">
					<div class="modal-header">
						<h4 class="modal-title" >消费日志</h4>
					</div>
					<div class="modal-body">
						<form class="form-horizontal form" role="form" >
							<div class="form-group">
								<label for="lastname" class="col-sm-2 control-label">消息ID<font color="red">*</font></label>
								<div class="col-sm-10 id"></div>
							</div>
							<div class="form-group">
								<label for="lastname" class="col-sm-2 control-label">消息数据<font color="red">*</font></label>
								<div class="col-sm-10 data"></div>
							</div>
							<div class="form-group">
								<label for="lastname" class="col-sm-2 control-label">新增时间<font color="red">*</font></label>
								<div class="col-sm-10 addTime"></div>
							</div>
							<div class="form-group">
								<label for="lastname" class="col-sm-2 control-label">生效时间<font color="red">*</font></label>
								<div class="col-sm-10 effectTime"></div>
							</div>
							<div class="form-group">
								<label for="lastname" class="col-sm-2 control-label">消费日志<font color="red">*</font></label>
								<div class="col-sm-10 consumeLog"></div>
							</div>

							<hr>
							<div class="form-group">
								<div class="col-sm-offset-5 col-sm-2">
									<button type="button" class="btn btn-primary" data-dismiss="modal">关闭</button>
								</div>
							</div>
						</form>
					</div>
				</div>
			</div>
		</div>

		<!-- 归档.模态框 -->
		<div class="modal fade" id="archiveModel" tabindex="-1" role="dialog"  aria-hidden="true">
			<div class="modal-dialog">
				<div class="modal-content">
					<div class="modal-header">
						<h4 class="modal-title" >归档消费</h4>
					</div>
					<div class="modal-body">
						<form class="form-horizontal form" role="form" >
							<div class="form-group">
								<label for="lastname" class="col-sm-2 control-label">Topic<font color="red">*</font></label>
								<div class="col-sm-10 consumeInstanceUuid">
									<input type="text" class="form-control" name="topic" placeholder="请输入Topic" maxlength="100" >
								</div>
							</div>
							<div class="form-group">
								<label for="lastname" class="col-sm-2 control-label">归档策略<font color="red">*</font></label>
								<div class="col-sm-10 consumeInstanceUuid">
									<select class="form-control" name="archiveStrategy" >
										<#list ArchiveStrategyEnum as item>
											<option value="${item.value}" <#if item_index == (ArchiveStrategyEnum?size - 1)>selected</#if> >${item.desc}</option>
										</#list>
									</select>
								</div>
							</div>

							<hr>
							<div class="form-group">
								<div class="col-sm-offset-4 col-sm-4">
									<button type="button" class="btn btn-primary ok" >确认</button>
									<button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
								</div>
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
<#-- daterangepicker -->
<script src="${request.contextPath}/static/adminlte/bower_components/moment/moment.min.js"></script>
<script src="${request.contextPath}/static/adminlte/bower_components/bootstrap-daterangepicker/daterangepicker.js"></script>
<script>
	$(function() {

		// ---------- ---------- ---------- filter ---------- ---------- ----------

		var rangesConf = {};
		rangesConf['今日'] = [moment().startOf('day'), moment().endOf('day')];
		rangesConf['昨日'] = [moment().subtract(1, 'days').startOf('day'), moment().subtract(1, 'days').endOf('day')];
		rangesConf['本月'] = [moment().startOf('month'), moment().endOf('month')];
		rangesConf['上月'] = [moment().subtract(1, 'months').startOf('month'), moment().subtract(1, 'months').endOf('month')];
		rangesConf['今年'] = [moment().startOf('year'), moment().endOf('year')];
		rangesConf['去年'] = [moment().subtract(1, 'year').startOf('year'), moment().subtract(1, 'year').endOf('year')];
		rangesConf['最近一周'] = [moment().subtract(1, 'weeks').startOf('day'), moment().endOf('day')];
		rangesConf['最近一月'] = [moment().subtract(1, 'months').startOf('day'), moment().endOf('day')];
		rangesConf['最近一年'] = [moment().subtract(1, 'year').startOf('day'), moment().endOf('day')];
		$("#data_filter .filterTime").daterangepicker({
			autoApply:false,
			singleDatePicker:false,		// 范围选择 or 单时间选择
			showDropdowns:true,         // 年月选择条件是否为下拉框
			timePicker: true,
			timePicker24Hour: true,
			timePickerSeconds: true,
			opens : 'left', 			//日期选择框的弹出位置
			ranges: rangesConf,
			locale: {
				format: 'YYYY-MM-DD HH:mm:ss',
				separator : ' - ',
				applyLabel: '确认',
				cancelLabel: '取消',
				fromLabel: '从',
				toLabel: '到',
				customRangeLabel: '自定义范围',
				daysOfWeek: ['日', '一', '二', '三', '四', '五', '六'],
				monthNames: ['一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月'],
				firstDay: 1
			}/*,
			startDate: rangesConf['本月'][0] ,
			endDate: rangesConf['本月'][1]*/
		});

		// init filter
		function initFilter(){
			$("#data_filter .filterTime").data("daterangepicker").setStartDate( rangesConf['本月'][0] );
			$("#data_filter .filterTime").data("daterangepicker").setEndDate( rangesConf['本月'][1] );

			<#if topic??>
			$("#data_filter .topic").val( '${topic}' );
			</#if>
		}
		initFilter();

		// ---------- ---------- ---------- main table  ---------- ---------- ----------

		/**
		 * init table
		 */
		$.adminTable.initTable({
			table: '#data_list',
			url: base_url + "/messageArchive/pageList",
			queryParams: function (params) {
				var obj = {};
				obj.topic = $('#data_filter .topic').val();
				obj.status = $('#data_filter .status').val();
				obj.filterTime = $('#data_filter .filterTime').val();
				obj.offset = params.offset;
				obj.pagesize = params.limit;
				return obj;
			},
			searchHandler: function(data){
				// valid
				if (!$("#data_filter .topic").val()) {
					layer.msg('请输入Topic');
					return;
				}
				$.adminTable.table.bootstrapTable('refresh');
			},
			resetHandler: function(data){
				// reset
				$('#data_filter input[type="text"]').val('');
				$('#data_filter select').each(function() {
					$(this).prop('selectedIndex', 0);
				});
				// init
				initFilter();
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
					title: '消息ID',
					field: 'id',
					width: '10',
					widthUnit: '%',
					align: 'left'
				}, {
					title: 'Topic',
					field: 'topic',
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
					title: 'partitionId',
					field: 'partitionId',
					width: '10',
					widthUnit: '%',
					align: 'left'
				},{
					title: '剩余重试次数',
					field: 'retryCountRemain',
					width: '10',
					widthUnit: '%',
					align: 'left'
				},{
					title: '关联业务ID',
					field: 'bizId',
					width: '10',
					widthUnit: '%',
					align: 'left'
				},{
					title: '新建时间',
					field: 'addTime',
					width: '13',
					widthUnit: '%',
					align: 'left'
				},{
					title: '生效时间',
					field: 'effectTime',
					width: '13',
					widthUnit: '%',
					align: 'left'
				}, {
					title: '状态',
					field: 'status',
					width: '10',
					widthUnit: '%',
					align: 'left',
					formatter: function(value, row, index) {
						var ret = row.status;
						$("#updateModal .form select[name='status']").children("option").each(function() {
							if ($(this).val() === row.status+"") {
								ret = $(this).html();
							}
						});

						const statueColor = ['#808080', '#F39C12', '#00A65A', '#c23632', '#c23632'];	// status: 0-4
						return '<span style="color:'+ statueColor[row.status] +'" >'+ ret +'</span>';
					}
				}
			]
		});

		// ---------- ---------- ---------- showConsumeLog ---------- ---------- ----------

		$("#data_operation").on('click', '.showConsumeLog',function() {
			// get selectRows
			var rows = $.adminTable.selectRows();
			if (rows.length !== 1) {
				layer.msg(I18n.system_please_choose + I18n.system_one + I18n.system_data);
				return;
			}
			var row = rows[0];

			// show
			$('#showConsumeLogModel .id').html(row.id);
			$('#showConsumeLogModel .data').html(row.data);
			$('#showConsumeLogModel .addTime').html( row.addTime );		// moment(row.addTime).format('YYYY-MM-DD HH:mm:ss')
			$('#showConsumeLogModel .effectTime').html(row.effectTime);
			$('#showConsumeLogModel .consumeLog').html(row.consumeLog);
			$('#showConsumeLogModel').modal({backdrop: false, keyboard: false}).modal('show');

		});

		// ---------- ---------- ---------- archive ---------- ---------- ----------

		$("#data_operation").on('click', '.archive',function() {
			let topic = $('#data_filter .topic').val();
			$("#archiveModel .form input[name='topic']").val( topic );
			// show
			$('#archiveModel').modal({backdrop: false, keyboard: false}).modal('show');
		});
		$("#archiveModel .ok").on('click', function(){

			// valid
			var topic = $("#archiveModel input[name='topic']").val();
			if (!(typeof topic === 'string' && topic.length > 0)) {
				layer.msg("请输入topic");
				return;
			}

			// invoke
			var loadingIndex = layer.load(2, {	// 菊花
				shade: [0.3, '#808080'] 		// 灰色背景，透明度 0.3
			});
			setTimeout(function(){
				$('#archiveModel').modal('hide');
			}, 50);
			$.post(base_url + "/messageArchive/archiveClean",  $("#archiveModel .form").serialize(), function(data, status) {
				layer.close(loadingIndex);
				if (data.code == "200") {
					layer.open({
						title: "系统提示",
						btn: [ "确认" ],
						content: (data.msg || "操作成功" ) ,
						icon: '1',
						end: function(layero, index){
							// refresh
							$('#data_filter .searchBtn').click();
						}
					});
				} else {
					layer.open({
						title: "系统提示",
						btn: [ "确认" ],
						content: (data.msg || "操作失败" ),
						icon: '2'
					});
				}
			});
		});
		$("#archiveModel").on('hide.bs.modal', function () {
			$("#archiveModel .form")[0].reset();
		});

		// ---------- ---------- ---------- query archive message ---------- ---------- ----------

		$("#data_operation .queryMessage").click(function(){
			let topic = $('#data_filter .topic').val();

			// open tab
			let url = base_url + "/message?topic=" + topic;
			openTab(url, '消息管理', false);
		});

	});

</script>
<!-- 3-script end -->

</body>
</html>