package com.openxsl.config.dal;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.openxsl.config.autodetect.PrefixProps;
import com.openxsl.config.autodetect.PrefixPropsRegistrar;
import com.openxsl.config.exception.SkipRetryException;
import com.openxsl.config.filter.ListableTracingFilter;
import com.openxsl.config.filter.tracing.TracingCollector;
import com.openxsl.config.loader.PrefixProperties;
import com.openxsl.config.thread.tracing.TracingParam;
import com.openxsl.config.util.StringUtils;

/**
 * 发送邮件，可以发给多个人，可以发送文本或带附件，失败可以重发
 * @author xiongsl
 */
@SuppressWarnings("serial")
@Import({PrefixPropsRegistrar.class})
public class MailSender {
	public static final String MAIL_HOST = "mail.smtp.host";
	public static final String MAIL_PORT = "mail.smtp.port";
	public static final String MAIL_USERNAME = "mail.from.username";
	public static final String MAIL_PASSWORD = "mail.from.password";
	public static final String MAIL_TIMEOUT = "mail.smtp.timeout";
	public static final String MAIL_AUTH = "mail.smtps.auth";
	public static final String MAIL_FILEPATH = "mail.attachment.path";
	
	private static final ListableTracingFilter FILTERS = new ListableTracingFilter();
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final String DEFAULT = "_DEFAULT_";
	private final Map<String, JavaMailWrapper> MAIL_CENTER
						= new HashMap<String, JavaMailWrapper>();
	
	@Resource
	private FtpClientInvoker ftpClient;
	@Resource
	@PrefixProps(prefix="${spring.application.name}", regexp="(.*).mail.(.*)")
	private Properties mailProps;    //all smtp-servers
	
	static {
		FILTERS.load("mail");
	}
	
	public MailSender(){}
	//不方便注解Ioc
	public MailSender(Properties mailProps){
		this.setMailProps(mailProps, DEFAULT);
	}
	
	public void setMailProps(Properties mailProps, String registry){
		Properties props = new Properties();
		props.putAll(mailProps);
		JavaMailWrapper javaMail = new JavaMailWrapper();
		javaMail.setHost((String)props.remove(MAIL_HOST));
		
		int port = 25;
		try{
			port = Integer.parseInt((String)props.remove(MAIL_PORT));
		}catch(Exception e){}
		javaMail.setPort(port);
		String from = (String)props.remove(MAIL_USERNAME);
		javaMail.setFrom(from);
		String filePath = (String)props.remove(MAIL_FILEPATH);
		javaMail.setFilePath(filePath);
		javaMail.setUsername(from.substring(0, from.indexOf("@")));
		if (javaMail.getPassword() == null){
			javaMail.setPassword((String)props.remove(MAIL_PASSWORD));
		}
		javaMail.setJavaMailProperties(props);
		MAIL_CENTER.put(registry, javaMail);
	}
	public Properties getMailProps() {
		return mailProps;
	}
//	//密码不要放在配置文件里
//	public void setPassword(String password){
//		DEFAULT_SENDER.setPassword(password);
//	}
	
	public boolean sendWithAttach(DelayMail mail) {
		boolean simple = mail.isPlainMail();
		FILTERS.before("MailSender", simple?"sendSimpleMail":"sendMimeMail", mail);
		try {
			if (simple){
				sendSimpleMail(mail);
			}else{
				sendMxmineMail(mail);
			}
		} catch (Throwable e) {
			MailException me = null;
			if (e instanceof MailException) {
				me = (MailException) e;
			} else {
				me = new MailSendException("send mail error", e);;
			}
			TracingCollector.markError(e);
			FILTERS.after("false");
			throw me;
		}
		FILTERS.after("true");
		return true;
	}
	/**
	 * 立即发送一封邮件，返回是否成功，失败会丢到队列中去
	 * @param subject 标题
	 * @param content 内容
	 * @param mailTo 接收人（多个人以分号隔开）
	 * @param attachements 附件（相对路径）
	 * @param isHtml HTML格式
	 */
	public boolean sendWithAttach(String subject, String content, String mailTo, 
								String[] attachments, boolean isHtml) {
		DelayMail mail = new DelayMail(mailTo, subject, content);
		mail.setAttachements(attachments);
		mail.setIsHtml(isHtml);
		return this.sendWithAttach(mail);
	}
	
	private final void sendSimpleMail(DelayMail mail){
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(mail.getTo().split(";"));
		message.setSubject(mail.getSubject());
		message.setText(mail.getText());
		logger.info("开始发送邮件[{}]...., 主题：{}", mail.getTo(),mail.getSubject());
		this.getMailSender(mail.getRegistry()).send(message);
	}
	private final void sendMxmineMail(DelayMail mail){
		JavaMailWrapper mailSender = this.getMailSender(mail.getRegistry());
		MimeMessage mimeMessage = mailSender.createMimeMessage();
		try {
			MimeMessageHelper messageHelper =
					new MimeMessageHelper(mimeMessage,true,"UTF-8");
			messageHelper.setFrom(mailSender.getFrom());
			messageHelper.setTo(mail.getTo().split(";"));
			messageHelper.setSubject(mail.getSubject());
			messageHelper.setText(mail.getText(), mail.isHtml());
			//附件
			if (mail.getAttachments() != null){
				for (String file : mail.getAttachments()){
					AbstractResource resource = new FileSystemResource(mailSender.getFilePath()+file);
					if (!resource.exists()){
						resource = new FtpResource(ftpClient, file);
					}
//					if (isHtml){ //图片文件放前面
//						messageHelper.addInline("image"+(i++), resource);
//					}
					messageHelper.addAttachment(resource.getFilename(), resource);
				}
			}
		} catch (MessagingException me) {
			throw new MailPreparationException("Can't construct MimeMessage: ", me);
		}
		
		logger.info("开始发送邮件[{}]...., 主题：{}", mail.getTo(),mail.getSubject());
		mailSender.send(mimeMessage);  //MailException
	}
	
