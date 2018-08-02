package android.electronicsoup.com.cinnamonbun;

/**
 * \class CanStatus
 *
 * \brief Class for the CAN Bus status
 *
 */
public class CanStatus {

	/*
	 * C Structure for CAN Status:
	 *
	 * typedef struct {
	 *     union {
	 *         struct {
	 *             u8 l2_status : 3;
	 *             u8 dcncp_initialised : 1;
	 *             u8 dcncp_l3_valid :1;
	 *             u8 l3_status : 1;
	 *         } bit_field;
	 *         u8 byte;
	 *     };
	 * } can_status_t;
	 */
	public static final byte L2_Uninitialised = 0x00;
	public static final byte L2_Listening     = 0x01;
	public static final byte L2_Connecting    = 0x02;
	public static final byte L2_Connected     = 0x03;
	public static final byte L2_ChangingBaud  = 0x04;

    public static final byte DCNCP_INIT_STATUS_MASK          = (byte)0x08;
    public static final byte DCNCP_NODE_ADDRESS_STATUS_MASK  = (byte)0x10;

    /**
	 * Baud rates:
     */
	public static final byte baud_10K   = (byte)0x00;
	public static final byte baud_20K   = (byte)0x01;
	public static final byte baud_50K   = (byte)0x02;
	public static final byte baud_125K  = (byte)0x03;
	public static final byte baud_250K  = (byte)0x04;
	public static final byte baud_500K  = (byte)0x05;
	public static final byte baud_800K  = (byte)0x06;
	public static final byte baud_1M    = (byte)0x07;
	public static final byte no_baud    = (byte)0x08;

    /* The MessageType for this message instance */
    private byte mCanStatus;
	private byte mBaudRate;

    /** 
     * \brief Constructor Creates new message of specified type
     * 
     */
    public CanStatus(byte status, byte baud) {
        mCanStatus = status;
		mBaudRate  = baud;
    }

	public byte getBaudRate() {
		return(mBaudRate);
	}

	public void setBaudRate(byte baud) {
		mBaudRate = baud;
	}

    public String getBaudRateString(){
		return(getBaudRateString(mBaudRate));
    }

    static public String getBaudRateString(byte baud_rate){
		switch(baud_rate) {
		case baud_10K: 
			return("baud_10k");
		case baud_20K: 
			return("baud_20k");
		case baud_50K: 
			return("baud_50k");
		case baud_125K:
			return("baud_125k");
		case baud_250K:
			return("baud_250k");
		case baud_500K:
			return("baud_500k");
		case baud_800K:
			return("baud_800k");
		case baud_1M:  
			return("baud_1M");
		case no_baud:  
			return("no_baud");
		}
		return("Bad Baud Rate");
    }

	public void setCanStatus(byte status) {
		mCanStatus = status;
	}

	public byte getCanL2Status() {
		return((byte)((byte)mCanStatus & (byte)0x07));
	}

	public String getCanL2StatusString() {
		byte l2Status = (byte)((byte)mCanStatus & (byte)0x07);

		if(l2Status == L2_Uninitialised) {
			return ("L2_Uninitialised");
		} else if(l2Status == L2_Listening) {
			return ("L2_Listening");
		} else if(l2Status == L2_Connecting) {
			return ("L2_Connecting");
		} else if(l2Status == L2_Connected) {
			return ("L2_Connected");
		} else if(l2Status == L2_ChangingBaud) {
			return ("L2_ChangingBaud");
		}
		return("Bad CAN L2 Status");
	}

	public boolean isDcncpInitialised() {
		byte initialised = ((byte)((byte)mCanStatus & DCNCP_INIT_STATUS_MASK));

		if(initialised != 0) {
			return(true);
		}
		return(false);
	}

	public boolean isNodeAddressValid() {
		byte valid = ((byte)((byte)mCanStatus & DCNCP_NODE_ADDRESS_STATUS_MASK));

		if(valid != 0) {
			return(true);
		}
		return(false);
	}
}
