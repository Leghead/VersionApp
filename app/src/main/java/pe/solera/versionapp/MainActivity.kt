package pe.solera.versionapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.android.synthetic.main.activity_main.*
import org.jsoup.Jsoup
import pe.solera.versionapp.BuildConfig.VERSION_NAME

class MainActivity : AppCompatActivity() {

    val MY_REQUEST_CODE = 1234


    internal var appUpdateManager: AppUpdateManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        txvMovil.text = VERSION_NAME


// Creates instance of the manager.
           appUpdateManager = AppUpdateManagerFactory.create(this)


        val appUpdateInfoTask = appUpdateManager!!.appUpdateInfo
        appUpdateManager!!.registerListener(installStateUpdatedListener)

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                // Request the update.
                appUpdateManager!!.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, MY_REQUEST_CODE)
            }
        }


        //VersioningTask(this, this).execute()
    }


    internal var installStateUpdatedListener: InstallStateUpdatedListener = object :
        InstallStateUpdatedListener {
        override fun onStateUpdate(state: InstallState) {
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                Toast.makeText(this@MainActivity,"Aplicación Actulizada",Toast.LENGTH_SHORT).show()
            } else if (state.installStatus() == InstallStatus.INSTALLED) {
                if (appUpdateManager != null) {
                    appUpdateManager!!.unregisterListener(this)
                }

            } else {
                Log.i("TAG", "InstallStateUpdatedListener: state: " + state.installStatus())
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Log.d("Updateflowfailedcode", requestCode.toString())
                // If the update is cancelled or fails,
                // you can request to start the update again.
            } else {
                Toast.makeText(this@MainActivity,"Aplicación Actulizada",Toast.LENGTH_SHORT).show()
                Log.d("Updatesuccessdcode", requestCode.toString())
            }
        }
    }

    fun openDiaglog(isMandatory: Boolean) {
        val dialog =
            AlertDialog.Builder(this).setTitle("Nueva actualización").setCancelable(false).create()

        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_version, null)
        dialog.setView(dialogView)

        val txvTitle = dialogView.findViewById<TextView>(R.id.tv_dialog_title)
        val txvMessage = dialogView.findViewById<TextView>(R.id.tv_dialog_message)
        val txvNegative = dialogView.findViewById<TextView>(R.id.tv_dialog_negative)
        val txvPositive = dialogView.findViewById<TextView>(R.id.tv_dialog_positive)

        txvTitle.text = "Nueva actualización"

        if (isMandatory) {
            txvMessage.text =
                "Estimado Socio, para una mejor experiencia, por favor actualice su APP."
            txvNegative.text = "Salir"
            txvPositive.text = "Actualizar"

            txvNegative.setOnClickListener {
                dialog.dismiss()
                this.finish()
            }
            txvPositive.setOnClickListener {
                dialog.dismiss()
                redirectStore()
            }
        } else {
            txvMessage.text =
                "Estimado Socio, para una mejor experiencia, por favor actualice su APP."
            txvNegative.text = "Más tarde"
            txvPositive.text = "Actualizar"

            txvNegative.setOnClickListener {
                dialog.dismiss()
            }
            txvPositive.setOnClickListener {
                dialog.dismiss()
                redirectStore()
            }
        }
        dialog.show()

    }

    fun redirectStore() {
        this.runOnUiThread {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=pe.solera.versionapp&hl=en")
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            this.finish()
            this.applicationContext.startActivity(intent)
        }
    }

    class VersioningTask(val activity: Activity, val context: Context) :
        AsyncTask<String, Int, String>() {

        override fun doInBackground(vararg params: String?): String {
            try {
                val version: String? =
                    Jsoup.connect("https://play.google.com/store/apps/details?id=pe.solera.versionapp&hl=en")
                        .timeout(60000)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .ignoreHttpErrors(true)
                        .get()
                        .select("div.hAyfc:nth-child(4) > span:nth-child(2) > div:nth-child(1) > span:nth-child(1)")
                        .first()
                        .ownText()

                if (version != null) {
                    when {
                        BuildConfig.VERSION_NAME < version -> {
                            onUpdateNeeded(true)
                        }
                        BuildConfig.VERSION_NAME == version -> {
                            Log.d("IGUALES", "IGUAL")
                        }
                        else -> {
                            onUpdateNeeded(false)
                        }
                    }
                }

                return version!!

            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            return ""
        }

        private fun onUpdateNeeded(isMandatory: Boolean) {
            activity.runOnUiThread {
                val currentActivity = activity as MainActivity
                currentActivity.openDiaglog(isMandatory)

            }

        }


    }
}