	private JavaMailWrapper getMailSender(String registry) {
		if (StringUtils.isEmpty(registry)) {
			if (!MAIL_CENTER.containsKey(DEFAULT)) {
				this.setMailProps(mailProps, DEFAULT);
			}
			return MAIL_CENTER.get(DEFAULT);
		}
		
		JavaMailWrapper mailSender = MAIL_CENTER.get(registry);
		if (mailSender == null) {
			Properties mailProps = new Properties();
			try {
				mailProps = PrefixProperties.prefixProperties(this.mailProps, registry, true);
			} catch (Exception e) {
				logger.warn("get {} mailProps error: ", registry,e);
			}
			if (mailProps.isEmpty()) {
				throw new SkipRetryException(String.format("邮件发送账号不存在(%s)", registry));
			}
			this.setMailProps(mailProps, registry);
			mailSender = MAIL_CENTER.get(registry);
		}
		return mailSender;
	}
	
	public static void main(String[] args) {
		String text = "2019!Fosun'$Pay";
		if (args.length > 0) {
			text = args[0];
		}
		text = com.openxsl.config.util.HexEncoder.encode(text);
		System.out.println(text);
	}
	
	public static class DelayMail extends TracingParam implements Serializable, Comparable<DelayMail>{
		private final String to, subject, text;
		private final long writeTime;
		private String[] attachments;
		private boolean isHtml;
		private String registry;
		private int fails;
		
		public DelayMail(String receiver, String subject, String text){
			super();
			this.to = receiver;
			this.subject = subject;
			this.text = text;
			this.writeTime = System.currentTimeMillis();
		}
		public void setAttachements(String[] attachments){
			this.attachments = attachments;
		}
		public void setIsHtml(boolean isHtml){
			this.isHtml = isHtml;
		}
		public boolean isPlainMail(){
			return (attachments==null||attachments.length==0) && !isHtml;
		}
		public void fail(){
			fails += 10;
		}
		public int getFails(){
			return fails;
		}
		
		@Override
		public int compareTo(DelayMail other) {
			if (other == null) {
				return -1;
			}
			return this.fails - other.fails;
		}
		
		@Override
		public String toString(){
			String dataStr = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm")
										.format(new Date(writeTime));
			return new StringBuilder() //"发件人：").append(MailSender.this.from)
					.append("\n收件人：").append(Arrays.asList(to.split("; ")))
					.append("\n日期：").append(dataStr)
					.append("\n标题：").append(subject)
					.append("\n邮件内容：").append(text)
					.append("\n附件数：").append(attachments==null?"0":attachments.length)
					.toString();
		}
		public String[] getAttachments() {
			return attachments;
		}
		public void setAttachments(String[] attachments) {
			this.attachments = attachments;
		}
		public boolean isHtml() {
			return isHtml;
		}
		public void setHtml(boolean isHtml) {
			this.isHtml = isHtml;
		}
		public long getWriteTime() {
			return writeTime;
		}
		public String getTo() {
			return to;
		}
		public String getSubject() {
			return subject;
		}
		public String getText() {
			return text;
		}
		public void setFails(int fails) {
			this.fails = fails;
		}
		public String getRegistry() {
			return registry;
		}
		public void setRegistry(String registry) {
			this.registry = registry;
		}
		
	}
	
	class JavaMailWrapper extends JavaMailSenderImpl{
		private String from;
		private String filePath;
		
		@Override
		public void send(SimpleMailMessage message) {
			message.setFrom(from);
			super.send(message);
		}

		public void setFrom(String from) {
			this.from = from;
		}
		public String getFrom() {
			return from;
		}
		public void setFilePath(String filePath) {
			this.filePath = filePath;
		}
		public String getFilePath() {
			return filePath;
		}
		
	}
	
	/**
	 * Ftp服务器上的资源（目录：mail_attach/）
	 * @author xiongsl
	 */
	class FtpResource extends AbstractResource{
		private String file;
		private FtpClientInvoker ftpClient;
		
		public FtpResource(FtpClientInvoker ftpClient, String file){
			this.ftpClient = ftpClient;
			this.file = file;
		}

		@Override
		public String getDescription() {
			return String.format("ftp://%s/%s", ftpClient.getRootPath(), file);
		}
		@Override
		public String getFilename(){
			return file;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return ftpClient.read("mail_attach", file);  //mail_attch目录下
		}
		
	}
	
}
