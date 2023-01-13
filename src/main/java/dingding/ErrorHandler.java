package dingding;

import agent.JavaAgent;
import com.dingtalk.chatbot.DingtalkChatbotClient;
import com.dingtalk.chatbot.message.MarkdownMessage;
import com.jedigames.logger.JLogger;
import com.jedigames.utils.ErrorMessage;
import config.Config;


public class ErrorHandler {
	private static DingtalkChatbotClient DINGTALK_CLIENT = null;
	private static String DING_TALK_HEADER = null;

	public static void init() {
		if (!Config.getConfig("AGENT_DINGDING_BUG").isEmpty()) {
			DINGTALK_CLIENT = new DingtalkChatbotClient();
			DING_TALK_HEADER = String.format("服务器热更错误: server_zone=%s, server_id=%d",
					Config.getConfig("SERVER_ZONE"), JavaAgent.serverid);
		}
	}

	private static void sendZentaoAndDingding(String msg, Throwable e) {
		String dingdingUrl = Config.getConfig("AGENT_DINGDING_BUG");
		if (!dingdingUrl.isEmpty()) {
			try {
			// 发送错误到dingding上
			if(DINGTALK_CLIENT == null) {
				return;
			}
			String msgDetail = ErrorMessage.getErrorDetail(e);
			// 检测是否存在
			MarkdownMessage dingdingMessage = new MarkdownMessage();
			dingdingMessage.setTitle("服务器发生错误");
			dingdingMessage.add(MarkdownMessage.getHeaderText(2, DING_TALK_HEADER));
			dingdingMessage.add(MarkdownMessage.getBoldText("错误信息:" + msg));
			dingdingMessage.add("\n\n");
			dingdingMessage
					.add(MarkdownMessage.getReferenceText(msgDetail));
				DINGTALK_CLIENT.send(dingdingUrl, dingdingMessage);
			} catch (Exception e1) {
				JLogger.error("发送钉钉错误日志时发生错误", e1);
			}
		}
	}

	public static void onCronErrorOccur(String msg, Throwable e) {
		sendZentaoAndDingding(msg, e);
	}

	public static void onErrorOccur(String msg, Throwable e) {
		sendZentaoAndDingding(msg, e);
	}
}
