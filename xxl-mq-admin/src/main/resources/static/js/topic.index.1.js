$(function() {

	// init date tables
	var dataTable = $("#data_list").dataTable({
		"deferRender": true,
		"processing" : true, 
	    "serverSide": true,
		"ajax": {
			url: base_url + "/topic/pageList",
	        data : function ( d ) {
				var obj = {};
				obj.start = d.start;
				obj.length = d.length;
				obj.bizId = $('#bizId').val();
				obj.topic = $('#topic').val();
				return obj;
            }
	    },
	    "searching": false,
	    "ordering": false,
	    //"scrollX": true,	// X轴滚动条，取消自适应
	    "columns": [
					{
						data: 'bizId',
						width: '20%',
						render : function ( data, type, row ) {
							return bizListObj[ data+'' ]
						}
					},
					{ data: 'topic', width: '40%'},
            		{ data: 'author', width: '20%'},
            		{ data: 'alarmEmails', visible: false},
	                {
	                	data: 'opt' ,
                        width: '20%',
	                	"render": function ( data, type, row ) {
	                		return function(){

	                			// data
                                tableData['key'+row.topic] = row;

                                // opt
	                			var html = '<p topic="'+ row.topic +'" >'+
										'<button class="btn btn-info btn-xs topic_update" type="button">编辑</button>  '+
										'<button class="btn btn-danger btn-xs topic_remove" type="button">删除</button>  '+
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

    // search btn
	$('#searchBtn').on('click', function(){
		dataTable.fnDraw();
	});
	
	// topic_remove
	$("#data_list").on('click', '.topic_remove',function() {

		var topic = $(this).parent('p').attr("topic");

        layer.confirm( "确认删除该消息主题?", {
            icon: 3,
            title: "系统提示" ,
            btn: [ "确认", "取消" ]
        }, function(index){
            layer.close(index);

            $.ajax({
                type : 'POST',
                url : base_url + "/topic/delete",
                data : {
                    "topic"  : topic
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

	// topic_add
	$('#topic_add').on('click', function(){
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
			}
		},
		messages : {
            topic : {
				required :'请输入"消息主题".'  ,
                rangelength: '消息主题长度限制为[4~255]'
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
			$.post(base_url + "/topic/add", $("#addModal .form").serialize(), function(data, status) {
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
	});


	// topic_update
	$("#data_list").on('click', '.topic_update',function() {
		var topic = $(this).parent('p').attr("topic");
        var row = tableData['key'+topic ];

        $("#updateModal .form select[name='bizId']").find("option[value='" + row.bizId + "']").prop("selected",true);
        $("#updateModal .form input[name='topic']").val( row.topic );
        $("#updateModal .form input[name='author']").val( row.author );
        $("#updateModal .form input[name='alarmEmails']").val( row.alarmEmails );

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
            }
        },
        messages : {
            topic : {
                required :'请输入"消息主题".'  ,
                rangelength: '消息主题长度限制为[4~255]'
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
			$.post(base_url + "/topic/update", $("#updateModal .form").serialize(), function(data, status) {
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
                        content: (data.msg || "操作失败" ),
                        icon: '2'
                    });
				}
			});
		}
	});
	$("#updateModal").on('hide.bs.modal', function () {
		$("#updateModal .form")[0].reset();
	});
	
});
