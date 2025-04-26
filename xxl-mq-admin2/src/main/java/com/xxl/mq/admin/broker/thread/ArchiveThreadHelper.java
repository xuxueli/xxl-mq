package com.xxl.mq.admin.broker.thread;

import com.xxl.mq.admin.broker.config.BrokerFactory;
import com.xxl.mq.admin.model.entity.Topic;
import com.xxl.mq.admin.util.I18nUtil;
import com.xxl.mq.admin.util.PropConfUtil;
import com.xxl.tool.concurrent.CyclicThread;
import com.xxl.tool.core.CollectionTool;
import com.xxl.tool.core.DateTool;
import com.xxl.tool.core.StringTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ArchiveThreadHelper
 *
 * @author xuxueli
 */
public class ArchiveThreadHelper {
    private static final Logger logger = LoggerFactory.getLogger(ArchiveThreadHelper.class);

    // ---------------------- init ----------------------

    private final BrokerFactory brokerFactory;
    public ArchiveThreadHelper(BrokerFactory brokerFactory) {
        this.brokerFactory = brokerFactory;
    }

    // ---------------------- start / stop ----------------------

    /**
     * start ArchiveThreadHelper (will stop with jvm)
     *
     * remark：
     *      1、archive message：          move "real-time" 2 "archive-message"
     *      2、refresh messge-report：    within 3 days
     *      3、alarm by topic：           by topic, detect new-fail-message
     *
     *      fail>0, by topic
     */
    public void start(){

        CyclicThread archiveThread = new CyclicThread("archiveThread", true, new Runnable() {
            @Override
            public void run() {
                // 1、move real-time 2 archive-message
                List<Topic> topicList = brokerFactory.getLocalCacheThreadHelper().findTopicAll();
                if (CollectionTool.isNotEmpty(topicList)) {
                    for (Topic topic : topicList) {
                        brokerFactory.getMessageService().archive(topic.getTopic(), topic.getArchiveStrategy(), 10000);
                    }
                }

                // 2、refresh daily message-report（within 3 days）       // TODO；real-time + archive data, generate info
                boolean competeResult = false;  // true, competed success 2 refresh；false sleep 2 next period
                // brokerFactory.getMessageReportMapper().


                // 3、alarm by email
                if (competeResult & CollectionTool.isNotEmpty(topicList)) {

                    // alarm by topic
                    for (Topic topic : topicList) {
                        Date dateFrom = DateTool.addDays(new Date(), -5);
                        Date dateTo = new Date();
                        int failCount = brokerFactory.getMessageMapper().queryFailCount(topic.getTopic(), dateFrom, dateTo);
                        if (failCount <= 0) {
                            continue;
                        }

                        // email cotent
                        String personal = I18nUtil.getString("admin_name_full");
                        String title = "【监控报警】" + personal;
                        String content = makeEmailConent(topic, dateFrom, dateTo, failCount);

                        Set<String> emailList = StringTool.isNotBlank(topic.getAlarmEmail())
                                ? Arrays.stream(topic.getAlarmEmail().split(",")).filter(StringTool::isNotBlank).collect(Collectors.toSet()) :
                                null;
                        if (CollectionTool.isNotEmpty(emailList)) {
                            for (String email : emailList) {

                                // make mail
                                try {
                                    MimeMessage mimeMessage = brokerFactory.getMailSender().createMimeMessage();

                                    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
                                    helper.setFrom(PropConfUtil.getSingle().getMailFrom(), personal);
                                    helper.setTo(email);
                                    helper.setSubject(title);
                                    helper.setText(content, true);

                                    brokerFactory.getMailSender().send(mimeMessage);
                                } catch (Exception e) {
                                    logger.error(">>>>>>>>>>> xxl-mq, fail alarm email send error, topic:{}", topic.getTopic(), e);
                                }

                            }
                        }
                    }
                }
            }
        }, 5 * 60 * 1000, true);
        archiveThread.start();
    }

    public void stop(){
        // do nothing
    }

    /**
     * makeEmailConent
     *
     * @return
     */
    private static String makeEmailConent(Topic topic, Date dateFrom, Date dateTo, int failCount){
        String mailBodyTemplate = "<h5>" + I18nUtil.getString("jobconf_monitor_detail") + "：</span>" +
                "<table border=\"1\" cellpadding=\"3\" style=\"border-collapse:collapse; width:80%;\" >\n" +
                "   <thead style=\"font-weight: bold;color: #ffffff;background-color: #ff8c00;\" >" +
                "      <tr>\n" +
                "         <td width=\"20%\" >"+ "Topic" +"</td>\n" +
                "         <td width=\"30%\" >"+ "主题描述" +"</td>\n" +
                "         <td width=\"10%\" >"+ "开始时间" +"</td>\n" +
                "         <td width=\"40%\" >"+ "结束时间" +"</td>\n" +
                "         <td width=\"40%\" >"+ "失败次数" +"</td>\n" +
                "      </tr>\n" +
                "   </thead>\n" +
                "   <tbody>\n" +
                "      <tr>\n" +
                "         <td>"+ topic.getTopic() +"</td>\n" +
                "         <td>"+ topic.getDesc() +"</td>\n" +
                "         <td>"+ DateTool.formatDateTime(dateFrom) +"</td>\n" +
                "         <td>"+ DateTool.formatDateTime(dateTo) +"</td>\n" +
                "         <td>"+ failCount +"</td>\n" +
                "      </tr>\n" +
                "   </tbody>\n" +
                "</table>";

        return mailBodyTemplate;
    }

}
