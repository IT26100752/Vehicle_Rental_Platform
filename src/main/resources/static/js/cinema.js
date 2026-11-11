/* cinema.js - intro loader timeline + cinematic polish. No dependencies. */
(function () {
  'use strict';
  var d = document, root = d.documentElement;
  var calm = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  var fine = window.matchMedia && window.matchMedia('(hover: hover) and (pointer: fine)').matches;

  /* ---------------- 1) 2-second intro: 1.2s fill + 0.4s fade ---------------- */
  var intro = d.getElementById('intro');
  if (intro) {
    if (calm || !root.classList.contains('intro-on')) {
      if (intro.parentNode) intro.parentNode.removeChild(intro);
    } else {
      var DUR = 1200, t0 = null, done = false;
      var fill = d.getElementById('introFill'), pct = d.getElementById('introPct'), label = d.getElementById('introStatus');
      var steps = [[0, 'Loading'], [40, 'Preparing your fleet'], [85, 'Almost ready']];
      var finish = function () {
        if (done) return; done = true;
        intro.classList.add('out');
        root.classList.remove('intro-on');
        setTimeout(function () { if (intro.parentNode) intro.parentNode.removeChild(intro); }, 480);
      };
      var tick = function (ts) {
        if (done) return;
        if (t0 === null) t0 = ts;
        var k = Math.min(1, (ts - t0) / DUR), p = k * k * (3 - 2 * k), n = Math.round(p * 100), s = steps[0][1];
        for (var i = 0; i < steps.length; i++) if (n >= steps[i][0]) s = steps[i][1];
        if (fill) fill.style.transform = 'scaleX(' + p.toFixed(3) + ')';
        if (pct) pct.textContent = n + '%';
        if (label && label.textContent !== s) label.textContent = s;
        if (k >= 1) finish(); else requestAnimationFrame(tick);
      };
      requestAnimationFrame(tick);
      intro.addEventListener('click', finish);
      d.addEventListener('keydown', function (e) { if (e.key === 'Escape') finish(); });
    }
  }

  /* ---------------- 2) cinematic polish ---------------- */
  var body = d.body;
  function add(cls) { var el = d.createElement('div'); el.className = cls; el.setAttribute('aria-hidden', 'true'); body.appendChild(el); return el; }

  var prog = add('cin-progress'), nav = d.querySelector('.nav');
  var hero = d.querySelector('.hero-photo'), heroH = 0;

  if (hero && !calm) {
    if (fine) {
      var hr = 0;
      hero.addEventListener('pointermove', function (e) {
        if (hr) return;
        hr = requestAnimationFrame(function () {
          hr = 0; var r = hero.getBoundingClientRect();
          hero.style.setProperty('--hx', (((e.clientX - r.left) / r.width - .5) * 2).toFixed(3));
          hero.style.setProperty('--hy', (((e.clientY - r.top) / r.height - .5) * 2).toFixed(3));
        });
      });
      hero.addEventListener('pointerleave', function () { hero.style.setProperty('--hx', 0); hero.style.setProperty('--hy', 0); });
    }
  }

  var ticking = false;
  function onScroll() {
    if (ticking) return; ticking = true;
    requestAnimationFrame(function () {
      ticking = false;
      var max = root.scrollHeight - window.innerHeight, y = window.pageYOffset;
      prog.style.setProperty('--sp', (max > 0 ? Math.min(1, y / max) : 0).toFixed(4));
      if (nav) nav.classList.toggle('cin-stuck', y > 8);
      if (hero && !calm) {
        heroH = heroH || hero.offsetHeight || 1;
        hero.style.setProperty('--hs', Math.max(0, Math.min(1, y / heroH)).toFixed(3));
      }
    });
  }
  window.addEventListener('scroll', onScroll, { passive: true });
  window.addEventListener('resize', function () { heroH = 0; onScroll(); });
  onScroll();

  /* cursor spotlight */
  if (fine && !calm) {
    /* 3D tilt for category tiles (KPI tiles and vehicle cards are handled by fx3d.js) */
    d.querySelectorAll('.type-tile').forEach(function (el) {
      el.addEventListener('pointermove', function (e) {
        if (el.classList.contains('reveal') && !el.classList.contains('in')) return;
        var r = el.getBoundingClientRect(), x = (e.clientX - r.left) / r.width, y = (e.clientY - r.top) / r.height;
        el.style.transitionDelay = '0ms';
        el.style.transform = 'perspective(800px) rotateX(' + ((.5 - y) * 8).toFixed(2) + 'deg) rotateY(' + ((x - .5) * 10).toFixed(2) + 'deg) scale(1.02)';
      });
      el.addEventListener('pointerleave', function () {
        el.style.transform = ''; setTimeout(function () { el.style.transitionDelay = ''; }, 260);
      });
    });
  }

  /* soft dim while navigating to another page (never blocks the click) */
  d.addEventListener('click', function (e) {
    if (e.defaultPrevented || e.button || e.metaKey || e.ctrlKey || e.shiftKey || e.altKey) return;
    var a = e.target.closest && e.target.closest('a[href]');
    if (!a || a.target || a.hasAttribute('download')) return;
    var u; try { u = new URL(a.href, location.href); } catch (x) { return; }
    if (u.origin !== location.origin || (u.pathname === location.pathname && u.search === location.search)) return;
    if (/\.(pdf|csv|xlsx?|zip)$/i.test(u.pathname) || /receipt|download|export/i.test(u.pathname)) return;
    root.classList.add('cin-leave');
    setTimeout(function () { root.classList.remove('cin-leave'); }, 2500);
  });
  window.addEventListener('pageshow', function (e) { if (e.persisted) root.classList.remove('cin-leave'); });
})();
