Pod::Spec.new do |spec|
    spec.name                     = 'SwitchyKMM'
    spec.version                  = '1.0'
    spec.homepage                 = 'https://github.com/rajasekar259/SwitchyKMM'
    spec.source                   = { :git => 'https://github.com/rajasekar259/SwitchyKMM.git', :tag => spec.version.to_s }
    spec.source_files             = '*', 'shared/src/commonMain/kotlin/**/*', 'shared/src/iosMain/kotlin/**/*'
    spec.authors                  = ''
    spec.license                  = ''
    spec.summary                  = 'Some description for the Shared Module'
    spec.vendored_frameworks      = 'shared/build/cocoapods/framework/switchykmmsdk.framework'
    spec.libraries                = 'c++'
    spec.ios.deployment_target = '14.0'
                
                
    if !Dir.exist?('shared/build/cocoapods/framework/switchykmmsdk.framework') || Dir.empty?('shared/build/cocoapods/framework/switchykmmsdk.framework')
        raise "

        Kotlin framework 'switchykmmsdk' doesn't exist yet, so a proper Xcode project can't be generated.
        'pod install' should be executed after running ':generateDummyFramework' Gradle task:

            ./gradlew :shared:generateDummyFramework

        Alternatively, proper pod installation is performed during Gradle sync in the IDE (if Podfile location is set)"
    end
                
    spec.pod_target_xcconfig = {
        'KOTLIN_PROJECT_PATH' => ':shared',
        'PRODUCT_MODULE_NAME' => 'switchykmmsdk',
    }
                
    spec.script_phases = [
        {
            :name => 'Build shared',
            :execution_position => :before_compile,
            :shell_path => '/bin/sh',
            :script => <<-SCRIPT
                if [ "YES" = "$OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED" ]; then
                  echo "Skipping Gradle build task invocation due to OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED environment variable set to \"YES\""
                  exit 0
                fi
                set -ev
                REPO_ROOT="$PODS_TARGET_SRCROOT/shared"
                "$REPO_ROOT/../gradlew" -p "$REPO_ROOT" $KOTLIN_PROJECT_PATH:syncFramework \
                    -Pkotlin.native.cocoapods.platform=$PLATFORM_NAME \
                    -Pkotlin.native.cocoapods.archs="$ARCHS" \
                    -Pkotlin.native.cocoapods.configuration="$CONFIGURATION"
            SCRIPT
        }
    ]
                
end