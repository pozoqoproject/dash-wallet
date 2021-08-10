/*
 * Copyright 2021 Dash Core Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.schildbach.wallet.ui.invite

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import de.schildbach.wallet.WalletApplication
import de.schildbach.wallet.data.BlockchainIdentityBaseData
import de.schildbach.wallet.data.InvitationLinkData
import de.schildbach.wallet.livedata.Resource
import de.schildbach.wallet.livedata.Resource.Companion.error
import de.schildbach.wallet.livedata.Resource.Companion.loading
import de.schildbach.wallet.livedata.Status
import de.schildbach.wallet.ui.OnboardingActivity
import de.schildbach.wallet.ui.dashpay.CreateIdentityService
import de.schildbach.wallet.ui.dashpay.PlatformRepo.Companion.getInstance
import de.schildbach.wallet_test.R
import org.dash.wallet.common.ui.FancyAlertDialog
import org.dash.wallet.common.ui.FancyAlertDialogViewModel

class InviteHandler(val activity: AppCompatActivity) {

    private lateinit var inviteLoadingDialog: FancyAlertDialog

    companion object {

        fun showUsernameAlreadyDialog(activity: AppCompatActivity) {
            val inviteErrorDialog = FancyAlertDialog.newInstance(
                    R.string.invitation_username_already_found_title,
                    R.string.invitation_username_already_found_message,
                    R.drawable.ic_invalid_invite, R.string.okay, 0)
            inviteErrorDialog.show(activity.supportFragmentManager, null)
            handleDialogResult(activity)
        }

        private fun handleDialogResult(activity: AppCompatActivity) {
            val errorDialogViewModel = ViewModelProvider(activity)[FancyAlertDialogViewModel::class.java]
            errorDialogViewModel.onPositiveButtonClick.observe(activity, Observer {
                activity.setResult(Activity.RESULT_CANCELED)
                activity.finish()
            })
            errorDialogViewModel.onNegativeButtonClick.observe(activity, Observer {
                activity.setResult(Activity.RESULT_CANCELED)
                activity.finish()
            })
        }
    }

    fun handle(inviteResource: Resource<InvitationLinkData>, silentMode: Boolean = false) {
        if (!silentMode && inviteResource.status != Status.LOADING) {
            inviteLoadingDialog.dismissAllowingStateLoss()
        }
        when (inviteResource.status) {
            Status.LOADING -> {
                if (!silentMode) {
                    showInviteLoadingProgress()
                }
            }
            Status.ERROR -> {
                val displayName = inviteResource.data!!.displayName
                showInvalidInviteDialog(displayName)
            }
            Status.CANCELED -> {
                showUsernameAlreadyDialog(activity)
            }
            Status.SUCCESS -> {
                val invite = inviteResource.data!!
                if (invite.isValid) {
                    activity.setResult(Activity.RESULT_OK)
                    val walletApplication = (activity.application as WalletApplication)
                    when {
                        silentMode -> {
                            activity.startService(CreateIdentityService.createIntentFromInvite(activity, walletApplication.configuration.onboardingInviteUsername, invite))
                        }
                        walletApplication.wallet != null -> {
                            activity.startActivity(AcceptInviteActivity.createIntent(activity, invite, false))
                        }
                        else -> {
                            walletApplication.configuration.onboardingInvite = invite.link
                            activity.startActivity(OnboardingActivity.createIntent(activity, invite))
                        }
                    }
                    activity.finish()
                } else {
                    showInviteAlreadyClaimedDialog(invite)
                }
            }
        }
    }

    private fun showInvalidInviteDialog(displayName: String) {
        val title = activity.getString(R.string.invitation_invalid_invite_title)
        val message = activity.getString(R.string.invitation_invalid_invite_message, displayName)
        val inviteErrorDialog = FancyAlertDialog.newInstance(title, message, R.drawable.ic_invalid_invite, R.string.okay, 0)
        inviteErrorDialog.show(activity.supportFragmentManager, null)
        handleDialogResult(activity)
    }

    private fun showInviteAlreadyClaimedDialog(invite: InvitationLinkData) {
        val inviteAlreadyClaimedDialog = InviteAlreadyClaimedDialog.newInstance(activity, invite)
        inviteAlreadyClaimedDialog.show(activity.supportFragmentManager, null)
        handleDialogResult(activity)
    }

    private fun showInviteLoadingProgress() {
        if (::inviteLoadingDialog.isInitialized && inviteLoadingDialog.isAdded) {
            inviteLoadingDialog.dismissAllowingStateLoss()
        }
        inviteLoadingDialog = FancyAlertDialog.newProgress(R.string.invitation_verifying_progress_title, 0)
        inviteLoadingDialog.show(activity.supportFragmentManager, null)
    }

    private fun showInsuffientFundsDialog() {
        val dialog = FancyAlertDialog.newProgress(R.string.invitation_invalid_invite_title, R.string.dashpay_insuffient_credits)
        dialog.show(activity.supportFragmentManager, null)
    }

    /**
     * handle non-recoverable errors from using an invite
     */
    fun handleError(blockchainIdentityData: BlockchainIdentityBaseData): Boolean {
        // handle errors
        var errorMessage: String
        if (blockchainIdentityData.creationStateErrorMessage.also { errorMessage = it!! } != null) {
            when {
                (errorMessage.contains("IdentityAssetLockTransactionOutPointAlreadyExistsError")) -> {
                    showInviteAlreadyClaimedDialog(blockchainIdentityData.invite!!)
                    // now erase the blockchain data
                    getInstance().clearBlockchainData()
                    return true
                }

                errorMessage.contains("InvalidIdentityAssetLockProofSignatureError") -> {
                    handle(loading(blockchainIdentityData.invite, 0))
                    handle(error(errorMessage, blockchainIdentityData.invite))
                    // now erase the blockchain data
                    getInstance().clearBlockchainData()
                    return true
                }
                errorMessage.contains("InsuffientFundsError") -> {
                    showInsuffientFundsDialog()
                    // now erase the blockchain data
                    getInstance().clearBlockchainData()
                    return true
                }
            }
        }
        return false
    }
}