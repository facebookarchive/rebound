// Rebound
// =======
// **Rebound** is a simple library that models Spring dynamics for the
// purpose of driving physical animations.
//
// Origin
// ------
// [Rebound](http://facebook.github.io/rebound) was originally written
// in Java to provide a lightweight physics system for 
// [Facebook Home](https://play.google.com/store/apps/details?id=com.facebook.home) 
// and [Chat Heads](https://play.google.com/store/apps/details?id=com.facebook.orca) 
// on Android. It's now been adopted by several other Android
// applications. This JavaScript port was written to provide a quick
// way to demonstrate Rebound animations on the web for a 
// [conference talk](https://www.youtube.com/watch?v=s5kNm-DgyjY). Since then the
// JavaScript version has been used to build some really nice interfaces.
// Check out [brandonwalkin.com](http://brandonwalkin.com) for an
// example.
//
// Overview
// --------
// The Library provides a SpringSystem for maintaining a set of Spring
// objects and iterating those Springs through a physics solver loop
// until equilibrium is achieved. The Spring class is the basic
// animation driver provided by Rebound. By attaching a listener to
// a Spring, you can observe its motion. The observer function is
// notified of position changes on the spring as it solves for
// equilibrium. These position updates can be mapped to an animation
// range to drive animated property updates on your user interface
// elements (translation, rotation, scale, etc).
//
// Example
// -------
// Here's a simple example. Pressing and releasing on the logo below
// will cause it to scale up and down with a springy animation.
//
// <div style="text-align:center; margin-bottom:50px; margin-top:50px">
//   <img src="http://facebook.github.io/rebound/images/rebound.png" id="logo" />
// </div>
// <script src="../rebound.min.js"></script>
// <script>
// 
// function scale(el, val) {
//   el.style.mozTransform =
//   el.style.msTransform =
//   el.style.webkitTransform =
//   el.style.transform = 'scale3d(' + val + ', ' + val + ', 1)';
// }
// var el = document.getElementById('logo');
// 
// var springSystem = new rebound.SpringSystem();
// var spring = springSystem.createSpring(50, 3);
// spring.addListener({
//   onSpringUpdate: function(spring) {
//     var val = spring.getCurrentValue();
//     val = rebound.MathUtil.mapValueInRange(val, 0, 1, 1, 0.5);
//     scale(el, val);
//   }
// });
// 
// el.addEventListener('mousedown', function() {
//   spring.setEndValue(1);
// });
// 
// el.addEventListener('mouseout', function() {
//   spring.setEndValue(0);
// });
//
// el.addEventListener('mouseup', function() {
//   spring.setEndValue(0);
// });
// 
// </script>
// 
// Here's how it works.
//
// ```
// // Get a reference to the logo element.
// var el = document.getElementById('logo');
// 
// // create a SpringSystem and a Spring with a bouncy config.
// var springSystem = new rebound.SpringSystem();
// var spring = springSystem.createSpring(50, 3);
// 
// // Add a listener to the spring. Every time the physics 
// // solver updates its value this will be called. The 
// spring.addListener({
//   onSpringUpdate: function(spring) {
//     var val = spring.getCurrentValue();
//     val = rebound.MathUtil
//                  .mapValueInRange(val, 0, 1, 1, 0.5);
//     scale(el, val);
//   }
// });
// 
// // Listen for mouse down/up/out and toggle the 
// //springs endValue from 0 to 1.
// el.addEventListener('mousedown', function() {
//   spring.setEndValue(1);
// });
//
// el.addEventListener('mouseout', function() {
//   spring.setEndValue(0);
// });
// 
// el.addEventListener('mouseup', function() {
//   spring.setEndValue(0);
// });
// 
// // Helper for scaling an element with css transforms. 
// function scale(el, val) {
//   el.style.mozTransform =
//   el.style.msTransform =
//   el.style.webkitTransform =
//   el.style.transform = 'scale3d(' + 
//     val + ', ' + val + ', 1)';
// }
// ```

