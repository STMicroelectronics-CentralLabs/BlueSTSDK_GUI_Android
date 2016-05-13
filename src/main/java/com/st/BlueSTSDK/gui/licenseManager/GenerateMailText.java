package com.st.BlueSTSDK.gui.licenseManager;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.SparseArray;

import com.st.BlueSTSDK.gui.licenseManager.storage.LicenseInfo;

import java.math.BigInteger;

public class GenerateMailText {

    private static final String MAIL_OPENLICENSE_ACCOUNT="open.license@st.com";
    private static final String TOOL_NAME="OSX License Requester for Android";
    private static final String TOOL_VERSION="1.0.0";


    private static class BoardInfo{

        public final String boardName;
        public final String mcuName;
        public final long uidAddress;

        private BoardInfo(String boardName, String mcuName, long uidAddress) {
            this.boardName = boardName;
            this.mcuName = mcuName;
            this.uidAddress = uidAddress;
        }
    }

    private static final SparseArray<BoardInfo> sBoardInfo= new SparseArray<>();

    static{
        sBoardInfo.append(0x410,
                new BoardInfo( "NucleoF103RB", "NucleoF103RB",0x1FFFF7E8));
        sBoardInfo.append(0x412,
                new BoardInfo( "", "Undefined", 0x1FFFF7E8));
        sBoardInfo.append(0x413,// STM32F405xx/07xx and STM32F415xx/17xx
                new BoardInfo( "DiscoF407VG", "STM32F405xx_07xx_15xx_17xx", 0x1FFF7A10));
        sBoardInfo.append(0x415, // STM32L4x6 family
                new BoardInfo( "DiscoL476VG", "STM32L4x6", 0x1FFF7590));
        sBoardInfo.append(0x416, // STM32L4x6 family
                new BoardInfo( "", "Undefined", 0x1FF80050));
        sBoardInfo.append(0x417, // STM32L4x6 family
                new BoardInfo( "NucleoL053R8", "NucleoL053R8", 0x1FF80050));
        sBoardInfo.append(0x419, // STM32F42xxx and STM32F43xxx
                new BoardInfo( "DiscoF429ZI", "STM32F42xxx_3xxx", 0x1FFF7A10));
        sBoardInfo.append(0x421, // STM32F446xx
                new BoardInfo( "NucleoF446RE", "STM32F446xx", 0x1FFF7A10));
        sBoardInfo.append(0x422,
                new BoardInfo( "DiscoF303VC", "Undefined", 0x1FFFF7AC));
        sBoardInfo.append(0x423, // STM32F401xB/C family
                new BoardInfo( "DiscoF401C", "STM32F401xB_C", 0x1FFF7A10));
        sBoardInfo.append(0x427,
                new BoardInfo( "", "Undefined", 0x1FF80050));
        sBoardInfo.append(0x431, // STM32F411xC/E family
                new BoardInfo( "NucleoF411RE", "STM32F411xC_E", 0x1FFF7A10));
        sBoardInfo.append(0x433, // STM32F401xD/E family
                new BoardInfo( "NucleoF401RE", "STM32F401xD_E",0x1FFF7A10));
        sBoardInfo.append(0x434, // STM32F469xx and STM32F479xx family
                new BoardInfo( "DiscoF469NI", "STM32F469xx_F479xx", 0x1FFF7A10));
        sBoardInfo.append(0x436,
                new BoardInfo( "", "Undefined", 0x1FF80050));
        sBoardInfo.append(0x437,// STM32L15xxE/L162xE family
                new BoardInfo( "NucleoL152RE", "STM32L15xxE_L162xE", 0x1FF80050));
        sBoardInfo.append(0x438,
                new BoardInfo( "NucleoF334R8", "NucleoF334R8", 0x1FFFF7AC));
        sBoardInfo.append(0x439,
                new BoardInfo("NucleoF302R8", "NucleoF302R8", 0x1FFFF7AC));
        sBoardInfo.append(0x440,
                new BoardInfo( "NucleoF030R8", "NucleoF030R8", 0x1FFFF7AC));
        sBoardInfo.append(0x442,
                new BoardInfo( "NucleoF091RC", "NucleoF091RC", 0x1FFFF7AC));
        sBoardInfo.append(0x444,
                new BoardInfo( "NucleoF031K6", "Undefined", 0x1FFFF7AC));
        sBoardInfo.append(0x445,
                new BoardInfo( "NucleoF042K6", "Undefined", 0x1FFFF7AC));
        sBoardInfo.append(0x446,
                new BoardInfo( "NucleoF303RE", "NucleoF303RE", 0x1FFFF7AC));
        sBoardInfo.append(0x448,
                new BoardInfo( "NucleoF072RB", "NucleoF072RB", 0x1FFFF7AC));
        sBoardInfo.append(0x449, // STM32F75xxx
                new BoardInfo( "NucleoF746ZG", "STM32F75xxx_4xxx", 0x1FF0F420));
    }

/*
    sb.append("Copy the text in the below section (between the lines)\n"));
    CString sTmp; sTmp.Format("and send it via email to %s\n\n", MAIL_OPENLICENSE_ACCOUNT);
    sb.append(sTmp);

    sb.append("NOTE: the message is digitally-signed, do NOT modify the fields or text.\n"));
    sb.append("Failure to do so will prevent license authorization by the licensing server.\n\n"));

    sb.append(LC_SEPARATOR_STRING);

    sb.append("OSX ") + m_csCategoryName + ": <") + m_csLidLongName + "> license request\n\n"));
    sb.append("\tUser name:\t") + m_csUserName + "\n"));
    sb.append("\tUser company:\t") + m_csCompanyName + "\n"));
    sb.append("\tUser email:\t") + m_csEmail + "\n"));
    sb.append("\tLibrary name:\t") + m_csLidName + "\n"));
    sb.append("\tBoard model:\t") + m_csBoardName + "\n"));
    sb.append("\tNode Code:\t") + m_sNodeCode + "\n"));
    sb.append("\tSite Code:\t") + m_sSiteCode + "\n"));
    sb.append("\tLicense Code:\t") + m_csLicenseCode + "\n"));
    sb.append("\tLicense Type:\t") + m_csLicenseType + "\n"));	// NOT part of the signed message

    sb.append("\nThis email has been generated by the ") + sample_sToolName + " v") + GetFileVersion() + "\n"));

    sb.append(LC_SEPARATOR_STRING);

    sb.append("\nYour license will then be processed by the licensing server\n"));
    sb.append("and you'll receive authorization license via email.\n\n"));
    sb.append("Enjoy the ST OpenSoftwareX development tools!\n"));

    sb.append("\n================== END OF LICENSE REQUEST MESSAGE ==================\n"));
*/

