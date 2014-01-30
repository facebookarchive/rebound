#Rebound

##About

<a href="http://facebook.github.io/rebound">Rebound</a> is a java library that
models spring dynamics. Rebound spring models can be used to create animations
that feel natural by introducing real world physics to your application.

Rebound is not a general purpose physics library; however, spring dynamics
can be used to drive a wide variety of animations. The simplicity of Rebound
makes it easy to integrate and use as a building block for creating more
complex components like pagers, toggles, and scrollers.

##Usage

Here's a simple example of using a Spring model to drive scaling animation
on a View.

```java
// Create a system to run the physics loop for a set of springs.
SpringSystem springSystem = SpringSystem.create();

// Add a spring to the system.
Spring spring = springSystem.createSpring();

// Add a listener to observe the motion of the spring.
spring.addListener(new SimpleSpringListener() {

  @Override
  public void onSpringUpdate(Spring spring) {
    // You can observe the updates in the spring 
    // state by asking its current value in onSpringUpdate.
    float value = (float) spring.getCurrentValue();
    float scale = 1f - (value * 0.5f);
    myView.setScaleX(scale);
    myView.setScaleY(scale);
  }

});

// Set the spring in motion; moving from 0 to 1
spring.setEndValue(1);
```

##Download

Rebound is available as a prebuilt jar or can be built using Gradle or Buck.

<a href="https://github.com/facebook/rebound/releases/download/v0.3/rebound-0.3.jar">Download Rebound v0.3</a>

<a href="http://github.com/facebook/rebound">View on Github</a>

<a href="http://facebook.github.io/buck/">Buck</a> is a build system developed
at Facebook that encourages the creation of small, reusable modules consisting
of code and resources.

[![Build Status](https://travis-ci.org/facebook/rebound.png?branch=master)](https://travis-ci.org/facebook/rebound)

##License

BSD License

For Rebound software

Copyright (c) 2013, Facebook, Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without 
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, 
this list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice, 
this list of conditions and the following disclaimer in the documentation 
and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
