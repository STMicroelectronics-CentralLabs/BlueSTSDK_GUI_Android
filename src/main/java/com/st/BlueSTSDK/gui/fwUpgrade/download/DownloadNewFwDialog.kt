package com.st.BlueSTSDK.gui.fwUpgrade.download

import android.app.AlertDialog
import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.DialogFragment
import com.st.BlueSTSDK.gui.R

class DownloadNewFwDialog : DialogFragment() {

    companion object{
        private const val ARG_FW_LOCATION = "EXTRA_FW_LOCATION"
        private const val ARG_FORCE_FW = "ARG_FORCE_FW"

        @JvmStatic
        fun buildDialogForUri(firmwareRemoteLocation:Uri, forceFwUpgrade:Boolean):DialogFragment{
            return DownloadNewFwDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_FW_LOCATION,firmwareRemoteLocation)
                    putByte(ARG_FORCE_FW,if(forceFwUpgrade) 0 else 1 )
                }
            }
        }
    }

    private fun buildDialogMessage(firmwareRemoteLocation:Uri):CharSequence{
        return getString(R.string.cloudLog_fwUpgrade_notification_desc,
                firmwareRemoteLocation.lastPathSegment);
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val fwLocation = arguments?.getParcelable(ARG_FW_LOCATION) as Uri
        val forceFwUpgrade = (arguments?.getByte(ARG_FORCE_FW) ?: 0) == 0.toByte()
        val message = buildDialogMessage(fwLocation)
        return AlertDialog.Builder(requireContext()).apply {

            setTitle(R.string.cloudLog_fwUpgrade_notification_title)
            setIcon(R.drawable.ota_upload_fw)
            setMessage(message)
            setPositiveButton(R.string.cloudLog_fwUpgrade_startUpgrade){ _, _ ->
                DownloadFwFileService.startDownloadFwFile(requireContext(),fwLocation)
            }
            if(!forceFwUpgrade)
                setNegativeButton(android.R.string.cancel){_,_ -> dismiss()}

        }.create()

    }

}