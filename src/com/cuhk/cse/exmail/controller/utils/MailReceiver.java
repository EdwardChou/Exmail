package com.cuhk.cse.exmail.controller.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.activation.CommandMap;
import javax.activation.DataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;

import android.util.Log;

import com.cuhk.cse.exmail.model.Attachment;
import com.cuhk.cse.exmail.model.app.MyApplication;

/**
 * 
 * @author EdwardChou edwardchou_gmail_com
 * @date 2015-4-8 PM2:47:56
 * @version V1.0
 * 
 */
public class MailReceiver implements Serializable {
	private static final long serialVersionUID = 1L;
	private MimeMessage mimeMessage = null;
	private StringBuffer mailContent = new StringBuffer();// mail content
	private StringBuffer mailContentPlainText = new StringBuffer();// mail
																	// content
	private String dataFormat = "yyyy-MM-dd HH:mm:ss";
	private String charset;
	private int uid;
	private boolean html;
	private ArrayList<Attachment> attachments = new ArrayList<Attachment>();

	public MailReceiver(MimeMessage mimeMessage) {
		fixProblem();
		this.mimeMessage = mimeMessage;
		try {
			// Log.i("charset",mimeMessage.getContentType());
			charset = parseCharset(mimeMessage.getContentType());
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	public MailReceiver(MimeMessage mimeMessage, int uid) {
		fixProblem();
		this.mimeMessage = mimeMessage;
		this.uid = uid;
		try {
			// Log.i("charset",mimeMessage.getContentType());
			charset = parseCharset(mimeMessage.getContentType());
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * retrieve sender's address
	 * 
	 * @throws Exception
	 * @return address
	 */
	public String getFrom() throws Exception {
		InternetAddress address[] = (InternetAddress[]) mimeMessage.getFrom();
		String addr = address[0].getAddress();
		if (addr == null) {
			addr = "Unknown";
		}
		return addr;
	}

	/**
	 * retrieve sender's name
	 * 
	 * @throws Exception
	 * @return name
	 */
	public String getSenderName() throws Exception {
		InternetAddress address[] = (InternetAddress[]) mimeMessage.getFrom();
		String name = address[0].getPersonal();
		if (name == null) {
			name = "Unknown";
		} else if (charset == null) {
			name = TranCharsetUtil.TranEncodeTOGB(name);
		}
		return name;
	}

	/**
	 * retrieve diverse types of receivers' addresses
	 * 
	 * @param type
	 * <br>
	 *            "TO"--ender "CC"--Carbon Copy "BCC"--Blind Carbon Copy
	 * @return String<br>
	 *         address
	 * @throws Exception
	 */

	public String getMailAddress(String type) throws Exception {
		String mailAddr = "";
		String addType = type.toUpperCase(Locale.CHINA);
		InternetAddress[] address = null;
		if (addType.equals("TO")) {
			address = (InternetAddress[]) mimeMessage
					.getRecipients(Message.RecipientType.TO);
		} else if (addType.equals("CC")) {
			address = (InternetAddress[]) mimeMessage
					.getRecipients(Message.RecipientType.CC);
		} else if (addType.equals("BCC")) {
			address = (InternetAddress[]) mimeMessage
					.getRecipients(Message.RecipientType.BCC);
		} else {
			System.out.println("error type!");
			throw new Exception("Error emailaddr type!");
		}
		if (address != null) {
			for (int i = 0; i < address.length; i++) {
				String mailaddress = address[i].getAddress();
				if (mailaddress != null) {
					mailaddress = MimeUtility.decodeText(mailaddress);
				} else {
					mailaddress = "";
				}
				String name = address[i].getPersonal();
				if (name != null) {
					name = MimeUtility.decodeText(name);
				} else {
					name = "";
				}
				mailAddr = name + "<" + mailaddress + ">";
			}
		}
		return mailAddr;
	}

	/**
	 * retrieve subject
	 * 
	 * @return String<br>
	 *         title
	 */
	public String getSubject() {
		String subject = "";
		try {
			subject = mimeMessage.getSubject();
			if (subject.indexOf("=?gb18030?") != -1) {
				subject = subject.replace("gb18030", "gb2312");
			}
			subject = MimeUtility.decodeText(subject);
			if (charset == null) {
				subject = TranCharsetUtil.TranEncodeTOGB(subject);
			}
		} catch (Exception e) {
		}
		return subject;
	}

	/**
	 * retrieve sent date
	 * 
	 * @return String<br>
	 *         yyyy-MM-dd HH:mm:ss
	 * @throws MessagingException
	 */
	public String getSentDate() throws MessagingException {
		Date sentdate = mimeMessage.getSentDate();
		if (sentdate != null) {
			SimpleDateFormat format = new SimpleDateFormat(dataFormat,
					Locale.CHINA);
			return format.format(sentdate);
		} else {
			return "Unknown";
		}
	}

	/**
	 * retrieve email content
	 * 
	 * @return String<br>
	 *         content
	 * @throws Exception
	 */
	public String[] getMailContent() throws Exception {
		String[] result = new String[2];
		compileMailContent((Part) mimeMessage);
		String warning = "This email has no text";
		String content = mailContent.toString();
		String plainText = mailContentPlainText.toString();
		if (plainText.length() >= 0) {
			result[0] = plainText;
		} else
			result[0] = warning;
		if (content.length() >= 0) {
			result[1] = content;
		} else
			result[1] = warning;
		if (content.indexOf("<html>") != -1) {
			html = true;
		}
		mailContentPlainText.setLength(0);
		mailContent.setLength(0);
		return result;
	}

	/**
	 * retrieve plain text content
	 * 
	 * @throws MessagingException
	 * @throws IOException
	 * @return String<br>
	 *         plain text
	 * @throws
	 */
	public String getPlainTextContent() throws MessagingException, IOException {
		boolean connName = false;
		Part part = (Part) mimeMessage;
		if (part.getContentType().indexOf("name") != -1) {
			connName = true;
		}
		if (part.isMimeType("text/plain") && !connName) {
			String content = parseInputStream((InputStream) part.getContent());
			mailContentPlainText.append(content);
		} else {
			mailContentPlainText.append("This Email has no plain text");
		}
		mailContentPlainText.setLength(0);
		return mailContentPlainText.toString();
	}

	/**
	 * set mail content
	 * 
	 * @param mailContent
	 */
	public void setMailContent(StringBuffer mailContent) {
		this.mailContent = mailContent;
	}

	/**
	 * whether has a receipt
	 * 
	 * @return boolean
	 * @throws MessagingException
	 */
	public boolean getReplySign() throws MessagingException {
		boolean replySign = false;
		String needreply[] = mimeMessage
				.getHeader("Disposition-Notification-To");
		if (needreply != null) {
			replySign = true;
		}
		return replySign;
	}

	/**
	 * retrieve「message-ID」
	 * 
	 * @return message-ID
	 * @throws MessagingException
	 */
	public String getMessageID() throws MessagingException {
		return mimeMessage.getMessageID();
	}

	/**
	 * whether is a unread email
	 * 
	 * @return boolean
	 * @throws MessagingException
	 */
	public boolean hasRead() throws MessagingException {
		boolean hasRead = false;
		Flags flags = ((Message) mimeMessage).getFlags();
		Flags.Flag[] flag = flags.getSystemFlags();
		for (int i = 0; i < flag.length; i++) {
			if (flag[i] == Flags.Flag.SEEN) {
				hasRead = true;
				break;
			}
		}
		return hasRead;
	}

	/**
	 * retrieve charset
	 * 
	 * @return charset
	 */
	public String getCharset() {
		return charset;
	}

	/**
	 * retrieve attachment list
	 * 
	 * @return ArrayList<Attachment>
	 */
	public ArrayList<Attachment> getAttachments() {
		return attachments;
	}

	/**
	 * whether content is HTML
	 * 
	 * @return boolean
	 */
	public boolean isHtml() {
		return html;
	}

	// /**
	// * 得到邮件正文内容 邮件的正文可能是多种类型:
	// * <ul>
	// * <li>text/plain</li>
	// * <li>text/html</li>
	// * <li>multipart/alternative</li>
	// * <li>multipart/related:内有内嵌的文件,噢噢~</li>
	// * <li>mutilpart/*</li>
	// * <li>message/rfc822</li>
	// * </ul>
	// *
	// * @param msg
	// * :待解析正文的邮件对象或邮件体(邮件的一部分)对象
	// * @author M.Liang Liu
	// * @version 1.0
	// * @since 1.6
	// * @return 根据邮件类型返回不同的邮件正文
	// */
	// private String parseMailContent(Part part) {
	// mailContent.append(new String(""));
	// try {
	// /**
	// * 纯文本或者html格式的,可以直接解析掉
	// */
	// if (part.isMimeType("text/plain") || part.isMimeType("text/html")) {
	// mailContent.append(part.getContent());
	// Log.i("MailReceiver", "plain/html");
	// } else if (part.isMimeType("multipart/*")) {
	// /**
	// * 可供选择的,一般情况下第一个是plain,第二个是html格式的
	// */
	// if (part.isMimeType("multipart/alternative")) {
	// Multipart mp = (Multipart) part.getContent();
	// int index = 0;// 兼容不正确的格式,返回第一个部分
	// if (mp.getCount() > 1)
	// index = 1;// 第2个部分为html格式的哦~
	// Log.i("MailReceiver",
	// "Now will choose the index(start from 0):" + index);
	// /**
	// * 已经根据情况进行了判断,就算不符合标准格式也不怕了.
	// */
	// Part tmp = mp.getBodyPart(index);
	// mailContent.append(tmp.getContent());
	// } else if (part.isMimeType("multipart/related")) {
	// Log.i("MailReceiver", "multipart/related");
	// /**
	// * related格式的,那么第一个部分包含了body,里面含有内嵌的内容的链接.
	// */
	// Multipart mp = (Multipart) part.getContent();
	// Part tmp = mp.getBodyPart(0);
	// String body = parseMailContent(tmp);
	// int count = mp.getCount();
	// /**
	// * 要把那些可能的内嵌对象都先读出来放在服务器上,然后在替换相对地址为绝对地址
	// */
	// for (int k = 1; count > 1 && k < count; k++) {
	// Part att = mp.getBodyPart(k);
	// String attname = att.getFileName();
	// attname = MimeUtility.decodeText(attname);
	// try {
	// Log.i("MailReceiver", "att:" + attname);
	// // File attFile = new File(
	// // Constants.tomcat_AttHome_Key,
	// // userName.concat(attname));
	// // FileOutputStream fileoutput = new
	// // FileOutputStream(
	// // attFile);
	// // InputStream is = att.getInputStream();
	// // BufferedOutputStream outs = new
	// // BufferedOutputStream(
	// // fileoutput);
	// // byte b[] = new byte[att.getSize()];
	// // is.read(b);
	// // outs.write(b);
	// // outs.close();
	// } catch (Exception e) {
	// Log.i("MailReceiver",
	// "Error occurred when to get the photos from server");
	// }
	// String Content_ID[] = att.getHeader("Content-ID");
	// if (Content_ID != null && Content_ID.length > 0) {
	// String cid_name = Content_ID[0].replaceAll("<", "")
	// .replaceAll(">", "");
	// // body = body.replaceAll("cid:" + cid_name,
	// // Constants.server_attHome_Key.concat("/")
	// // .concat(userName.concat(attname)));
	// }
	// }
	//
	// mailContent.append(body);
	// return mailContent.toString();
	// } else {
	// /**
	// * 其他multipart/*格式的如mixed格式,那么第一个部分包含了body,用递归解析第一个部分就可以了
	// */
	// Log.i("MailReceiver", "other multipart/*");
	// Multipart mp = (Multipart) part.getContent();
	// Part tmp = mp.getBodyPart(0);
	// return parseMailContent(tmp);
	// }
	// } else if (part.isMimeType("message/rfc822")) {
	// Log.i("MailReceiver", "rfc822");
	// return parseMailContent((Message) part.getContent());
	// } else {
	// /**
	// * 否则的话,死马当成活马医,直接解析第一部分,呜呜~
	// */
	// Object obj = part.getContent();
	// if (obj instanceof String) {
	// mailContent.append(obj);
	// } else {
	// Multipart mp = (Multipart) obj;
	// Part tmp = mp.getBodyPart(0);
	// return parseMailContent(tmp);
	// }
	// }
	// } catch (Exception e) {
	// return "解析正文错误!";
	// }
	// return mailContent.toString();
	// }

	/**
	 * parse content, cover diverse types:
	 * <ul>
	 * <li>text/plain</li>
	 * <li>text/html</li>
	 * <li>multipart/alternative</li>
	 * <li>multipart/related (include embedded files)</li>
	 * <li>mutilpart/*</li>
	 * <li>message/rfc822</li>
	 * </ul>
	 * 
	 * @param part
	 *            {@link javax.mail.Part}
	 * @throws Exception
	 */
	private void compileMailContent(Part part) throws Exception {
		mailContent.append("");
		mailContentPlainText.append("");
		String contentType = part.getContentType();
		boolean connName = false;
		if (contentType.indexOf("name") != -1) {
			connName = true;
		}
		if (part.isMimeType("text/plain") && !connName) {
			String content = parseInputStream((InputStream) part.getContent());
			mailContentPlainText.append(content);
		} else if (part.isMimeType("text/html") && !connName) {
			html = true;
			Object object = part.getContent();
			String content = "";
			if (object instanceof InputStream) {
				content = parseInputStream((InputStream) object);
			} else {
				content = (String) object;
			}
			mailContent.append(content);
		} else if (part.isMimeType("multipart/*")
				|| part.isMimeType("message/rfc822")) {

			// fault-tolerant mechanism, normally first part is plain text and
			// second part is html, return first part as html in case fault
			// happens
			if (part.isMimeType("multipart/alternative")) {
				Multipart mp = (Multipart) part.getContent();
				// Compatible with incorrect format and return first part as
				// html
				int index = 0;
				if (mp.getCount() > 1)
					index = 1;// second part is html
				Log.i("MailReceiver",
						"Now will choose the index(start from 0):" + index);
				html = true;

				Part tmp = mp.getBodyPart(index);
				mailContentPlainText.append(mp.getBodyPart(0).getContent());
				mailContent.append(tmp.getContent());
				checkAttachment(tmp);
			} else if (part.isMimeType("multipart/related")) {
				Log.i("MailReceiver", "multipart/related");
				// multipart/related, first part contains body, which has
				// embedded content links
				html = true;
				Multipart mp = (Multipart) part.getContent();
				Part tmp = mp.getBodyPart(0);
				checkAttachment(tmp);
				compileMailContent(tmp);
				int count = mp.getCount();

				for (int k = 1; count > 1 && k < count; k++) {
					Part att = mp.getBodyPart(k);
					checkAttachment(att);
					String attname = att.getFileName();
					attname = MimeUtility.decodeText(attname);
					try {
						Log.i("MailReceiver", "att:" + attname);
						InputStream inputStream = part.getInputStream();
						DataSource dataSource = new ByteArrayDataSource(
								inputStream, "multipart/*");
						Multipart multipart = new MimeMultipart(dataSource);
						int counts = multipart.getCount();
						for (int i = 0; i < counts; i++) {
							compileMailContent(multipart.getBodyPart(i));
						}
					} catch (Exception e) {
						Log.i("MailReceiver",
								"Error occurred when to get the photos from server");
					}
				}
			} else {
				/**
				 * other multipart/* formats like multipart/mixed, whose first
				 * part contains body,parse first part via recursive operation
				 */
				Log.i("MailReceiver", "other multipart/*");
				Multipart mp = (Multipart) part.getContent();
				Part tmp = mp.getBodyPart(0);
				for (int i = 0; i < mp.getCount(); i++)
					checkAttachment(mp.getBodyPart(i));
				compileMailContent(tmp);
			}
		} else {
			checkAttachment(part);
		}
	}

	private void checkAttachment(Part part) {
		try {
			if (part.getDisposition() != null
					&& part.getDisposition().equals(Part.ATTACHMENT)) {
				// retrieve attachment
				String filename = part.getFileName();
				if (filename != null) {
					if (filename.indexOf("=?gb18030?") != -1) {
						filename = filename.replace("gb18030", "gb2312");
					}
					filename = MimeUtility.decodeText(filename);

					Log.i("MailReceiver", "att:" + filename);
					Attachment attachment = new Attachment();
					attachment.setAddress(MyApplication.info.getUserName());
					attachment.setUid(uid);
					attachment.setDraftid(-1);
					attachment.setFilename(filename);
					attachment.setFilesize(part.getSize());
					attachment.setFiletype(filename.substring(filename
							.indexOf(".") + 1));
					attachment.setInputstream(inputStream2byte(part
							.getInputStream()));
					attachments.add(0, attachment);
				}
				// Log.e("content", "attachment name:" + filename);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void fixProblem() {
		// There is something wrong with MailCap, javamail can not find a
		// handler for the multipart/mixed part, so this bit needs to be added.
		MailcapCommandMap mc = (MailcapCommandMap) CommandMap
				.getDefaultCommandMap();
		mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
		mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
		mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
		mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
		mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
		CommandMap.setDefaultCommandMap(mc);
	}

	public byte[] inputStream2byte(InputStream inStream) throws IOException {
		ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
		byte[] buff = new byte[100];
		int rc = 0;
		while ((rc = inStream.read(buff, 0, 100)) > 0) {
			swapStream.write(buff, 0, rc);
		}
		byte[] in2b = swapStream.toByteArray();
		return in2b;
	}

	/**
	 * parse Charset
	 * 
	 * @param contentType
	 * @return utf-8, GBK, gb2312, others
	 */
	private String parseCharset(String contentType) {
		if (!contentType.contains("charset")) {
			return "utf-8";
		}
		if (contentType.contains("gbk")) {
			return "GBK";
		} else if (contentType.contains("GB2312")
				|| contentType.contains("gb18030")) {
			return "gb2312";
		} else {
			String sub = contentType.substring(
					contentType.indexOf("charset") + 8).replace("\"", "");
			if (sub.contains(";")) {
				return sub.substring(0, sub.indexOf(";"));
			} else {
				return sub;
			}
		}
	}

	/**
	 * parse InputStream
	 * 
	 * @param is
	 * @param contentType
	 * @return String
	 * @throws IOException
	 * @throws MessagingException
	 */
	private String parseInputStream(InputStream is) throws IOException,
			MessagingException {
		StringBuffer str = new StringBuffer();
		byte[] readByte = new byte[1024];
		int count;
		try {
			while ((count = is.read(readByte)) != -1) {
				if (charset == null) {
					// default charset utf-8
					str.append(new String(readByte, 0, count, "utf-8")); // "GBK"
				} else {
					str.append(new String(readByte, 0, count, charset));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return str.toString();
	}

}
