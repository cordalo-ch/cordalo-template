package ch.cordalo.template.states;

import ch.cordalo.corda.common.test.CordaNodeEnvironment;
import ch.cordalo.template.CordaloTemplateBaseTests;
import net.corda.core.contracts.UniqueIdentifier;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.security.DigestException;

public class SignedDocumentStateTests extends CordaloTemplateBaseTests {

    @Before
    public void setup() {
        this.setup(false);
    }

    @After
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void testHash() throws DigestException {
        String test = "This is a test";
        String hash = SignedDocument.hashString(test);
        Assert.assertNotNull("hash is built and not null", hash);
        Assert.assertTrue("hash is not empty", !hash.isEmpty());
    }

    @Test
    public void testHash_size() throws DigestException {
        String test1 = "This is a test";
        String test2 = "This is a test.";
        String test3 = "!";
        String hash1 = SignedDocument.hashString(test1);
        String hash2 = SignedDocument.hashString(test2);
        String hash3 = SignedDocument.hashString(test3);
        Assert.assertNotEquals("hash not equals", hash1, hash2);
        Assert.assertEquals("hash size is the same", hash1.length(), hash2.length());
        Assert.assertEquals("hash size is the same", hash1.length(), hash3.length());
        Assert.assertEquals("hash size must be 64", 64, hash3.length());
    }

    public SignedDocument newDocument(CordaNodeEnvironment owner, String documentContent, String docRefId, String title) throws DigestException {
        return new SignedDocument(new UniqueIdentifier(), owner.party,
                SignedDocument.hashString(documentContent),
                docRefId,
                title);
    }

    public SignedDocument newDocument(CordaNodeEnvironment owner, byte[] documentBytes, String docRefId, String title) throws DigestException {
        return new SignedDocument(new UniqueIdentifier(), owner.party,
                SignedDocument.hashBytes(documentBytes),
                docRefId,
                title);
    }

    public SignedDocument newUpdateMetaDataDocument(SignedDocument document, String docRefId, String title, String mimeType, String url) throws DigestException {
        return document.updateMetaData(docRefId, title, mimeType, url);
    }

    public SignedDocument newUpdateContent(SignedDocument document, String content) throws DigestException {
        return document.updateContent(content);
    }

    public SignedDocument newUpdateContent(SignedDocument document, byte[] content) throws DigestException {
        return document.updateContent(content);
    }

    @Test
    public void test_create_minimal_document() throws DigestException {
        SignedDocument signedDocument1 = newTestSignedDocument("abcd-xyz-1234");
        Assert.assertEquals("document is equal to itself", signedDocument1, signedDocument1);

        SignedDocument signedDocument2 = newTestSignedDocument("abcd-xyz-1234");
        Assert.assertNotEquals("document are not the same due to ID", signedDocument1, signedDocument2);
    }

    @Test
    public void test_verify_proove_checksum() throws DigestException {
        SignedDocument signedDocument1 = newTestSignedDocument("abcd-xyz-1234");
        SignedDocument signedDocument2 = newTestSignedDocument("abcd-xyz-1234");
        Assert.assertTrue("document content are same", signedDocument1.prooveChecksum(signedDocument2.getChecksum()));
    }

    @Test
    public void test_verify_proove_document() throws DigestException {
        SignedDocument signedDocument1 = newTestSignedDocument("abcd-xyz-1234");
        SignedDocument signedDocument2 = newTestSignedDocument("abcd-xyz-1234");
        Assert.assertTrue("document content are same", signedDocument1.prooveDocument(signedDocument2));
    }

    @Test
    public void test_verify_proove_document_orginal() throws DigestException {
        SignedDocument signedDocument1 = newTestSignedDocument("abcd-xyz-1234");
        Assert.assertTrue("document content are same", signedDocument1.prooveDocument("abcd-xyz-1234"));
    }

    @Test
    public void test_equal_proove_str_bytes_content() throws DigestException {
        String content = "this is a content";
        SignedDocument signedDocument1 = newTestSignedDocument(content);
        SignedDocument signedDocument2 = this.newDocument(this.companyA, content.getBytes(), "T42-0815", "test1.docx");
        Assert.assertTrue("document are the same for string and bytes", signedDocument1.prooveChecksum(signedDocument2.getChecksum()));
    }


    @Test
    public void test_update_meta() throws DigestException {
        String content = "this is a content";
        SignedDocument signedDocument = newTestSignedDocument(content);
        Assert.assertNull("application mime type is null", signedDocument.getMimeType());
        SignedDocument updatedDocument = newUpdateMetaDataDocument(signedDocument,
                "T24-4711",
                "test2.pdf",
                "application/pdf",
                "https://anydocument.com/asdf234rfw234t2t24g2s");

        Assert.assertNotNull("updated document is not null", updatedDocument);
        Assert.assertEquals("application mime type is updated", "application/pdf", updatedDocument.getMimeType());
        Assert.assertEquals("title is updated", "test2.pdf", updatedDocument.getTitle());
        Assert.assertEquals("docRefId is updated", "T24-4711", updatedDocument.getReferenceId());
        Assert.assertEquals("url is updated", "https://anydocument.com/asdf234rfw234t2t24g2s", updatedDocument.getUrl());
    }


    @Test
    public void test_update_content() throws DigestException {
        String content = "this is a content";
        SignedDocument signedDocument = newTestSignedDocument(content);
        Assert.assertNull("application mime type is null", signedDocument.getMimeType());
        SignedDocument updatedDocument = signedDocument.updateContent("what a nice new text");

        Assert.assertNotNull("updated document is not null", updatedDocument);
        Assert.assertEquals("application mime type is updated", signedDocument.getMimeType(), updatedDocument.getMimeType());
        Assert.assertEquals("title is updated", signedDocument.getTitle(), updatedDocument.getTitle());
        Assert.assertEquals("docRefId is updated", signedDocument.getReferenceId(), updatedDocument.getReferenceId());
        Assert.assertEquals("url is updated", signedDocument.getUrl(), updatedDocument.getUrl());

        Assert.assertNotEquals("checksum is different", signedDocument.getChecksum(), updatedDocument.getChecksum());

    }


    @Test
    public void test_update_content_bytes() throws DigestException {
        String content = "this is a content";
        SignedDocument signedDocument = newTestSignedDocument(content);
        Assert.assertNull("application mime type is null", signedDocument.getMimeType());
        SignedDocument updatedDocument = signedDocument.updateContent("what a nice new text".getBytes());

        Assert.assertNotEquals("checksum is different", signedDocument.getChecksum(), updatedDocument.getChecksum());

    }

    private SignedDocument newTestSignedDocument(String content) throws DigestException {
        return this.newDocument(this.companyA, content, "T42-0815", "test1.docx");
    }

    @Test
    public void test_share_party() throws DigestException {
        String content = "this is a content";
        SignedDocument signedDocument = newTestSignedDocument(content);
        Assert.assertNull("application mime type is null", signedDocument.getMimeType());
        SignedDocument updatedDocument = signedDocument.updateContent("what a nice new text".getBytes());

        Assert.assertNotEquals("checksum is different", signedDocument.getChecksum(), updatedDocument.getChecksum());

    }

}