(function() {
  var rebound = {};

  // SpringSystem
  // ------------ 
  // **SpringSystem** is a set of Springs that all run on the same physics
  // timing loop. To get started with a Rebound animation you first
  // create a new SpringSystem and then add springs to it.
  var SpringSystem = rebound.SpringSystem = function SpringSystem() {
    this._springRegistry = {};
    this._activeSprings = [];
    this._listeners = [];
    this._idleSpringIndices = [];
    this._boundFrameCallback = bind(this._frameCallback, this);
  };

  extend(SpringSystem, {});

  extend(SpringSystem.prototype, {

    _springRegistry: null,

    _isIdle: true,

    _lastTimeMillis: -1,

    _activeSprings: null,

    _listeners: null,

    _idleSpringIndices: null,

    _frameCallback: function() {
      this.loop();
    },

    _frameCallbackId: null,

    // Create and register a new spring with the SpringSystem. This
    // Spring will now be solved for during the physics iteration loop. By default
    // the spring will use the default Origami spring config with 40 tension and 7
    // friction, but you can also provide your own values here.
    createSpring: function(tension, friction) {
      var spring = new Spring(this);
      this.registerSpring(spring);
      if (typeof tension === 'undefined' || typeof friction === 'undefined') {
        spring.setSpringConfig(SpringConfig.DEFAULT_ORIGAMI_SPRING_CONFIG);
      } else {
        var springConfig = SpringConfig.fromOrigamiTensionAndFriction(tension, friction);
        spring.setSpringConfig(springConfig);
      }
      return spring;
    },

    // You can check if a SpringSystem is idle or active by calling
    // getIsIdle. If all of the Springs in the SpringSystem are at rest,
    // i.e. the physics forces have reached equilibrium, then this
    // method will return true.
    getIsIdle: function() {
      return this._isIdle;
    },

    // Retrieve a specific Spring from the SpringSystem by id. This
    // can be useful for inspecting the state of a spring before
    // or after an integration loop in the SpringSystem executes.
    getSpringById: function (id) {
      return this._springRegistry[id];
    },

    // Get a listing of all the springs registered with this
    // SpringSystem.
    getAllSprings: function() {
      return Object.values(this._springRegistry);
    },

    // registerSpring is called automatically as soon as you create
    // a Spring with SpringSystem#createSpring. This method sets the
    // spring up in the registry so that it can be solved in the
    // solver loop.
    registerSpring: function(spring) {
      this._springRegistry[spring.getId()] = spring;
    },

    // Deregister a spring with this SpringSystem. The SpringSystem will
    // no longer consider this Spring during its integration loop once
    // this is called. This is normally done automatically for you when
    // you call Spring#destroy.
    deregisterSpring: function(spring) {
      removeFirst(this._activeSprings, spring);
      delete this._springRegistry[spring.getId()];
    },

    advance: function(time, deltaTime) {
      while(this._idleSpringIndices.length > 0) this._idleSpringIndices.pop();
      for (var i = 0, len = this._activeSprings.length; i < len; i++) {
        var spring = this._activeSprings[i];
        if (spring.systemShouldAdvance()) {
          spring.advance(time / 1000.0, deltaTime / 1000.0);
        } else {
          this._idleSpringIndices.push(this._activeSprings.indexOf(spring));
        }
      }
      while(this._idleSpringIndices.length > 0) {
        var idx = this._idleSpringIndices.pop();
        idx >= 0 && this._activeSprings.splice(idx, 1);
      }
    },

    // This is our main solver loop called to move the simulation
    // forward through time. Before each pass in the solver loop
    // onBeforeIntegrate is called on an any listeners that have
    // registered themeselves with the SpringSystem. This gives you
    // an opportunity to apply any constraints or adjustments to
    // the springs that should be enforced before each iteration
    // loop. Next the advance method is called to move each Spring in
    // the systemShouldAdvance forward to the current time. After the
    // integration step runs in advance, onAfterIntegrate is called
    // on any listeners that have registered themselves with the
    // SpringSystem. This gives you an opportunity to run any post
    // integration constraints or adjustments on the Springs in the
    // SpringSystem.
    loop: function() {
      var listener;
      var currentTimeMillis = Date.now();
      if (this._lastTimeMillis === -1) {
        this._lastTimeMillis = currentTimeMillis -1;
      }
      var ellapsedMillis = currentTimeMillis - this._lastTimeMillis;
      this._lastTimeMillis = currentTimeMillis;

      var i = 0, len = this._listeners.length;
      for (i = 0; i < len; i++) {
        var listener = this._listeners[i];
        listener.onBeforeIntegrate && listener.onBeforeIntegrate(this);
      }

      this.advance(currentTimeMillis, ellapsedMillis);
      if (this._activeSprings.length === 0) {
        this._isIdle = true;
        this._lastTimeMillis = -1;
      }

      for (i = 0; i < len; i++) {
        var listener = this._listeners[i];
        listener.onAfterIntegrate && listener.onAfterIntegrate(this);
      }

      compatCancelAnimationFrame(this._frameCallbackId);
      if (!this._isIdle) {
        this._frameCallbackId =
          compatRequestAnimationFrame(this._boundFrameCallback);
      }
    },

    // activateSpring is used to notify the SpringSystem that a Spring
    // has become displaced. The system responds by starting its solver
    // loop up if it is currently idle.
    activateSpring: function(springId) {
      var spring = this._springRegistry[springId];
      if (this._activeSprings.indexOf(spring) == -1) {
        this._activeSprings.push(spring);
      }
      if (this.getIsIdle()) {
        this._isIdle = false;
        compatCancelAnimationFrame(this._frameCallbackId);
        this._frameCallbackId =
          compatRequestAnimationFrame(this._boundFrameCallback);
      }
    },

    // Add a listener to the SpringSystem so that you can receive
    // before/after integration notifications allowing Springs to be
    // constrained or adjusted.
    addListener: function(listener) {
      this._listeners.push(listener);
    },

    // Remove a previously added listener on the SpringSystem.
    removeListener: function(listener) {
      removeFirst(this._listeners, listener);
    },

    // Remove all previously added listeners on the SpringSystem.
    removeAllListeners: function() {
      this._listeners = [];
    }

  });

  // Spring
  // ------
  // **Spring** provides a model of a classical spring acting to
  // resolve a body to equilibrium. Springs have configurable
  // tension which is a force multipler on the displacement of the
  // spring from its rest point or `endValue` as defined by [Hooke’s
  // law](http://en.wikipedia.org/wiki/Hooke's_law). Springs also have
  // configurable friction, which ensures that they do not oscillate
  // infinitely. When a Spring is displaced by updating it’s resting
  // or `currentValue`, the SpringSystems that contain that Spring
  // will automatically start looping to solve for equilibrium. As each
  // timestep passes, `SpringListener` objects attached to the Spring
  // will be notified of the updates providing a way to drive an
  // animation off of the spring's resolution curve.
  var Spring = rebound.Spring = function Spring(springSystem) {
    this._id = Spring._ID++;
    this._springSystem = springSystem;
    this._listeners = [];
    this._currentState = new PhysicsState();
    this._previousState = new PhysicsState();
    this._tempState = new PhysicsState();
  };

  extend(Spring, {
    _ID: 0,

    MAX_DELTA_TIME_SEC: 0.064,

    SOLVER_TIMESTEP_SEC: 0.001

  });

  extend(Spring.prototype, {

    _id: 0,

    _springConfig: null,

    _overshootClampingEnabled: false,

    _currentState: null,

    _previousState: null,

    _tempState: null,

    _startValue: 0,

    _endValue: 0,

    _wasAtRest: true,

    _restSpeedThreshold: 0.001,

    _displacementFromRestThreshold: 0.001,

    _listeners: null,

    _timeAccumulator: 0,

    _springSystem: null,

    // Remove a Spring from simulation and clear its listeners.
    destroy: function() {
      this._listeners = [];
      this._springSystem.deregisterSpring(this);
    },

    // Get the id of the spring, which can be used to retrieve it from
    // the SpringSystems it participates in later.
    getId: function() {
      return this._id;
    },

    // Set the configuration values for this Spring. A SpringConfig
    // contains the tension and friction values used to solve for the
    // equilibrium of the Spring in the physics loop.
    setSpringConfig: function(springConfig) {
      this._springConfig = springConfig;
      return this;
    },

    // Retrieve the SpringConfig used by this Spring.
    getSpringConfig: function() {
      return this._springConfig;
    },

    // Set the current position of this Spring. Listeners will be updated
    // with this value immediately. If the rest or `endValue` is not
    // updated to match this value, then the spring will be dispalced and
    // the SpringSystem will start to loop to restore the spring to the
    // `endValue`.
    // 
    // A common pattern is to move a Spring around without animation by
    // calling.
    // 
    // ```
    // spring.setCurrentValue(n).setAtRest();
    // ```
    // 
    // This moves the Spring to a new position `n`, sets the endValue
    // to `n`, and removes any velocity from the `Spring`. By doing
    // this you can allow the `SpringListener` to manage the position
    // of UI elements attached to the spring even when moving without
    // animation. For example, when dragging an element you can
    // update the position of an attached view through a spring
    // by calling `spring.setCurrentValue(x).setAtRest()`. When
    // the gesture ends you can update the Springs
    // velocity and endValue without calling setAtRest
    // `spring.setVelocity(gestureEndVelocity).setEndValue(flingTarget)`
    // to cause it to naturally animate the UI element to the resting
    // position taking into account existing velocity. The codepath for
    // synchronous movement and spring driven animation can
    // be unified using this technique.
    setCurrentValue: function(currentValue) {
      this._startValue = currentValue;
      this._currentState.position = currentValue;
      for (var i = 0, len = this._listeners.length; i < len; i++) {
        var listener = this._listeners[i];
        listener.onSpringUpdate && listener.onSpringUpdate(this);
      }
      return this;
    },

    // Get the position that the most recent animation started at. This
    // can be useful for determining the number off oscillations that
    // have occurred.
    getStartValue: function() {
      return this._startValue;
    },

    // Retrieve the current value of the Spring.
    getCurrentValue: function() {
      return this._currentState.position;
    },

    // Get the absolute distance of the Spring from it’s resting endValue position.
    getCurrentDisplacementDistance: function() {
      return this.getDisplacementDistanceForState(this._currentState);
    },

    getDisplacementDistanceForState: function(state) {
      return Math.abs(this._endValue - state.position);
    },

    // Set the endValue or resting position of the spring. If this
    // value is different than the current value, the SpringSystem will
    // be notified and will begin running its solver loop to resolve
    // the Spring to equilibrium. Any listeners that are registered
    // for onSpringEndStateChange will also be notified of this update
    // immediately.
    setEndValue: function(endValue) {
      if (this._endValue == endValue && this.isAtRest())  {
        return this;
      }
      this._startValue = this.getCurrentValue();
      this._endValue = endValue;
      this._springSystem.activateSpring(this.getId());
      for (var i = 0, len = this._listeners.length; i < len; i++) {
        var listener = this._listeners[i];
        listener.onSpringEndStateChange && listener.onSpringEndStateChange(this);
      }
      return this;
    },

    // Retrieve the endValue or resting position of this spring.
    getEndValue: function() {
      return this._endValue;
    },

    // Set the current velocity of the Spring. As previously mentioned,
    // this can be useful when you are performing a direct manipulation
    // gesture. When a UI element is released you may call setVelocity
    // on its animation Spring so that the Spring continues with the
    // same velocity as the gesture ended with. The friction, tension,
    // and displacement of the Spring will then govern its motion to
    // return to rest on a natural feeling curve.
    setVelocity: function(velocity) {
      this._currentState.velocity = velocity;
      return this;
    },

    // Get the current velocity of the Spring.
    getVelocity: function() {
      return this._currentState.velocity;
    },

    // Set a threshold value for the movement speed of the Spring below
    // which it will be considered to be not moving or resting.
    setRestSpeedThreshold: function(restSpeedThreshold) {
      this._restSpeedThreshold = restSpeedThreshold;
      return this;
    },

    // Retrieve the rest speed threshold for this Spring.
    getRestSpeedThreshold: function() {
      return this._restSpeedThreshold;
    },

    // Set a threshold value for displacement below which the Spring
    // will be considered to be not displaced i.e. at its resting
    // `endValue`.
    setRestDisplacementThreshold: function(displacementFromRestThreshold) {
      this._displacementFromRestThreshold = displacementFromRestThreshold;
    },

    // Retrieve the rest displacement threshold for this spring.
    getRestDisplacementThreshold: function() {
      return this._displacementFromRestThreshold;
    },

    // Enable overshoot clamping. This means that the Spring will stop
    // immediately when it reaches its resting position regardless of
    // any existing momentum it may have. This can be useful for certain
    // types of animations that should not oscillate such as a scale
    // down to 0 or alpha fade.
    setOvershootClampingEnabled: function(enabled) {
      this._overshootClampingEnabled = enabled;
      return this;
    },

    // Check if overshoot clamping is enabled for this spring.
    isOvershootClampingEnabled: function() {
      return this._overshootClampingEnabled;
    },

    // Check if the Spring has gone past its end point by comparing
    // the direction it was moving in when it started to the current
    // position and end value.
    isOvershooting: function() {
      return (this._startValue < this._endValue &&
              this.getCurrentValue() > this._endValue) ||
             (this._startValue > this._endValue &&
              this.getCurrentValue() < this._endValue);
    },

    // Spring.advance is the main solver method for the Spring. It takes
    // the current time and delta since the last time step and performs
    // an RK4 integration to get the new position and velocity state
    // for the Spring based on the tension, friction, velocity, and
    // displacement of the Spring.
    advance: function(time, realDeltaTime) {
      var isAtRest = this.isAtRest();

      if (isAtRest && this._wasAtRest) {
        return;
      }

      var adjustedDeltaTime = realDeltaTime;
      if (realDeltaTime > Spring.MAX_DELTA_TIME_SEC) {
        adjustedDeltaTime = Spring.MAX_DELTA_TIME_SEC;
      }

      this._timeAccumulator += adjustedDeltaTime;

      var tension = this._springConfig.tension,
          friction = this._springConfig.friction,

          position = this._currentState.position,
          velocity = this._currentState.velocity,
          tempPosition = this._tempState.position,
          tempVelocity = this._tempState.velocity,

          aVelocity, aAcceleration,
          bVelocity, bAcceleration,
          cVelocity, cAcceleration,
          dVelocity, dAcceleration,

          dxdt, dvdt;

      while(this._timeAccumulator >= Spring.SOLVER_TIMESTEP_SEC) {

        this._timeAccumulator -= Spring.SOLVER_TIMESTEP_SEC;

        if (this._timeAccumulator < Spring.SOLVER_TIMESTEP_SEC) {
          this._previousState.position = position;
          this._previousState.velocity = velocity;
        }

        aVelocity = velocity;
        aAcceleration = (tension * (this._endValue - tempPosition)) - friction * velocity;

        tempPosition = position + aVelocity * Spring.SOLVER_TIMESTEP_SEC * 0.5;
        tempVelocity = velocity + aAcceleration * Spring.SOLVER_TIMESTEP_SEC * 0.5;
        bVelocity = tempVelocity;
        bAcceleration = (tension * (this._endValue - tempPosition)) - friction * tempVelocity;

        tempPosition = position + bVelocity * Spring.SOLVER_TIMESTEP_SEC * 0.5;
        tempVelocity = velocity + bAcceleration * Spring.SOLVER_TIMESTEP_SEC * 0.5;
        cVelocity = tempVelocity;
        cAcceleration = (tension * (this._endValue - tempPosition)) - friction * tempVelocity;

        tempPosition = position + cVelocity * Spring.SOLVER_TIMESTEP_SEC * 0.5;
        tempVelocity = velocity + cAcceleration * Spring.SOLVER_TIMESTEP_SEC * 0.5;
        dVelocity = tempVelocity;
        dAcceleration = (tension * (this._endValue - tempPosition)) - friction * tempVelocity;

        dxdt = 1.0/6.0 * (aVelocity + 2.0 * (bVelocity + cVelocity) + dVelocity);
        dvdt = 1.0/6.0 *
          (aAcceleration + 2.0 * (bAcceleration + cAcceleration) + dAcceleration);

        position += dxdt * Spring.SOLVER_TIMESTEP_SEC;
        velocity += dvdt * Spring.SOLVER_TIMESTEP_SEC;
      }

      this._tempState.position = tempPosition;
      this._tempState.velocity = tempVelocity;

      this._currentState.position = position;
      this._currentState.velocity = velocity;

      if (this._timeAccumulator > 0) {
        this.interpolate(this._timeAccumulator / Spring.SOLVER_TIMESTEP_SEC);
      }

      if (this.isAtRest() ||
          this._overshootClampingEnabled && this.isOvershooting()) {
        this._startValue = this._endValue;
        this._currentState.position = this._endValue;
        this.setVelocity(0);
        isAtRest = true;
      }

      var notifyActivate = false;
      if (this._wasAtRest) {
        this._wasAtRest = false;
        notifyActivate = true;
      }

      var notifyAtRest = false;
      if (isAtRest) {
        this._wasAtRest = true;
        notifyAtRest = true;
      }

      for (var i = 0, len = this._listeners.length; i < len; i++) {
        var listener = this._listeners[i];
        if (notifyActivate) {
          listener.onSpringActivate && listener.onSpringActivate(this);
        }

        listener.onSpringUpdate && listener.onSpringUpdate(this);

        if (notifyAtRest) {
          listener.onSpringAtRest && listener.onSpringAtRest(this);
        }
      }
    },

    
    // Check if the SpringSystem should advance. Springs are advanced
    // a final frame after they reach equilibrium to ensure that the
    // currentValue is exactly the requested endValue regardless of the
    // displacement threshold.
    systemShouldAdvance: function() {
      return !this.isAtRest() || !this.wasAtRest();
    },

    wasAtRest: function() {
      return this._wasAtRest;
    },

    // Check if the Spring is atRest meaning that it’s currentValue and
    // endValue are the same and that it has no velocity. The previously
    // described thresholds for speed and displacement define the bounds
    // of this equivalence check.
    isAtRest: function() {
      return Math.abs(this._currentState.velocity) < this._restSpeedThreshold &&
             this.getDisplacementDistanceForState(this._currentState) <=
               this._displacementFromRestThreshold;
    },

    // Force the spring to be at rest at its current position. As
    // described in the documentation for setCurrentValue, this method
    // makes it easy to do synchronous non-animated updates to ui
    // elements that are attached to springs via SpringListeners.
    setAtRest: function() {
      this._endValue = this._currentState.position;
      this._tempState.position = this._currentState.position;
      this._currentState.velocity = 0;
      return this;
    },

    interpolate: function(alpha) {
      this._currentState.position = this._currentState.position *
        alpha + this._previousState.position * (1 - alpha);
      this._currentState.velocity = this._currentState.velocity *
        alpha + this._previousState.velocity * (1 - alpha);
    },

    addListener: function(newListener) {
      this._listeners.push(newListener);
      return this;
    },

    removeListener: function(listenerToRemove) {
      removeFirst(this._listeners, listenerToRemove);
      return this;
    },

    removeAllListeners: function() {
      this._listeners = [];
      return this;
    },

    currentValueIsApproximately: function(value) {
      return Math.abs(this.getCurrentValue() - value) <=
        this.getRestDisplacementThreshold();
    }

  });

  // PhysicsState
  // ------------
  // **PhysicsState** consists of a position and velocity. A Spring uses
  // this internally to keep track of its current and prior position and
  // velocity values.
  var PhysicsState = function PhysicsState() {};

  extend(PhysicsState.prototype, {
    position: 0,
    velocity: 0
  });

  // SpringConfig
  // ------------
  // **SpringConfig** maintains a set of tension and friction constants
  // for a Spring. You can use fromOrigamiTensionAndFriction to convert
  // values from the [Origami](http://facebook.github.io/origami/)
  // design tool directly to Rebound spring constants.
  var SpringConfig = rebound.SpringConfig =
    function SpringConfig(tension, friction) {
      this.tension = tension;
      this.friction = friction;
    };
  
  // Math for converting from
  // [Origami](http://facebook.github.io/origami/) to
  // [Rebound](http://facebook.github.io/rebound).
  // You mostly don't need to worry about this, just use
  // SpringConfig.fromOrigamiTensionAndFriction(v, v);
  var OrigamiValueConverter = {
    tensionFromOrigamiValue: function(oValue) {
      return (oValue - 30.0) * 3.62 + 194.0;
    },

    origamiValueFromTension: function(tension) {
      return (tension - 194.0) / 3.62 + 30.0;
    },

    frictionFromOrigamiValue: function(oValue) {
      return (oValue - 8.0) * 3.0 + 25.0;
    },

    origamiFromFriction: function(friction) {
      return (friction - 25.0) / 3.0 + 8.0;
    }
  };

  extend(SpringConfig, {
    // Convert an origami Spring tension and friction to Rebound spring
    // constants. If you are prototyping a design with Origami, this 
    // makes it easy to make your springs behave exactly the same in
    // Rebound.
    fromOrigamiTensionAndFriction: function(oTension, oFriction) {
      return new SpringConfig(
        OrigamiValueConverter.tensionFromOrigamiValue(oTension),
        OrigamiValueConverter.frictionFromOrigamiValue(oFriction));
    }
  });

  SpringConfig.DEFAULT_ORIGAMI_SPRING_CONFIG = SpringConfig.fromOrigamiTensionAndFriction(40, 7);

  extend(SpringConfig.prototype, {
    friction: 0,
    tension: 0
  });

  var MathUtil = rebound.MathUtil = {
    // This helper function does a linear interpolation of a value from
    // one range to another. This can be very useful for converting the
    // motion of a Spring to a range of UI property values. For example a
    // spring moving from position 0 to 1 could be interpolated to move a
    // view from pixel 300 to 350 and scale it from 0.5 to 1. The current
    // position of the `Spring` just needs to be run through this method
    // taking its input range in the _from_ parameters with the property
    // animation range in the _to_ parameters.
    mapValueInRange: function(value, fromLow, fromHigh, toLow, toHigh) {
      fromRangeSize = fromHigh - fromLow;
      toRangeSize = toHigh - toLow;
      valueScale = (value - fromLow) / fromRangeSize;
      return toLow + (valueScale * toRangeSize);
    }
  }


  // Utilities
  // ---------
  // Here are a few useful JavaScript utilities.

  // Lop off the first occurence of the reference in the Array.
  function removeFirst(array, item) {
    var idx = array.indexOf(item);
    idx != -1 && array.splice(idx, 1);
  }

  function compatCancelAnimationFrame(id) {
    return typeof window != 'undefined' &&
      window.cancelAnimationFrame &&
      cancelAnimationFrame(id);
  }

  // Cross browser/node timer functions.
  function compatRequestAnimationFrame(func) {
    var meth;
    if (typeof process != 'undefined') {
      meth = process.nextTick;
    } else {
      meth = window.requestAnimationFrame ||
      window.webkitRequestAnimationFrame ||
      window.mozRequestAnimationFrame ||
      window.msRequestAnimationFrame ||
      window.oRequestAnimationFrame;
    }
    return meth(func);
  }

  var concat = Array.prototype.concat;
  var slice = Array.prototype.slice;

  // Bind a function to a context object.
  function bind(func, context) {
    args = slice.call(arguments, 2);
    return function() {
      func.apply(context, concat.call(args, slice.call(arguments)));
    };
  }

  // Add all the properties in the source to the target.
  function extend(target, source) {
    for (var key in source) {
      if (source.hasOwnProperty(key)) {
        target[key] = source[key];
      }
    }
  }

  // Export the public api using exports for common js or the window for
  // normal browser inclusion.
  if (typeof exports != 'undefined') {
    extend(exports, rebound);
  } else if (typeof window != 'undefined') {
    window.rebound = rebound;
  }
})();

// Legal Stuff
// -----------
/**
 *  Copyright (c) 2013, Facebook, Inc.
 *  All rights reserved.
 *
 *  This source code is licensed under the BSD-style license found in the
 *  LICENSE file in the root directory of this source tree. An additional grant
 *  of patent rights can be found in the PATENTS file in the same directory.
 */
