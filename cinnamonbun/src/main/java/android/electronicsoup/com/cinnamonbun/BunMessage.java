package android.electronicsoup.com.cinnamonbun;

/**
 * \class BunMessage
 *
 * \brief Class for the Message to be sent from the CinnamonBun to the 
 * Android Application.
 *
 */
public class BunMessage {
    /**
     * \brief The messages that can be sent from the Cinnamon Bun.
     */
    public static final byte BUN_MSG_ERROR          = (byte)0x00;
    public static final byte BUN_MSG_COMMAND_READY  = (byte)0x01;
    public static final byte BUN_MSG_CAN_STATUS     = (byte)0x02;
    public static final byte BUN_MSG_CAN_L2_FRAME   = (byte)0x03;
    public static final byte BUN_MSG_ISO15765_MSG   = (byte)0x04;
    public static final byte BUN_MSG_ISO11783_MSG   = (byte)0x05;

    public static final byte BUN_MSG_USER_OFFSET    = (byte)0x20;

    /* The MessageType for this message instance */
    private byte id;

    /* Data send in the Message */
    private byte[] data = null;

    /** 
     * \brief Constructor Creates new message of specified type
     * 
     * \param[in] type : The type of this message (MessageType)
     */
    public BunMessage(byte id) {
        this.id = id;
    }
	
    /**
     * \brief Constructor Creates a new message of specified type with 
     *        specified data
     * 
     * \param[in] type : The type of this message (MessageType)
     *
     * \param[in] data : The data associated with this message (byte[])
     */
    public BunMessage(byte id, byte[] data) {
        this.id = id;
        this.data = data;
    }	

    /**
     * \brief Returns the tpye of this message Instance.
     * 
     * \return Message id : The MessageType associated with this message
     */
    public byte id(){
        return(this.id);
    }

    /**
     * \brief Returns the data from this Bun Message Instance.
     * 
     * \return Message id : The MessageType associated with this message
     */
    public byte[] data(){
        return(this.data);
    }
}
