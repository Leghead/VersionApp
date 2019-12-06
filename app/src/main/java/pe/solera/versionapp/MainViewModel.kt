package pe.solera.versionapp

import androidx.lifecycle.MutableLiveData
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

class MainViewModel : BaseViewModel() {

    val isVersionUpdated = MutableLiveData<AppUpdateInfo?>()

    fun validateAppVersion(appUpdateManager: AppUpdateManager) {
        execute {
            appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                if ((appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(
                        AppUpdateType.IMMEDIATE))) {
                    isVersionUpdated.postValue(appUpdateInfo)
                } else {
                    isVersionUpdated.postValue(null)
                }
            }
            appUpdateManager.appUpdateInfo.addOnFailureListener {
                isVersionUpdated.postValue(null)
            }
        }
    }

}