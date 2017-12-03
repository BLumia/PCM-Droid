package net.blumia.pcm.privatecloudmusic

import android.util.Log
import okhttp3.*
import org.junit.Test

import org.junit.Assert.*
import java.io.IOException

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class OkHttpFetchTest {
    @Test
    fun okHttpFetch_isCorrect() {
        val httpClient = OkHttpClient()
        val formBody = FormBody.Builder()
                .add("do", "getfilelist")
                .build()
        val request = Request.Builder()
                .url("https://pcm.blumia.cn/api.php")
                .post(formBody)
                .build()
        httpClient.newCall(request).enqueue(object: okhttp3.Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                e?.printStackTrace()
            }

            override fun onResponse(call: Call?, response: Response?) {
                val result = response!!.body()!!.string()
                Log.e("result", result)
            }
        })
        assertEquals(4, 2 + 2)
    }
}
