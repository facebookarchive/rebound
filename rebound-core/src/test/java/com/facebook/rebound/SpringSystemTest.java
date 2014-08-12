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
  private SynchronousLooper mSynchronousLooper;
  private Spring mMockSpring;

  @Before
  public void beforeEach() {
    mSynchronousLooper = new SynchronousLooper();
    mSpringSystemSpy = spy(new BaseSpringSystem(mSynchronousLooper));
    mSynchronousLooper.setSpringSystem(mSpringSystemSpy);
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
    verify(mSpringSystemSpy, times(2)).advance(mSynchronousLooper.getTimeStep());
    verify(mMockSpring, times(1)).advance(mSynchronousLooper.getTimeStep() / 1000);
    assertTrue(mSpringSystemSpy.getIsIdle());
  }

  @Test
  public void testLoopWithMultiplePassesRunsAndTerminates() {
    InOrder inOrder = inOrder(mMockSpring, mSpringSystemSpy);
    when(mMockSpring.systemShouldAdvance()).thenReturn(true, true, true, false);

    mSpringSystemSpy.registerSpring(mMockSpring);
    mSpringSystemSpy.activateSpring(mMockSpring.getId());

    double stepMillis = mSynchronousLooper.getTimeStep();
    double stepSeconds = mSynchronousLooper.getTimeStep() / 1000;

    inOrder.verify(mSpringSystemSpy, times(1)).advance(stepMillis);
    inOrder.verify(mMockSpring, times(1)).advance(stepSeconds);
    inOrder.verify(mSpringSystemSpy, times(1)).advance(stepMillis);
    inOrder.verify(mMockSpring, times(1)).advance(stepSeconds);
    inOrder.verify(mSpringSystemSpy, times(1)).advance(stepMillis);
    inOrder.verify(mMockSpring, times(1)).advance(stepSeconds);
    inOrder.verify(mSpringSystemSpy, times(1)).advance(stepMillis); // one extra pass through the system

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

    double stepMillis = mSynchronousLooper.getTimeStep();
    double stepSeconds = mSynchronousLooper.getTimeStep() / 1000;

    mSpringSystemSpy.activateSpring(mMockSpring.getId());
    inOrder.verify(mSpringSystemSpy).advance(stepMillis);
    inOrder.verify(mMockSpring).systemShouldAdvance();
    inOrder.verify(mMockSpring).advance(stepSeconds);

    inOrder.verify(mSpringSystemSpy).advance(stepMillis);
    inOrder.verify(mMockSpring).systemShouldAdvance();
    inOrder.verify(mMockSpring, never()).advance(stepSeconds);
    assertTrue(mSpringSystemSpy.getIsIdle());

    mSpringSystemSpy.loop(stepMillis);
    inOrder.verify(mSpringSystemSpy).advance(stepMillis);
    inOrder.verify(mMockSpring, never()).systemShouldAdvance();
    inOrder.verify(mMockSpring, never()).advance(stepSeconds);
    assertTrue(mSpringSystemSpy.getIsIdle());

    mSpringSystemSpy.activateSpring(mMockSpring.getId());
    inOrder.verify(mSpringSystemSpy).advance(stepMillis);
    inOrder.verify(mMockSpring).systemShouldAdvance();
    inOrder.verify(mMockSpring, never()).advance(stepSeconds);
    assertTrue(mSpringSystemSpy.getIsIdle());

    mSpringSystemSpy.loop(stepMillis);
    inOrder.verify(mSpringSystemSpy).advance(stepMillis);
    inOrder.verify(mMockSpring, never()).systemShouldAdvance();
    inOrder.verify(mMockSpring, never()).advance(stepSeconds);
    assertTrue(mSpringSystemSpy.getIsIdle());
  }

  @Test
  public void testCanAddListenersWhileIterating() {
    when(mMockSpring.systemShouldAdvance()).thenReturn(true, false);
    mSpringSystemSpy.addListener(new SimpleSpringSystemListener() {
      @Override
      public void onAfterIntegrate(BaseSpringSystem springSystem) {
        springSystem.addListener(new SimpleSpringSystemListener());
      }
    });
    mSpringSystemSpy.addListener(new SimpleSpringSystemListener());
    mSpringSystemSpy.loop(1);
  }

  @Test
  public void testCanRemoveListenersWhileIterating() {
    when(mMockSpring.systemShouldAdvance()).thenReturn(true, true, false);
    final SimpleSpringSystemListener nextListener = new SimpleSpringSystemListener();
    mSpringSystemSpy
        .addListener(new SimpleSpringSystemListener() {
          @Override
          public void onAfterIntegrate(BaseSpringSystem springSystem) {
            springSystem.removeListener(nextListener);
          }
        });
    mSpringSystemSpy
        .addListener(nextListener);
    mSpringSystemSpy.loop(1);
    mSpringSystemSpy.loop(1);
    mSpringSystemSpy.loop(1);
  }

  private class SimpleSpringSystemListener implements SpringSystemListener {
    @Override
    public void onBeforeIntegrate(BaseSpringSystem springSystem) {

    }

    @Override
    public void onAfterIntegrate(BaseSpringSystem springSystem) {

    }
  }
}

