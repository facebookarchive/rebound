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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;
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
  public void testSpringDoesNotActivateAfterRest() {
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

    for (float frameTime : frameTimes) {
      mSpring.advance(frameTime);
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
  public void testSpringDoesNotActivateAfterSetAtRestCall() {
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

    for (int i = 0; i < frameTimes.length; ++i) {
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
  public void testSpringBehaviorWhenUpdatingEndStateWhileNotAtRest() {
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

  @Test
  public void testCanAddListenersWhileIterating() {
    Spring spring = createTestSpring()
      .setEndValue(END_VALUE)
      .addListener(new SimpleSpringListener() {
      @Override
      public void onSpringUpdate(Spring spring) {
        spring.addListener(new SimpleSpringListener());
      }
    });
    iterateUntilRest(spring);
  }

  @Test
  public void testCanRemoveListenersWhileIterating() {
    final SpringListener nextListener = new SimpleSpringListener();
    Spring spring = createTestSpring()
        .setEndValue(END_VALUE)
        .addListener(new SimpleSpringListener() {
          @Override
          public void onSpringUpdate(Spring spring) {
            spring.removeListener(nextListener);
          }
        })
        .addListener(nextListener);
    iterateUntilRest(spring);
  }

  @Test
  public void testSettingVelocityTriggersIteration() {

    List<Double> expectedValues = new ArrayList<Double>();
    Collections.addAll(expectedValues,
        12.939654049942755, 22.577876614612844, 28.54783297480804, 31.509232428471236,
        32.10098439026363, 30.914342688053463, 28.475499622860113, 25.236032817414525,
        21.56962941080124, 17.77360771096671, 14.073901757184718, 10.632348970485324,
        7.555308642976277, 4.902826584083833, 2.6977393197080004, 0.9342733116071972,
        -0.41416327060194336, -1.3881785731030014, -2.0366693731801266, -2.412175708003395,
        -2.567269005820985, -2.551885441954701, -2.4114915851846606, -2.185956917644723,
        -1.9090049430690101, -1.608118968920348, -1.3047881389601688, -1.0149920820059835,
        -0.7498371186711879, -0.5162721253093042, -0.3178269743126781, -0.15533029140660962,
        -0.027575656442567317, 0.06808392556148704, 0.13522454097952807, 0.17792698196814632,
        0.2004380871345706, 0.20691246148636744, 0.20122656573017264, 0.1868556051535494,
        0.16680298130501897, 0.14357209517052433, 0.1191708257094987, 0.09513989179074418,
        0.0725974025569678, 0.05229309978864437, 0.03466700870434143, 0.019908375738852607,
        0.008011837724304135, -0.0011712921133166356, -0.007887934682286893, -0.012445007796290883,
        -0.015178178515461428, -0.01642735293911623, -0.016518376724730492, -0.01575024362102694,
        -0.014387014983916622, -0.012653623953639396, -0.01073475814439276, -0.008776070347631434,
        -0.006887045763082467, -0.005144946527506555, -0.003599351673633401, -0.0022769068585264973,
        -0.00118598868985456, -3.2107017314128447E-4, 3.333551226580478E-4, 7.993729400056721E-4,
        0.0011028068129919262, 0.001270932409643302, 0.0);

    final List<Double> actualValues = new ArrayList<Double>();
    SpringListener listener = new SimpleSpringListener() {
      @Override
      public void onSpringUpdate(Spring spring) {
        actualValues.add(spring.getCurrentValue());
      }
    };
    Spring spring = createTestSpring().addListener(listener);
    spring.setVelocity(1000);
    iterateUntilRest(spring);

    assertThat(actualValues, is(expectedValues));
  }

  @Test
  public void testSetCurrentValueSetsAtRestByDefault() {
    List<Double> expectedValues = new ArrayList<Double>();
    Collections.addAll(expectedValues, 1.0);
    final List<Double> actualValues = new ArrayList<Double>();
    SpringListener listener = new SimpleSpringListener() {
      @Override
      public void onSpringUpdate(Spring spring) {
        actualValues.add(spring.getCurrentValue());
      }
    };
    Spring spring = createTestSpring().addListener(listener);
    spring.setCurrentValue(1);
    iterateUntilRest(spring);
    assertThat(actualValues, is(expectedValues));
  }

  @Test
  public void testSettingCurrentValueWithoutSettingAtRestTriggersIteration() {

    List<Double> expectedValues = new ArrayList<Double>();
    Collections.addAll(expectedValues,
        1.0, 0.9769932005557715, 0.9107271512877068, 0.8157786676931283, 0.7044678629533047,
        0.5867126710189403, 0.47015702489116307, 0.36038042531129016, 0.261156966845248,
        0.17473775829560714, 0.10213632352725191, 0.043401769365927934, -0.002130886168309641,
        -0.03561989664068272, -0.05852891121737896, -0.07247290518279491, -0.07909684588748486,
        -0.07998464847076281, -0.07659508512043615, -0.07022082321273125, -0.061966600693927947,
        -0.052742625703973076, -0.04326954333812012, -0.03409168618938477, -0.025595767119455923,
        -0.01803264238692162, -0.011540239761078923, -0.006166186811482669, -0.0018890737087958408,
        0.0013623667255423287, 0.0036925856408489555, 0.005225237487373128, 0.006091893160832346,
        0.006423311557232353, 0.006343040255727984, 0.005963058372530052, 0.005381146583670259,
        0.004679664549915674, 0.003925428631431925, 0.003170407646874172, 0.0024529870525211365,
        0.0017995886097055978, 0.001226470450320867, 7.415692258700482E-4, 3.462801551103353E-4,
        3.710123662101473E-5, -1.9290591003037256E-4, -3.528643770514182E-4, 0.0);

    final List<Double> actualValues = new ArrayList<Double>();
    SpringListener listener = new SimpleSpringListener() {
      @Override
      public void onSpringUpdate(Spring spring) {
        actualValues.add(spring.getCurrentValue());
      }
    };
    Spring spring = createTestSpring().addListener(listener);
    spring.setCurrentValue(1, false);
    iterateUntilRest(spring);
    assertThat(actualValues, is(expectedValues));
  }

  @Test
  public void testSpringMotionWithNoFriction() {
    List<Double> expectedValues = new ArrayList<Double>();
    Collections.addAll(expectedValues,
        13.051881339165476, 23.427268966531795, 31.082861473588856, 36.73162359593332,
        40.89962411129771, 43.975028583962526, 46.24424919361015, 47.918618279373895,
        49.15406970416802, 50.06566096322999, 50.73828847971584, 51.234594001995085,
        51.600798424811074, 51.87100633761877, 52.07038218235174, 52.21749381594507,
        52.32604173340962, 52.40613499386828, 52.465232676446966, 52.50883854373434,
        52.541013606764125, 52.56475432665734, 52.58227167481379, 52.59519704050676,
        52.60473416203935, 52.6117712307992, 52.616963608453354, 52.620794860729475,
        52.62362179185448, 52.62570767370448, 52.627246764294135, 52.62838239901053,
        52.629220339426624, 52.629838622868284, 52.63029483002443, 52.630631447432044,
        52.630879824244175, 52.631063091774045, 52.631198317712474, 52.6312980956406,
        52.631371717869136 );

    final List<Double> actualValues = new ArrayList<Double>();
    Spring spring = createTestSpring().addListener(new SimpleSpringListener() {
      @Override
      public void onSpringUpdate(Spring spring) {
        actualValues.add(spring.getCurrentValue());
      }
    });
    spring.getSpringConfig().tension = 0;
    spring.setVelocity(1000);
    iterateUntilRest(spring);
    assertThat(actualValues, is(expectedValues));
  }

  private Spring createTestSpring () {
    return new Spring(mSpringSystem).setSpringConfig(new SpringConfig(TENSION, FRICTION));
  }

  private void iterateUntilRest(Spring spring) {
    int simulatedMsPerFrame = 16;
    while (!spring.isAtRest() ) {
      spring.advance(simulatedMsPerFrame /1000.0);
    }
  }

}
