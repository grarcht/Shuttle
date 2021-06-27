package com.grarcht.shuttle.framework.addons.bundle

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.os.PersistableBundle
import android.util.SparseArray
import com.grarcht.shuttle.framework.content.bundle.BundleFactory
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.stubbing.Answer
import java.io.Serializable
import java.util.*

class MockBundleFactory: BundleFactory {
    override fun create(): Bundle {
        return create(mutableMapOf())
    }

    override fun create(loader: ClassLoader?): Bundle {
        return create(mutableMapOf())
    }

    override fun create(capacity: Int): Bundle {
        return create(mutableMapOf())
    }

    override fun create(bundle: Bundle): Bundle {
        return create(mutableMapOf())
    }

    override fun create(bundle: PersistableBundle): Bundle {
        return create(mutableMapOf())
    }

    fun create(map: MutableMap<String?, Any?> = mutableMapOf()): Bundle {
        //https://github.com/konmik/nucleus/blob/master/nucleus-test-kit/src/main/java/mocks/BundleMock.java
        val unsupported: Answer<*> = Answer<Any?> { throw UnsupportedOperationException() }
        val put: Answer<*> = Answer<Any?> { invocation ->
            map[invocation.arguments[0] as String] = invocation.arguments[1]
            null
        }
        val get: Answer<Any> = Answer { invocation -> map[invocation.arguments[0]] }
        val getOrDefault: Answer<Any> = Answer { invocation ->
            val key = invocation.arguments[0]
            if (map.containsKey(key)) map[key] else invocation.arguments[1]
        }
        val bundle = Mockito.mock(Bundle::class.java)
        Mockito.doAnswer { map.size }.`when`(bundle).size()
        Mockito.doAnswer { map.isEmpty() }.`when`(bundle).isEmpty
        Mockito.doAnswer {
            map.clear()
            null
        }.`when`(bundle).clear()
        Mockito.doAnswer { invocation -> map.containsKey(invocation.arguments[0]) }.`when`(bundle)
            .containsKey(ArgumentMatchers.anyString())
        Mockito.doAnswer { invocation -> map[invocation.arguments[0]] }.`when`(bundle)[ArgumentMatchers.anyString()]
        Mockito.doAnswer { invocation ->
            map.remove(invocation.arguments[0])
            null
        }.`when`(bundle).remove(ArgumentMatchers.anyString())
        Mockito.doAnswer { map.keys }.`when`(bundle).keySet()
        Mockito.doAnswer { MockBundleFactory::class.java.simpleName + "{map=" + map.toString() + "}" }.`when`(bundle)
            .toString()
        Mockito.doAnswer(put).`when`(bundle).putBoolean(ArgumentMatchers.anyString(), ArgumentMatchers.anyBoolean())
        Mockito.`when`(bundle.getBoolean(ArgumentMatchers.anyString())).thenAnswer(get)
        Mockito.`when`(bundle.getBoolean(ArgumentMatchers.anyString(), ArgumentMatchers.anyBoolean()))
            .thenAnswer(getOrDefault)
        Mockito.doAnswer(put).`when`(bundle).putByte(ArgumentMatchers.anyString(), ArgumentMatchers.anyByte())
        Mockito.`when`(bundle.getByte(ArgumentMatchers.anyString())).thenAnswer(get)
        Mockito.`when`(bundle.getByte(ArgumentMatchers.anyString(), ArgumentMatchers.anyByte()))
            .thenAnswer(getOrDefault)
        Mockito.doAnswer(put).`when`(bundle).putChar(ArgumentMatchers.anyString(), ArgumentMatchers.anyChar())
        Mockito.`when`(bundle.getChar(ArgumentMatchers.anyString())).thenAnswer(get)
        Mockito.`when`(bundle.getChar(ArgumentMatchers.anyString(), ArgumentMatchers.anyChar()))
            .thenAnswer(getOrDefault)
        Mockito.doAnswer(put).`when`(bundle).putInt(ArgumentMatchers.anyString(), ArgumentMatchers.anyShort().toInt())
        Mockito.`when`(bundle.getShort(ArgumentMatchers.anyString())).thenAnswer(get)
        Mockito.`when`(bundle.getShort(ArgumentMatchers.anyString(), ArgumentMatchers.anyShort()))
            .thenAnswer(getOrDefault)
        Mockito.doAnswer(put).`when`(bundle).putLong(ArgumentMatchers.anyString(), ArgumentMatchers.anyLong())
        Mockito.`when`(bundle.getLong(ArgumentMatchers.anyString())).thenAnswer(get)
        Mockito.`when`(bundle.getLong(ArgumentMatchers.anyString(), ArgumentMatchers.anyLong()))
            .thenAnswer(getOrDefault)
        Mockito.doAnswer(put).`when`(bundle).putFloat(ArgumentMatchers.anyString(), ArgumentMatchers.anyFloat())
        Mockito.`when`(bundle.getFloat(ArgumentMatchers.anyString())).thenAnswer(get)
        Mockito.`when`(bundle.getFloat(ArgumentMatchers.anyString(), ArgumentMatchers.anyFloat()))
            .thenAnswer(getOrDefault)
        Mockito.doAnswer(put).`when`(bundle).putDouble(ArgumentMatchers.anyString(), ArgumentMatchers.anyDouble())
        Mockito.`when`(bundle.getDouble(ArgumentMatchers.anyString())).thenAnswer(get)
        Mockito.`when`(bundle.getDouble(ArgumentMatchers.anyString(), ArgumentMatchers.anyDouble()))
            .thenAnswer(getOrDefault)
        Mockito.doAnswer(put).`when`(bundle).putString(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())
        Mockito.`when`(bundle.getString(ArgumentMatchers.anyString())).thenAnswer(get)
        Mockito.`when`(bundle.getString(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenAnswer(getOrDefault)
        Mockito.doAnswer(put).`when`(bundle).putBooleanArray(
            ArgumentMatchers.anyString(), ArgumentMatchers.any(
                BooleanArray::class.java
            )
        )
        Mockito.`when`(bundle.getBooleanArray(ArgumentMatchers.anyString())).thenAnswer(get)
        Mockito.doAnswer(put).`when`(bundle).putLongArray(
            ArgumentMatchers.anyString(), ArgumentMatchers.any(
                LongArray::class.java
            )
        )
        Mockito.`when`(bundle.getLongArray(ArgumentMatchers.anyString())).thenAnswer(get)
        Mockito.doAnswer(put).`when`(bundle).putDoubleArray(
            ArgumentMatchers.anyString(), ArgumentMatchers.any(
                DoubleArray::class.java
            )
        )
        Mockito.`when`(bundle.getDoubleArray(ArgumentMatchers.anyString())).thenAnswer(get)
        Mockito.doAnswer(put).`when`(bundle).putIntArray(
            ArgumentMatchers.anyString(), ArgumentMatchers.any(
                IntArray::class.java
            )
        )
        Mockito.`when`(bundle.getIntArray(ArgumentMatchers.anyString())).thenAnswer(get)
        Mockito.doAnswer(put).`when`(bundle).putInt(ArgumentMatchers.anyString(), ArgumentMatchers.anyInt())
        Mockito.`when`(bundle.getInt(ArgumentMatchers.anyString())).thenAnswer(get)
        Mockito.`when`(bundle.getInt(ArgumentMatchers.anyString(), ArgumentMatchers.anyInt())).thenAnswer(getOrDefault)
        Mockito.doAnswer(unsupported).`when`(bundle).putAll(ArgumentMatchers.any(Bundle::class.java))
        Mockito.`when`(bundle.hasFileDescriptors()).thenAnswer(unsupported)
        Mockito.doAnswer(put).`when`(bundle).putShort(ArgumentMatchers.anyString(), ArgumentMatchers.anyShort())
        Mockito.`when`(bundle.getShort(ArgumentMatchers.anyString())).thenAnswer(get)
        Mockito.`when`(bundle.getShort(ArgumentMatchers.anyString(), ArgumentMatchers.anyShort()))
            .thenAnswer(getOrDefault)
        Mockito.doAnswer(put).`when`(bundle).putFloat(ArgumentMatchers.anyString(), ArgumentMatchers.anyFloat())
        Mockito.`when`(bundle.getFloat(ArgumentMatchers.anyString())).thenAnswer(get)
        Mockito.`when`(bundle.getFloat(ArgumentMatchers.anyString(), ArgumentMatchers.anyFloat()))
            .thenAnswer(getOrDefault)
        Mockito.doAnswer(put).`when`(bundle).putCharSequence(
            ArgumentMatchers.anyString(), ArgumentMatchers.any(
                CharSequence::class.java
            )
        )
        Mockito.`when`(bundle.getCharSequence(ArgumentMatchers.anyString())).thenAnswer(get)
        Mockito.`when`(
            bundle.getCharSequence(
                ArgumentMatchers.anyString(), ArgumentMatchers.any(
                    CharSequence::class.java
                )
            )
        ).thenAnswer(getOrDefault)
        Mockito.doAnswer(put).`when`(bundle).putBundle(
            ArgumentMatchers.anyString(), ArgumentMatchers.any(
                Bundle::class.java
            )
        )
        Mockito.`when`(bundle.getBundle(ArgumentMatchers.anyString())).thenAnswer(get)
        Mockito.doAnswer(put).`when`(bundle).putParcelable(
            ArgumentMatchers.anyString(), ArgumentMatchers.any(
                Parcelable::class.java
            )
        )
        Mockito.`when`<Any?>(bundle.getParcelable(ArgumentMatchers.anyString())).thenAnswer(get)
        Mockito.doAnswer(put).`when`(bundle).putParcelableArray(
            ArgumentMatchers.anyString(), ArgumentMatchers.any(
                Array<Parcelable>::class.java
            )
        )
        Mockito.`when`(bundle.getParcelableArray(ArgumentMatchers.anyString())).thenAnswer(get)
        Mockito.doAnswer(put).`when`(bundle).putParcelableArrayList(
            ArgumentMatchers.anyString(), ArgumentMatchers.any(
                ArrayList::class.java
            ) as ArrayList<out Parcelable>?
        )
        Mockito.`when`(bundle.getParcelableArrayList<Parcelable>(ArgumentMatchers.anyString())).thenAnswer(get)
        Mockito.doAnswer(put).`when`(bundle).putSparseParcelableArray(
            ArgumentMatchers.anyString(), ArgumentMatchers.any(
                SparseArray::class.java
            ) as SparseArray<out Parcelable>?
        )
        Mockito.`when`(bundle.getSparseParcelableArray<Parcelable>(ArgumentMatchers.anyString())).thenAnswer(get)
        Mockito.doAnswer(put).`when`(bundle).putSerializable(
            ArgumentMatchers.anyString(), ArgumentMatchers.any(
                Serializable::class.java
            )
        )
        Mockito.`when`(bundle.getSerializable(ArgumentMatchers.anyString())).thenAnswer(get)
        Mockito.doAnswer(put).`when`(bundle).putIntegerArrayList(
            ArgumentMatchers.anyString(), ArgumentMatchers.any(
                ArrayList::class.java
            ) as ArrayList<Int>?
        )
        Mockito.`when`(bundle.getIntegerArrayList(ArgumentMatchers.anyString())).thenAnswer(get)
        Mockito.doAnswer(put).`when`(bundle).putStringArrayList(
            ArgumentMatchers.anyString(), ArgumentMatchers.any(
                ArrayList::class.java
            ) as ArrayList<String>?
        )
        Mockito.`when`(bundle.getStringArrayList(ArgumentMatchers.anyString())).thenAnswer(get)
        Mockito.doAnswer(put).`when`(bundle).putCharSequenceArrayList(
            ArgumentMatchers.anyString(), ArgumentMatchers.any(
                ArrayList::class.java
            ) as ArrayList<CharSequence>?
        )
        Mockito.`when`(bundle.getCharSequenceArrayList(ArgumentMatchers.anyString())).thenAnswer(get)
        Mockito.doAnswer(put).`when`(bundle).putCharArray(
            ArgumentMatchers.anyString(), ArgumentMatchers.any(
                CharArray::class.java
            )
        )
        Mockito.`when`(bundle.getCharArray(ArgumentMatchers.anyString())).thenAnswer(get)
        Mockito.doAnswer(put).`when`(bundle).putByteArray(
            ArgumentMatchers.anyString(), ArgumentMatchers.any(
                ByteArray::class.java
            )
        )
        Mockito.`when`(bundle.getByteArray(ArgumentMatchers.anyString())).thenAnswer(get)
        Mockito.doAnswer(put).`when`(bundle).putShortArray(
            ArgumentMatchers.anyString(), ArgumentMatchers.any(
                ShortArray::class.java
            )
        )
        Mockito.`when`(bundle.getShortArray(ArgumentMatchers.anyString())).thenAnswer(get)
        Mockito.doAnswer(put).`when`(bundle).putFloatArray(
            ArgumentMatchers.anyString(), ArgumentMatchers.any(
                FloatArray::class.java
            )
        )
        Mockito.`when`(bundle.getFloatArray(ArgumentMatchers.anyString())).thenAnswer(get)
        Mockito.doAnswer(put).`when`(bundle).putCharSequenceArray(
            ArgumentMatchers.anyString(), ArgumentMatchers.any(
                Array<CharSequence>::class.java
            )
        )
        Mockito.`when`(bundle.getCharSequenceArray(ArgumentMatchers.anyString())).thenAnswer(get)
        return bundle
    }

    companion object {
        val creator: Parcelable.Creator<Bundle> = object : Parcelable.Creator<Bundle> {
            override fun createFromParcel(`in`: Parcel): Bundle? {
                return `in`.readBundle()
            }

            override fun newArray(size: Int): Array<Bundle?> {
                return arrayOfNulls(size)
            }
        }
    }
}