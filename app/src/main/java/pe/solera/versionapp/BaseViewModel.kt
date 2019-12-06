package pe.solera.versionapp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

open class BaseViewModel : ViewModel() {
    val loadingLiveData = MutableLiveData<Boolean>()
    val errorLiveData = MutableLiveData<Exception>()

    fun execute(func: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                loadingLiveData.postValue(true)
                func()
                loadingLiveData.postValue(false)
            } catch (ex: Exception) {
                ex.printStackTrace()
                errorLiveData.postValue(ex)
                loadingLiveData.postValue(false)
            }
        }
    }
}