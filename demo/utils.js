(function() {
  window.createSpring = function createSpring(springSystem, friction, tension, rawValues) {
    var spring = springSystem.createSpring();
    var springConfig;
    if (rawValues) {
      springConfig = new SpringConfig(friction, tension);
    } else {
      springConfig = SpringConfig.fromQcTensionAndFriction(friction, tension);
    }
    spring.setSpringConfig(springConfig);
    spring.setCurrentValue(0);
    return spring;
  }

  window.xlat = function xlat(el, x, y) {
    el.style.mozTransform =
    el.style.msTransform =
    el.style.webkitTransform =
    el.style.transform = 'translate3d(' + x + 'px, ' + y + 'px, 0px)';
  }

  window.scale = function scale(el, val) {
    el.style.mozTransform =
    el.style.msTransform =
    el.style.webkitTransform =
    el.style.transform = 'scale3d(' + val + ', ' + val + ', 1)';
  }

  window.drawGridLines = function(canvas, ctx, graphScale) {
    ctx.beginPath();
    ctx.moveTo(0, canvas.height / 2);
    ctx.lineTo(canvas.width, canvas.height / 2);
    ctx.moveTo(0, -1 * graphScale + canvas.height / 2);
    ctx.lineTo(canvas.width, -1 * graphScale + canvas.height / 2);
    ctx.moveTo(0, 1 * graphScale + canvas.height / 2);
    ctx.lineTo(canvas.width, 1 * graphScale + canvas.height / 2);
    ctx.lineWidth = 0.5;
    ctx.strokeStyle = '#ff0000';
    ctx.stroke();
    ctx.closePath();
    ctx.lineWidth = 0.25;
    ctx.beginPath();
    for (var i = 0; i < 600; i+= 10) {
      ctx.moveTo(0, i);
      ctx.lineTo(canvas.width, i);
    }
    for (var i = 0; i < 600; i+=10) {
      ctx.moveTo(i, 0);
      ctx.lineTo(i, canvas.height);
    }
    ctx.strokeStyle = '#0000ff';
    ctx.stroke();
    ctx.lineWidth = 1;
    ctx.closePath();
  }

  window.mapValueFromRangeToRange = function(value, fromLow, fromHigh, toLow, toHigh) {
    fromRangeSize = fromHigh - fromLow;
    toRangeSize = toHigh - toLow;
    valueScale = (value - fromLow) / fromRangeSize;
    return toLow + (valueScale * toRangeSize);
  }

  window.bindSpacebar = function(url) {
    document.addEventListener('keypress', function(evt) {
      if (evt.keyCode === 32) {
        document.location.href = url;
      }
    });
  }


  window.downEvt = window.ontouchstart !== undefined ? 'touchstart' : 'mousedown';
  window.upEvt = window.ontouchend !== undefined ? 'touchend' : 'mouseup';
})();


