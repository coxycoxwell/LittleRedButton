package tr.edu.boun.swe599.littleredbutton.recipients;

public class Recipient {

	private String recipientName;
	private String recipientEmailAddress;
	private String recipientPhoneNumber;
		
	public Recipient() {}
	
	public Recipient(String name,String email,String phone)
	{
		super();
		this.recipientName = name;
		this.recipientEmailAddress = email;
		this.recipientPhoneNumber = phone;
	}
	
	public String getRecipientName() {
		return recipientName;
	}

	public void setRecipientName(String recipientName) {
		this.recipientName = recipientName;
	}

	public String getRecipientEmailAddress() {
		return recipientEmailAddress;
	}

	public void setRecipientEmailAddress(String recipientEmailAddress) {
		this.recipientEmailAddress = recipientEmailAddress;
	}

	public String getRecipientPhoneNumber() {
		return recipientPhoneNumber;
	}

	public void setRecipientPhoneNumber(String recipientPhoneNumber) {
		this.recipientPhoneNumber = recipientPhoneNumber;
	}

	@Override
    public String toString() {
        return "Recipient [recipientname=" + recipientName + ", recipientemailaddress=" + recipientEmailAddress 
        		+ ", recipientphonenumber=" + recipientPhoneNumber
                + "]";
    }
}
