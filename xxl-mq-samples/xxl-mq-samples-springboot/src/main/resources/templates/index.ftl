<script src="${request.contextPath}/static/jquery/jquery.min.js"></script>
<style>
    table {
        width: 80%;
        border-collapse: collapse;
        margin: 20px 0;
        font-family: Arial, sans-serif;
    }
    th, td {
        padding: 10px 15px;
        border: 1px solid #ccc;
        text-align: left;
    }
    th {
        background-color: #f4f4f4;
    }
    tr:hover {
        background-color: #f9f9f9;
    }
</style>
<body>

    <table>
        <tr>
            <td>Topic</td>
            <td>说明</td>
            <td>操作</td>
        </tr>
        <tr>
            <td>topic_sample</td>
            <td>全局并行消费：Topic 路由策略 = “轮询”</td>
            <td>
                <input type="button" class="send" _type="1" value="触发生产" />
            </td>
        </tr>
        <tr>
            <td>topic_sample_02</td>
            <td>全局串行消费：Topic 路由策略 = “第一个”</td>
            <td>
                <input type="button" class="send" _type="2" value="触发生产" />
            </td>
        </tr>
        <tr>
            <td>topic_sample_03</td>
            <td>串并行结合消费：Topic 路由策略 = “Hash”；且生产消息时，自定义消息bizId（作为分区hash参数） </td>
            <td>
                <input type="button" class="send" _type="3" value="触发生产" />
            </td>
        </tr>
        <tr>
            <td>topic_sample_04</td>
            <td>广播消费：Topic 路由策略 = “广播”</td>
            <td>
                <input type="button" class="send" _type="4" value="触发生产" />
            </td>
        </tr>
        <tr>
            <td>topic_sample_05</td>
            <td>延时消息：生产消息时，生效时间设置为 3min 后；</td>
            <td>
                <input type="button" class="send" _type="5" value="触发生产" />
            </td>
        </tr>
        <tr>
            <td>topic_sample_06</td>
            <td>失败重试消息：Topic 重试次数 = 3（自定义）；</td>
            <td>
                <input type="button" class="send" _type="6" value="触发生产" />
            </td>
        </tr>
        <tr>
            <td>topic_sample_07</td>
            <td>性能测试：批量发送消息，模拟生产 10000 条消息；</td>
            <td>
                <input type="button" class="send" _type="7" value="触发生产" />
            </td>
        </tr>
    </table>


    <hr>
    <div id="console"></div>

    <script>
        $(function(){
            $(".send").click(function () {
                var _type = $(this).attr("_type");
                $.post( '${request.contextPath}/produce', {'type':_type}, function(data,status){
                    var temp = "<br>" + new Date().format("yyyy-MM-dd HH:mm:ss") + "：  ";
                    temp += ("SUCCESS" == data)?('成功触发消息生产！'):data;
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