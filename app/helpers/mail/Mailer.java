package helpers.mail;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.*;
import play.Logger;
import play.Play;
import play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesSupport;
import play.exceptions.MailException;
import play.exceptions.UnexpectedException;
import play.i18n.Lang;
import play.libs.F;
import play.libs.F.T4;
import play.libs.Mail;
import play.libs.MimeTypes;
import play.vfs.VirtualFile;

import javax.activation.DataSource;
import javax.activation.URLDataSource;
import javax.mail.internet.InternetAddress;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Application mailer support.
 */
public class Mailer implements LocalVariablesSupport {

    public static final String INPUT_STREAM_ATTACHMENTS = "inputStreamAttachments";

    protected static ThreadLocal<HashMap<String, Object>> infos = new ThreadLocal<>();

    public Mailer() {
        infos.set(new java.util.HashMap());
    }

    /**
     * Set subject of mail, optionally providing formatting arguments
     * @param subject plain String or formatted string - interpreted as formatted string only if aguments are provided
     * @param args optional arguments for formatting subject
     */
    public static void setSubject(String subject, Object... args) {
        HashMap<String, Object> map = infos.get();
        if (map == null) {
            throw new UnexpectedException("Mailer not instrumented ?");
        }
	if(args.length != 0){
	    subject = String.format(subject, args);
	}
        map.put("subject", subject);
        infos.set(map);
    }

    @SuppressWarnings("unchecked")
    public static void addRecipient(Object... recipients) {
        HashMap<String, Object> map = infos.get();
        if (map == null) {
            throw new UnexpectedException("Mailer not instrumented ?");
        }
        List recipientsList = (List<String>) map.get("recipients");
        if (recipientsList == null) {
            recipientsList = new ArrayList<String>();
            map.put("recipients", recipientsList);
        }
        recipientsList.addAll(Arrays.asList(recipients));
        infos.set(map);
    }

    @SuppressWarnings("unchecked")
    public static void addBcc(Object... bccs) {
        HashMap<String, Object> map = infos.get();
        if (map == null) {
            throw new UnexpectedException("Mailer not instrumented ?");
        }
        List bccsList = (List<String>) map.get("bccs");
        if (bccsList == null) {
            bccsList = new ArrayList<String>();
            map.put("bccs", bccsList);
        }
        bccsList.addAll(Arrays.asList(bccs));
        infos.set(map);
    }

    @SuppressWarnings("unchecked")
    public static void addCc(Object... ccs) {
        HashMap<String, Object> map = infos.get();
        if (map == null) {
            throw new UnexpectedException("Mailer not instrumented ?");
        }
        List ccsList = (List<String>) map.get("ccs");
        if (ccsList == null) {
            ccsList = new ArrayList<String>();
            map.put("ccs", ccsList);
        }
        ccsList.addAll(Arrays.asList(ccs));
        infos.set(map);
    }

    public static void addAttachment(InputStreamAttachement... attachments) {
        HashMap<String, Object> map = infos.get();
        if (map == null) {
            throw new UnexpectedException("Mailer not instrumented ?");
        }
        List<InputStreamAttachement> attachmentsList = (List<InputStreamAttachement>) map.get(INPUT_STREAM_ATTACHMENTS);
        if (attachmentsList == null) {
            attachmentsList = new ArrayList<>();
            map.put(INPUT_STREAM_ATTACHMENTS, attachmentsList);
        }
        attachmentsList.addAll(Arrays.asList(attachments));
        infos.set(map);
    }

    @SuppressWarnings("unchecked")
    public static void addAttachment(EmailAttachment... attachments) {
        HashMap<String, Object> map = infos.get();
        if (map == null) {
            throw new UnexpectedException("Mailer not instrumented ?");
        }
        List<EmailAttachment> attachmentsList = (List<EmailAttachment>) map.get("attachments");
        if (attachmentsList == null) {
            attachmentsList = new ArrayList<EmailAttachment>();
            map.put("attachments", attachmentsList);
        }
        attachmentsList.addAll(Arrays.asList(attachments));
        infos.set(map);
    }

   @SuppressWarnings("unchecked")
   public static void attachDataSource(DataSource dataSource, String name, String description, String disposition) {
        HashMap<String, Object> map = infos.get();
        if (map == null) {
            throw new UnexpectedException("Mailer not instrumented ?");
        }
        List<T4<DataSource, String, String, String>> datasourceList = (List<T4<DataSource, String, String, String>>) map.get("datasources");
        if (datasourceList == null) {
            datasourceList = new ArrayList<T4<DataSource, String, String, String>>();
            map.put("datasources", datasourceList);
        }
        datasourceList.add(F.T4(dataSource, name, description, disposition));
        infos.set(map);
    }
    
