/**
 *  Copyright (c) 2013, Facebook, Inc.
 *  All rights reserved.
 *
 *  This source code is licensed under the BSD-style license found in the
 *  LICENSE file in the root directory of this source tree. An additional grant
 *  of patent rights can be found in the PATENTS file in the same directory.
 */

/**
 * Example: http://jsfiddle.net/Xz464/7/
 */

// *** SpringSystem ***
(function() {
  var rebound = {};

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

    getIsIdle: function() {
      return this._isIdle;
    },

    createSpring: function() {
      var spring = new Spring(this);
      this.registerSpring(spring);
      return spring;
    },

    getSpringById: function (id) {
      return this._springRegistry[id];
    },

    getAllSprings: function() {
      return Object.values(this._springRegistry);
    },

    registerSpring: function(spring) {
      this._springRegistry[spring.getId()] = spring;
    },

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

    addListener: function(listener) {
      this._listeners.push(listener);
    },

    removeListener: function(listener) {
      removeFirst(this._listeners, listener);
    },

    removeAllListeners: function() {
      this._listeners = [];
    }

  });

  // *** Spring ***

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

    destroy: function() {
      this._listeners = [];
      this._springSystem.deregisterSpring(this);
    },

    getId: function() {
      return this._id;
    },

    setSpringConfig: function(springConfig) {
      this._springConfig = springConfig;
      return this;
    },

    getSpringConfig: function() {
      return this._springConfig;
    },

    setCurrentValue: function(currentValue) {
      this._startValue = currentValue;
      this._currentState.position = currentValue;
      for (var i = 0, len = this._listeners.length; i < len; i++) {
        var listener = this._listeners[i];
        listener.onSpringUpdate && listener.onSpringUpdate(this);
      }
      return this;
    },

    getStartValue: function() {
      return this._startValue;
    },

    getCurrentValue: function() {
      return this._currentState.position;
    },

    getCurrentDisplacementDistance: function() {
      return this.getDisplacementDistanceForState(this._currentState);
    },

    getDisplacementDistanceForState: function(state) {
      return Math.abs(this._endValue - state.position);
    },

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

    getEndValue: function() {
      return this._endValue;
    },

    setVelocity: function(velocity) {
      this._currentState.velocity = velocity;
      return this;
    },

    getVelocity: function() {
      return this._currentState.velocity;
    },

    setRestSpeedThreshold: function(restSpeedThreshold) {
      this._restSpeedThreshold = restSpeedThreshold;
      return this;
    },

    getRestSpeedThreshold: function() {
      return this._restSpeedThreshold;
    },

    setRestDisplacementThreshold: function(displacementFromRestThreshold) {
      this._displacementFromRestThreshold = displacementFromRestThreshold;
    },

    getRestDisplacementThreshold: function() {
      return this._displacementFromRestThreshold;
    },

    setOvershootClampingEnabled: function(enabled) {
      this._overshootClampingEnabled = enabled;
      return this;
    },

    isOvershootClampingEnabled: function() {
      return this._overshootClampingEnabled;
    },

    isOvershooting: function() {
      return (this._startValue < this._endValue &&
              this.getCurrentValue() > this._endValue) ||
             (this._startValue > this._endValue &&
              this.getCurrentValue() < this._endValue);
    },

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

    systemShouldAdvance: function() {
      return !this.isAtRest() || !this.wasAtRest();
    },

    wasAtRest: function() {
      return this._wasAtRest;
    },

    isAtRest: function() {
      return Math.abs(this._currentState.velocity) < this._restSpeedThreshold &&
             this.getDisplacementDistanceForState(this._currentState) <=
               this._displacementFromRestThreshold;
    },

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

  var PhysicsState = function PhysicsState() {};

  extend(PhysicsState.prototype, {
    position: 0,
    velocity: 0
  });

  var SpringConfig = rebound.SpringConfig =
    function SpringConfig(tension, friction) {
      this.tension = tension;
      this.friction = friction;
    };

  extend(SpringConfig, {
    fromQcTensionAndFriction: function(qcTension, qcFriction) {
      return new SpringConfig(
        QcValueConverter.tensionFromQcValue(qcTension),
        QcValueConverter.frictionFromQcValue(qcFriction));
    }
  });

  extend(SpringConfig.prototype, {
    friction: 0,
    tension: 0
  });

  var QcValueConverter = {
    tensionFromQcValue: function(qcValue) {
      return (qcValue - 30.0) * 3.62 + 194.0;
    },

    qcValueFromTension: function(tension) {
      return (tension - 194.0) / 3.62 + 30.0;
    },

    frictionFromQcValue: function(qcValue) {
      return (qcValue - 8.0) * 3.0 + 25.0;
    },

    qcValueFromFriction: function(friction) {
      return (friction - 25.0) / 3.0 + 8.0;
    }
  };

  var MathUtil = rebound.MathUtil = {
    mapValueInRange: function(value, fromLow, fromHigh, toLow, toHigh) {
      fromRangeSize = fromHigh - fromLow;
      toRangeSize = toHigh - toLow;
      valueScale = (value - fromLow) / fromRangeSize;
      return toLow + (valueScale * toRangeSize);
    }
  }

  // Utilities
  var concat = Array.prototype.concat;
  var slice = Array.prototype.slice;

  function removeFirst(array, item) {
    var idx = array.indexOf(item);
    idx != -1 && array.splice(idx, 1);
  }

  function compatCancelAnimationFrame(id) {
    return typeof window != 'undefined' &&
      window.cancelAnimationFrame &&
      cancelAnimationFrame(id);
  }

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

  function bind(func, target) {
    args = slice.call(arguments, 2);
    return function() {
      func.apply(target, concat.call(args, slice.call(arguments)));
    };
  }

  function extend(target, source) {
    for (var key in source) {
      if (source.hasOwnProperty(key)) {
        target[key] = source[key];
      }
    }
  }

  if (typeof exports != 'undefined') {
    extend(exports, rebound);
  } else if (typeof window != 'undefined') {
    window.rebound = rebound;
  }
})();
