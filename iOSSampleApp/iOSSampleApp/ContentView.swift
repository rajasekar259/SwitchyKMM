//
//  ContentView.swift
//  iOSSampleApp
//
//  Created by rajasekar.r on 20/12/23.
//

import SwiftUI
import switchykmmsdk

struct ContentView: View {
    @StateObject var viewModel = ContentViewModel()
    
    var body: some View {
        VStack {
            Image(systemName: "globe")
                .imageScale(.large)
                .foregroundStyle(.tint)
            Text(viewModel.greetingText)
            Button(action: {
                viewModel.loadData()
            }, label: {
                Text("Load Greeting")
            })
            Button(action: {
                viewModel.fetchNewer()
            }, label: {
                Text("Fetch New")
            })
            
            Button(action: {
                viewModel.fetchOlder()
            }, label: {
                Text("Fetch Old")
            })
            Button(action: {
                viewModel.fetchItems()
            }, label: {
                Text("Fetch")
            })
            ScrollView {
                ForEach(viewModel.items, id: \.id) { item in
                    Text(item.displayString)
                }
            }
        }
        .padding()
    }
}

#Preview {
    ContentView()
}


struct PowerUsage: Identifiable {
    var id: TimeInterval { time }
    let time: TimeInterval
    let usage: Double
    let displayString: String
    
    init(time: TimeInterval, usage: Double) {
        self.time = time
        self.usage = usage
        self.displayString = "\(usage)W on \(Date(timeIntervalSince1970: time / 1000))"
    }
}

class Authenticator: APIAuthorizationDelegate {
    private init() {}
    static let shared = Authenticator()
    func getAccessToken() async throws -> String? {
        "eyJraWQiOiJMZTZpVEtObUZvMmNiakVmKzhWYVJzejRDK0lxRTZpWG4yZGRFSDVHc2hJPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJhZGM1MjdmYy05MmMwLTRkMDgtYjZhZS0wMjE1ZmQ1Yjk0YzUiLCJldmVudF9pZCI6IjAyYWNmZDFmLWZiNjUtNGQ0NC04ZmYyLTJjNmRmNWE2Y2VlMCIsInRva2VuX3VzZSI6ImFjY2VzcyIsInNjb3BlIjoiYXdzLmNvZ25pdG8uc2lnbmluLnVzZXIuYWRtaW4iLCJhdXRoX3RpbWUiOjE3MDgwMDM5MDYsImlzcyI6Imh0dHBzOlwvXC9jb2duaXRvLWlkcC5hcC1zb3V0aC0xLmFtYXpvbmF3cy5jb21cL2FwLXNvdXRoLTFfUzZxaEhMdnB3IiwiZXhwIjoxNzA4MDA3NTA2LCJpYXQiOjE3MDgwMDM5MDYsImp0aSI6IjUyN2U2MTAxLTE4NTItNDQyYS1iZWZmLWJiOWZhMmY4ZmQxYSIsImNsaWVudF9pZCI6IjNudTlvcDQ5Nm4xaWFuNGh2Y21mMXZkZTQwIiwidXNlcm5hbWUiOiJ0ZXN0In0.iIlK1iprf7Ximvl4pXhw0p-nbAbNJc4V4HujGRoH_u5d5ajARNHK4IE0-LtYJIPOduWi8cX_nr31eWXvzOeTlrwl8BNRu0vglovo9U3RGRt9P-fAqE8geyF0-mzIZYvNry5DTOw19op61zt8wA6_O2khSzgbL74U1IBMFzNCOnyYf89LShMtWANaP2GiQldhUMvLUh0Riiqzgk_VzIVfG_4_tawr9_b8XmsoH7dezxzzYQClp1r6MPprCWwZUuTRF2J-r3gm-COkHdXRkgOoxfOegS5tmddrnA6bR4Y3wmxYBwpstT_kccMJ9bgKOcoepT8joJBEWgcT-kEdUl5_Bw"
    }
}

@Observable
class ContentViewModel: ObservableObject {
    var greetingText = "Hello, world"
    var items: [PowerUsage] = [.init(time: 2834545, usage: 45)]
    
    let deviceId = "E1S002"
    
    
    var _sdk: SwitchyKMMSDK?
    var sdk: SwitchyKMMSDK {
        if let _sdk { return _sdk }
        _sdk = SwitchyKMMSDK(driverFactory: DatabaseDriverFactory(dbName: "test"), authDelegate: Authenticator.shared)
        return _sdk!
    }
    
    func getItems() {
        Task {
            let items = try? await sdk.getAllHousePowerUsages()
            self.items = items?.map({
                PowerUsage(
                    time: TimeInterval($0.epochMilliSeconds),
                    usage: Double($0.power)
                )
            }) ?? []
        }
    }
    
    func loadData() {
        greetingText = Greeting().greet()
        getItems()
    }
    
    func fetchNewer() {
        Task {
            try await sdk.fetchNewerPowerUsage(deviceId:deviceId)
            getItems()
        }
    }
    
    func fetchOlder() {
        Task {
            try await sdk.fetchOlderPowerUsage(deviceId:deviceId)
            getItems()
        }
    }
    
    func fetchItems() {
        Task {
            getItems()
        }
    }
}