    public static void attachDataSource(DataSource dataSource, String name, String description){
       attachDataSource(dataSource, name, description, EmailAttachment.ATTACHMENT);
    }
    
	public static String attachInlineEmbed(DataSource dataSource, String name) {
		HashMap<String, Object> map = infos.get();
		if (map == null) {
			throw new UnexpectedException("Mailer not instrumented ?");
		}
		
		InlineImage inlineImage = new InlineImage(dataSource);
		
		Map<String, InlineImage> inlineEmbeds = (Map<String, InlineImage>) map.get("inlineEmbeds");
		if (inlineEmbeds == null) {
			inlineEmbeds = new HashMap<String, InlineImage>();
			map.put("inlineEmbeds", inlineEmbeds);
		}
		
		inlineEmbeds.put(name, inlineImage);
		infos.set(map);
		
		return "cid:" + inlineImage.cid;
	}

    public static void setContentType(String contentType) {
        HashMap<String, Object> map = infos.get();
        if (map == null) {
            throw new UnexpectedException("Mailer not instrumented ?");
        }
        map.put("contentType", contentType);
        infos.set(map);
    }

    /**
     * Can be of the form xxx &lt;m@m.com&gt;
     *
     * @param from
     */
    public static void setFrom(Object from) {
        HashMap<String, Object> map = infos.get();
        if (map == null) {
            throw new UnexpectedException("Mailer not instrumented ?");
        }
        map.put("from", from);
        infos.set(map);
    }
    
    /**
     * Can be of the form xxx &lt;m@m.com&gt;
     *
     * @param locale
     */
    public static void setLocale(Object locale) {
        HashMap<String, Object> map = infos.get();
        if (map == null) {
            throw new UnexpectedException("Mailer not instrumented ?");
        }
        map.put("locale", locale);
        infos.set(map);
    }

    private static class InlineImage {
        /** content id */
        private final String cid;
        /** <code>DataSource</code> for the content */
        private final DataSource dataSource;

        public InlineImage(DataSource dataSource) {
        	this(null, dataSource);
        }

        public InlineImage(String cid, DataSource dataSource) {
            super();
            this.cid = cid != null ? cid : RandomStringUtils.randomAlphabetic(HtmlEmail.CID_LENGTH).toLowerCase();
            this.dataSource = dataSource;
        }

        public String getCid() {
            return this.cid;
        }

        public DataSource getDataSource() {
            return this.dataSource;
        }
    }
    
    private static class VirtualFileDataSource implements DataSource {
        private final VirtualFile virtualFile;

        public VirtualFileDataSource(VirtualFile virtualFile) {
            this.virtualFile = virtualFile;
        }

        public VirtualFileDataSource(String relativePath) {
            this.virtualFile = VirtualFile.fromRelativePath(relativePath);
        }

        @Override
        public String getContentType() {
            return MimeTypes.getContentType(this.virtualFile.getName());
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return this.virtualFile.inputstream();
        }

        @Override
        public String getName() {
            return this.virtualFile.getName();
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return this.virtualFile.outputstream();
        }

        public VirtualFile getVirtualFile() {
            return this.virtualFile;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof VirtualFileDataSource)) {
                return false;
            }

            VirtualFileDataSource rhs = (VirtualFileDataSource) obj;

