package socket4;

import java.io.Serializable;

public class Payload implements Serializable{
	private static final long serialVersionUID = -2204;
	public PayloadType payloadType;
	public String message;
	public int number;
	
	public Payload(PayloadType type, String message) {
		this.payloadType = type;
		this.message = message;
	}
	@Override
	public String toString() {
		return "Payload[payloadType: " + payloadType.toString() + ", message: " + message + ", number: " + number;
	}

}
