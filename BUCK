prebuilt_jar(
  name = 'mockito',
  binary_jar = 'libs/mockito-all-1.9.5.jar',
)

prebuilt_jar(
  name = 'robolectric',
  binary_jar = 'libs/robolectric-2.2-20130606.235928-4-jar-with-dependencies.jar',
)

prebuilt_jar(
  name = 'guava',
  binary_jar = 'libs/guava-14.0.1.jar',
)

prebuilt_jar(
  name = 'junit',
  binary_jar = 'libs/junit-4.11.jar',
)

android_resource(
  name = 'res',
  res = 'res',
  package = 'com.facebook.rebound',
  visibility = ['//:rebound'],
)

java_test(
  name = 'rebound_test',
  srcs = glob(['test/**/*Test.java']),
  visibility = ['//:rebound'],
  deps = [
    '//:guava',
    '//:robolectric',
    '//:mockito',
    '//:junit',
    '//:rebound'
  ],
  source_under_test = ['//:rebound'],
)

android_library(
  name = 'rebound',
  srcs = glob(['src/**/*.java']),
  deps = [
    '//:guava',
    '//:res'
  ],
  visibility = ['PUBLIC'],
)

project_config(
  src_target = '//:rebound',
  test_target = '//:rebound_test',
  src_roots = ['src'],
  test_roots = ['test'],
)