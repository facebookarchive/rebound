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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class SpringTest {

  private static final double TENSION = 230.2;
  private static final double FRICTION = 19;
  private static final double START_VALUE = 0f;
  private static final double END_VALUE = 1f;
  private static final double[] EXPECTED_POSITIONS_UNCLAMPED = {
      0.02350326962145051, 0.09013912023073196, 0.18531666042039596, 0.2967410888550004,
      0.4145189853782035, 0.5310291023265858, 0.6407121280050319, 0.7398112940647817,
      0.8260898294043233, 0.8985456177264777, 0.9571382208882482, 1.002538830281137,
      1.0359097802864248, 1.0587170239216486, 1.0725764126694235, 1.0791326923618418,
      1.0799687578592558, 1.0765418232895394, 1.0701426799489315, 1.0618740499181873,
      1.0526441242670932, 1.0431716321304505, 1.0339991616313917, 1.025511895911699,
      1.0179593973200416, 1.0114785391407843, 1.0061161244437307, 1.0018501302898977,
      0.998608863371096, 0.9962876059220026, 0.9947625680573757, 0.9939021470884736,
      0.9935756304073171, 0.9936595719950525, 0.9940421299433493, 0.9946256801487239,
      0.9953280258938787, 0.9960825102232976, 0.9968373130510945, 0.9975541822583256,
      0.9982068113187458, 0.9987790381514378, 0.9992630031435948, 0.9996573701910703,
      0.9999656841951263, 1.0001949123024465, 1.0003541944882257, 1.0004538117854302,
      1.000504367280912, 1.0005161655189725, 1.0004987696841499, 1.0004607123525702,
      1.000409334182166, 1.0003507251610428, 1.0002897445004184, 1.0002300975503762,
      1.0001744509010717, 1.000124569839086, 1.0000814653479198, 1.0000455407172641,
      1.000016730449907, 0.9999946264611708, 0.999978588520391, 0.9999678374804437,
      0.9999615310928505, 0.999958823139664, 0.9999589072653003, 0.9999589072653003,
      0.9999589072653003, 0.9999589072653003, 0.9999589072653003, 0.9999589072653003,
      0.9999589072653003, 0.9999589072653003, 0.9999589072653003, 0.9999589072653003,
      0.9999589072653003, 0.9999589072653003, 0.9999589072653003, 0.9999589072653003,
      0.9999589072653003, 0.9999589072653003, 0.9999589072653003, 0.9999589072653003,
      0.9999589072653003, 0.9999589072653003, 0.9999589072653003, 0.9999589072653003,
      0.9999589072653003, 0.9999589072653003, 0.9999589072653003, 0.9999589072653003,
      0.9999589072653003, 0.9999589072653003, 0.9999589072653003, 0.9999589072653003,
      0.9999589072653003, 0.9999589072653003, 0.9999589072653003, 0.9999589072653003,
  };
  private static final double[] EXPECTED_POSITIONS_CLAMPED = {
      0.02350326962145051, 0.09013912023073196, 0.18531666042039596, 0.2967410888550004,
      0.4145189853782035, 0.5310291023265858, 0.6407121280050319, 0.7398112940647817,
      0.8260898294043233, 0.8985456177264777, 0.9571382208882482, 1};

  private static final double[] EXPECTED_VELOCITIES_UNCLAMPED = {
      2.9787085330347476, 5.197427505815113, 6.571711546415007, 7.2534257444553765,
      7.389647056163013, 7.116482121037456, 6.5550604141989215, 5.809335110787, 4.965328995516549,
      4.0914847471027755, 3.2398123846080047, 2.4475668846708247, 1.7392321578676635,
      1.1286307504054793, 0.6210196308949527, 0.21506973077377356, -0.09534038966917527,
      -0.3195587262438851, -0.4688413177602538, -0.555282881508532, -0.5909853610083475,
      -0.5874440645207716, -0.555125396819726, -0.5032073132555667, -0.4394529648652968,
      -0.37018900941686794, -0.3003622481099588, -0.23365119172655993, -0.17261251543127643,
      -0.11884585066126811, -0.07316377409334163, -0.03575703538313702, -0.00634791659710436,
      0.01567292054562716, 0.031128691177765087, 0.040958793710573566, 0.04614085045084054,
      0.04763125152961558, 0.046322358256116185, 0.04301416293655951, 0.03839804864987598,
      0.033050298338501954, 0.027433125767422104, 0.021901204442189126, 0.01671192310344934,
      0.012037872319858822, 0.007980345903135427, 0.004582908385413513, 0.001844325165521582,
      -2.696314538921814E-4, -0.0018158026692998745, -0.0028648409656691523, -0.0034940169049269623,
      -0.0037815768758969986, -0.003802530553507281, -0.0036257063029244987, -0.003311891052007393,
      -0.002912864412823041, -0.002471141476778791, -0.002020251518540508, -0.0015853980326463435,
      -0.001184366764085854, -8.28570806909637E-4, -5.241439917651027E-4, -2.730146138682927E-4,
      -7.391035902437433E-5, 7.673835340267081E-5, 7.673835340267081E-5, 7.673835340267081E-5,
      7.673835340267081E-5, 7.673835340267081E-5, 7.673835340267081E-5, 7.673835340267081E-5,
      7.673835340267081E-5, 7.673835340267081E-5, 7.673835340267081E-5, 7.673835340267081E-5,
      7.673835340267081E-5, 7.673835340267081E-5, 7.673835340267081E-5, 7.673835340267081E-5,
      7.673835340267081E-5, 7.673835340267081E-5, 7.673835340267081E-5, 7.673835340267081E-5,
      7.673835340267081E-5, 7.673835340267081E-5, 7.673835340267081E-5, 7.673835340267081E-5,
      7.673835340267081E-5, 7.673835340267081E-5, 7.673835340267081E-5, 7.673835340267081E-5,
      7.673835340267081E-5, 7.673835340267081E-5, 7.673835340267081E-5, 7.673835340267081E-5,
      7.673835340267081E-5, 7.673835340267081E-5, 7.673835340267081E-5,
  };

  private static final double[] EXPECTED_VELOCITIES_CLAMPED = {
      2.9787085330347476, 5.197427505815113, 6.571711546415007, 7.2534257444553765,
      7.389647056163013, 7.116482121037456, 6.5550604141989215, 5.809335110787, 4.965328995516549,
      4.0914847471027755, 3.2398123846080047, 0
  };

  private BaseSpringSystem mSpringSystem;
  private Spring mSpring;

  @Before
  public void beforeEach() {
    mSpringSystem = mock(BaseSpringSystem.class);
    mSpring = spy(new Spring(mSpringSystem))
        .setRestDisplacementThreshold(0.0001)
        .setRestSpeedThreshold(0.0001);
  }

  @Test
  public void testSetupSpring() {
    mSpring
        .setSpringConfig(new SpringConfig(TENSION, FRICTION))
        .setCurrentValue(START_VALUE);

    assertTrue(mSpring.getSpringConfig().tension == TENSION);
    assertTrue(mSpring.getSpringConfig().friction == FRICTION);
    assertTrue(mSpring.getStartValue() == START_VALUE);
    assertTrue(mSpring.getCurrentValue() == START_VALUE);
    assertTrue(mSpring.getVelocity() == 0);
  }

  @Test
  public void testActivateSpringOnEndValueChange() {
    verify(mSpringSystem, never()).activateSpring(mSpring.getId());
    mSpring.setEndValue(1);
    verify(mSpringSystem).activateSpring(mSpring.getId());
  }

  @Test
  public void testSpringSimulation() {
    mSpring
        .setSpringConfig(new SpringConfig(TENSION, FRICTION))
        .setCurrentValue(START_VALUE)
        .setEndValue(END_VALUE);

    int i = 0;
    int simulatedMsPerFrame = 16;
    int runtime = simulatedMsPerFrame * 100;
    double[] positionValues = new double[100];
    double[] velocityValues = new double[100];
    while (i < runtime) {
      mSpring.advance(simulatedMsPerFrame / 1000.0);
      positionValues[i / simulatedMsPerFrame] = mSpring.getCurrentValue();
      velocityValues[i / simulatedMsPerFrame] = mSpring.getVelocity();
      i+=simulatedMsPerFrame;
    }
    assertArrayEquals(positionValues, EXPECTED_POSITIONS_UNCLAMPED, 0.0001);
    assertArrayEquals(velocityValues, EXPECTED_VELOCITIES_UNCLAMPED, 0.0001);
  }

  @Test
  public void testSpringListeners() {
    SpringListener listener = spy(new SimpleSpringListener());
    mSpring.addListener(listener);
    mSpring
        .setSpringConfig(new SpringConfig(TENSION, FRICTION))
        .setCurrentValue(START_VALUE)
        .setEndValue(END_VALUE);
    int i = 0;
    InOrder inOrder = inOrder(listener);
    int simulatedMsPerFrame = 16;
    int runtime = simulatedMsPerFrame * 3000;
    while (i < runtime) {
      mSpring.advance(simulatedMsPerFrame /1000.0);
      i += simulatedMsPerFrame;
    }

    inOrder.verify(listener).onSpringEndStateChange(mSpring);
    inOrder.verify(listener).onSpringActivate(mSpring);
    inOrder.verify(listener, times(66)).onSpringUpdate(mSpring);
    inOrder.verify(listener).onSpringAtRest(mSpring);
    inOrder.verify(listener, never()).onSpringUpdate(mSpring);
  }

  /**
   * This test for SpringListeners has been formatted with a specific case that has been known to
   * push a Spring out of rest right after having gone to rest.
   */
  @Test
  public void testSpringNoActivateAfterRest() {
    SpringListener listener = spy(new SimpleSpringListener());
    mSpring.addListener(listener);
    mSpring
        .setSpringConfig(new SpringConfig(628.4, 37.0))
        .setRestSpeedThreshold(0.005f)
        .setRestDisplacementThreshold(0.005f)
        .setCurrentValue(0f)
        .setEndValue(1f);
    InOrder inOrder = inOrder(listener);

    float[] frameTimes = new float[] {
        0.034f, 0.050f, 0.019f, 0.016f, 0.016f, 0.017f, 0.031f, 0.016f, 0.017f, 0.034f,
        0.018f, 0.017f, 0.032f, 0.014f, 0.016f, 0.033f, 0.017f, 0.016f, 0.036f
    };

    float totalTime = 0;
    for (int i = 0; i < frameTimes.length; ++i) {
      totalTime += frameTimes[i];
      mSpring.advance(frameTimes[i]);
    }

    inOrder.verify(listener).onSpringEndStateChange(mSpring);
    inOrder.verify(listener).onSpringActivate(mSpring);
    inOrder.verify(listener, times(17)).onSpringUpdate(mSpring);
    inOrder.verify(listener).onSpringAtRest(mSpring);
    inOrder.verify(listener, never()).onSpringActivate(mSpring);
    inOrder.verify(listener, never()).onSpringUpdate(mSpring);
    inOrder.verify(listener, never()).onSpringAtRest(mSpring);
  }

  @Test
  public void testSpringNoActivateAfterSetAtRestCall() {
    SpringListener listener = spy(new SimpleSpringListener());
    mSpring.addListener(listener);
    mSpring
        .setSpringConfig(new SpringConfig(393.1, 31))
        .setRestSpeedThreshold(0.001f)
        .setRestDisplacementThreshold(0.001f)
        .setCurrentValue(0f)
        .setEndValue(45.f);
    InOrder inOrder = inOrder(listener);

    int numOfFrameBeforeStop = 5;
    int totalUpdateNo = numOfFrameBeforeStop + 1;

    float[] frameTimes = new float[] {
        0.034f, 0.050f, 0.019f, 0.016f, 0.016f, 0.017f, 0.031f, 0.016f, 0.017f, 0.034f,
        0.018f, 0.017f, 0.032f, 0.014f, 0.016f, 0.033f, 0.017f, 0.016f, 0.036f
    };

    float totalTime = 0;
    for (int i = 0; i < frameTimes.length; ++i) {
      totalTime += frameTimes[i];
      mSpring.advance(frameTimes[i]);
      if ((i + 1) == numOfFrameBeforeStop) {
        mSpring.setAtRest();
      }
    }

    inOrder.verify(listener).onSpringEndStateChange(mSpring);
    inOrder.verify(listener).onSpringActivate(mSpring);
    inOrder.verify(listener, times(totalUpdateNo)).onSpringUpdate(mSpring);
    inOrder.verify(listener).onSpringAtRest(mSpring);
    inOrder.verify(listener, never()).onSpringActivate(mSpring);
    inOrder.verify(listener, never()).onSpringUpdate(mSpring);
    inOrder.verify(listener, never()).onSpringAtRest(mSpring);
  }

  @Test
  public void testOvershootClamping() {
    mSpring
        .setOvershootClampingEnabled(true)
        .setSpringConfig(new SpringConfig(TENSION, FRICTION))
        .setCurrentValue(START_VALUE)
        .setEndValue(END_VALUE);

    int i = 0;
    int simulatedMsPerFrame = 16;
    int runtime = simulatedMsPerFrame * 12;
    double[] positionValues = new double[12];
    double[] velocityValues = new double[12];
    while (i < runtime) {
      mSpring.advance(simulatedMsPerFrame / 1000.0);
      if (mSpring.getCurrentValue() > 1) {
        positionValues[i / simulatedMsPerFrame] = 1;
        velocityValues[i / simulatedMsPerFrame] = 0;
      } else {
        positionValues[i / simulatedMsPerFrame] = mSpring.getCurrentValue();
        velocityValues[i / simulatedMsPerFrame] = mSpring.getVelocity();
      }
      i+=simulatedMsPerFrame;
    }
    assertArrayEquals(positionValues, EXPECTED_POSITIONS_CLAMPED, 0.0001);
    assertArrayEquals(velocityValues, EXPECTED_VELOCITIES_CLAMPED, 0.0001);
  }

  @Test
  public void testSpringListenersWithOvershootClamping() {
    SpringListener listener = spy(new SimpleSpringListener());
    mSpring.addListener(listener);
    mSpring
        .setOvershootClampingEnabled(true)
        .setSpringConfig(new SpringConfig(TENSION, FRICTION))
        .setCurrentValue(START_VALUE)
        .setEndValue(END_VALUE);
    int i = 0;
    InOrder inOrder = inOrder(listener);
    int simulatedMsPerFrame = 16;
    int runtime = simulatedMsPerFrame * 3000;
    while (i < runtime) {
      mSpring.advance(simulatedMsPerFrame /1000.0);
      i += simulatedMsPerFrame;
    }

    inOrder.verify(listener).onSpringEndStateChange(mSpring);
    inOrder.verify(listener).onSpringActivate(mSpring);
    inOrder.verify(listener, times(12)).onSpringUpdate(mSpring);
    inOrder.verify(listener).onSpringAtRest(mSpring);
    inOrder.verify(listener, never()).onSpringUpdate(mSpring);
  }

  @Test
  public void testSpringBehaviorWhenUpdatingEndStateButAndNotAtRest() {
    SpringListener listener = spy(new SimpleSpringListener());
    mSpring
        .setCurrentValue(1)
        .setSpringConfig(new SpringConfig(TENSION, FRICTION))
        .setEndValue(END_VALUE)
        .setVelocity(END_VALUE)
        .setAtRest();
    mSpring.addListener(listener);
    mSpring.setCurrentValue(START_VALUE);
    mSpring.setEndValue(END_VALUE);

    int i = 0;
    InOrder inOrder = inOrder(listener);
    int simulatedMsPerFrame = 16;
    int runtime = simulatedMsPerFrame * 3000;
    while (i < runtime) {
      mSpring.advance(simulatedMsPerFrame /1000.0);
      i += simulatedMsPerFrame;
    }

    inOrder.verify(listener).onSpringEndStateChange(mSpring);
    inOrder.verify(listener).onSpringActivate(mSpring);
    inOrder.verify(listener, times(66)).onSpringUpdate(mSpring);
    inOrder.verify(listener).onSpringAtRest(mSpring);
    inOrder.verify(listener, never()).onSpringUpdate(mSpring);
  }

}
