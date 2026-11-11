package com.vehiclerental.notification;

/** A file that travels with an email (for example the PDF payment receipt). Immutable. */
public final class EmailAttachment {

    private final String filename;
    private final String contentType;
    private final byte[] content;

    public EmailAttachment(String filename, String contentType, byte[] content) {
        this.filename = filename;
        this.contentType = contentType;
        this.content = content == null ? new byte[0] : content.clone();
    }

    public static EmailAttachment pdf(String filename, byte[] content) {
        return new EmailAttachment(filename, "application/pdf", content);
    }

    public String getFilename() {
        return filename;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getContent() {
        return content.clone();
    }

    public int getSize() {
        return content.length;
    }
}
