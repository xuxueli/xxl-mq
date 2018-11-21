$(function() {

    // filter Time
    var rangesConf = {};
    rangesConf['今日'] = [moment().startOf('day'), moment().endOf('day')];
    rangesConf['昨日'] = [moment().subtract(1, 'days').startOf('day'), moment().subtract(1, 'days').endOf('day')];
    rangesConf['本月'] = [moment().startOf('month'), moment().endOf('month')];
    rangesConf['上个月'] = [moment().subtract(1, 'months').startOf('month'), moment().subtract(1, 'months').endOf('month')];
    rangesConf['最近一周'] = [moment().subtract(1, 'weeks').startOf('day'), moment().endOf('day')];
    rangesConf['最近一月'] = [moment().subtract(1, 'months').startOf('day'), moment().endOf('day')];

    $('#filterTime').daterangepicker({
        autoApply:false,
        singleDatePicker:false,
        showDropdowns:false,        // 是否显示年月选择条件
        timePicker: true, 			// 是否显示小时和分钟选择条件
        timePickerIncrement: 10, 	// 时间的增量，单位为分钟
        timePicker24Hour : true,
        opens : 'left', //日期选择框的弹出位置
        ranges: rangesConf,
        locale : {
            format: 'YYYY-MM-DD HH:mm:ss',
            separator : ' - ',
            customRangeLabel : '自定义' ,
            applyLabel : '确定' ,
            cancelLabel : '取消' ,
            fromLabel : '起始时间' ,
            toLabel : '结束时间' ,
            daysOfWeek : '日,一,二,三,四,五,六'.split(',') ,        // '日', '一', '二', '三', '四', '五', '六'
            monthNames : '一月,二月,三月,四月,五月,六月,七月,八月,九月,十月,十一月,十二月'.split(',') ,        // '一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月'
            firstDay : 1
        },
        startDate: rangesConf['最近一月'][0] ,
        endDate: rangesConf['最近一月'][1]
    });

	// 时间格式化
    $(".inputmask").inputmask({
        mask: "y-m-d h:s:s",
        hourFormat: "24",
        placeholder: "yyyy-mm-dd hh:mm:ss"
    });

	// init date tables
	var dataTable = $("#data_list").dataTable({
		"deferRender": true,
		"processing" : true, 
	    "serverSide": true,
		"ajax": {
			url: base_url + "/message/pageList",
	        data : function ( d ) {
				var obj = {};
				obj.start = d.start;
				obj.length = d.length;
				obj.topic = $('#topic').val();
				obj.status = $('#status').val();
                obj.filterTime = $('#filterTime').val();
				return obj;
            }
	    },
	    "searching": false,
	    "ordering": false,
	    //"scrollX": true,	// X轴滚动条，取消自适应
	    "columns": [
	                { data: 'id'},
					{ data: 'topic'},
            		{ data: 'group'},
					{ data: 'data', visible: false},
            		{ data: 'status'},
            		{ data: 'retryCount'},
					{ data: 'shardingId'},
					{ data: 'timeout'},
					{
						data: 'effectTime',
                        ordering: true,
						render : function ( data, type, row ) {
							var temp = data?moment(new Date(data)).format("YYYY-MM-DD HH:mm:ss"):"";
							return temp;
						}
					},
					{
						data: 'addTime',
						render : function ( data, type, row ) {
							var temp = data?moment(new Date(data)).format("YYYY-MM-DD HH:mm:ss"):"";
							return temp;
						}
					},
					{
						data: 'log',
                        ordering: true,
						render : function ( data, type, row ) {
							if (data) {
								return '<a href="javascript:;" class="showLog" _id="'+ row.id +'">查看</spam></a>';
							} else {
								return '空';
							}
						}
					},
	                { data: 'opt' ,
	                	"render": function ( data, type, row ) {
	                		return function(){

	                			// data
                                tableData['key'+row.id] = row;

                                // opt
	                			var html = '<p id="'+ row.id +'" >'+
										'<button class="btn btn-info btn-xs msg_update" type="button">编辑</button>  '+
										'<button class="btn btn-danger btn-xs msg_remove" type="button">删除</button>  '+
								  		'</p>';
	                			return html;
	                		};
	                	}
	                }
	            ],
		"language" : {
			"sProcessing" : "处理中...",
			"sLengthMenu" : "每页 _MENU_ 条记录",
			"sZeroRecords" : "没有匹配结果",
			"sInfo" : "第 _PAGE_ 页 ( 总共 _PAGES_ 页 ) 总记录数 _MAX_ ",
			"sInfoEmpty" : "无记录",
			"sInfoFiltered" : "(由 _MAX_ 项结果过滤)",
			"sInfoPostFix" : "",
			"sSearch" : "搜索:",
			"sUrl" : "",
			"sEmptyTable" : "表中数据为空",
			"sLoadingRecords" : "载入中...",
			"sInfoThousands" : ",",
			"oPaginate" : {
				"sFirst" : "首页",
				"sPrevious" : "上页",
				"sNext" : "下页",
				"sLast" : "末页"
			},
			"oAria" : {
				"sSortAscending" : ": 以升序排列此列",
				"sSortDescending" : ": 以降序排列此列"
			}
		}
	});

    // table data
    var tableData = {};

	// msg 弹框
    $("#data_list").on('click', '.showLog',function() {
        var _id = $(this).attr('_id');
        var row = tableData['key' + _id ];
        ComAlertTec.show(row.log);
    });

    // search btn
	$('#searchBtn').on('click', function(){
		dataTable.fnDraw();
	});
	
	// msg_remove
	$("#data_list").on('click', '.msg_remove',function() {

		var id = $(this).parent('p').attr("id");

        layer.confirm( "确认删除该消息?", {
            icon: 3,
            title: "系统提示" ,
            btn: [ "确认", "取消" ]
        }, function(index){
            layer.close(index);

            $.ajax({
                type : 'POST',
                url : base_url + "/message/delete",
                data : {
                    "id"  : id
                },
                dataType : "json",
                success : function(data){
                    if (data.code == 200) {

                        layer.open({
                            title: "系统提示",
                            btn: [ "确认" ],
                            content: "删除成功" ,
                            icon: '1',
                            end: function(layero, index){
                                dataTable.fnDraw(false);
                            }
                        });
                    } else {
                        layer.open({
                            title: "系统提示",
                            btn: [ "确认" ],
                            content: (data.msg || "删除失败" ),
                            icon: '2'
                        });
                    }
                }
            });
        });

	});

	// msg_add
	$('#msg_add').on('click', function(){
		//$("#addModal .form input[name='effectTime']").val( moment(new Date()).format("YYYY-MM-DD HH:mm:ss") );
		$('#addModal').modal({backdrop: false, keyboard: false}).modal('show');
	});
	var addModalValidate = $("#addModal .form").validate({
		errorElement : 'span',
		errorClass : 'help-block',
		focusInvalid : true,
		rules : {
            topic : {
                required : true ,
                rangelength:[4,255]
            },
			retryCount : {
				digits : true
			},
            shardingId : {
                digits : true
            },
            timeout : {
                digits : true
            }
			
		},
		messages : {
            topic : {
                required :'请输入"消息主题".'  ,
                rangelength: '消息主题长度限制为[4~255]'
            },
			retryCount : {
				digits :'请输入"正整数".'
			},
            shardingId : {
                digits :'请输入"正整数".'
            },
            timeout : {
                digits :'请输入"正整数".'
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
			$.post(base_url + "/message/add", $("#addModal .form").serialize(), function(data, status) {
				if (data.code == "200") {
					$('#addModal').modal('hide');

                    layer.open({
                        title: "系统提示",
                        btn: [ "确认" ],
                        content: "新增成功" ,
                        icon: '1',
                        end: function(layero, index){
                            dataTable.fnDraw(false);
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
		}
	});
	$("#addModal").on('hide.bs.modal', function () {
		$("#addModal .form")[0].reset();
		addModalValidate.resetForm();
		$("#addModal .form .form-group").removeClass("has-error");
	});

	// msg_update
	$("#data_list").on('click', '.msg_update',function() {
		var id = $(this).parent('p').attr("id");
        var row = tableData['key' + id ];

		$("#updateModal .form input[name='id']").val( id );
		$("#updateModal .form input[name='topic']").val( row.topic );
        $("#updateModal .form input[name='group']").val( row.group );
        $("#updateModal .form textarea[name='data']").val( row.data );
        $("#updateModal .form select[name='status']").find("option[value='" + row.status + "']").prop("selected",true);
        $("#updateModal .form input[name='retryCount']").val( row.retryCount );
        $("#updateModal .form input[name='shardingId']").val( row.shardingId );
        $("#updateModal .form input[name='timeout']").val( row.timeout );
        $("#updateModal .form input[name='effectTime']").val( moment(new Date(Number( row.effectTime ))).format("YYYY-MM-DD HH:mm:ss") );

		$('#updateModal').modal({backdrop: false, keyboard: false}).modal('show');
	});
	var updateModalValidate = $("#updateModal .form").validate({
		errorElement : 'span',
		errorClass : 'help-block',
		focusInvalid : true,
        rules : {
            topic : {
                required : true ,
                rangelength:[4,255]
            },
            retryCount : {
                digits : true
            },
            shardingId : {
                digits : true
            },
            timeout : {
                digits : true
            }

        },
        messages : {
            topic : {
                required :'请输入"消息主题".'  ,
                rangelength: '消息主题长度限制为[4~255]'
            },
            retryCount : {
                digits :'请输入"正整数".'
            },
            shardingId : {
                digits :'请输入"正整数".'
            },
            timeout : {
                digits :'请输入"正整数".'
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
			$.post(base_url + "/message/update", $("#updateModal .form").serialize(), function(data, status) {
				if (data.code == "200") {
					$('#updateModal').modal('hide');

                    layer.open({
                        title: "系统提示",
                        btn: [ "确认" ],
                        content: "更新成功" ,
                        icon: '1',
                        end: function(layero, index){
                            dataTable.fnDraw(false);
                        }
                    });
				} else {
                    layer.open({
                        title: "系统提示",
                        btn: [ "确认" ],
                        content: (data.msg || "更新失败" ),
                        icon: '2'
                    });
				}
			});
		}
	});
	$("#updateModal").on('hide.bs.modal', function () {
		$("#updateModal .form")[0].reset();
		updateModalValidate.resetForm();
		$("#updateModal .form .form-group").removeClass("has-error");
	});
	
});


// Com Alert by Tec theme
var ComAlertTec = {
    html:function(){
        var html =
            '<div class="modal fade" id="ComAlertTec" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">' +
            '<div class="modal-dialog">' +
            '<div class="modal-content-tec">' +
            '<div class="modal-body"><div class="alert" style="color:#fff;"></div></div>' +
            '<div class="modal-footer">' +
            '<div class="text-center" >' +
            '<button type="button" class="btn btn-info ok" data-dismiss="modal" >确认</button>' +
            '</div>' +
            '</div>' +
            '</div>' +
            '</div>' +
            '</div>';
        return html;
    },
    show:function(msg, callback){
        // dom init
        if ($('#ComAlertTec').length == 0){
            $('body').append(ComAlertTec.html());
        }

        // init com alert
        $('#ComAlertTec .alert').html(msg);
        $('#ComAlertTec').modal('show');

        $('#ComAlertTec .ok').click(function(){
            $('#ComAlertTec').modal('hide');
            if(typeof callback == 'function') {
                callback();
            }
        });
    }
};
