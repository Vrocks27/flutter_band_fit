#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint flutter_band_fit.podspec` to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'flutter_band_fit'
  s.version          = '0.0.1'
  s.summary          = 'A new Flutter project.'
  s.description      = <<-DESC
A new Flutter project.
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'email@example.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.dependency 'Flutter'
  s.platform = :ios, '9.0'

  # Flutter.framework does not contain a i386 slice.
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386' }
  s.swift_version = '5.0'

  s.preserve_paths = 'UTESmartBandApi.framework'
  s.xcconfig = { 'OTHER_LDFLAGS' => '-framework UTESmartBandApi' }
  s.vendored_frameworks = 'UTESmartBandApi.framework'


end


=begin
  #s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES'}
  #s.swift_versions = ['5.0', '5.6']
  #s.ios.vendored_frameworks = "**/iOS/UTESmartBandApi.framework"
=end