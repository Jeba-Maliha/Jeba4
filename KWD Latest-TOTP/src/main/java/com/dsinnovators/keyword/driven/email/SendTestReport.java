package com.dsinnovators.keyword.driven.email;

import com.dsinnovators.keyword.driven.utils.Constants;
import com.dsinnovators.keyword.driven.utils.TestHelper;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;

import java.io.File;
import java.util.List;

public class SendTestReport {

    public void sendReport(String message, List<String> emailEligibleFilesName) throws EmailException {

        String authuser = TestHelper.getTestConfPropertyValue(Constants.REPORT_SENDER_EMAIL).trim();
        String authpwd = TestHelper.getTestConfPropertyValue(Constants.REPORT_SENDER_PASSWORD).trim();
        String mailName = TestHelper.getTestConfPropertyValue(Constants.REPORT_MAIL_NAME).trim();
        String defaultSender = TestHelper.getTestConfPropertyValue(Constants.DEFAULT_SENDER_EMAIL).trim();
        String mailSubject = TestHelper.getTestConfPropertyValue(Constants.REPORT_MAIL_SUBJECT).trim();
        String mailHost = TestHelper.getTestConfPropertyValue(Constants.REPORT_MAIL_HOST).trim();
        String smtpPort = TestHelper.getTestConfPropertyValue(Constants.REPORT_SMTP_PORT).trim();
        String smtpAuth = TestHelper.getTestConfPropertyValue(Constants.MAIL_SMTP_AUTH).trim();
        String mailDebug = TestHelper.getTestConfPropertyValue(Constants.MAIL_DEBUG).trim();
        String mailSmtpStarttlsEnable = TestHelper.getTestConfPropertyValue(Constants.MAIL_SMTP_STARTTLS_ENABLE).trim();



        try {
            File directory = new File(TestHelper.getTestConfPropertyValue(Constants.REPORT_FILE_PATH_KEY).trim());
            File[] files = directory.listFiles();
            File metaFileDirectory = new File(TestHelper.getTestConfPropertyValue(Constants.META_FILE_PATH).trim());
            File[] metaFiles = metaFileDirectory.listFiles();

            EmailAttachment attachment = new EmailAttachment();
            MultiPartEmail email = new MultiPartEmail();


            for (File file : files) {
                for (String subString : emailEligibleFilesName) {
                    if (file.getName().contains(subString)) {
                        attachment.setPath(TestHelper.getTestConfPropertyValue(Constants.REPORT_FILE_PATH_KEY).trim() + "/" + file.getName());
                        attachment.setDisposition(EmailAttachment.ATTACHMENT);
                        // attachment.setDescription("Automation Test Report");
                        attachment.setName(file.getName());
                        email.attach(attachment);
                    }
                }
            }

            for (File file : metaFiles) {
                attachment.setPath(TestHelper.getTestConfPropertyValue(Constants.META_FILE_PATH).trim() + "/" + file.getName());
                attachment.setDisposition(EmailAttachment.ATTACHMENT);
                //attachment.setDescription("Automation Test Report");
                attachment.setName(file.getName());
                email.attach(attachment);
            }

//            email.setSmtpPort(587);
//            email.setStartTLSEnabled(true);
//            email.setAuthenticator(new DefaultAuthenticator(authuser, authpwd));
//            email.setHostName("smtp.gmail.com");
//            email.getMailSession().getProperties().put("mail.smtp.auth", "true");
//            email.getMailSession().getProperties().put("mail.debug", "true");
//            email.getMailSession().getProperties().put("mail.smtp.starttls.enable", "true");
//            email.setFrom(authuser, "Mail from Automation Test Script");
//            email.setSubject("Automation Test Report");
//            email.setMsg(message);


            if (!TestHelper.isEmpty(authuser) && !TestHelper.isEmpty(authpwd)) {
                System.out.println("authU:" + authuser + " :pass:" + authpwd);

                email.setAuthenticator(new DefaultAuthenticator(authuser, authpwd));
                email.setFrom(authuser, mailName);
            } else {
                System.out.println("authUdefault:" + defaultSender + "mail name: " + mailName);
                email.setFrom(defaultSender, mailName);
            }

            if (!TestHelper.isEmpty(TestHelper.getTestConfPropertyValue(Constants.REPORT_CC_IDS))) {
                String[] ccIds = TestHelper.getTestConfPropertyValue(Constants.REPORT_CC_IDS).split(",");
                for (String temp : ccIds) {
                    System.out.println("ccIds:: " + temp);
                    email.addCc(temp);
                }
            }
            String[] receiverIds = TestHelper.getTestConfPropertyValue(Constants.REPORT_TO_ID).split(",");
            for (String temp : receiverIds) {
                System.out.println("toIds:: " + temp);
                email.addTo(temp);
            }

            email.setSmtpPort(Integer.parseInt(smtpPort));
            email.setStartTLSEnabled(mailSmtpStarttlsEnable.equals("true"));
            email.setHostName(mailHost);
            email.getMailSession().getProperties().put("mail.smtp.auth", smtpAuth);
            email.getMailSession().getProperties().put("mail.debug", mailDebug);
            email.getMailSession().getProperties().put("mail.smtp.starttls.enable", mailSmtpStarttlsEnable);
            email.setSubject(mailSubject);
            email.setMsg(message);

            email.send();
            System.out.println(">>>> email sent >>>>>");
        } catch (Exception e) {
            System.out.println("Exception while sending test report: " + e.getMessage());
//            e.printStackTrace();
        }
    }
}
