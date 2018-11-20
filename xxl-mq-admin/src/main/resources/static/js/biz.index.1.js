$(function() {

	// remove
	$('.remove').on('click', function(){
		var id = $(this).data('id');

		layer.confirm( ('确认是否删除？') , {
			icon: 3,
			title: '系统提示' ,
            btn: [ '确认', '取消' ]
		}, function(index){
			layer.close(index);

			$.ajax({
				type : 'POST',
				url : base_url + '/biz/remove',
				data : {"id":id},
				dataType : "json",
				success : function(data){
					if (data.code == 200) {
						layer.open({
							title: '系统提示' ,
                            btn: [ '确认' ],
							content: '删除成功',
							icon: '1',
							end: function(layero, index){
								window.location.reload();
							}
						});
					} else {
						layer.open({
							title: '系统提示',
                            btn: [ '确认' ],
							content: (data.msg || '删除失败' ),
							icon: '2'
						});
					}
				},
			});
		});

	});


	$('.add').on('click', function(){
		$('#addModal').modal({backdrop: false, keyboard: false}).modal('show');
	});
	var addModalValidate = $("#addModal .form").validate({
		errorElement : 'span',
		errorClass : 'help-block',
		focusInvalid : true,
		rules : {
            bizName : {
				required : true,
				rangelength:[4,50]
			},
			order : {
				required : true,
				digits:true,
				range:[1,1000]
			}
		},
		messages : {
            bizName : {
				required : '请输入业务线名称',
				rangelength: 'AppName长度限制为4~50'
			},
			order : {
				required : '请输入业务线顺序' ,
				digits: '请输入整数' ,
				range: '取值范围为1~1000'
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
			$.post(base_url + "/biz/save",  $("#addModal .form").serialize(), function(data, status) {
				if (data.code == "200") {
					$('#addModal').modal('hide');
					layer.open({
						title: '系统提示' ,
                        btn: [ '确认' ],
						content: '新增成功' ,
						icon: '1',
						end: function(layero, index){
							window.location.reload();
						}
					});
				} else {
					layer.open({
						title: '系统提示',
                        btn: [ '确认' ],
						content: (data.msg || '新增失败'  ),
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


	// update
	$('.update').on('click', function(){
		$("#updateModal .form input[name='id']").val($(this).data("id"));
		$("#updateModal .form input[name='bizName']").val($(this).data("bizName".toLowerCase()));
		$("#updateModal .form input[name='order']").val($(this).data("order"));

		$('#updateModal').modal({backdrop: false, keyboard: false}).modal('show');
	});
	var updateModalValidate = $("#updateModal .form").validate({
		errorElement : 'span',
		errorClass : 'help-block',
		focusInvalid : true,
        rules : {
            bizName : {
                required : true,
                rangelength:[4,50]
            },
            order : {
                required : true,
                digits:true,
                range:[1,1000]
            }
        },
        messages : {
            bizName : {
                required : '请输入业务线名称',
                rangelength: 'AppName长度限制为4~50'
            },
            order : {
                required : '请输入业务线顺序' ,
                digits: '请输入整数' ,
                range: '取值范围为1~1000'
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
			$.post(base_url + "/biz/update",  $("#updateModal .form").serialize(), function(data, status) {
				if (data.code == "200") {
					$('#addModal').modal('hide');

					layer.open({
						title: '系统提示' ,
                        btn: [ '确认' ],
						content: '更新成功' ,
						icon: '1',
						end: function(layero, index){
							window.location.reload();
						}
					});
				} else {
					layer.open({
						title: '系统提示',
                        btn: [ '确认' ],
						content: (data.msg || '更新失败'  ),
						icon: '2'
					});
				}
			});
		}
	});
	$("#updateModal").on('hide.bs.modal', function () {
		$("#updateModal .form")[0].reset();
		addModalValidate.resetForm();
		$("#updateModal .form .form-group").removeClass("has-error");
	});

	
});
