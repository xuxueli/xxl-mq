/**
 * Created by xuxueli on 17/4/24.
 */
$(function () {

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
    }, function (start, end, label) {
        freshChartDate(start, end);
    });
    freshChartDate(rangesConf['最近一月'][0], rangesConf['最近一月'][1] );

    /**
     * fresh Chart Date
     *
     * @param startDate
     * @param endDate
     */
    function freshChartDate(startDate, endDate) {
        $.ajax({
            type : 'POST',
            url : base_url + '/chartInfo',
            data : {
                'startDate':startDate.format('YYYY-MM-DD HH:mm:ss'),
                'endDate':endDate.format('YYYY-MM-DD HH:mm:ss')
            },
            dataType : "json",
            success : function(data){
                if (data.code == 200) {
                    lineChartInit(data)
                    pieChartInit(data);
                } else {
                    layer.open({
                        title: '系统提示' ,
                        btn: [ '确定' ],
                        content: (data.msg || '运行报表数据加载异常' ),
                        icon: '2'
                    });
                }
            }
        });
    }

    /**
     * line Chart Init
     */
    function lineChartInit(data) {
        var option = {
               title: {
                   text: '日期分布图'
               },
               tooltip : {
                   trigger: 'axis',
                   axisPointer: {
                       type: 'cross',
                       label: {
                           backgroundColor: '#6a7985'
                       }
                   }
               },
               legend: {
                   data:['未消费', '消费中', '消费成功', '消费失败']
               },
               toolbox: {
                   feature: {
                       /*saveAsImage: {}*/
                   }
               },
               grid: {
                   left: '3%',
                   right: '4%',
                   bottom: '3%',
                   containLabel: true
               },
               xAxis : [
                   {
                       type : 'category',
                       boundaryGap : false,
                       data : data.data.messageDay_list
                   }
               ],
               yAxis : [
                   {
                       type : 'value'
                   }
               ],
               series : [
                   {
                       name:'未消费',
                       type:'line',
                       stack: 'Total',
                       areaStyle: {normal: {}},
                       data: data.data.newNum_list
                   }, {
                       name:'消费中',
                       type:'line',
                       stack: 'Total',
                       label: {
                           normal: {
                               show: true,
                               position: 'top'
                           }
                       },
                       areaStyle: {normal: {}},
                       data: data.data.ingNum_list
                   }, {
                       name:'消费成功',
                       type:'line',
                       stack: 'Total',
                       areaStyle: {normal: {}},
                       data: data.data.successNum_list
                   }, {
                       name:'消费失败',
                       type:'line',
                       stack: 'Total',
                       areaStyle: {normal: {}},
                       data: data.data.failNum_list
                   }
               ],
                color : [ '#BFBFBF', '#F39C12', '#00A65A', '#C23632' ]
        };

        var lineChart = echarts.init(document.getElementById('lineChart'));
        lineChart.setOption(option);
    }

    /**
     * pie Chart Init
     */
    function pieChartInit(data) {
        var option = {
            title : {
                text: '成功比例图' ,
                /*subtext: 'subtext',*/
                x:'center'
            },
            tooltip : {
                trigger: 'item',
                formatter: "{b} : {c} ({d}%)"
            },
            legend: {
                orient: 'vertical',
                left: 'left',
                data:['未消费', '消费中', '消费成功', '消费失败']
            },
            series : [
                {
                    //name: '分布比例',
                    type: 'pie',
                    radius : '55%',
                    center: ['50%', '60%'],
                    data:[
                        {
                            name:'未消费',
                            value:data.data.newNum_total
                        },
                        {
                            name:'消费中',
                            value:data.data.ingNum_total
                        },
                        {
                            name:'消费成功',
                            value:data.data.successNum_total
                        },
                        {
                            name:'消费失败',
                            value:data.data.failNum_total
                        }
                    ],
                    itemStyle: {
                        emphasis: {
                            shadowBlur: 10,
                            shadowOffsetX: 0,
                            shadowColor: 'rgba(0, 0, 0, 0.5)'
                        }
                    }
                }
            ],
            color : [ '#BFBFBF', '#F39C12', '#00A65A', '#C23632' ]
        };
        var pieChart = echarts.init(document.getElementById('pieChart'));
        pieChart.setOption(option);
    }

});
