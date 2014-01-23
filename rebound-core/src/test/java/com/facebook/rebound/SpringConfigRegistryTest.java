/*
 *  Copyright (c) 2013, Facebook, Inc.
 *  All rights reserved.
 *
 *  This source code is licensed under the BSD-style license found in the
 *  LICENSE file in the root directory of this source tree. An additional grant
 *  of patent rights can be found in the PATENTS file in the same directory.
 *
 */

package com.facebook.rebound;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

public class SpringConfigRegistryTest {

  private static final String CONFIG_NAME = "test config";
  private static final double TENSION = SpringConfig.defaultConfig.tension;
  private static final double FRICTION = SpringConfig.defaultConfig.friction;

  private SpringConfigRegistry mSpringConfigRegistrySpy;

  @Before
  public void beforeEach() {
    mSpringConfigRegistrySpy = spy(new SpringConfigRegistry(false));
  }

  @Test
  public void testAddSpringConfig() {
    SpringConfig config = new SpringConfig(TENSION, FRICTION);
    mSpringConfigRegistrySpy.addSpringConfig(config, CONFIG_NAME);
    Map<SpringConfig, String> configs = mSpringConfigRegistrySpy.getAllSpringConfig();

    assertEquals(1, configs.size());
    assertTrue(configs.containsKey(config));
    String configName = configs.get(config);
    assertEquals(configName, CONFIG_NAME);
  }

  @Test
  public void testRemoveSpringConfig() {
    SpringConfig config = new SpringConfig(TENSION, FRICTION);
    mSpringConfigRegistrySpy.addSpringConfig(config, CONFIG_NAME);

    Map<SpringConfig, String> configs = mSpringConfigRegistrySpy.getAllSpringConfig();
    assertEquals(1, configs.size());
    mSpringConfigRegistrySpy.removeSpringConfig(config);

    configs = mSpringConfigRegistrySpy.getAllSpringConfig();
    assertEquals(0, configs.size());
  }

  @Test
  public void testRemoveAllSpringConfig() {
    SpringConfig configA = new SpringConfig(0, 0);
    SpringConfig configB = new SpringConfig(0, 0);
    SpringConfig configC = new SpringConfig(0, 0);

    mSpringConfigRegistrySpy.addSpringConfig(configA, "a");
    mSpringConfigRegistrySpy.addSpringConfig(configB, "b");
    mSpringConfigRegistrySpy.addSpringConfig(configC, "c");

    Map<SpringConfig, String> configs = mSpringConfigRegistrySpy.getAllSpringConfig();
    assertEquals(3, configs.size());

    mSpringConfigRegistrySpy.removeAllSpringConfig();
    configs = mSpringConfigRegistrySpy.getAllSpringConfig();
    assertEquals(0, configs.size());
  }
}
