package com.cuhk.cse.exmail.utils;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import com.cuhk.cse.exmail.app.MyApplication;
import com.cuhk.cse.exmail.bean.Attachment;
import com.cuhk.cse.exmail.bean.MailInfo;
import com.cuhk.cse.exmail.bean.MyAuthenticator;

/**
 * Connection of mailbox, login and mail sending
 * 
 * @author EdwardChou edwardchou_gmail_com
 * @date 2014-10-4 下午4:38:39
 * @version V1.0
 * 
 */
public class HttpUtil {
	/**
	 * connect email
	 * 
	 * @param info
	 * @return
	 */
	public Session login() {
		// connect email server
		Session session = isLoginRight(MyApplication.info);
		return session;
	}

	/**
	 * login email
	 * 
	 * @param info
	 * @return
	 */
	private Session isLoginRight(MailInfo info) {
		// login authenticate
		MyAuthenticator authenticator = null;
		if (info.isValidate()) {
			// initialize password authenticator
			authenticator = new MyAuthenticator(info.getUserName(),
					info.getPassword());
		}
		// create a sending session according to session property and
		// authenticator
		Session sendMailSession = Session.getDefaultInstance(
				info.getProperties(), authenticator);
		// TODO set to true if need debug report
		// sendMailSession.setDebug(true);
		try {
			Transport transport = sendMailSession.getTransport("smtp");
			transport.connect(info.getMailServerHost(), info.getUserName(),
					info.getPassword());
		} catch (MessagingException e) {
			e.printStackTrace();
			return null;
		}
		return sendMailSession;
	}

	/**
	 * send pure text email
	 * 
	 * @param mailInfo
	 *            email info
	 */
	public boolean sendTextMail(MailInfo mailInfo, Session sendMailSession) {
		// decide whehter need an authenticator
		try {
			// create message according to session type
			Message mailMessage = new MimeMessage(sendMailSession);
			// create sender's address
			Address address = new InternetAddress(mailInfo.getFromAddress());
			// set the sender
			mailMessage.setFrom(address);
			// set the receivers' address
			Address[] tos = null;
			String[] receivers = mailInfo.getReceivers();
			if (receivers != null) {
				// create receivers' address
				tos = new InternetAddress[receivers.length];
				for (int i = 0; i < receivers.length; i++) {
					tos[i] = new InternetAddress(receivers[i]);
				}
			} else {
				return false;
			}
			// Message.RecipientType.TO property of receiver is TO
			mailMessage.setRecipients(Message.RecipientType.TO, tos);
			// email subject
			mailMessage.setSubject(mailInfo.getSubject());
			// email sent date
			mailMessage.setSentDate(new Date());
			// email content
			String mailContent = mailInfo.getContent();

			// create MimeMultipart objects to hold BodyPart objects
			Multipart mm = new MimeMultipart();
			// setting email text
			// create BodyPart object to store content
			BodyPart mdp = new MimeBodyPart();
			// format/charset for BodyPart object
			mdp.setContent(mailContent, "text/html;charset=gb2312");
			// add BodyPart to MimeMultipart
			mm.addBodyPart(mdp);

			Attachment affInfos;
			FileDataSource fds1;
			List<Attachment> list = mailInfo.getAttachmentInfos();
			for (int i = 0; i < list.size(); i++) {
				affInfos = list.get(i);
				fds1 = new FileDataSource(affInfos.getFilepath());
				mdp = new MimeBodyPart();
				mdp.setDataHandler(new DataHandler(fds1));
				try {
					mdp.setFileName(MimeUtility.encodeText(fds1.getName()));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				mm.addBodyPart(mdp);
			}
			mailMessage.setContent(mm);
			mailMessage.saveChanges();

			// add mutiply support format
			MailcapCommandMap mc = (MailcapCommandMap) CommandMap
					.getDefaultCommandMap();
			mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
			mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
			mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
			mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
			mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
			CommandMap.setDefaultCommandMap(mc);

			// send email
			Transport.send(mailMessage);
			return true;
		} catch (MessagingException ex) {
			ex.printStackTrace();
		}
		return false;
	}
}
