package com.grarcht.shuttle.framework.respository

import android.os.Parcelable
import android.util.SparseArray
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.grarcht.persistence.ShuttleDatabaseBlobAdapter
import com.grarcht.persistence.ShuttleDataAccessObject
import com.grarcht.persistence.ShuttleDataModel
import com.grarcht.persistence.ShuttleDataModelFactory
import com.grarcht.shuttle.framework.content.ShuttleResult
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

open class ShuttleRepository(
    private val parcelDao: ShuttleDataAccessObject,
    private val parcelDataModelFactory: ShuttleDataModelFactory,
    private val shuttleDatabaseBlobAdapter: ShuttleDatabaseBlobAdapter
) : ShuttleWarehouse {

    override val id: String = "1"
    private val cache = HashMap<String, ShuttleDataModel>()
    private var observableDeferred: Deferred<Unit>? = null
    private var parcelIdLiveData: LiveData<ShuttleDataModel>? = null
    private var storedDataObserver: Observer<ShuttleDataModel>? = null


    /**
     * @param lookupKey
     */
    override suspend fun <D : Parcelable> get(
        lookupKey: String,
        parcelableCreator: Parcelable.Creator<D>,
        lifecycleOwner: LifecycleOwner
    ): Channel<ShuttleResult> {
        cancelChannelDeferred()

        // init the channel for sending and receiving data
        val channel = Channel<ShuttleResult>(5)
        channel.send(ShuttleResult.Loading)

        // Check the cache for the data before attempting to acquire it from the db, disk, etc.
        val shuttleDataModel: ShuttleDataModel? = cache[lookupKey]

        if (null == shuttleDataModel) {
            tearDownObserverIfExists()
            initStoredDataObserver(channel, lookupKey, parcelableCreator)
            observePersistedData(lookupKey, lifecycleOwner)
        } else {
            val cachedData = shuttleDatabaseBlobAdapter.adaptToParcelable(shuttleDataModel.data, parcelableCreator)
            channel.send(ShuttleResult.Success(cachedData))
        }

        return channel
    }

    private fun <D : Parcelable> initStoredDataObserver(
        channel: Channel<ShuttleResult>,
        lookupKey: String,
        parcelableCreator: Parcelable.Creator<D>
    ) {
        storedDataObserver = Observer<ShuttleDataModel> { model ->
            observableDeferred = GlobalScope.async {
                if (null == model) {
                    val exception = IllegalStateException("Result unavailable for lookupKey: $lookupKey")
                    channel.send(ShuttleResult.Error(exception))
                } else {
                    val parcelable = shuttleDatabaseBlobAdapter.adaptToParcelable(model.data, parcelableCreator)
                    channel.send(ShuttleResult.Success(parcelable))
                }
            }
        }
    }

    private suspend fun observePersistedData(lookupKey: String, lifecycleOwner: LifecycleOwner) {
        storedDataObserver?.let { observer ->
            // Get the parcel id
            val parcelIdLiveData = parcelDao.getParcelById(lookupKey)

            // Observe the live data from the db
            withContext(MainScope().coroutineContext) {
                parcelIdLiveData.observe(lifecycleOwner, observer)
            }
        }
    }

    private fun cancelChannelDeferred() {
        if (observableDeferred?.isCancelled == false) {
            observableDeferred?.cancel()
        }
    }

    private fun tearDownObserverIfExists() {
        storedDataObserver?.let {
            parcelIdLiveData?.removeObserver(it)
        }
    }

    /**
     *
     */
    override suspend fun <D : Parcelable> save(lookupKey: String, data: D?) {
        saveData(lookupKey, data)
    }

    /**
     *
     */
    override suspend fun <D : Parcelable> save(lookupKey: String, data: Array<D>?) {
//        saveData(parcelId, data)
    }

    /**
     *
     */
    override suspend fun <D : Parcelable> save(lookupKey: String, data: ArrayList<D>?) {
//        data?.let {
//            val parcelDataModel = parcelDataModelFactory.createParcelDataModel(parcelId, it)
//            cache[parcelDataModel.lookupKey] = parcelDataModel
//
//            try {
//                val result = parcelDao.insertParcel(parcelDataModel)
//                println("insert result $result")
//            } catch (e: Exception) {
//                println("Caught when inserting data info the db: $e")
//            }
//        }
    }

    /**
     *
     */
    override suspend fun <D : Parcelable> save(lookupKey: String, data: SparseArray<D>?) {

//        data?.let {
//            val parcelDataModel = parcelDataModelFactory.createParcelDataModel(parcelId, it)
//            cache[parcelDataModel.lookupKey] = parcelDataModel
//
//            try {
//                val result = parcelDao.insertParcel(parcelDataModel)
//                println("insert result $result")
//            } catch (e: Exception) {
//                println("Caught when inserting data info the db: $e")
//            }
//        }
    }

    private fun <D : Parcelable> saveData(lookupKey: String, data: D?) {
        data?.let {
            val shuttleDataModel = parcelDataModelFactory.createParcelDataModel(lookupKey, it)
            cache[lookupKey] = shuttleDataModel

            try {
                parcelDao.insertParcel(shuttleDataModel)
            } catch (e: Exception) {
                println("Caught when inserting data info the db: $e")
            }
        }
    }
}