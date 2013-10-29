project_config(
  src_target = ':rebound_lib',
  test_target = ':rebound_test',
  src_roots = ['src'],
  test_roots = ['test'],
)

java_binary(
  name = 'rebound',
  deps = [
    ':rebound_lib',
  ],
)

java_library(
  name = 'rebound_lib',
  srcs = glob(['src/**/*.java'], excludes = ['src/com/facebook/rebound/android/**/*.java']),
  visibility = [
    'PUBLIC'
  ],
)

java_test(
  name = 'rebound_test',
  srcs = glob(['test/**/*Test.java']),
  deps = [
    ':rebound_lib',
    ':robolectric',
    ':mockito',
    ':junit',
    '//src/com/facebook/rebound/android:android',
  ],
  source_under_test = [
    ':rebound_lib',
  ],
)

prebuilt_jar(
  name = 'mockito',
  binary_jar = 'libs/mockito-all-1.9.5.jar',
)

prebuilt_jar(
  name = 'robolectric',
  binary_jar = 'libs/robolectric-2.2-20130606.235928-4-jar-with-dependencies.jar',
)

prebuilt_jar(
  name = 'junit',
  binary_jar = 'libs/junit-4.11.jar',
)
