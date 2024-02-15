package com.example.switchykmmsdk

import com.example.switchykmmsdk.Network.APIAuthorizationDelegate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidGreetingTest {

    @Test
    fun testExample() {
        assertTrue("Check Android is mentioned", Greeting().greet().contains("Android"))

        val sdk = SwitchyKMMSDK(null, object: APIAuthorizationDelegate {
            override suspend fun getAccessToken(): String {
                return "eyJraWQiOiJMZTZpVEtObUZvMmNiakVmKzhWYVJzejRDK0lxRTZpWG4yZGRFSDVHc2hJPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJhZGM1MjdmYy05MmMwLTRkMDgtYjZhZS0wMjE1ZmQ1Yjk0YzUiLCJldmVudF9pZCI6IjY2N2ViM2EyLWE5YjAtNDQ3NC04YTdkLWE2OTM2NTY2YjY2ZiIsInRva2VuX3VzZSI6ImFjY2VzcyIsInNjb3BlIjoiYXdzLmNvZ25pdG8uc2lnbmluLnVzZXIuYWRtaW4iLCJhdXRoX3RpbWUiOjE3MDU4MTc2ODIsImlzcyI6Imh0dHBzOlwvXC9jb2duaXRvLWlkcC5hcC1zb3V0aC0xLmFtYXpvbmF3cy5jb21cL2FwLXNvdXRoLTFfUzZxaEhMdnB3IiwiZXhwIjoxNzA2NzgxODk4LCJpYXQiOjE3MDY3NzgyOTgsImp0aSI6IjFhYjA5ZTc1LTNiOTEtNDBhOS1hZDRhLWFkNjU3OTNhMjk1MCIsImNsaWVudF9pZCI6IjNudTlvcDQ5Nm4xaWFuNGh2Y21mMXZkZTQwIiwidXNlcm5hbWUiOiJ0ZXN0In0.aYPR_d--DKAeES43diF_4AG2mLjJ5oxKNf9AVI3e0-jq-y0G8hpg7e-JeW1kZHHd_uMlVHVHvNt4fZAlPA1kbl7_6fga1Pyn6wUtb6TxJuCqOfSqAEn59SUOiNAGhVf9pNRset1IAlH7mu0gYlwY1LZ4hylUWKJKtex11NaRQiLAumNmgzEl4gids74ueLzl4YkkF0pe_lsMtxwoHghlmxmz9_mOKOx23FbWlnEPnsM3BgQDz8kH7Lw0EfscDQGJF8juca66xK2fO3mM1D0FNwA6u-Yh8kWzIEJx5FEO-wY6D66_9lqGO4MS3XN8hwW7odpcr7_N-mfS8gwYLNZ9qA"
            }
        })

        runBlocking {
            val launches = sdk.getAllLaunches()
            println(launches)

            val energyData = sdk.getEnergyData(1703701800, 1704083429)
            print(energyData)

            val powerUsage = sdk.getPowerUsage(1703701800, 1704083429)
            print(powerUsage)
        }
    }
}