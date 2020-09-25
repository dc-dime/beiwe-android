package org.beiwe.app.ui.registration

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_phone_number_entry.*
import org.beiwe.app.R
import org.beiwe.app.RunningBackgroundServiceActivity
import org.beiwe.app.storage.PersistentData
import org.beiwe.app.survey.TextFieldKeyboard
import org.beiwe.app.ui.utils.AlertsManager

class PhoneNumberEntryActivity : RunningBackgroundServiceActivity() {
    private val phoneNumberLength = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_number_entry)

        val textFieldKeyboard = TextFieldKeyboard(applicationContext)
        if (PersistentData.callClinicianButtonEnabled) {
            textFieldKeyboard.makeKeyboardBehave(primaryCareNumber)
        } else {
            primaryCareNumberLabel.visibility = View.GONE
            primaryCareNumber.visibility = View.GONE
        }
        if (PersistentData.callResearchAssistantButtonEnabled) {
            textFieldKeyboard.makeKeyboardBehave(passwordResetNumber)
        } else {
            passwordResetNumberLabel.visibility = View.GONE
            passwordResetNumber.visibility = View.GONE
        }
    }

    fun checkAndPromptConsent(view: View?) {
        val primary = primaryCareNumber.text.toString().replace("\\D+".toRegex(), "")
        val reset = passwordResetNumber.text.toString().replace("\\D+".toRegex(), "")
        if (PersistentData.callClinicianButtonEnabled) {
            if (primary == null || primary.length == 0) {
                AlertsManager.showAlert(getString(R.string.enter_clinician_number), this)
                return
            }
            if (primary.length != phoneNumberLength) {
                AlertsManager.showAlert(String.format(getString(R.string.phone_number_length_error), phoneNumberLength), this)
                return
            }
        }
        if (PersistentData.callResearchAssistantButtonEnabled) {
            if (reset == null || reset.length == 0) {
                AlertsManager.showAlert(getString(R.string.enter_research_assitant_number), this)
                return
            }
            if (reset.length != phoneNumberLength) {
                AlertsManager.showAlert(String.format(getString(R.string.phone_number_length_error), phoneNumberLength), this)
                return
            }
        }
        PersistentData.primaryCareNumber = primary
        PersistentData.passwordResetNumber = reset
        startActivity(Intent(applicationContext, ConsentFormActivity::class.java))
        finish()
    }
}