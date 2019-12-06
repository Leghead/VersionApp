package pe.solera.versionapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModel(clazz = MainViewModel::class)

    private lateinit var appUpdateManager: AppUpdateManager

    companion object {
        const val UPDATE_CODE = 1234
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.appUpdateManager = AppUpdateManagerFactory.create(this)
        this.mainViewModel.validateAppVersion(this.appUpdateManager)
        this.observeViewModel()
    }

    private fun observeViewModel() {
        this.mainViewModel.isVersionUpdated.observe(this, Observer {appUpdateInfo ->
            if (appUpdateInfo != null) {
                appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, UPDATE_CODE)
            } else {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPDATE_CODE) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(this, "Update flow failed! Result code: $resultCode", Toast.LENGTH_SHORT).show()
                // If the update is cancelled or fails,
                // you can request to start the update again.
            }
        }
    }
}
