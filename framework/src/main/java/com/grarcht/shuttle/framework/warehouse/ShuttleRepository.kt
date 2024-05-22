@file:Suppress("MaxLineLength")

package com.grarcht.shuttle.framework.warehouse

import android.os.Parcelable
import com.grarcht.shuttle.framework.integrations.persistence.ShuttleDataAccessObject
import com.grarcht.shuttle.framework.integrations.persistence.ShuttleDataAccessObject.Companion.REMOVE_CARGO_FAILED
import com.grarcht.shuttle.framework.integrations.persistence.ShuttleDataAccessObject.Companion.STORE_CARGO_FAILED
import com.grarcht.shuttle.framework.integrations.persistence.datamodel.ShuttleDataModelFactory
import com.grarcht.shuttle.framework.integrations.persistence.io.file.gateway.ShuttleFileSystemGateway
import com.grarcht.shuttle.framework.integrations.persistence.result.ShuttlePersistenceRemoveCargoResult
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult
import com.grarcht.shuttle.framework.result.ShuttleRemoveCargoResult
import com.grarcht.shuttle.framework.result.ShuttleRemoveCargoResult.Companion.ALL_CARGO
import com.grarcht.shuttle.framework.result.ShuttleStoreCargoResult
import kotlinx.coroutines.channels.Channel
import java.io.Serializable
import java.sql.SQLException

private const val CARGO_DIRECTORY_SEGMENT = "/cargo/"
private const val PICKUP_CARGO_CHANNEL_CAPACITY = 2
private const val REMOVE_CARGO_CHANNEL_CAPACITY = 2
private const val STORE_CARGO_CHANNEL_CAPACITY = 2

/**
 * This class provides the solution for picking up and storing cargo within the warehouse by using the
 * Repository Design Pattern.  For more information on this design pattern, refer to:
 * <a href="https://docs.microsoft.com/en-us/dotnet/architecture/microservices/microservice-ddd-cqrs-patterns/infrastructure-persistence-layer-design">The Repository pattern</a>
 * <a href="https://martinfowler.com/eaaCatalog/repository.html">Repository</a>
 */
