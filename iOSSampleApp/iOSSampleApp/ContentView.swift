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
        }
        .padding()
    }
}

#Preview {
    ContentView()
}

@Observable
class ContentViewModel: ObservableObject {
    var greetingText = "Hello, world"
    
    func loadData() {
        greetingText = Greeting().greet()
    }
}
