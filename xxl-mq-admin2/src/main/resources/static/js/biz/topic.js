$(function() {

	// ---------- ---------- ---------- main table  ---------- ---------- ----------
	// init date tables
	$.dataTableSelect.init();
	var mainDataTable = $("#data_list").dataTable({
		"deferRender": true,
		"processing" : true, 
	    "serverSide": true,
		"ajax": {
			url: base_url + "/topic/pageList",
			type:"post",
			// request data
	        data : function ( d ) {
	        	var obj = {};
                obj.appname = $('#data_filter .appname').val();
                obj.topic = $('#data_filter .topic').val();
	        	obj.start = d.start;
	        	obj.length = d.length;
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
				"title": '主题名称',
				"data": 'desc',
				"width":'15%',
				"render": function ( data, type, row ) {
					var result = data.length<20
						?data
						:data.substring(0, 20) + '...';
					return '<span title="'+ data +'">'+ result +'</span>';
				}
			},
			{
				"title": '分区策略',
				"data": 'partitionStrategy',
				"width":'10%',
				"render": function ( data, type, row ) {
					var ret = data;
					$("#addModal .form select[name='partitionStrategy']").children("option").each(function() {
						if ($(this).val() === row.partitionStrategy+"") {
							ret = $(this).html();
						}
					});
					return ret;
				}
			},
			{
				"title": '重试策略',
				"data": 'retryStrategy',
				"width":'10%',
				"render": function ( data, type, row ) {
					var ret = data;
					$("#addModal .form select[name='retryStrategy']").children("option").each(function() {
						if ($(this).val() === row.retryStrategy+"") {
							ret = $(this).html();
						}
					});
					return ret;
				}
			},
			{
				"title": '负责人',
				"data": 'owner',
				"width":'10%'
			},
			{
				"title": '状态',
				"data": 'status',
				"width":'10%',
				"render": function ( data, type, row ) {
					// status
					if (0 == data) {
						return '<small class="label label-success" >RUNNING</small>';
					} else {
						return '<small class="label label-default" >STOP</small>';
					}
					return data;
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
        mainDataTable.fnDraw();
	});

	// ---------- ---------- ---------- delete operation ---------- ---------- ----------
	// delete
	$("#data_operation").on('click', '.delete',function() {

		// find select ids
		var selectIds = $.dataTableSelect.selectIdsFind();
		if (selectIds.length <= 0) {
			layer.msg(I18n.system_please_choose + I18n.system_data);
			return;
		}

		// do delete
		layer.confirm( I18n.system_ok + I18n.system_opt_del + '?', {
			icon: 3,
			title: I18n.system_tips ,
            btn: [ I18n.system_ok, I18n.system_cancel ]
		}, function(index){
			layer.close(index);

			$.ajax({
				type : 'POST',
				url : base_url + "/topic/delete",
				data : {
					"ids" : selectIds
				},
				dataType : "json",
				success : function(data){
					if (data.code == 200) {
                        layer.msg( I18n.system_opt_del + I18n.system_success );
						mainDataTable.fnDraw(false);	// false，refresh current page；true，all refresh
					} else {
                        layer.msg( data.msg || I18n.system_opt_del + I18n.system_fail );
					}
				},
				error: function(xhr, status, error) {
					// Handle error
					console.log("Error: " + error);
					layer.open({
						icon: '2',
						content: (I18n.system_opt_del + I18n.system_fail)
					});
				}
			});
		});
	});

	// ---------- ---------- ---------- add operation ---------- ---------- ----------
	// add validator method
	jQuery.validator.addMethod("topicValid", function(value, element) {
		var valid = /^[a-z][a-z0-9-]*$/;
		return this.optional(element) || valid.test(value);
	}, '限制小写字母开头，由小写字母、数字和中划线组成' );
	// add
	$("#data_operation .add").click(function(){
		$('#addModal').modal({backdrop: false, keyboard: false}).modal('show');
	});
	var addModalValidate = $("#addModal .form").validate({
		errorElement : 'span',  
        errorClass : 'help-block',
        focusInvalid : true,  
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
		highlight : function(element) {  
            $(element).closest('.form-group').addClass('has-error');  
        },
        success : function(label) {  
            label.closest('.form-group').removeClass('has-error');  
            label.remove();  
        },
        errorPlacement : function(error, element) {  
            element.parent('div').append(error);  
        },
        submitHandler : function(form) {

			// valid
			var executionTimeout = $("#addModal .form input[name='executionTimeout']").val();
			if(!/^\d+$/.test(executionTimeout)) {
				executionTimeout = 0;
			}
			$("#addModal .form input[name='executionTimeout']").val(executionTimeout);

			// request
			var paramData = $("#addModal .form").serializeArray();

			// post
        	$.post(base_url + "/topic/insert", paramData, function(data, status) {
    			if (data.code == "200") {
					$('#addModal').modal('hide');

                    layer.msg( I18n.system_opt_add + I18n.system_success );
                    mainDataTable.fnDraw();
    			} else {
					layer.open({
						title: I18n.system_tips ,
                        btn: [ I18n.system_ok ],
						content: (data.msg || I18n.system_opt_add + I18n.system_fail ),
						icon: '2'
					});
    			}
    		});
		}
	});
	$("#addModal").on('hide.bs.modal', function () {
		addModalValidate.resetForm();

		$("#addModal .form")[0].reset();
		$("#addModal .form .form-group").removeClass("has-error");
	});

	// ---------- ---------- ---------- update operation ---------- ---------- ----------
	$("#data_operation .update").click(function(){

		// find select ids
		var selectIds = $.dataTableSelect.selectIdsFind();
		if (selectIds.length != 1) {
			layer.msg(I18n.system_please_choose + I18n.system_one + I18n.system_data);
			return;
		}
		var row = tableData[ 'key' + selectIds[0] ];

		// base data
		$("#updateModal .form input[name='id']").val( row.id );
		$("#updateModal .form input[name='appname']").val( row.appname );
		$("#updateModal .form input[name='topic']").val( row.topic );
		$("#updateModal .form input[name='desc']").val( row.desc );
		$("#updateModal .form input[name='owner']").val( row.owner );
		$("#updateModal .form input[name='alarmEmail']").val( row.alarmEmail );
		$("#updateModal .form select[name='storeStrategy']").val( row.storeStrategy );
		$("#updateModal .form select[name='partitionStrategy']").val( row.partitionStrategy );
		$("#updateModal .form select[name='archiveStrategy']").val( row.archiveStrategy );
		$("#updateModal .form select[name='retryStrategy']").val( row.retryStrategy );
		$("#updateModal .form input[name='retryCount']").val( row.retryCount );
		$("#updateModal .form input[name='retryInterval']").val( row.retryInterval );
		$("#updateModal .form input[name='executionTimeout']").val( row.executionTimeout );
		$("#updateModal .form select[name='level']").val( row.level );

		// show
		$('#updateModal').modal({backdrop: false, keyboard: false}).modal('show');
	});
	var updateModalValidate = $("#updateModal .form").validate({
		errorElement : 'span',  
        errorClass : 'help-block',
        focusInvalid : true,
		highlight : function(element) {
            $(element).closest('.form-group').addClass('has-error');  
        },
        success : function(label) {  
            label.closest('.form-group').removeClass('has-error');  
            label.remove();  
        },
        errorPlacement : function(error, element) {  
            element.parent('div').append(error);  
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
        submitHandler : function(form) {

			// request
			var paramData = $("#updateModal .form").serializeArray();

            $.post(base_url + "/topic/update", paramData, function(data, status) {
                if (data.code == "200") {
                    $('#updateModal').modal('hide');

                    layer.msg( I18n.system_opt_edit + I18n.system_success );
					mainDataTable.fnDraw(false);
                } else {
                    layer.open({
                        title: I18n.system_tips ,
                        btn: [ I18n.system_ok ],
                        content: (data.msg || I18n.system_opt_edit + I18n.system_fail ),
                        icon: '2'
                    });
                }
            });
		}
	});
	$("#updateModal").on('hide.bs.modal', function () {

		// reset
		updateModalValidate.resetForm();

		$("#updateModal .form")[0].reset();
        $("#updateModal .form .form-group").removeClass("has-error");
	});


	// ---------- ---------- ---------- update status ---------- ---------- ----------
	$("#data_operation").on('click', '.updateStatus',function() {

		// find select ids
		var selectIds = $.dataTableSelect.selectIdsFind();
		if (selectIds.length <= 0) {
			layer.msg(I18n.system_please_choose + I18n.system_data);
			return;
		}


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
					mainDataTable.fnDraw(false);	// false，refresh current page；true，all refresh
				} else {
					layer.msg( data.msg || I18n.system_opt + I18n.system_fail );
				}
			}
		});
	}

});
