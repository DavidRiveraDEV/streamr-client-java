package com.streamr.client.utils

import com.streamr.client.protocol.message_layer.MessageID
import com.streamr.client.protocol.message_layer.StreamMessage
import com.streamr.client.protocol.message_layer.StreamMessageV30
import org.apache.commons.codec.binary.Hex
import org.ethereum.crypto.ECKey
import spock.lang.Specification

class SigningUtilSpec extends Specification {
    ECKey account
    String address
    SigningUtil signingUtil
    MessageID msgId

    void setup() {
        // The EthereumAuthenticationMethod accepts a private key with or without the '0x' prefix. It is removed if present to work with ECKey.fromPrivate.
        // Since we are testing an internal component (SigningUtil), the private key is without prefix.
        String withoutPrefix = "23bead9b499af21c4c16e4511b3b6b08c3e22e76e0591f5ab5ba8d4c3a5b1820"
        account = ECKey.fromPrivate(new BigInteger(withoutPrefix, 16))
        address = "0x" + Hex.encodeHexString(account.getAddress())
        signingUtil = new SigningUtil(account)
        msgId = new MessageID("streamId", 0, 425235315L, 0L, "publisherId", "msgChainId")
    }

    void "should correctly sign arbitrary data"() {
        String payload = "data-to-sign"
        when:
        String signature = SigningUtil.sign(payload, account)
        then:
        address.toLowerCase() == "0xa5374e3C19f15E1847881979Dd0C6C9ffe846BD5".toLowerCase()
        signature == "0x787cd72924153c88350e808de68b68c88030cbc34d053a5c696a5893d5e6fec1687c1b6205ec99aeb3375a81bf5cb8857ae39c1b55a41b32ed6399ae8da456a61b"
    }

    void "should correctly sign a StreamMessage"() {
        StreamMessage msg = new StreamMessageV30(msgId, null, StreamMessage.ContentType.CONTENT_TYPE_JSON, [foo: 'bar'], StreamMessage.SignatureType.SIGNATURE_TYPE_NONE, null)
        String expectedPayload = "streamId04252353150publisherIdmsgChainId"+'{"foo":"bar"}'
        when:
        signingUtil.signStreamMessage(msg)
        then:
        msg.signatureType == StreamMessage.SignatureType.SIGNATURE_TYPE_ETH
        msg.signature == SigningUtil.sign(expectedPayload, account)
    }

    void "returns false if no signature"() {
        when:
        StreamMessage msg = new StreamMessageV30(msgId, null, StreamMessage.ContentType.CONTENT_TYPE_JSON, [foo: 'bar'], StreamMessage.SignatureType.SIGNATURE_TYPE_NONE, null)
        then:
        !SigningUtil.hasValidSignature(msg, ["publisherId"].toSet())
    }

    void "returns false if wrong signature"() {
        when:
        StreamMessage msg = new StreamMessageV30(msgId, null, StreamMessage.ContentType.CONTENT_TYPE_JSON, [foo: 'bar'],
                StreamMessage.SignatureType.SIGNATURE_TYPE_ETH, "0x787cd72924153c88350e808de68b68c88030cbc34d053a5c696a5893d5e6fec1687c1b6205ec99aeb3375a81bf5cb8857ae39c1b55a41b32ed6399ae8da456a61b")
        then:
        !SigningUtil.hasValidSignature(msg, ["publisherId"].toSet())
    }

    void "returns true if correct signature"() {
        MessageID msgId = new MessageID("streamId", 0, 425235315L, 0L, address, "msgChainId")
        StreamMessage msg = new StreamMessageV30(msgId, null, StreamMessage.ContentType.CONTENT_TYPE_JSON, [foo: 'bar'],
                StreamMessage.SignatureType.SIGNATURE_TYPE_NONE, null)
        when:
        signingUtil.signStreamMessage(msg)
        then:
        SigningUtil.hasValidSignature(msg, [address].toSet())
    }

    void "returns false if correct signature but not from a trusted publisher"() {
        MessageID msgId = new MessageID("streamId", 0, 425235315L, 0L, address, "msgChainId")
        StreamMessage msg = new StreamMessageV30(msgId, null, StreamMessage.ContentType.CONTENT_TYPE_JSON, [foo: 'bar'],
                StreamMessage.SignatureType.SIGNATURE_TYPE_NONE, null)
        when:
        signingUtil.signStreamMessage(msg)
        then:
        !SigningUtil.hasValidSignature(msg, ["trustedPublisher"].toSet())
    }
}
