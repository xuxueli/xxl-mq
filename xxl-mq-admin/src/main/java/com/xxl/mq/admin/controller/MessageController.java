package com.xxl.mq.admin.controller;

import com.xxl.mq.admin.core.result.ReturnT;
import com.xxl.mq.admin.service.IXxlMqMessageService;
import com.xxl.mq.client.message.XxlMqMessage;
import com.xxl.mq.client.message.XxlMqMessageStatus;
import com.xxl.mq.client.util.DateFormatUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

/**
 * Base 
 * @author xuxueli 2016-3-19 13:56:28
 */
@Controller
@RequestMapping("/message")
public class MessageController {

	@Resource
	private IXxlMqMessageService xxlMqMessageService;

	@RequestMapping("")
	public String index(Model model, String topic){

		model.addAttribute("status", XxlMqMessageStatus.values());
		model.addAttribute("topic", topic);

		return "message/message.index";
	}
	
	@RequestMapping("/pageList")
	@ResponseBody
	public Map<String, Object> pageList(@RequestParam(required = false, defaultValue = "0") int start,
										@RequestParam(required = false, defaultValue = "10") int length,
										String topic,
										String status,
										String filterTime){

		// parse param
		Date addTimeStart = null;
		Date addTimeEnd = null;
		if (filterTime!=null && filterTime.trim().length()>0) {
			String[] temp = filterTime.split(" - ");
			if (temp!=null && temp.length == 2) {
				try {
					addTimeStart = DateFormatUtil.parseDateTime(temp[0]);
					addTimeEnd = DateFormatUtil.parseDateTime(temp[1]);
				} catch (ParseException e) {	}
			}
		}


		return xxlMqMessageService.pageList(start, length, topic, status, addTimeStart, addTimeEnd);
	}
	
	@RequestMapping("/delete")
	@ResponseBody
	public ReturnT<String> delete(int id){
		return xxlMqMessageService.delete(id);
	}

	@RequestMapping("/update")
	@ResponseBody
	public ReturnT<String> update(long id,
                                  String topic,
                                  String group,
                                  String data,
                                  String status,
                                  @RequestParam(required = false, defaultValue = "0") int retryCount,
                                  @RequestParam(required = false, defaultValue = "0") long shardingId,
                                  @RequestParam(required = false, defaultValue = "0") int timeout,
                                  String effectTime){

	    // effectTime
	    Date effectTimeObj = null;
        try {
            if (effectTime!=null && effectTime.trim().length()>0) {
                effectTimeObj = DateFormatUtil.parseDateTime(effectTime);
            }
        } catch (ParseException e) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "生效时间格式非法");
        }

        // message
        XxlMqMessage message = new XxlMqMessage();
        message.setId(id);
        message.setTopic(topic);
        message.setGroup(group);
        message.setData(data);
        message.setStatus(status);
        message.setRetryCount(retryCount);
        message.setShardingId(shardingId);
        message.setTimeout(timeout);
        message.setEffectTime(effectTimeObj);

		return xxlMqMessageService.update(message);
	}

	@RequestMapping("/add")
	@ResponseBody
	public ReturnT<String> add(String topic,
                               String group,
                               String data,
                               String status,
                               @RequestParam(required = false, defaultValue = "0") int retryCount,
                               @RequestParam(required = false, defaultValue = "0") long shardingId,
                               @RequestParam(required = false, defaultValue = "0") int timeout,
                               String effectTime){

        // effectTime
        Date effectTimeObj = null;
        try {
            if (effectTime!=null && effectTime.trim().length()>0) {
                effectTimeObj = DateFormatUtil.parseDateTime(effectTime);
            }
        } catch (ParseException e) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "生效时间格式非法");
        }

        // message
        XxlMqMessage message = new XxlMqMessage();
        message.setTopic(topic);
        message.setGroup(group);
        message.setData(data);
        message.setStatus(status);
        message.setRetryCount(retryCount);
        message.setShardingId(shardingId);
        message.setTimeout(timeout);
        message.setEffectTime(effectTimeObj);

		return xxlMqMessageService.add(message);
	}

    @RequestMapping("/clearMessage")
    @ResponseBody
    public ReturnT<String> clearMessage(String topic, String status, int type){
	    return xxlMqMessageService.clearMessage(topic, status, type);
    }

}
