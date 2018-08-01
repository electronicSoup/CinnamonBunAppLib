package android.electronicsoup.com.cinnamonbun;

import android.util.Log;

/**
 * \class CanL2Frame
 *
 * \brief Class representing a CAN Layer 2 Message.
 *
 * 
 */
public class CanL2Frame
{
    private String TAG = "ES-CanL2Frame";

    /*
     * special address description flags for the CAN_ID
	 * 
	 * SFF - Standard Frame Format
	 * EFF - Extended Frame Format
	 */
	public static final int CAN_EFF_FLAG = 0x80000000; /* EFF/SFF is set in the MSB */
	public static final int CAN_RTR_FLAG = 0x40000000; /* remote transmission request */
	public static final int CAN_ERR_FLAG = 0x20000000; /* error message frame */

    /*
	 * valid bits in CAN ID for frame formats 
	 */
	public static final int CAN_SFF_MASK = 0x000007FF; /* standard frame format (SFF) */
	public static final int CAN_EFF_MASK = 0x1FFFFFFF; /* extended frame format (EFF) */
	public static final int CAN_ERR_MASK = 0x1FFFFFFF; /* omit EFF, RTR, ERR flags */

    
    private int     mCanId;
    private byte    mSize;
    private byte[]  mData;

    /**
     * \brief Constructor for Layer 2 Frame in byte array
     *
     * \param[in] canMsg : (CanL2Frame)
     *
     * The Android Device receives the Layer 2 CAN Messages from the Dongle over the USB Serial Bus
     * so messages are received in a serial fashion as an Array of bytes. This constructor
     * is used by the CanDongle Class to convert the stream of bytes received into a 
     * CanL2Frame Object to pass to the Application.
     */
    public CanL2Frame(byte[] frame) {
        int loop;
        int id = 0x0;
		int size;

		Log.d(TAG, "CanL2Frame() constructor with byte[]");
//		for(loop = 0; loop < frame.length; loop++) {
//			Log.d(TAG, "frame[" + Integer.toString(loop) + "]:" + CinnamonBun.byteToString(frame[loop]));
//		}

		id = 0x00;
		for(loop = 0; loop < 4; loop++){
			id = (id << 8) | ((int)frame[loop] & 0xff);
		}

		this.mCanId = id;
		this.mSize = (byte)(frame[4] & (byte)0x0f);

		mData = new byte[this.mSize];

		for(loop = 0; loop < this.mSize; loop++) {
			this.mData[loop] = frame[loop + 5];
		}
    }

    /**
     * \brief Constructor for Layer 2 Message with CAN Identifier in a byte array
     *
     * \param[in] idBytes : (byte[])
     *
     * \param[in] extended : (boolean)
     *
     * \param[in] rnr : (boolean)
     *
     * \param[in] data : (byte[])
     *
     * \param[in] size : (byte)
     *
     * This constructor is mainly used by the CAN Layer 3 Protocol where the CAN Identifier
     * is made up of source and destination address bytes of the Layer 3 Message.
     */
//    public CanL2Frame(byte[] idBytes, boolean extended, boolean rnr, byte[] data, byte size) throws CinnamonBunException {
//        int loop;
//        int id = 0x0;
//
//        if(size > 8){
//            throw(new CinnamonBunException("Can Message Size limit Exceeded"));
//        }
//
//        if(idBytes.length != 4) {
//            throw(new CinnamonBunException("Can Message ID Insufficient length"));
//        }
//
//        for(loop = 3; loop >= 0; loop--) {
//            id = (id << 8) | (((int)idBytes[loop]) & 0xff);
//        }
// 
//        this.canId = id;
//        this.extended = extended;
//        this.rnr = rnr;
//        this.size = size;
//        
//        for(loop = 0; loop < size; loop++) {
//            this.data[loop] = data[loop];
//        }
//    }
    
    /**
     * \brief Constructor for Layer 2 Message with CAN Identifier specified as an integer
     *
     * \param[in] id : (int)
     *
     * \param[in] extended : (boolean)
     *
     * \param[in] rnr : (boolean)
     *
     * \param[in] data : (byte[])
     *
     * \param[in] size : (byte)
     *
     * This constructor is mainly used by the CAN Layer 3 Protocol where the CAN Identifier
     * is made up of source and destination address bytes of the Layer 3 Message.
     */
//    public CanL2Frame(int id, boolean extended, boolean rnr, byte[] data, byte size) throws CinnamonBunException {
//        int loop;
//
//        if(size > 8) {
//            throw(new CinnamonBunException("Can Message Size limit Exceeded"));
//        }
//
//        this.canId = id;
//        this.extended = extended;
//        this.rnr = rnr;
//        this.size = size;
//
//        for(loop = 0; loop < size; loop++){
//            this.data[loop] = data[loop];
//        }
//    }

    /**
     * \brief Constructor for Layer 2 Message with CAN Identifier specified as an integer
     *
     * \param[in] id : (int)
     *
     * This constructor creates a simple L2 message with no data at all.
     */
    public CanL2Frame(int id) {
        int loop;

        this.mCanId = id;
        this.mSize = 0;
    }

    /**
     * \brief return the identifier of the CAN Layer 2 Message
     *
     * \return int CAN Identifier
     */
    public int getCanId() {
        return this.mCanId;
    }

    /**
     * \brief return byte value of number of data bytes in the message.
     *
     * \return byte value of the numer of data bytes in the message.
     */
    public byte getSize(){
        return mSize;
    }

    /**
     * \brief return byte Array of the data bytes in the message.
     *
     * \return byte Array of data bytes in the message.
     */
    public byte[] getData() {
        return mData;
    }

	public byte[] asByteArray() {
		int loop;
		byte[] array = new byte[mSize + 5];

        array[0] = (byte)((mCanId >> 24) & 0xff);
        array[1] = (byte)((mCanId >> 16) & 0xff);
        array[2] = (byte)((mCanId >> 8) & 0xff);
        array[3] = (byte)(mCanId & 0xff);
        array[4] = mSize;

        for(loop = 0; loop < mSize; loop++) {
            array[loop + 5] = mData[loop];
        }
		return(array);
	}

	public String toString() {
		String frameString = new String();

		if((mCanId & CAN_EFF_FLAG) != 0x00) {
			frameString += "X";
		} else {
			frameString += "S";
		}

		if((mCanId & CAN_RTR_FLAG) != 0x00) {
			frameString += "R";
		} else {
			frameString += "-";
		}

		if((mCanId & CAN_ERR_FLAG) != 0x00) {
			frameString += "E: ";
		} else {
			frameString += "-: ";
		}

		frameString += Integer.toHexString(mCanId);

		if(mSize > 0) {
			frameString += " [";
			for(int loop = 0; loop < mSize; loop++) {
				frameString += CinnamonBun.byteToString(mData[loop]);
				frameString += ",";
			}
			frameString += "]";
		}
		return(frameString);
	}

	public Boolean send() {
		Log.d(TAG, "send(" + Integer.toString(mCanId) + ")");
		AppMessage appMsg = new AppMessage(this);
		return(appMsg.send());
	}
}
