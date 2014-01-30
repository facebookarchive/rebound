# A special build that includes rebound-android but not the resources so that a jar file can be
# created for distribution to users who do not use the gradle aar and don't need the utils like
# SpringConfiguratorView.
java_binary(
  name = 'rebound',
  deps = ['//rebound-android:src-no-res'],
  visibility = ['PUBLIC'],
)
