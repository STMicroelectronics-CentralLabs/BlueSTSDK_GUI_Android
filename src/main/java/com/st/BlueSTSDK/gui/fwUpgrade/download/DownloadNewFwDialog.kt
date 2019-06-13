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

        @JvmStatic
        fun buildDialogForUri(firmwareRemoteLocation:Uri):DialogFragment{
            return DownloadNewFwDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_FW_LOCATION,firmwareRemoteLocation)
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
        val message = buildDialogMessage(fwLocation)
        return AlertDialog.Builder(requireContext()).apply {

            setTitle(R.string.cloudLog_fwUpgrade_notification_title)
            setIcon(R.drawable.ota_upload_fw)
            setMessage(message)
            setPositiveButton(R.string.cloudLog_fwUpgrade_startUpgrade){ _, _ ->
                DownloadFwFileService.startDownloadFwFile(requireContext(),fwLocation)
            }
            setNegativeButton(android.R.string.cancel){_,_ -> dismiss()}

        }.create()

    }

}