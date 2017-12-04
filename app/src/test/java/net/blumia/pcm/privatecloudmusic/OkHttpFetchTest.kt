package net.blumia.pcm.privatecloudmusic

import okhttp3.*
import okhttp3.mockwebserver.MockWebServer
import org.junit.Test

import org.junit.Assert.*
import java.io.IOException
import okhttp3.mockwebserver.MockResponse


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class OkHttpFetchTest {
    @Test
    fun okHttpFetch_isCorrect() {
        var server = MockWebServer()
        server.enqueue(MockResponse().setBody("[Artist/Kevin%20Penkin]"))
        server.start()

        val baseUrl = server.url("/api.php")

        val httpClient = OkHttpClient()
        val formBody = FormBody.Builder()
                .add("do", "getfilelist")
                .build()
        val request = Request.Builder()
                .url(baseUrl)
                .post(formBody)
                .build()

        val response = httpClient.newCall(request).execute()
        val result = response!!.body()!!.string()
        //System.out.println("Get:" + result)
        assertEquals("[Artist/Kevin%20Penkin]", result)
        /*
        httpClient.newCall(request).enqueue(object: okhttp3.Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                e?.printStackTrace()
            }

            override fun onResponse(call: Call?, response: Response?) {
                val result = response!!.body()!!.string()
                System.out.println(result)
            }
        })*/
    }

    @Test
    fun realFetch_isCorrect() {
        val httpClient = OkHttpClient()
        val formBody = FormBody.Builder()
                .add("do", "getfilelist")
                .add("folder", "GameOST/")
                .build()
        val request = Request.Builder()
                .url("https://pcm.blumia.cn/api.php")
                .post(formBody)
                .build()

        /*
        httpClient.newCall(request).enqueue(object: okhttp3.Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                e?.printStackTrace()
            }

            override fun onResponse(call: Call?, response: Response?) {
                val result = response!!.body()!!.string()
                System.out.println(result)
            }
        })
        Thread.sleep(2000)
        */
        val response = httpClient.newCall(request).execute()
        val result = response!!.body()!!.string()
        System.out.println("Get:" + result)

    }
}
