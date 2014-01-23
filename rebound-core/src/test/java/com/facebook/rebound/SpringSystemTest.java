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
import org.mockito.InOrder;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SpringSystemTest {

  private BaseSpringSystem mSpringSystemSpy;
  private Spring mMockSpring;

  @Before
  public void beforeEach() {
    SpringLooper testSpringLooper = new SpringLooper() {
      private boolean mStarted;

      @Override
      public void start() {
        if (mStarted) {
          return;
        }
        mStarted = true;
        while (mStarted) {
          mSpringSystemSpy.loop();
        }
      }

      @Override
      public void stop() {
        mStarted = false;
      }
    };
    SpringClock clockSpy = spy(new SpringClock() {
      private int time = 0;
      @Override
      public long now() {
        return time++;
      }
    });
    when(clockSpy.now()).thenReturn(1L, 2L, 3L, 4L, 5L);
    mSpringSystemSpy = spy(new BaseSpringSystem(
        clockSpy,
        testSpringLooper));
    // NB: make sure the runnable calls the spy
    mMockSpring = mock(Spring.class);
    when(mMockSpring.getId()).thenReturn("spring_id");
  }

  @Test
  public void testRegisterSpringOnCreation() {
    Spring spring = mSpringSystemSpy.createSpring();
    verify(mSpringSystemSpy).registerSpring(spring);
  }

  @Test
  public void testCreateSpringWithoutName() {
    Spring spring = mSpringSystemSpy.createSpring();
    assertNotNull(spring.getId());
    assertEquals(spring, mSpringSystemSpy.getSpringById(spring.getId()));
    assertEquals(1, mSpringSystemSpy.getAllSprings().size());
  }

  @Test
  public void testLoop() {
    mSpringSystemSpy.registerSpring(mMockSpring);
    when(mMockSpring.systemShouldAdvance()).thenReturn(true, false);
    mSpringSystemSpy.activateSpring(mMockSpring.getId());
    verify(mSpringSystemSpy).advance(1, 1);
    verify(mMockSpring).advance(0.001,0.001);
    assertTrue(mSpringSystemSpy.getIsIdle());
  }

  @Test
  public void testLoopWithMultiplePassesRunsAndTerminates() {
    InOrder inOrder = inOrder(mMockSpring, mSpringSystemSpy);
    when(mMockSpring.systemShouldAdvance()).thenReturn(true, true, true, false);

    mSpringSystemSpy.registerSpring(mMockSpring);
    mSpringSystemSpy.activateSpring(mMockSpring.getId());

    inOrder.verify(mSpringSystemSpy).loop();
    inOrder.verify(mSpringSystemSpy).advance(1L, 1L);
    inOrder.verify(mMockSpring).advance(0.001, 0.001);

    inOrder.verify(mSpringSystemSpy).loop();
    inOrder.verify(mSpringSystemSpy).advance(2L, 1L);
    inOrder.verify(mMockSpring).advance(0.002, 0.001);

    inOrder.verify(mSpringSystemSpy).loop();
    inOrder.verify(mSpringSystemSpy).advance(3L, 1L);
    inOrder.verify(mMockSpring).advance(0.003, 0.001);

    inOrder.verify(mSpringSystemSpy).loop();
    inOrder.verify(mSpringSystemSpy).advance(4L, 1L);
    inOrder.verify(mMockSpring, never()).advance(0.004, 0.001);

    inOrder.verify(mSpringSystemSpy, never()).loop();
    inOrder.verify(mSpringSystemSpy, never()).advance(5L, 1L);

    assertTrue(mSpringSystemSpy.getIsIdle());
  }

  @Test
  public void testSpringSystemListener() {
    SpringSystemListener listener = spy(new SpringSystemListener() {
      @Override
      public void onBeforeIntegrate(BaseSpringSystem springSystem) {
      }

      @Override
      public void onAfterIntegrate(BaseSpringSystem springSystem) {
      }
    });

    InOrder inOrder = inOrder(listener);
    mSpringSystemSpy.registerSpring(mMockSpring);
    when(mMockSpring.systemShouldAdvance()).thenReturn(true, false);

    mSpringSystemSpy.addListener(listener);
    mSpringSystemSpy.activateSpring(mMockSpring.getId());

    inOrder.verify(listener).onBeforeIntegrate(mSpringSystemSpy);
    inOrder.verify(listener).onAfterIntegrate(mSpringSystemSpy);
    inOrder.verify(listener).onBeforeIntegrate(mSpringSystemSpy);
    inOrder.verify(listener).onAfterIntegrate(mSpringSystemSpy);

    mSpringSystemSpy.removeListener(listener);

    mSpringSystemSpy.activateSpring(mMockSpring.getId());
    inOrder.verify(listener, never()).onBeforeIntegrate(mSpringSystemSpy);
    inOrder.verify(listener, never()).onAfterIntegrate(mSpringSystemSpy);
  }

  @Test
  public void testActivatingAndDeactivatingSpring() {
    when(mMockSpring.systemShouldAdvance()).thenReturn(true, false, false);
    InOrder inOrder = inOrder(mMockSpring, mSpringSystemSpy);
    mSpringSystemSpy.registerSpring(mMockSpring);
    assertTrue(mSpringSystemSpy.getIsIdle());

    mSpringSystemSpy.activateSpring(mMockSpring.getId());
    inOrder.verify(mSpringSystemSpy).advance(1L, 1L);
    inOrder.verify(mMockSpring).systemShouldAdvance();
    inOrder.verify(mMockSpring).advance(0.001, 0.001);

    inOrder.verify(mSpringSystemSpy).advance(2L, 1L);
    inOrder.verify(mMockSpring).systemShouldAdvance();
    inOrder.verify(mMockSpring, never()).advance(0.002, 0.001);
    assertTrue(mSpringSystemSpy.getIsIdle());

    mSpringSystemSpy.loop();
    inOrder.verify(mSpringSystemSpy).advance(3L, 1L);
    inOrder.verify(mMockSpring, never()).systemShouldAdvance();
    inOrder.verify(mMockSpring, never()).advance(0.003, 0.001);
    assertTrue(mSpringSystemSpy.getIsIdle());

    mSpringSystemSpy.activateSpring(mMockSpring.getId());
    inOrder.verify(mSpringSystemSpy).advance(4L, 1L);
    inOrder.verify(mMockSpring).systemShouldAdvance();
    inOrder.verify(mMockSpring, never()).advance(0.004, 0.001);
    assertTrue(mSpringSystemSpy.getIsIdle());

    mSpringSystemSpy.loop();
    inOrder.verify(mSpringSystemSpy).advance(5L, 1L);
    inOrder.verify(mMockSpring, never()).systemShouldAdvance();
    inOrder.verify(mMockSpring, never()).advance(0.005, 0.001);
    assertTrue(mSpringSystemSpy.getIsIdle());
  }


}

