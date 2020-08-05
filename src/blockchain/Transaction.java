package blockchain;

import java.io.Serializable;
import java.security.PublicKey;
import java.time.LocalDateTime;

public class Transaction implements Serializable {

    private static final long serialVersionUID = 13L;

    private final VCoin value;
    private final String senderId;
    private final String receiverId;
    private final LocalDateTime dateTime;
    private final long id;
    private final byte[] signature;
    private final PublicKey publicKey;

    public Transaction(VCoin value, String senderId, String receiverId, LocalDateTime dateTime,
                       long id, byte[] signature, PublicKey publicKey) {
        this.value = value;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.dateTime = dateTime;
        this.id = id;
        this.signature = signature;
        this.publicKey = publicKey;
    }

    public VCoin getValue() {
        return value;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public long getId() {
        return id;
    }

    public byte[] getSignature() {
        return signature;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    @Override
    public String toString() {
        return String.format("%s sent %s to %s", senderId, value, receiverId);
    }
}
