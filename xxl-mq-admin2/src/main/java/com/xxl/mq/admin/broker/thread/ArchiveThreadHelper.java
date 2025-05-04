package com.xxl.mq.admin.broker.thread;

import com.xxl.mq.admin.broker.config.BrokerBootstrap;
import com.xxl.mq.admin.model.entity.MessageReport;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * ArchiveThreadHelper
 *
 * @author xuxueli
 */
public class ArchiveThreadHelper {
    private static final Logger logger = LoggerFactory.getLogger(ArchiveThreadHelper.class);

    // ---------------------- init ----------------------

    private final BrokerBootstrap brokerBootstrap;
    public ArchiveThreadHelper(BrokerBootstrap brokerBootstrap) {
        this.brokerBootstrap = brokerBootstrap;
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
                List<Topic> topicList = brokerBootstrap.getLocalCacheThreadHelper().findTopicAll();
                if (CollectionTool.isNotEmpty(topicList)) {
                    for (Topic topic : topicList) {
                        brokerBootstrap.getMessageService().archive(topic.getTopic(), topic.getArchiveStrategy(), 10000);
                    }
                }

                // 2、refresh daily message-report（within 3 days）
                Date startOfToday = DateTool.setStartOfDay(new Date());
                Date dateFrom = DateTool.addDays(startOfToday, -2);
                Date dateTo = DateTool.addDays(startOfToday, 1);
                List<MessageReport> messageReportList = brokerBootstrap.getMessageMapper().queryReport(dateFrom, dateTo);
                List<MessageReport> messageReportList2 = brokerBootstrap.getMessageArchiveMapper().queryReport(dateFrom, dateTo);

                Map<Date, MessageReport> reportMap = new HashMap<>();
                for (MessageReport report : messageReportList) {
                    MessageReport existingReport = reportMap.get(report.getProduceDay());
                    if (existingReport == null) {
                        reportMap.put(report.getProduceDay(), report);
                    } else {
                        existingReport.setNewCount(existingReport.getNewCount() + report.getNewCount());
                        existingReport.setRunningCount(existingReport.getRunningCount() + report.getRunningCount());
                        existingReport.setSucCount(existingReport.getSucCount() + report.getSucCount());
                        existingReport.setFailCount(existingReport.getFailCount() + report.getFailCount());
                    }
                }
                for (MessageReport report : messageReportList2) {
                    MessageReport existingReport = reportMap.get(report.getProduceDay());
                    if (existingReport == null) {
                        reportMap.put(report.getProduceDay(), report);
                    } else {
                        existingReport.setNewCount(existingReport.getNewCount() + report.getNewCount());
                        existingReport.setRunningCount(existingReport.getRunningCount() + report.getRunningCount());
                        existingReport.setSucCount(existingReport.getSucCount() + report.getSucCount());
                        existingReport.setFailCount(existingReport.getFailCount() + report.getFailCount());
                    }
                }

                // write report
                if (CollectionTool.isNotEmpty(reportMap.values())) {
                    for (MessageReport messageReport : reportMap.values()) {
                        messageReport.setUpdateTime(new Date());
                        brokerBootstrap.getMessageReportMapper().insert(messageReport);
                    }
                }

                // 3、alarm by topic
                boolean clusterCompeteSuccess = true;  // todo，avoid concurrent competition in the cluster
                if (clusterCompeteSuccess & CollectionTool.isNotEmpty(topicList)) {
                    for (Topic topic : topicList) {
                        // email alarm
                        Set<String> emailList = StringTool.isNotBlank(topic.getAlarmEmail())
                                ? Arrays.stream(topic.getAlarmEmail().split(",")).filter(StringTool::isNotBlank).collect(Collectors.toSet()) :
                                null;
                        if (emailList == null) {
                            continue;
                        }

                        // filter fail count
                        Date failFrom = DateTool.addMinutes(new Date(), -5);
                        Date failTo = new Date();
                        int failCount = brokerBootstrap.getMessageMapper().queryFailCount(topic.getTopic(), failFrom, failTo);
                        if (failCount <= 0) {
                            continue;
                        }

                        // email cotent
                        String title = "【监控报警】" + I18nUtil.getString("admin_name_full");
                        String content = makeEmailConent(topic, failFrom, failTo, failCount);

                        // send mail
                        for (String email : emailList) {
                            try {
                                MimeMessage mimeMessage = brokerBootstrap.getMailSender().createMimeMessage();

                                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
                                helper.setFrom(PropConfUtil.getSingle().getMailUsername());
                                helper.setTo(email);
                                helper.setSubject(title);
                                helper.setText(content, true);

                                brokerBootstrap.getMailSender().send(mimeMessage);
                            } catch (Exception e) {
                                logger.error(">>>>>>>>>>> xxl-mq, fail alarm email send error, topic:{}", topic.getTopic(), e);
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
    private static String makeEmailConent(Topic topic, Date dateFrom, Date dateTo, long failCount){
        String mailBodyTemplate = "<h5> 监控告警明细：</span>" +
                "<table border=\"1\" cellpadding=\"3\" style=\"border-collapse:collapse; width:80%;\" >\n" +
                "   <thead style=\"font-weight: bold;color: #ffffff;background-color: #ff8c00;\" >" +
                "      <tr>\n" +
                "         <td width=\"20%\" >"+ "Topic" +"</td>\n" +
                "         <td width=\"25%\" >"+ "主题描述" +"</td>\n" +
                "         <td width=\"20%\" >"+ "告警时间" +"</td>\n" +
                "         <td width=\"15%\" >"+ "告警标题" +"</td>\n" +
                "         <td width=\"20%\" >"+ "告警内容" +"</td>\n" +
                "      </tr>\n" +
                "   </thead>\n" +
                "   <tbody>\n" +
                "      <tr>\n" +
                "         <td>"+ topic.getTopic() +"</td>\n" +
                "         <td>"+ topic.getDesc() +"</td>\n" +
                "         <td>"+ DateTool.formatDateTime(dateFrom) +" ~ \n"+DateTool.formatDateTime(dateTo) +"</td>\n" +
                "         <td>消费失败</td>\n" +
                "         <td>失败次数："+ failCount +"</td>\n" +
                "      </tr>\n" +
                "   </tbody>\n" +
                "</table>";

        return mailBodyTemplate;
    }

}