open class ShuttleRepository(
    private val shuttleDao: ShuttleDataAccessObject,
    private val shuttleDataModelFactory: ShuttleDataModelFactory,
    private val appFileDirectoryPath: String,
    private val shuttleFileSystemGateway: ShuttleFileSystemGateway
) : ShuttleWarehouse {

    /**
     * Obtains the cargo as [ShuttlePickupCargoResult] using a [cargoId].
     * @param cargoId Used to get the cargo.
     * @return the channel for the results
     */
    @Suppress("unused")
    override suspend fun <D : Serializable> pickup(cargoId: String): Channel<ShuttlePickupCargoResult> {
        val pickupCargoChannel = Channel<ShuttlePickupCargoResult>(PICKUP_CARGO_CHANNEL_CAPACITY)
        pickupCargoChannel.send(ShuttlePickupCargoResult.Loading(cargoId))

        pickupCargoChannel.apply {
            val shuttleDataModel = shuttleDao.getCargoBy(cargoId)
            if (null == shuttleDataModel) {
                val errorMessage = "Result unavailable for cargoId: $cargoId"
                pickupCargoChannel.send(ShuttlePickupCargoResult.Error<Throwable>(cargoId, errorMessage))
            } else {
                val blob = shuttleFileSystemGateway.readFromFile(shuttleDataModel.filePath)

                if (null == blob) {
                    val errorMessage = "Unable to retrieve result for cargoId: $cargoId"
                    pickupCargoChannel.send(ShuttlePickupCargoResult.Error<Throwable>(cargoId, errorMessage))
                } else {
                    pickupCargoChannel.send(ShuttlePickupCargoResult.Success(blob))
                }
            }
        }
        return pickupCargoChannel
    }

    /**
     * Stores the cargo.
     * @param cargoId Used to get the cargo.
     * @param data The [Parcelable] cargo to store.
     */
    override suspend fun <D : Serializable> store(cargoId: String, data: D?): Channel<ShuttleStoreCargoResult> {
        val storeCargoChannel = Channel<ShuttleStoreCargoResult>(STORE_CARGO_CHANNEL_CAPACITY)
        storeCargoChannel.send(ShuttleStoreCargoResult.Storing(cargoId))

        if (null == data) {
            val errorMessage = "The cargo is null."
            val result = ShuttleStoreCargoResult.Error<Throwable>(cargoId, errorMessage)
            storeCargoChannel.send(result)
            return storeCargoChannel
        }

        storeCargoChannel.apply {
            val directoryName = appFileDirectoryPath + CARGO_DIRECTORY_SEGMENT
            val filePath = shuttleFileSystemGateway.writeToFile(directoryName, cargoId, data as Serializable)

            if (null == filePath) {
                val errorMessage = "File path is null for cargoId: $cargoId."
                val storeResult = ShuttleStoreCargoResult.Error<Throwable>(cargoId, errorMessage)
                storeCargoChannel.send(storeResult)
            } else {
                val shuttleDataModel = shuttleDataModelFactory.createDataModel(cargoId, filePath)

                try {
                    val insertValue = shuttleDao.insertCargo(shuttleDataModel)

                    val storeResult = if (STORE_CARGO_FAILED != insertValue) {
                        ShuttleStoreCargoResult.Success(cargoId)
                    } else {
                        ShuttleStoreCargoResult.Error<Throwable>(cargoId)
                    }
                    storeCargoChannel.send(storeResult)
                } catch (e: SQLException) {
                    val errorMessage = "Caught when storing cargo for cargoId: $cargoId."
                    val result = ShuttleStoreCargoResult.Error(cargoId, errorMessage, throwable = e)
                    storeCargoChannel.send(result)
                } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                    val errorMessage = "Caught when storing cargo for cargoId: $cargoId."
                    val result = ShuttleStoreCargoResult.Error(cargoId, errorMessage, throwable = e)
                    storeCargoChannel.send(result)
                }
            }
        }
        return storeCargoChannel
    }

    override suspend fun removeCargoBy(cargoId: String): Channel<ShuttleRemoveCargoResult> {
        val removeCargoChannel = Channel<ShuttleRemoveCargoResult>(REMOVE_CARGO_CHANNEL_CAPACITY)
        removeCargoChannel.send(ShuttleRemoveCargoResult.Removing(cargoId))

        removeCargoChannel.apply {
            val file = appFileDirectoryPath + CARGO_DIRECTORY_SEGMENT + cargoId
            try {
                when (shuttleFileSystemGateway.deleteFile(file)) {
                    is ShuttlePersistenceRemoveCargoResult.DoesNotExist -> {
                        val result = ShuttleRemoveCargoResult.DoesNotExist(cargoId = cargoId)
                        removeCargoChannel.send(result)
                    }

                    is ShuttlePersistenceRemoveCargoResult.UnableToRemove -> {
                        val result = ShuttleRemoveCargoResult.UnableToRemove<Throwable>(
                            cargoId = cargoId,
                            message = "Unable to delete the cargo: $cargoId"
                        )
                        removeCargoChannel.send(result)
                    }

                    is ShuttlePersistenceRemoveCargoResult.Removed -> {
                        val returnValue = shuttleDao.deleteCargoBy(cargoId)

                        if (REMOVE_CARGO_FAILED != returnValue) {
                            val result = ShuttleRemoveCargoResult.Removed(cargoId)
                            removeCargoChannel.send(result)
                        } else {
                            val result = ShuttleRemoveCargoResult.UnableToRemove<Throwable>(
                                cargoId = cargoId,
                                message = "Unable to delete persisted entry for cargoId: $cargoId."
                            )
                            removeCargoChannel.send(result)
                        }
                    }
                }
            } catch (e: SQLException) {
                val errorMessage = "Caught when removing cargo for cargoId: $cargoId."
                val result = ShuttleRemoveCargoResult.UnableToRemove(cargoId, errorMessage, throwable = e)
                removeCargoChannel.send(result)
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                val errorMessage = "Caught when removing cargo for cargoId: $cargoId."
                val result = ShuttleRemoveCargoResult.UnableToRemove(cargoId, errorMessage, throwable = e)
                removeCargoChannel.send(result)
            }
        }
        return removeCargoChannel
    }

    override suspend fun removeAllCargo(): Channel<ShuttleRemoveCargoResult> {
        val removeCargoChannel = Channel<ShuttleRemoveCargoResult>(REMOVE_CARGO_CHANNEL_CAPACITY)
        removeCargoChannel.send(ShuttleRemoveCargoResult.Removing(ALL_CARGO))

        removeCargoChannel.apply {
            try {
                val directory = appFileDirectoryPath + CARGO_DIRECTORY_SEGMENT
                when (shuttleFileSystemGateway.deleteAllFilesAt(directory)) {
                    is ShuttlePersistenceRemoveCargoResult.UnableToRemove -> {
                        val result = ShuttleRemoveCargoResult.UnableToRemove<Throwable>(cargoId = ALL_CARGO)
                        removeCargoChannel.send(result)
                    }

                    is ShuttlePersistenceRemoveCargoResult.DoesNotExist -> {
                        val result = ShuttleRemoveCargoResult.DoesNotExist(cargoId = ALL_CARGO)
                        removeCargoChannel.send(result)
                    }

                    is ShuttlePersistenceRemoveCargoResult.Removed -> {
                        val returnValue = shuttleDao.deleteAllCargoData()

                        if (REMOVE_CARGO_FAILED != returnValue) {
                            val result = ShuttleRemoveCargoResult.Removed(ALL_CARGO)
                            removeCargoChannel.send(result)
                        } else {
                            val result = ShuttleRemoveCargoResult.UnableToRemove<Throwable>(cargoId = ALL_CARGO)
                            removeCargoChannel.send(result)
                        }
                    }
                }
            } catch (e: SQLException) {
                val errorMessage = "Caught when removing all fo the cargo."
                val result = ShuttleRemoveCargoResult.UnableToRemove(ALL_CARGO, errorMessage, throwable = e)
                removeCargoChannel.send(result)
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                val errorMessage = "Caught when removing all fo the cargo."
                val result = ShuttleRemoveCargoResult.UnableToRemove(ALL_CARGO, errorMessage, throwable = e)
                removeCargoChannel.send(result)
            }
        }
        return removeCargoChannel
    }
}
