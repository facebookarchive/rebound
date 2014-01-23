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

import com.facebook.rebound.android.AndroidSpringLooper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class SpringSystemTest {

  private SpringSystem mSpringSystem;
  private Spring mMockSpring;
  private SpringClock mClockSpy;
  private SpringLooper mSpringLooper;

  @Before
  public void beforeEach() {
    mClockSpy = spy(new SpringClock());
    mSpringLooper = spy(new AndroidSpringLooper());
    when(mClockSpy.now()).thenReturn(1L, 2L, 3L, 4L, 5L);
    mSpringSystem = spy(new SpringSystem(mClockSpy, mSpringLooper));
    mMockSpring = mock(Spring.class);
    when(mMockSpring.getId()).thenReturn("spring_id");
  }

  @Test
  public void testRegisterSpringOnCreation() {
    Spring spring = mSpringSystem.createSpring();
    verify(mSpringSystem).registerSpring(spring);
  }

  @Test
  public void testCreateSpringWithoutName() {
    Spring spring = mSpringSystem.createSpring();
    assertNotNull(spring.getId());
    assertEquals(spring, mSpringSystem.getSpringById(spring.getId()));
    assertEquals(1, mSpringSystem.getAllSprings().size());
  }

  @Test
  public void testLoop() {
    mSpringSystem.registerSpring(mMockSpring);
    when(mMockSpring.systemShouldAdvance()).thenReturn(true, false);
    mSpringSystem.activateSpring(mMockSpring.getId());
    verify(mSpringSystem).advance(1, 1);
    verify(mMockSpring).advance(0.001,0.001);
    assertTrue(mSpringSystem.getIsIdle());
  }

  @Test
  public void testLoopWithMultiplePassesRunsAndTerminates() {
    InOrder inOrder = inOrder(mMockSpring, mSpringSystem);
    when(mMockSpring.systemShouldAdvance()).thenReturn(true, true, true, false);

    mSpringSystem.registerSpring(mMockSpring);
    mSpringSystem.activateSpring(mMockSpring.getId());

    inOrder.verify(mSpringSystem).loop();
    inOrder.verify(mSpringSystem).advance(1L, 1L);
    inOrder.verify(mMockSpring).advance(0.001, 0.001);

    inOrder.verify(mSpringSystem).loop();
    inOrder.verify(mSpringSystem).advance(2L, 1L);
    inOrder.verify(mMockSpring).advance(0.002, 0.001);

    inOrder.verify(mSpringSystem).loop();
    inOrder.verify(mSpringSystem).advance(3L, 1L);
    inOrder.verify(mMockSpring).advance(0.003, 0.001);

    inOrder.verify(mSpringSystem).loop();
    inOrder.verify(mSpringSystem).advance(4L, 1L);
    inOrder.verify(mMockSpring, never()).advance(0.004, 0.001);

    inOrder.verify(mSpringSystem, never()).loop();
    inOrder.verify(mSpringSystem, never()).advance(5L, 1L);

    assertTrue(mSpringSystem.getIsIdle());
  }

  @Test
  public void testSpringSystemListener() {
    SpringSystemListener listener = spy(new SpringSystemListener() {
      @Override
      public void onBeforeIntegrate(SpringSystem springSystem) {
      }

      @Override
      public void onAfterIntegrate(SpringSystem springSystem) {
      }
    });

    InOrder inOrder = inOrder(listener);
    mSpringSystem.registerSpring(mMockSpring);
    when(mMockSpring.systemShouldAdvance()).thenReturn(true, false);

    mSpringSystem.addListener(listener);
    mSpringSystem.activateSpring(mMockSpring.getId());

    inOrder.verify(listener).onBeforeIntegrate(mSpringSystem);
    inOrder.verify(listener).onAfterIntegrate(mSpringSystem);
    inOrder.verify(listener).onBeforeIntegrate(mSpringSystem);
    inOrder.verify(listener).onAfterIntegrate(mSpringSystem);

    mSpringSystem.removeListener(listener);

    mSpringSystem.activateSpring(mMockSpring.getId());
    inOrder.verify(listener, never()).onBeforeIntegrate(mSpringSystem);
    inOrder.verify(listener, never()).onAfterIntegrate(mSpringSystem);
  }

  @Test
  public void testActivatingAndDeactivatingSpring() {
    when(mMockSpring.systemShouldAdvance()).thenReturn(true, false, false);
    InOrder inOrder = inOrder(mMockSpring, mSpringSystem);
    mSpringSystem.registerSpring(mMockSpring);
    assertTrue(mSpringSystem.getIsIdle());

    mSpringSystem.activateSpring(mMockSpring.getId());
    inOrder.verify(mSpringSystem).advance(1L, 1L);
    inOrder.verify(mMockSpring).systemShouldAdvance();
    inOrder.verify(mMockSpring).advance(0.001, 0.001);

    inOrder.verify(mSpringSystem).advance(2L, 1L);
    inOrder.verify(mMockSpring).systemShouldAdvance();
    inOrder.verify(mMockSpring, never()).advance(0.002, 0.001);
    assertTrue(mSpringSystem.getIsIdle());

    mSpringSystem.loop();
    inOrder.verify(mSpringSystem).advance(3L, 1L);
    inOrder.verify(mMockSpring, never()).systemShouldAdvance();
    inOrder.verify(mMockSpring, never()).advance(0.003, 0.001);
    assertTrue(mSpringSystem.getIsIdle());

    mSpringSystem.activateSpring(mMockSpring.getId());
    inOrder.verify(mSpringSystem).advance(4L, 1L);
    inOrder.verify(mMockSpring).systemShouldAdvance();
    inOrder.verify(mMockSpring, never()).advance(0.004, 0.001);
    assertTrue(mSpringSystem.getIsIdle());

    mSpringSystem.loop();
    inOrder.verify(mSpringSystem).advance(5L, 1L);
    inOrder.verify(mMockSpring, never()).systemShouldAdvance();
    inOrder.verify(mMockSpring, never()).advance(0.005, 0.001);
    assertTrue(mSpringSystem.getIsIdle());
  }


}