            return this.virtualFile.equals(rhs.virtualFile);
        }
    }
    
    public static String getEmbedddedSrc(String urlString, String name) {
        HashMap<String, Object> map = infos.get();
        if (map == null) {
            throw new UnexpectedException("Mailer not instrumented ?");
        }
        
        DataSource dataSource = null;
        URL url = null;

        VirtualFile img = Play.getVirtualFile(urlString);
        if (img == null) {
            // Not a local image, check for a distant image
            try {
                url = new URL(urlString);
            } catch (MalformedURLException e1) {
                throw new UnexpectedException("Invalid URL '" + urlString + "'", e1);
            }

            if (name == null || name.isEmpty()) {
                String[] parts = url.getPath().split("/");
                name = parts[parts.length - 1];
            }

            if (StringUtils.isEmpty(name)) {
                throw new UnexpectedException("name cannot be null or empty");
            }

            dataSource = url.getProtocol().equals("file") ? new VirtualFileDataSource(
                    url.getFile()) : new URLDataSource(url);
        } else {
            dataSource = new VirtualFileDataSource(img);
        }

        Map<String, InlineImage> inlineEmbeds = (Map<String, InlineImage>) map
                .get("inlineEmbeds");

        // Check if a URLDataSource for this name has already been attached;
        // if so, return the cached CID value.
        if (inlineEmbeds != null && inlineEmbeds.containsKey(name)) {
            InlineImage ii = inlineEmbeds.get(name);

            if (ii.getDataSource() instanceof URLDataSource) {
                URLDataSource urlDataSource = (URLDataSource) ii
                        .getDataSource();
                // Make sure the supplied URL points to the same thing
                // as the one already associated with this name.
                // NOTE: Comparing URLs with URL.equals() is a blocking
                // operation
                // in the case of a network failure therefore we use
                // url.toExternalForm().equals() here.
                if (url == null || urlDataSource == null || !url.toExternalForm().equals(
                        urlDataSource.getURL().toExternalForm())) {
                    throw new UnexpectedException("embedded name '" + name
                            + "' is already bound to URL "
                            + urlDataSource.getURL()
                            + "; existing names cannot be rebound");
                }
            } else if (!ii.getDataSource().equals(dataSource)) {
                throw new UnexpectedException("embedded name '" + name
                        + "' is already bound to URL " + dataSource.getName()
                        + "; existing names cannot be rebound");
            }

            return "cid:" + ii.getCid();
        }

        // Verify that the data source is valid.
        InputStream is = null;
        try {
            is = dataSource.getInputStream();
        } catch (IOException e) {
            throw new UnexpectedException("Invalid URL", e);
        } finally {
            IOUtils.closeQuietly(is);
        }

        return attachInlineEmbed(dataSource, name);
    }

    /**
     * Can be of the form xxx &lt;m@m.com&gt;
     *
     * @param replyTo
     */
    public static void setReplyTo(Object replyTo) {
        HashMap<String, Object> map = infos.get();
        if (map == null) {
            throw new UnexpectedException("Mailer not instrumented ?");
        }
        map.put("replyTo", replyTo);
        infos.set(map);
    }

    public static void setCharset(String bodyCharset) {
        HashMap<String, Object> map = infos.get();
        if (map == null) {
            throw new UnexpectedException("Mailer not instrumented ?");
        }
        map.put("charset", bodyCharset);
        infos.set(map);
    }

    public static void setSubjectTemplate(String subjectTemplate) {
        HashMap<String, Object> map = infos.get();
        if (map == null) {
            throw new UnexpectedException("Mailer not instrumented ?");
        }
        map.put("subjectTemplate", subjectTemplate);
        infos.set(map);
    }

    public static void setBodyTemplate(String bodyTemplate) {
        HashMap<String, Object> map = infos.get();
        if (map == null) {
            throw new UnexpectedException("Mailer not instrumented ?");
        }
        map.put("bodyTemplate", bodyTemplate);
        infos.set(map);
    }

    @SuppressWarnings("unchecked")
    public static void addHeader(String key, String value) {
        HashMap<String, Object> map = infos.get();
        if (map == null) {
            throw new UnexpectedException("Mailer not instrumented ?");
        }
        HashMap<String, String> headers = (HashMap<String, String>) map.get("headers");
        if (headers == null) {
            headers = new HashMap<String, String>();
        }
        headers.put(key, value);
        map.put("headers", headers);
        infos.set(map);
    }

    @SuppressWarnings("unchecked")
    public static Future<Boolean> send(Map<String, Object> params) {
        try {
            final HashMap<String, Object> map = infos.get();
            if (map == null) {
                throw new UnexpectedException("Mailer not instrumented ?");
            }

            // Body character set
            final String charset = (String) infos.get().get("charset");

            // Headers
            final Map<String, String> headers = (Map<String, String>) infos.get().get("headers");

            // Subject
            String subject = (String) infos.get().get("subject");

            String bodyTemplate = (String) infos.get().get("bodyTemplate");

            Locale locale = (Locale) infos.get().get("locale");
            if (locale == null) {
                locale = Lang.getLocale();
            }

            // Subject template
            final String subjectTemplate = (String) infos.get().get("subjectTemplate");
            if (subjectTemplate != null) {
                subject = TemplateUtil.render(subjectTemplate, params, locale);
            }

            // Template rendering
            String body = TemplateUtil.render(bodyTemplate, params, locale);

            // Content type
            String contentType = (String) infos.get().get("contentType");
            if (contentType == null) {
                contentType = "text/html";
            }

            // Recipients
            final List<Object> recipientList = (List<Object>) infos.get().get("recipients");
            // From
            final Object from = infos.get().get("from");
            final Object replyTo = infos.get().get("replyTo");

            Email email;
            if (infos.get().get("attachments") == null && infos.get().get(INPUT_STREAM_ATTACHMENTS) == null && infos.get().get("datasources") == null && infos.get().get("inlineEmbeds") == null ) {
                if (contentType.equals("text/plain")) {
                    email = new SimpleEmail();
                    email.setMsg(body);
                } else {
                    HtmlEmail htmlEmail = new HtmlEmail();
                    htmlEmail.setHtmlMsg(body);
                    email = htmlEmail;
                }

            } else {
                if (contentType.equals("text/plain")) {
                    email = new MultiPartEmail();
                    email.setMsg(body);
                } else {
                    HtmlEmail htmlEmail = new HtmlEmail();
                    htmlEmail.setHtmlMsg(body);
                    email = htmlEmail;
                    
                    Map<String, InlineImage> inlineEmbeds = (Map<String, InlineImage>) infos.get().get("inlineEmbeds");
                    if (inlineEmbeds != null) {
                        for (Map.Entry<String, InlineImage> entry : inlineEmbeds.entrySet()) {
	                    	htmlEmail.embed(entry.getValue().getDataSource(), entry.getKey(), entry.getValue().getCid());
	                    }
                    }
                }
                
                MultiPartEmail multiPartEmail = (MultiPartEmail) email;
                List<EmailAttachment> objectList = (List<EmailAttachment>) infos.get().get("attachments");
                if (objectList != null) {
                    for (EmailAttachment object : objectList) {
                        multiPartEmail.attach(object);
                    }
                }

                List<InputStreamAttachement> inputStreamAttachements = (List<InputStreamAttachement>) infos.get().get(INPUT_STREAM_ATTACHMENTS);
                if (inputStreamAttachements != null) {
                    for (InputStreamAttachement attachement : inputStreamAttachements) {
                        // FIXME Close input stream?
                        try {
                            DataSource source = new ByteArrayDataSource(attachement.is, attachement.mimeType);
                            multiPartEmail.attach(source, attachement.fileName, attachement.description);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                // Handle DataSource
                List<T4<DataSource, String, String, String>> datasourceList = (List<T4<DataSource, String, String, String>>) infos
                        .get().get("datasources");
                if (datasourceList != null) {
                    for (T4<DataSource, String, String, String> ds : datasourceList) {
                        multiPartEmail.attach(ds._1, ds._2, ds._3, ds._4);
                    }
                }
            }
            email.setCharset("utf-8");

            if (from != null) {
                try {
                    InternetAddress iAddress = new InternetAddress(from.toString());
                    email.setFrom(iAddress.getAddress(), iAddress.getPersonal());
                } catch (Exception e) {
                    email.setFrom(from.toString());
                }

            }

            if (replyTo != null) {
                try {
                    InternetAddress iAddress = new InternetAddress(replyTo.toString());
                    email.addReplyTo(iAddress.getAddress(), iAddress.getPersonal());
                } catch (Exception e) {
                    email.addReplyTo(replyTo.toString());
                }

            }

            if (recipientList != null) {
                for (Object recipient : recipientList) {
                    try {
                        InternetAddress iAddress = new InternetAddress(recipient.toString());
                        email.addTo(iAddress.getAddress(), iAddress.getPersonal());
                    } catch (Exception e) {
                        email.addTo(recipient.toString());
                    }
                }
            } else {
                throw new MailException("You must specify at least one recipient.");
            }


            List<Object> ccsList = (List<Object>) infos.get().get("ccs");
            if (ccsList != null) {
                for (Object cc : ccsList) {
                    email.addCc(cc.toString());
                }
            }

            List<Object> bccsList = (List<Object>) infos.get().get("bccs");
            if (bccsList != null) {

                for (Object bcc : bccsList) {
                    try {
                        InternetAddress iAddress = new InternetAddress(bcc.toString());
                        email.addBcc(iAddress.getAddress(), iAddress.getPersonal());
                    } catch (Exception e) {
                        email.addBcc(bcc.toString());
                    }
                }
            }
            if (!StringUtils.isEmpty(charset)) {
                email.setCharset(charset);
            }

            email.setSubject(subject);
            email.updateContentType(contentType);

            if (headers != null) {
                for (String key : headers.keySet()) {
                    email.addHeader(key, headers.get(key));
                }
            }

            return Mail.send(email);
        } catch (EmailException ex) {
            throw new MailException("Cannot send email", ex);
        }
    }

    public static boolean sendAndWait(Map<String, Object> params) {
        try {
            Future<Boolean> result = send(params);
            return result.get();
        } catch (InterruptedException | ExecutionException e) {
            Logger.error(e, "Error while waiting Mail.send result");
        }
        return false;
    }
}
