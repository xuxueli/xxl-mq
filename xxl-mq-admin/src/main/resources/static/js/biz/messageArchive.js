$(function() {

	// ---------- ---------- ---------- main table  ---------- ---------- ----------
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
		},
		startDate: rangesConf['本月'][0] ,
		endDate: rangesConf['本月'][1]
	});

	// ---------- ---------- ---------- main table  ---------- ---------- ----------
	// init date tables
	$.dataTableSelect.init();
	var mainDataTable = $("#data_list").dataTable({
		"deferRender": true,
		"processing" : true, 
	    "serverSide": true,
		"ajax": {
			url: base_url + "/messageArchive/pageList",
			type:"post",
			// request data
	        data : function ( d ) {
	        	var obj = {};
                obj.topic = $('#data_filter .topic').val();
				obj.status = $('#data_filter .status').val();
				obj.filterTime = $('#data_filter .filterTime').val();
				obj.offset = d.start;
				obj.pagesize = d.length;

                return obj;
            },
			// response data filter
			dataFilter: function (originData) {
				var originJson = $.parseJSON(originData);
				return JSON.stringify({
					recordsTotal: originJson.data.totalCount,
					recordsFiltered: originJson.data.totalCount,
					data: originJson.data.pageData
				});
			}
	    },
	    "searching": false,
	    "ordering": false,
	    //"scrollX": true,																		// scroll x，close self-adaption
		//"dom": '<"top" t><"bottom" <"col-sm-3" i><"col-sm-3 right" l><"col-sm-6" p> >',		// dataTable "DOM layout"：https://datatables.club/example/diy.html
		"drawCallback": function( settings ) {
			$.dataTableSelect.selectStatusInit();
		},
	    "columns": [
			{
				"title": '<input align="center" type="checkbox" id="checkAll" >',
				"data": 'id',
				"visible" : true,
				"width":'5%',
				"render": function ( data, type, row ) {
					tableData['key'+row.id] = row;
					return '<input align="center" type="checkbox" class="checkItem" data-id="'+ row.id +'"  >';
				}
			},
			{
				"title": '消息ID',
				"data": 'id',
				"width":'5%'
			},
			{
				"title": 'Topic（消息主题）',
				"data": 'topic',
				"width":'20%',
				"render": function ( data, type, row ) {
					var result = data.length<20
						?data
						:data.substring(0, 20) + '...';
					return '<span title="'+ data +'">'+ result +'</span>';
				}
			},
			{
				"title": 'partitionId',
				"data": 'partitionId',
				"width":'10%'
			},
			{
				"title": '生效时间',
				"data": 'effectTime',
				"width":'15%'
			},
			{
				"title": '状态',
				"data": 'status',
				"width":'10%',
				"render": function ( data, type, row ) {
					var ret = data;
					$("#updateModal .form select[name='status']").children("option").each(function() {
						if ($(this).val() === row.status+"") {
							ret = $(this).html();
						}
					});
					return ret;
				}
			},

		],
		"language" : {
			"sProcessing" : I18n.dataTable_sProcessing ,
			"sLengthMenu" : I18n.dataTable_sLengthMenu ,
			"sZeroRecords" : I18n.dataTable_sZeroRecords ,
			"sInfo" : I18n.dataTable_sInfo ,
			"sInfoEmpty" : I18n.dataTable_sInfoEmpty ,
			"sInfoFiltered" : I18n.dataTable_sInfoFiltered ,
			"sInfoPostFix" : "",
			"sSearch" : I18n.dataTable_sSearch ,
			"sUrl" : "",
			"sEmptyTable" : I18n.dataTable_sEmptyTable ,
			"sLoadingRecords" : I18n.dataTable_sLoadingRecords ,
			"sInfoThousands" : ",",
			"oPaginate" : {
				"sFirst" : I18n.dataTable_sFirst ,
				"sPrevious" : I18n.dataTable_sPrevious ,
				"sNext" : I18n.dataTable_sNext ,
				"sLast" : I18n.dataTable_sLast
			},
			"oAria" : {
				"sSortAscending" : I18n.dataTable_sSortAscending ,
				"sSortDescending" : I18n.dataTable_sSortDescending
			}
		}
	});

    // table data
    var tableData = {};

	// search btn
	$('#data_filter .searchBtn').on('click', function(){

		// valid
		if (!$('#data_filter .topic').val()) {
			//layer.msg('请输入Topic');
			layer.msg('请输入Topic', {icon: 2, time: 3000});
			return;
		}

        mainDataTable.fnDraw();
	});

	// ---------- ---------- ---------- showConsumeLog ---------- ---------- ----------

	$("#data_operation").on('click', '.showConsumeLog',function() {

		// find select ids
		var selectIds = $.dataTableSelect.selectIdsFind();
		if (selectIds.length != 1) {
			layer.msg(I18n.system_please_choose + I18n.system_one + I18n.system_data);
			return;
		}
		var row = tableData[ 'key' + selectIds[0] ];

		// show
		$('#showConsumeLogModel .id').html(row.id);
		$('#showConsumeLogModel .consumeInstanceUuid').html(row.consumeInstanceUuid);
		$('#showConsumeLogModel .addTime').html( moment(row.addTime).format('YYYY-MM-DD HH:mm:ss') );
		$('#showConsumeLogModel .updateTime').html( moment(row.updateTime).format('YYYY-MM-DD HH:mm:ss') );
		$('#showConsumeLogModel .consumeLog').html(row.consumeLog);
		$('#showConsumeLogModel').modal({backdrop: false, keyboard: false}).modal('show');

	});

	// ---------- ---------- ---------- archive ---------- ---------- ----------

	$("#data_operation").on('click', '.archive',function() {

		let topic = $('#data_filter .topic').val();
		$("#archiveModel .form input[name='topic']").val( topic );

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
		$.post(base_url + "/messageArchive/archiveClean",  $("#archiveModel .form").serialize(), function(data, status) {
			if (data.code == "200") {
				$('#archiveModel').modal('hide');
				layer.open({
					title: "系统提示",
					btn: [ "确认" ],
					content: "操作成功" ,
					icon: '1',
					end: function(layero, index){
						mainDataTable.fnDraw(false);
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


});
