<script src="${request.contextPath}/static/jquery/jquery.min.js"></script>
<body>

    <input type="button" class="send" _type="0" value="并行消费" />
    <br><br>

    <input type="button" class="send" _type="1" value="串行消费" />
    <br><br>

    <input type="button" class="send" _type="2" value="广播消息" />
    <br><br>

    <input type="button" class="send" _type="3" value="延时消息：5分钟后执行" />
    <br><br>

    <input type="button" class="send" _type="4" value="性能测试：批量发送10000条消息" />

    <hr>
    <div id="console"></div>

    <script>
        $(function(){
            $(".send").click(function () {
                var _type = $(this).attr("_type");
                $.post( '${request.contextPath}/produce', {'type':_type}, function(data,status){
                    var temp = "<br>" + new Date().format("yyyy-MM-dd HH:mm:ss") + "：  ";
                    temp += ("SUCCESS" == data)?('成功发送一条消息！'):data;
                    $("#console").prepend(temp);
                });
            });
        });

        // Format
        Date.prototype.format = function(fmt) {
            var o = {
                "M+" : this.getMonth()+1,                 //月份
                "d+" : this.getDate(),                    //日
                "H+" : this.getHours(),                   //小时
                "m+" : this.getMinutes(),                 //分
                "s+" : this.getSeconds(),                 //秒
                "q+" : Math.floor((this.getMonth()+3)/3), //季度
                "S"  : this.getMilliseconds()             //毫秒
            };
            if(/(y+)/.test(fmt))
                fmt=fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length));
            for(var k in o)
                if(new RegExp("("+ k +")").test(fmt))
                    fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length)));
            return fmt;
        }

    </script>

</body>