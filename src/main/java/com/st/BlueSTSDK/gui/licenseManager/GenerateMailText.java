package com.st.BlueSTSDK.gui.licenseManager;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.util.SparseArray;

import com.st.BlueSTSDK.gui.licenseManager.storage.LicenseInfo;
import com.st.generatelicensecodelib.GenerateLicenseCode.UserInfo;
import com.st.generatelicensecodelib.GenerateLicenseCode.LicenseCode;
import com.st.generatelicensecodelib.GenerateLicenseCode;

/**
 * Class that generate the request mail for the license
 */
public class GenerateMailText {

    /**
     * default name used for identify the tool that generate the mail
     */
    private static final String DEFAULT_TOOL_NAME="Android v1.0.0";

    /**
     * address where send the request
     */
    private static final String MAIL_OPENLICENSE_ACCOUNT="open.license@st.com";


    /**
     * Struct that contains the board infor needed for generate the license
     */
    private static class BoardInfo{

        /**
         * board name type
         */
        public final String boardName;

        /**
         * mcu name/type
         */
        public final String mcuName;

        /**
         * address where read the mcu id
         */
        public final int uidAddress;

        private BoardInfo(String boardName, String mcuName, int uidAddress) {
            this.boardName = boardName;
            this.mcuName = mcuName;
            this.uidAddress = uidAddress;
        }
    }

    /**
     * list of know board the id will be transmitted ad part of the uuid from the board
     */
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

    /**
     * license that we are requesting
     */
    private LicenseInfo mLicInfo;

    /**
     * board information used for require the license
     */
    private GenerateLicenseCode.BoardInfo mBoardInfo;

    /**
     * user information used fro require the license
     */
    private UserInfo mUserInfo;


    private static GenerateLicenseCode.BoardInfo createBoardInfo(String mcuId){

        //split the mcuid from the board code
        String[] splitMcuId = mcuId.split("_");
        mcuId=splitMcuId[0];
        BoardInfo info = sBoardInfo.get(Integer.parseInt(splitMcuId[1],16));

        if(info==null){ //if is an unknown board
            throw  new IllegalArgumentException("Invalid mcuId: Board type unknown");
        }

        return new GenerateLicenseCode.BoardInfo(info.boardName,info.uidAddress,mcuId);
    }//createBoardInfo

    /**
     *
     * @param userName user name
     * @param companyName company name
     * @param eMail user email
     * @param lic license request
     * @param mcuId mcu id from the board
     * @throws IllegalArgumentException if one of the parameters isn't valid
     */
    public GenerateMailText(String userName, String companyName, String eMail, LicenseInfo lic,
                            String mcuId) throws IllegalArgumentException{


        mUserInfo = new UserInfo(userName,companyName,eMail);
        mLicInfo=lic;

        mBoardInfo = createBoardInfo(mcuId);
    }

    /**
     * Create an intent for send the request mail
     * @param c contex
     * @return
     */
    public Intent prepareSendMailIntent(Context c){
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{MAIL_OPENLICENSE_ACCOUNT});
        i.putExtra(Intent.EXTRA_SUBJECT, "Request License for: "+mLicInfo.longName);
        i.putExtra(Intent.EXTRA_TEXT, getMailContent(c));
        return i;
    }

    /**
     * generate the mail body
     * @param c context
     * @return reuquest mail body
     */
    private String getMailContent(Context c){
        String siteCode = getSiteCode(c);
        LicenseCode licCode= GenerateLicenseCode.generateLicenseCodes(mUserInfo,
                mBoardInfo,siteCode,mLicInfo.requestCodeName);
        return
        "OSX " + mLicInfo.type + ": <" + mLicInfo.requestCodeNameLong + "> license request\n\n" +
        "\tUser name:\t" + mUserInfo.getUserName() + "\n"+
        "\tUser company:\t" + mUserInfo.getCompanyName() + "\n"+
        "\tUser email:\t" + mUserInfo.getEmail()+ "\n"+
        "\tLibrary name:\t" + mLicInfo.requestCodeName + "\n"+
        "\tBoard model:\t" + mBoardInfo.getBoardTypeName() + "\n"+
        "\tNode Code:\t" + licCode.boardCode + "\n"+
        "\tSite Code:\t" + siteCode + "\n"+
        "\tLicense Code:\t" + licCode.licenseCode + "\n"+
        "\tLicense Type:\tEVALUATION\n"+ // NOT part of the signed message
        "\nThis email has been generated by "+getGeneratorName(c)+"\n";

    }


    /**
     * return a string that
     * @param c context used for generate the mail text
     * @return string that identify the application that is using the library
     */
    private static String getGeneratorName(Context c){
        CharSequence version=null;
        CharSequence appName=null;

        try {
            PackageInfo pInfo = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            version = pInfo.versionName;
            appName = c.getPackageManager().getApplicationLabel(pInfo.applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }//try-catch

        if(appName!=null && version!=null){
            return appName+" v"+version;
        }else
            return DEFAULT_TOOL_NAME;

    }

    /**
     * generate an unique 128bit hex string, depending on the system where the code is running
     * @param c
     * @return unique string
     */
    private static String getSiteCode(Context c){
        String temp =Settings.Secure.getString(c.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return (temp+temp).toUpperCase();
    }

}
