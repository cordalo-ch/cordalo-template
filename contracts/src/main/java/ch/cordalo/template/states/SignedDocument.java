package ch.cordalo.template.states;

import ch.cordalo.corda.common.states.CordaloLinearState;
import ch.cordalo.corda.common.states.Parties;
import ch.cordalo.template.contracts.SignedDocumentContract;
import com.fasterxml.jackson.annotation.JsonIgnore;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import net.corda.core.serialization.CordaSerializable;
import org.jetbrains.annotations.NotNull;

import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;


@CordaSerializable
@BelongsToContract(SignedDocumentContract.class)
public class SignedDocument extends CordaloLinearState {

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static final Set<Character> HEX_CHARS;

    static {
        HEX_CHARS = new HashSet<>();
        for (char c : HEX_ARRAY) {
            HEX_CHARS.add(c);
        }
    }

    @NotNull
    @JsonIgnore
    Party owner;
    @NotNull
    String checksum;
    @NotNull
    String referenceId;
    @NotNull
    String title;
    String mimeType;
    String url;
    @NotNull
    @JsonIgnore
    List<Party> sharedParties;

    @ConstructorForDeserialization
    public SignedDocument(UniqueIdentifier linearId, @NotNull Party owner, @NotNull String checksum, @NotNull String referenceId, @NotNull String title, String mimeType, String url, List<Party> sharedParties) {
        super(linearId);
        this.owner = owner;
        this.checksum = checksum;
        this.referenceId = referenceId;
        this.mimeType = mimeType;
        this.title = title;
        this.url = url;
        this.sharedParties = sharedParties;
    }

    public SignedDocument(UniqueIdentifier linearId, @NotNull Party owner, @NotNull String checksum, @NotNull String referenceId, @NotNull String title) {
        this(linearId, owner, checksum, referenceId, title, null, null, new ArrayList<>());
    }

    /* static features for hashing */

    public static String hashString(String content) throws DigestException {
        return hashBytes(content.getBytes());
    }

    public static String hashBytes(byte[] content) throws DigestException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(content);
            return bytesToHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new DigestException("couldn't make digest of content");
        }
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static boolean isValidHash(String hash) {
        if (hash == null || hash.length() != 64) {
            return false;
        }
        int len = hash.length();
        for (int j = 0; j < len; j++) {
            if (!HEX_CHARS.contains(hash.charAt(j))) return false;
        }
        return true;
    }

    /* END static features for hashing */

    @NotNull
    public String getChecksum() {
        return checksum;
    }

    @NotNull
    public String getReferenceId() {
        return referenceId;
    }

    @NotNull
    public String getTitle() {
        return title;
    }

    @NotNull
    public Party getOwner() {
        return owner;
    }

    @NotNull
    public List<Party> getSharedParties() {
        return sharedParties;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getUrl() {
        return url;
    }

    @NotNull
    @Override
    protected Parties getParties() {
        return Parties.fromParties(this.owner).add(this.sharedParties);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SignedDocument)) return false;
        if (!super.equals(o)) return false;
        SignedDocument that = (SignedDocument) o;
        return getOwner().equals(that.getOwner()) &&
                getChecksum().equals(that.getChecksum()) &&
                getReferenceId().equals(that.getReferenceId()) &&
                getTitle().equals(that.getTitle()) &&
                Objects.equals(getMimeType(), that.getMimeType()) &&
                Objects.equals(getUrl(), that.getUrl()) &&
                getSharedParties().equals(that.getSharedParties());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getOwner(), getChecksum(), getReferenceId(), getMimeType(), getUrl(), getSharedParties());
    }

    public boolean prooveChecksum(String otherChecksum) {
        return
                isValidHash(this.getChecksum()) && isValidHash(otherChecksum) && this.getChecksum().equals(otherChecksum);
    }

    public boolean prooveDocument(SignedDocument document) {
        return this.prooveChecksum(document.getChecksum());
    }

    public boolean prooveDocument(String content) throws DigestException {
        return this.prooveChecksum(
                hashString(content));
    }

    public SignedDocument updateMetaData(@NotNull String referenceId, @NotNull String title, String mimeType, String url) {
        return new SignedDocument(this.linearId, this.owner, this.checksum,
                referenceId, title, mimeType, url, this.sharedParties);
    }

    public SignedDocument updateContent(String content) throws DigestException {
        return new SignedDocument(this.linearId, this.owner, hashString(content),
                this.referenceId, this.title, this.mimeType, this.url, this.sharedParties);
    }

    public SignedDocument updateContent(byte[] content) throws DigestException {
        return new SignedDocument(this.linearId, this.owner, hashBytes(content),
                this.referenceId, this.title, this.mimeType, this.url, this.sharedParties);
    }

    public SignedDocument share(Party party) throws DigestException {
        ArrayList<Party> newParties = new ArrayList<>(this.sharedParties);
        newParties.add(party);
        return new SignedDocument(this.linearId, this.owner, this.checksum,
                this.referenceId, this.title, this.mimeType, this.url, newParties);
    }

}
