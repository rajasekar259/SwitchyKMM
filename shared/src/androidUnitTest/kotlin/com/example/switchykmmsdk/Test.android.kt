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
                return "eyJraWQiOiJMZTZpVEtObUZvMmNiakVmKzhWYVJzejRDK0lxRTZpWG4yZGRFSDVHc2hJPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJhZGM1MjdmYy05MmMwLTRkMDgtYjZhZS0wMjE1ZmQ1Yjk0YzUiLCJldmVudF9pZCI6IjAyYWNmZDFmLWZiNjUtNGQ0NC04ZmYyLTJjNmRmNWE2Y2VlMCIsInRva2VuX3VzZSI6ImFjY2VzcyIsInNjb3BlIjoiYXdzLmNvZ25pdG8uc2lnbmluLnVzZXIuYWRtaW4iLCJhdXRoX3RpbWUiOjE3MDgwMDM5MDYsImlzcyI6Imh0dHBzOlwvXC9jb2duaXRvLWlkcC5hcC1zb3V0aC0xLmFtYXpvbmF3cy5jb21cL2FwLXNvdXRoLTFfUzZxaEhMdnB3IiwiZXhwIjoxNzA4NDA5NTcxLCJpYXQiOjE3MDg0MDU5NzEsImp0aSI6ImYyMzYxMmI3LWZjZjQtNDQ4Yy04YmU1LWMxOWZiZDRiZGFhYSIsImNsaWVudF9pZCI6IjNudTlvcDQ5Nm4xaWFuNGh2Y21mMXZkZTQwIiwidXNlcm5hbWUiOiJ0ZXN0In0.FgJxBnohprzrnl8htbVFSWAdoPvTRtWF9vHZ37t9CiOnuzqJXZYitP2Gu7fJ9nniYPFV_XhpRBlH2YFcVKA49-8X6FB02A2OhUhnhsOibN6F4O6dKGXPTscTt6AbUPC66FOKaUa6iKcx4Uz6DVjiAur97MmHb4nzzZz6X9IFr_D-crNAenlGHBnnmAsfFidvYuyW7icA9-tJFMnwatKsPckCPYpLTiXoj0e_wPyTSqNNRV1U8zhIu8iT_LacUPR4SNvmbxd4Hal3bsH_MZM41fzVQMLAU_9IqIInO0XgN3dAOUcd1OYwVbHyxrftVArw2nJNdKImc0q9d5_sa1mgLQ"
            }
        })

        runBlocking {
            val launches = sdk.getAllLaunches()
            println(launches)

            val energyData = sdk.fetchEnergyDataFromAPI("E1S002", 1707924716, 1708356716)
            print(energyData)

        }
    }
}