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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * class for maintaining a registry of all spring configs
 */
public class SpringConfigRegistry {

  private static SpringConfigRegistry instance;

  public static SpringConfigRegistry getInstance() {
    if (instance == null) {
      instance = new SpringConfigRegistry(true);
    }
    return instance;
  }

  private final Map<SpringConfig, String> mSpringConfigMap;

  /**
   * constructor for the SpringConfigRegistry
   */
  @VisibleForTesting
  SpringConfigRegistry(boolean includeDefaultEntry) {
    mSpringConfigMap = Maps.newHashMap();
    if (includeDefaultEntry) {
      addSpringConfig(SpringConfig.defaultConfig, "default config");
    }
  }

  /**
   * add a SpringConfig to the registry
   *
   * @param springConfig SpringConfig to add to the registry
   * @param configName name to give the SpringConfig in the registry
   * @return true if the SpringConfig was added, false if a config with that name is already
   *    present.
   */
  public boolean addSpringConfig(SpringConfig springConfig, String configName) {
    if (mSpringConfigMap.containsKey(Preconditions.checkNotNull(springConfig))) {
      return false;
    }
    mSpringConfigMap.put(springConfig, Preconditions.checkNotNull(configName));
    return true;
  }

  /**
   * remove a specific SpringConfig from the registry
   * @param springConfig the of the SpringConfig to remove
   * @return true if the SpringConfig was removed, false if it was not present.
   */
  public boolean removeSpringConfig(SpringConfig springConfig) {
    return mSpringConfigMap.remove(Preconditions.checkNotNull(springConfig)) != null;
  }

  /**
   * retrieve all SpringConfig in the registry
   * @return a list of all SpringConfig
   */
  public ImmutableMap<SpringConfig, String> getAllSpringConfig() {
    return ImmutableMap.copyOf(mSpringConfigMap);
  }

  /**
   * clear all SpringConfig in the registry
   */
  public void removeAllSpringConfig() {
    mSpringConfigMap.clear();
  }
}

