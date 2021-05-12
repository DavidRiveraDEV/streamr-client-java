package com.streamr.client.protocol.message_layer


import spock.lang.Specification

class GroupKeyResponseAdapterSpec extends Specification {
	static final EncryptedGroupKey key1 = new EncryptedGroupKey("groupKeyId1", "encrypted1", null)
	static final EncryptedGroupKey key2 = new EncryptedGroupKey("groupKeyId2", "encrypted2", null)

	def "serialization and deserialization"(String serializedMessage, GroupKeyResponse message) {
		expect:
		AbstractGroupKeyMessage.deserialize(serializedMessage, StreamMessage.MessageType.GROUP_KEY_RESPONSE) == message
		message.serialize() == serializedMessage

		where:
		serializedMessage | message
		'["requestId","streamId",[["groupKeyId1","'+key1.encryptedGroupKeyHex+'"],["groupKeyId2","'+key2.encryptedGroupKeyHex+'"]]]' | new GroupKeyResponse("requestId", "streamId", [key1, key2])
	}

}
