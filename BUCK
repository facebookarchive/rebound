project_config(
  src_target = ':rebound',
  test_target = ':rebound_test',
  src_roots = ['src'],
  test_roots = ['test'],
)

java_library(
  name = 'rebound',
  srcs = glob(['src/**/*.java'], excludes = ['src/com/facebook/rebound/android/**/*.java']),
  visibility = [
    'PUBLIC'
  ],
)

java_test(
  name = 'rebound_test',
  srcs = glob(['test/**/*Test.java']),
  deps = [
    ':rebound',
    ':robolectric',
    ':mockito',
    ':junit',
    '//src/com/facebook/rebound/android:android',
  ],
  source_under_test = [':rebound', ],
  visibility = [
    '//:project_config',
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