    private String mUserName;
    private String mCompanyName;
    private String mEmail;
    private LicenseInfo mLicInfo;
    private String mMcuId;
    private BoardInfo mBoardInfo;

    private static String sanitizeUserName(String name) throws IllegalArgumentException{
        return name;
    }

    private static String sanitizeCompanyName(String name) throws IllegalArgumentException{
        return name;
    }

    private static String sanitizeMcuId(String id) throws IllegalArgumentException{
        return id;
    }

    public GenerateMailText(String userName, String companyName, String eMail, LicenseInfo lic,
                            String mcuId) throws IllegalArgumentException{

        mUserName=sanitizeUserName(userName);
        mCompanyName=sanitizeCompanyName(companyName);
        mEmail=eMail;
        mLicInfo=lic;
        mcuId=sanitizeMcuId(mcuId);
        String[] splitMcuId = mcuId.split("_");
        mMcuId=splitMcuId[0];
        mBoardInfo = sBoardInfo.get(Integer.parseInt(splitMcuId[1],16));

        if(mBoardInfo==null){
            throw  new IllegalArgumentException("Invalid mcuId: Board type unknown");
        }

    }

    public Intent prepareSendMailIntent(Context c){
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{MAIL_OPENLICENSE_ACCOUNT});
        i.putExtra(Intent.EXTRA_SUBJECT, "Request License for: "+mLicInfo.longName);
        i.putExtra(Intent.EXTRA_TEXT, getMailContent(c));
        return i;
    }


    private String getMailContent(Context c){

        return
        "OSX " + mLicInfo.type + ": <" + mLicInfo.requestCodeNameLong + "> license request\n\n" +
        "\tUser name:\t" + mUserName + "\n"+
        "\tUser company:\t" + mCompanyName + "\n"+
        "\tUser email:\t" + mEmail + "\n"+
        "\tLibrary name:\t" + mLicInfo.requestCodeName + "\n"+
        "\tBoard model:\t" + mBoardInfo.boardName + "\n"+
        "\tNode Code:\t" + generateNodeCode() + "\n"+
        "\tSite Code:\t" + getSiteCode(c) + "\n"+
        "\tLicense Code:\t" + generateLicenseCode() + "\n"+
        "\tLicense Type:\t" +  mLicInfo.type + "\n"+ // NOT part of the signed message
        "\nThis email has been generated by the " + TOOL_NAME + " v" + TOOL_VERSION +"\n";


    }

    /**
     * generate an unique 128bit hex string, depending on the system where the code is running
     * @param c
     * @return unique string
     */
    private static String getSiteCode(Context c){
        String temp =Settings.Secure.getString(c.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return temp+temp;
    }

    private String generateNodeCode(){
        //TODO implement as native
        return new BigInteger(mMcuId,16).add(BigInteger.valueOf(mBoardInfo.uidAddress)).toString(16);
    }

    private String generateLicenseCode(){
        return "";
    }


}
