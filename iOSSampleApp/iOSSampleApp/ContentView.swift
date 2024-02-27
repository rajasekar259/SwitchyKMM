//
//  ContentView.swift
//  iOSSampleApp
//
//  Created by rajasekar.r on 20/12/23.
//

import SwiftUI
import switchykmmsdk
import SwiftUIGraphs

struct ContentView: View {
    @StateObject var viewModel = ContentViewModel()
    @State var selectedChart: DYBarDataSet?
    
    var body: some View {
        VStack {
            DYBarChartView(barDataSets: [
                .init(fractions: [
                    .init(value: 4, gradient: .linearGradient(colors: [.blue, .red, .green], startPoint: .init(x: 0, y: 0), endPoint: .init(x: 0, y: 1)))], xAxisLabel: "0"),
                
                    .init(fractions: [
                        .init(value: 3, gradient: .linearGradient(colors: [.blue, .red, .green], startPoint: .init(x: 0, y: 0), endPoint: .init(x: 0, y: 1)))], xAxisLabel: "1"),
                
                .init(fractions: [
                    .init(value: 3, gradient: .linearGradient(colors: [.blue, .red, .green], startPoint: .init(x: 0, y: 0), endPoint: .init(x: 0, y: 1)))], xAxisLabel: "2"),
                
                    .init(fractions: [
                        .init(value: 4, gradient: .linearGradient(colors: [.blue, .red, .green], startPoint: .init(x: 0, y: 0), endPoint: .init(x: 0, y: 1)))], xAxisLabel: "0"),
                    
                        .init(fractions: [
                            .init(value: 3, gradient: .linearGradient(colors: [.blue, .red, .green], startPoint: .init(x: 0, y: 0), endPoint: .init(x: 0, y: 1)))], xAxisLabel: "1"),
                    
                    .init(fractions: [
                        .init(value: 3, gradient: .linearGradient(colors: [.blue, .red, .green], startPoint: .init(x: 0, y: 0), endPoint: .init(x: 0, y: 1)))], xAxisLabel: "2"),
                
                    .init(fractions: [
                        .init(value: 4, gradient: .linearGradient(colors: [.blue, .red, .green], startPoint: .init(x: 0, y: 0), endPoint: .init(x: 0, y: 1)))], xAxisLabel: "0"),
                    
                        .init(fractions: [
                            .init(value: 3, gradient: .linearGradient(colors: [.blue, .red, .green], startPoint: .init(x: 0, y: 0), endPoint: .init(x: 0, y: 1)))], xAxisLabel: "1"),
                    
                    .init(fractions: [
                        .init(value: 3, gradient: .linearGradient(colors: [.blue, .red, .green], startPoint: .init(x: 0, y: 0), endPoint: .init(x: 0, y: 1)))], xAxisLabel: "2"),
                
                    .init(fractions: [
                        .init(value: 4, gradient: .linearGradient(colors: [.blue, .red, .green], startPoint: .init(x: 0, y: 0), endPoint: .init(x: 0, y: 1)))], xAxisLabel: "0"),
                    
                        .init(fractions: [
                            .init(value: 3, gradient: .linearGradient(colors: [.blue, .red, .green], startPoint: .init(x: 0, y: 0), endPoint: .init(x: 0, y: 1)))], xAxisLabel: "1"),
                    
                    .init(fractions: [
                        .init(value: 3, gradient: .linearGradient(colors: [.blue, .red, .green], startPoint: .init(x: 0, y: 0), endPoint: .init(x: 0, y: 1)))], xAxisLabel: "2"),
            ], selectedBarDataSet: $selectedChart)
            
            Button(action: {
                viewModel.loadEnergyData()
            }, label: {
                Text("Load data")
            })
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
        "eyJraWQiOiJMZTZpVEtObUZvMmNiakVmKzhWYVJzejRDK0lxRTZpWG4yZGRFSDVHc2hJPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJhZGM1MjdmYy05MmMwLTRkMDgtYjZhZS0wMjE1ZmQ1Yjk0YzUiLCJldmVudF9pZCI6IjAyYWNmZDFmLWZiNjUtNGQ0NC04ZmYyLTJjNmRmNWE2Y2VlMCIsInRva2VuX3VzZSI6ImFjY2VzcyIsInNjb3BlIjoiYXdzLmNvZ25pdG8uc2lnbmluLnVzZXIuYWRtaW4iLCJhdXRoX3RpbWUiOjE3MDgwMDM5MDYsImlzcyI6Imh0dHBzOlwvXC9jb2duaXRvLWlkcC5hcC1zb3V0aC0xLmFtYXpvbmF3cy5jb21cL2FwLXNvdXRoLTFfUzZxaEhMdnB3IiwiZXhwIjoxNzA4NDA5NTcxLCJpYXQiOjE3MDg0MDU5NzEsImp0aSI6ImYyMzYxMmI3LWZjZjQtNDQ4Yy04YmU1LWMxOWZiZDRiZGFhYSIsImNsaWVudF9pZCI6IjNudTlvcDQ5Nm4xaWFuNGh2Y21mMXZkZTQwIiwidXNlcm5hbWUiOiJ0ZXN0In0.FgJxBnohprzrnl8htbVFSWAdoPvTRtWF9vHZ37t9CiOnuzqJXZYitP2Gu7fJ9nniYPFV_XhpRBlH2YFcVKA49-8X6FB02A2OhUhnhsOibN6F4O6dKGXPTscTt6AbUPC66FOKaUa6iKcx4Uz6DVjiAur97MmHb4nzzZz6X9IFr_D-crNAenlGHBnnmAsfFidvYuyW7icA9-tJFMnwatKsPckCPYpLTiXoj0e_wPyTSqNNRV1U8zhIu8iT_LacUPR4SNvmbxd4Hal3bsH_MZM41fzVQMLAU_9IqIInO0XgN3dAOUcd1OYwVbHyxrftVArw2nJNdKImc0q9d5_sa1mgLQ"
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
        
    }
    
    func loadEnergyData() {
        let now = Date().timeIntervalSince1970
        let fiveDaysBack = Date(timeIntervalSince1970: now - 60 * 60 * 24 * 5).timeIntervalSince1970
        
        Task {
            let items = try await sdk.getEnergyData(deviceId: deviceId, from: Int64(fiveDaysBack), to: Int64(now))
            print(items)
            
            let powerUsages = try await sdk.getPowerUsages(deviceId: deviceId, from: Int64(fiveDaysBack), to: Int64(now))
            print(powerUsages)
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
